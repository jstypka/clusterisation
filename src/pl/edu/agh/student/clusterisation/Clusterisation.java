package pl.edu.agh.student.clusterisation;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Clusterisation {

    public Map<Coordinates, Grid> gridList;
    public Map<Integer, Set<Coordinates>> clusters;
    public Set<Coordinates> transitionalGrids;

    public static final String filename = "output/part-r-00000";
    public static final double TRANSITIONAL_THRESHOLD = 0.0;
    public static final double DENSE_THRESHOLD = 0.0;

    public Clusterisation() {
        gridList = new HashMap<>();
        transitionalGrids = new HashSet<>();
        clusters = new HashMap<>();
    }

    public void readFromFile() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                String[] coords = line.split("\\t")[0].split(" ");
                double density = Double.parseDouble(line.split("\\t")[1]);

                if(density > TRANSITIONAL_THRESHOLD) { // te filtracje mozna przeprowadzic juz wczesniej
                    ArrayList<Integer> key = new ArrayList<>();

                    for(String coord : coords) {
                        key.add(Integer.parseInt(coord));
                    }

                    gridList.put(new Coordinates(key), new Grid(false, -1, density > DENSE_THRESHOLD));
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LinkedList<Coordinates> getNeighbours(Coordinates from) {
        LinkedList<Coordinates> neighbours = new LinkedList<>();

        for(int dim = 0; dim < from.getSize(); ++dim) {
            int val = from.getDimension(dim);

            Coordinates bigger = new Coordinates(from);
            bigger.setDimension(dim, val + 1);
            if(gridList.containsKey(bigger)) {
                neighbours.add(bigger);
            }

            Coordinates smaller = new Coordinates(from);
            smaller.setDimension(dim, val - 1);
            if(gridList.containsKey(smaller)) {
                neighbours.add(smaller);
            }
        }
        return neighbours;
    }


    private Coordinates getBiggestDense(List<Coordinates> neighbours) {
        int clusterSize = -1;
        Coordinates bestYet = null;

        for(Coordinates n : neighbours) {
            Grid g = gridList.get(n);
            if(g.isDense() && clusters.get(g.cluster).size() > clusterSize) {
                clusterSize = clusters.get(g.cluster).size();
                bestYet = n;
            }
        }

        return bestYet;
    }

    private List<Coordinates> getCoordsInCluster(int cluster, List<Coordinates> neighbours) {
        List<Coordinates> clusterFellows = new LinkedList<>();
        for(Coordinates n : neighbours) {
            if(gridList.get(n).cluster == cluster) {
                clusterFellows.add(n);
            }
        }
        return clusterFellows;
    }

    private LinkedList<Integer> getPossibleClusters(List<Coordinates> neighbours) {
        LinkedList<Integer> result = new LinkedList<>();
        int largestYet = -1;
        for(Coordinates c : neighbours) {
            int cluster = gridList.get(c).cluster;
            if(cluster != -1) {
                if(result.isEmpty() || clusters.get(cluster).size() > largestYet) {
                    largestYet = clusters.get(cluster).size();
                    result.addFirst(cluster);
                } else {
                    result.addLast(cluster);
                }
            }
        }
        return result;
    }

    private void addGridToCluster(Coordinates gridToAdd, int cluster, List<Coordinates> neighbours) {
        Grid g = gridList.get(gridToAdd);
        g.cluster = cluster;
        clusters.get(cluster).add(gridToAdd);

        // add transitional neighbouring grids to the global list
        for(Coordinates c : neighbours){
            Grid neighbour_grid = gridList.get(c);
            if(!neighbour_grid.isDense() && neighbour_grid.cluster == -1){
                transitionalGrids.add(c);
            }
        }

//        transitionalGrids.remove(gridToAdd); <- pewnie wyskoczy error
    }

    private boolean checkIfOutside(Coordinates potentialFellow, Coordinates current_trans) {
        int dimensions = potentialFellow.getSize();
        List<Coordinates> neighbours = getNeighbours(potentialFellow);
        if(2 * dimensions > neighbours.size()) {
            return true;
        }
        for(Coordinates n: neighbours) {
            int neighbourCluster = gridList.get(n).cluster;
            if(neighbourCluster == -1 && !n.equals(current_trans)) {
                return true;
            }
        }
        return false;
    }

    private void initialClustering() {
        int cluster = 1;

        // iterate through the gridList
        for (Map.Entry<Coordinates,Grid> entry : gridList.entrySet()){

            Coordinates key = entry.getKey();
            Grid denseGrid = entry.getValue();

            // leave only unvisited dense grids
            if(!denseGrid.isDense() || denseGrid.visited) {
                continue;
            }

            // Create a new cluster
            int currentCluster = cluster++;
            clusters.put(currentCluster,new HashSet<Coordinates>());

            // Run a DFS and assign dense grids to this cluster
            Stack<Coordinates> dfsStack = new Stack<>();
            dfsStack.push(key);

            while(!dfsStack.empty()) {
                Coordinates coords = dfsStack.pop();
                Grid grid = gridList.get(coords);

                assert grid.isDense(); // just in case

                if(grid.visited) {
                    continue;
                }

                grid.visited = true;
                grid.cluster = currentCluster;
                clusters.get(currentCluster).add(coords);

                LinkedList<Coordinates> neighbours = getNeighbours(coords);
                for(Coordinates ngbr : neighbours) {
                    if(gridList.get(ngbr).isDense()) {
                        dfsStack.push(ngbr);
                    } else {
                        transitionalGrids.add(ngbr);
                    }
                }
            }
        }
    }

    private void adjustClustering() {
        boolean appliedChanges = true;
        while(appliedChanges) {
            appliedChanges = false;
            for(Coordinates current_trans : transitionalGrids) {
                List<Coordinates> neighbours = getNeighbours(current_trans);

                // check if all neighbours are in the same cluster
                boolean oneCluster = true;
                for(int i = 1; i < neighbours.size(); ++i) {
                    if(gridList.get(neighbours.get(i)).cluster != gridList.get(neighbours.get(i-1)).cluster) {
                        oneCluster = false;
                        break;
                    }
                }
                if(oneCluster) {
                    continue;
                }

                // choose a dense grid which is a part of the biggest neighbouring cluster
                Coordinates biggestDense = getBiggestDense(neighbours);
                if(biggestDense != null) {
                    // add current grid to the cluster
                    int newCluster = gridList.get(biggestDense).cluster;
                    addGridToCluster(current_trans, newCluster, neighbours);
                    appliedChanges = true;
                    continue;
                }

                // try to join a cluster through a transitional grid
                List<Integer> possibleClusters = getPossibleClusters(neighbours);
                for(int cluster : possibleClusters) {
                    List<Coordinates> coordsInCluster = getCoordsInCluster(cluster, neighbours);

                    // check if all coordsInCluster are still outside grids, when we add current_trans grid
                    boolean allAreOutside = true;
                    for(Coordinates potentialFellow : coordsInCluster) {
                        if(!checkIfOutside(potentialFellow, current_trans)) {
                            allAreOutside = false;
                            break;
                        }
                    }

                    if(allAreOutside) {
                        addGridToCluster(current_trans, cluster, neighbours);
                        appliedChanges = true;
                        break;
                    }
                }
            }
        }
    }

    public void clusterize() {
        initialClustering();
        adjustClustering();
    }

    public void printClusters() {
        for (Map.Entry<Integer,Set<Coordinates>> entry : clusters.entrySet()){
            System.out.println("Cluster " + entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    public static void main(String[] args) {
        Clusterisation c = new Clusterisation();

        c.readFromFile();

        c.clusterize();

        c.printClusters();
    }
}
