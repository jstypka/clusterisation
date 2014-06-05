package pl.edu.agh.student.clusterisation;

class Grid {
    public Boolean visited;
    public Boolean dense;
    public int cluster;

    public Grid(Boolean v, int c, Boolean d) {
        visited = v;
        dense = d;
        cluster = c;
    }

    public Boolean isDense() {
        return dense;
    }
}
