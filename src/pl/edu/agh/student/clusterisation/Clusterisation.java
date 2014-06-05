package pl.edu.agh.student.clusterisation;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Clusterisation {

    public Map<List<Integer>, Grid> gridList;

    public static final String filename = "output/part-r-00000";
    public static final double TRANSITIONAL_THRESHOLD = 0.0;
    public static final double DENSE_THRESHOLD = 0.0;

    public Clusterisation() {
        gridList = new HashMap<>();
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

                    gridList.put(key, new Grid(false, -1, density > DENSE_THRESHOLD));
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<ArrayList<Integer>> getNeighbours(ArrayList<Integer> from) {
        LinkedList<ArrayList<Integer>> neighbours = new LinkedList<>();
        for(int dim = 0; dim < from.size(); ++dim) {
            int val = from.get(dim);
            ArrayList<Integer> bigger = new ArrayList<>(from);
            bigger.set(dim, val + 1);
            if(gridList.containsKey(bigger)) {
                neighbours.add(bigger);
            }
            ArrayList<Integer> smaller = new ArrayList<>(from);
            smaller.set(dim, val - 1);
            if(gridList.containsKey(smaller)) {
                neighbours.add(smaller);
            }
        }
        return neighbours;
    }

    public void initialClustering() {
        int cluster = 1;

        // iterate through the gridList
        for (Map.Entry<List<Integer>,Grid> entry : gridList.entrySet()){

            ArrayList<Integer> key = (ArrayList<Integer>) entry.getKey();
            Grid denseGrid = entry.getValue();

            // leave only unvisited dense grids
            if(!denseGrid.isDense() || denseGrid.visited) {
                continue;
            }
            System.out.println("New take");
            // Run a DFS and create clusters
            int currentCluster = cluster++;
            Stack<ArrayList<Integer>> dfsStack = new Stack<>();
            dfsStack.push(key);

            while(!dfsStack.empty()) {
                ArrayList<Integer> coords = dfsStack.pop();
                Grid grid = gridList.get(coords);

                assert grid.isDense(); // just in case

                if(grid.visited) {
                    continue;
                }
                grid.visited = true;
                grid.cluster = currentCluster;
                System.out.println(coords + " " + currentCluster); // debug

                LinkedList<ArrayList<Integer>> neighbours = getNeighbours(coords);
                for(ArrayList<Integer> ngbr : neighbours) {
                    if(gridList.get(ngbr).isDense()) {
                        dfsStack.push(ngbr);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Clusterisation c = new Clusterisation();

        c.readFromFile();

        c.initialClustering();

//        System.out.println(c.hashmap);
    }
}
