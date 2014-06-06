package pl.edu.agh.student.clusterisation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Main {

    public static final int TOTAL_GRIDS = 1000;
    public static final int BOARD_SIZE = 30;
    public static final int PROBLEM_DIMENSION = 2;
    public static final String INPUT_FILE = "output/part-r-00000";
    public static final double TRANSITIONAL_THRESHOLD = 0.0;
    public static final double DENSE_THRESHOLD = 0.33;

    private static void generateData(){
        PrintWriter writer;
        try {
            writer = new PrintWriter(Main.INPUT_FILE, "UTF-8");
            Random rand = new Random();
            for(int line = 0; line < TOTAL_GRIDS; ++line) {
                StringBuilder sb = new StringBuilder();
                sb.append(rand.nextInt(BOARD_SIZE));
                for(int dim = 1; dim < PROBLEM_DIMENSION; ++dim) {
                    sb.append(" ").append(rand.nextInt(BOARD_SIZE));
                }
                sb.append("\t");
                sb.append(rand.nextDouble());
                writer.println(sb.toString());
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Clusterisation c = new Clusterisation();

        Main.generateData();

        c.readFromFile();

        c.printGridList();

        c.clusterize();

        c.printClusters();

        c.listClusters();
    }
}
