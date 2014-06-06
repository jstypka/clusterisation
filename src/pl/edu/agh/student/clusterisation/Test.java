package pl.edu.agh.student.clusterisation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Test {

    public static final int TOTAL_GRIDS = 15;
    public static final int BOARD_SIZE = 5;
    public static final int PROBLEM_DIMENSION = 2;

    private static void generateData(){
        PrintWriter writer;
        try {
            writer = new PrintWriter(Clusterisation.filename, "UTF-8");
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

//        generateData();

        c.readFromFile();

        c.printGridList();

        System.out.println();
        System.out.println();

        c.clusterize();

        c.printClusters();
    }
}
