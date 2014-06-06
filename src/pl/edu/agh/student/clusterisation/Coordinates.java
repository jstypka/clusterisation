package pl.edu.agh.student.clusterisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Coordinates {

    public List<Integer> coords;

    public Coordinates(List<Integer> c) {
        coords = c;
    }

    public Coordinates(Coordinates other) {
        coords = new ArrayList<>(other.coords);
    }

    public Coordinates() {
        coords = new ArrayList<>();
    }

    public Integer getDimension(int d) {
        if(coords != null && coords.size() > d) {
            return coords.get(d);
        } else {
            System.out.println("Cannot get selected value");
            return null;
        }
    }

    public void setDimension(int d, int val) {
        if(coords != null && coords.size() > d) {
            coords.set(d, val);
        } else {
            System.out.println("Cannot set selected value");
        }
    }

    public int getSize() {
        return coords.size();
    }

    public int getCluster(Map<Coordinates,Grid> gridList) {
        return gridList.get(this).cluster;
    }

    @Override
    public int hashCode() {
        return coords.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Coordinates)) {
            return false;
        }
        List<Integer> other_list = ((Coordinates)other).coords;
        if(other_list.size() != coords.size()) {
            return false;
        }
        for(int i = 0; i < coords.size(); ++i) {
            if(coords.get(i).intValue() != other_list.get(i).intValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return coords.toString();
    }
}
