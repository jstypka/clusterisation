package pl.edu.agh.student.clusterisation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Clusterisation {

    public Map<Coordinates, Grid> gridList;
    public Map<Integer, Set<Coordinates>> clusters;
    public LinkedList<Coordinates> transitionalGrids;

    public Clusterisation() {
        gridList = new HashMap<>();
        transitionalGrids = new LinkedList<>();
        clusters = new HashMap<>();
    }

    public void printGridList() {
        for(int i = Main.BOARD_SIZE -1; i >= 0; --i) {
            for(int j = 0; j < Main.BOARD_SIZE; ++j) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(j);
                list.add(i);
                String c = " . ";
                if(gridList.containsKey(new Coordinates(list))) {
                    Grid g = gridList.get(new Coordinates(list));
                    if(g.isDense()) {
                        c = " D ";
                    } else {
                        c = " T ";
                    }
                }
                System.out.print(c);
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printClusters() {
        for(int i = Main.BOARD_SIZE -1; i >= 0; --i) {
            for(int j = 0; j < Main.BOARD_SIZE; ++j) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(j);
                list.add(i);
                String c = " . ";
                if(gridList.containsKey(new Coordinates(list))) {
                    int g = new Coordinates(list).getCluster(gridList);
                    if(g == -1){
                        c = Integer.toString(g) + " ";
                    } else if (g < 10){
                        c = " " + Integer.toString(g) + " ";
                    } else {
                        c = Integer.toString(g) + " ";
                    }

                }
                System.out.print(c);
            }
            System.out.println();
        }
        System.out.println();
    }

    public void listClusters() {
        int cluster = 1;
        for (Map.Entry<Integer,Set<Coordinates>> entry : clusters.entrySet()){
            System.out.println("Cluster " + cluster++);
            System.out.println(entry.getValue());
        }
    }

    public void readFromFile() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Main.INPUT_FILE));
            String line;
            while ((line = br.readLine()) != null) {
                String[] coords = line.split("\\t")[0].replaceAll("\\(|\\)","").split(",");
                double density = Double.parseDouble(line.split("\\t")[1]);

                if(density > Main.TRANSITIONAL_THRESHOLD) { // te filtracje mozna przeprowadzic juz wczesniej
                    ArrayList<Integer> key = new ArrayList<>();

                    for(String coord : coords) {
                        key.add(Integer.parseInt(coord));
                    }

                    gridList.put(new Coordinates(key), new Grid(false, -1, density > Main.DENSE_THRESHOLD));
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
            if(n.getCluster(gridList) == cluster) {
                clusterFellows.add(n);
            }
        }
        return clusterFellows;
    }

    private void addGridToCluster(Coordinates gridToAdd, int cluster, List<Coordinates> neighbours) {
        Grid g = gridList.get(gridToAdd);
        g.cluster = cluster;
        clusters.get(cluster).add(gridToAdd);

        // add transitional neighbouring grids to the global list
        for(Coordinates c : neighbours){
            Grid neighbour_grid = gridList.get(c);
            if(!neighbour_grid.isDense() && neighbour_grid.cluster == -1 && !transitionalGrids.contains(c)){
                transitionalGrids.addLast(c);
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
            int neighbourCluster = n.getCluster(gridList);
            if(neighbourCluster == -1 && !n.equals(current_trans)) {
                return true;
            }
        }
        return false;
    }

    private LinkedList<Integer> getAdjacentClusters(List<Coordinates> neighbours) {
        LinkedList<Integer> adjacentClusters = new LinkedList<>();
        int largestYet = -1;
        for(Coordinates n: neighbours) {
            int c = n.getCluster(gridList);
            if(c != -1 && !adjacentClusters.contains(c)) {
                if(adjacentClusters.isEmpty() || clusters.get(c).size() > largestYet) {
                    largestYet = clusters.get(c).size();
                    adjacentClusters.addFirst(c);
                } else {
                    adjacentClusters.addLast(c);
                }
            }
        }
        return adjacentClusters;
    }

    private void mergeTwoClusters(int destination, int from) {
        Set<Coordinates> destinationSet = clusters.get(destination);
        Set<Coordinates> fromSet = clusters.get(from);

        for(Coordinates coords: fromSet) {
            Grid g = gridList.get(coords);
            g.cluster = destination;
        }

        destinationSet.addAll(fromSet);
        clusters.remove(from);
    }

    private boolean tryToMerge(int c, int other) {
        Set<Coordinates> cluster = clusters.get(c);
        for(Coordinates current_trans : cluster) {
            // we look only for transitional grids
            if(gridList.get(current_trans).isDense()) {
                continue;
            }

            List<Coordinates> neighbours = getNeighbours(current_trans);
            List<Integer> adjacentClusters = getAdjacentClusters(neighbours);
            if(adjacentClusters.size() == 2 && adjacentClusters.contains(other) && neighbours.size() == 2 * current_trans.getSize()) {
                return false;
            }
        }
        return true;
    }

    private void createClusters() {
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
                    } else if(!transitionalGrids.contains(ngbr)){
                        transitionalGrids.add(ngbr);
                    }
                }
            }
        }
    }

    private void attachTransitionalGrids() {
        boolean appliedChanges = true;
        while(appliedChanges) {
            appliedChanges = false;
            for(int tr_grid = 0; tr_grid < transitionalGrids.size(); ++tr_grid) {
                Coordinates current_trans = transitionalGrids.get(tr_grid);

                // if it is already assigned, continue
                if(gridList.get(current_trans).cluster != -1) {
                    continue;
                }

                List<Coordinates> neighbours = getNeighbours(current_trans);

                // check if all neighbours are in the same cluster
                if(neighbours.size() == 2 * current_trans.getSize()){
                    boolean oneCluster = true;
                    for(int i = 1; i < neighbours.size(); ++i) {
                        if(gridList.get(neighbours.get(i)).cluster != gridList.get(neighbours.get(i - 1)).cluster) {
                            oneCluster = false;
                            break;
                        }
                    }
                    if(oneCluster) {
                        continue;
                    }
                }

                // choose a dense grid which is a part of the biggest neighbouring cluster
                Coordinates biggestDense = getBiggestDense(neighbours);
                if(biggestDense != null) {

                    assert !gridList.get(current_trans).isDense();
                    assert gridList.get(biggestDense).isDense();
                    assert biggestDense.getCluster(gridList) > -1;

                    // add current grid to the cluster
                    int newCluster = biggestDense.getCluster(gridList);
                    addGridToCluster(current_trans, newCluster, neighbours);
                    appliedChanges = true;
                    continue;
                }

                // try to join a cluster through a transitional grid
                List<Integer> possibleClusters = getAdjacentClusters(neighbours);
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
                    // if yes add our grid to the cluster
                    if(allAreOutside) {
                        addGridToCluster(current_trans, cluster, neighbours);
                        appliedChanges = true;
                        break;
                    }
                }
            }
        }
    }

    private void mergeClusters() {
        boolean appliedChanges = true;
        while(appliedChanges) {
            appliedChanges = false;
            for(Coordinates current_trans: transitionalGrids) {
                // we are only interested in transitional grids
                if(current_trans.getCluster(gridList) == -1) {
                    continue;
                }

                int thisCluster = current_trans.getCluster(gridList);
                List<Coordinates> neighbours = getNeighbours(current_trans);
                List<Integer> adjacentClusters = getAdjacentClusters(neighbours); // might be good to sort it
                adjacentClusters.remove(new Integer(thisCluster));

                // if surrounded by only two clusters - continue
                if(adjacentClusters.size() == 1 && neighbours.size() == 2 * current_trans.getSize()) {
                    continue;
                }

                // try to merge each cluster with thisCluster
                for(int c : adjacentClusters) {
                    if(tryToMerge(c, thisCluster) && tryToMerge(thisCluster, c)) {
                        mergeTwoClusters(min(c, thisCluster), max(c, thisCluster));
                        appliedChanges = true;
                        break;
                    }
                }
            }
        }
    }

    public void clusterize() {
        createClusters();
        attachTransitionalGrids();
        mergeClusters();
    }
}
