package pl.edu.agh.student.clusterisation;

class Grid {
    public boolean visited;
    public boolean dense;
    public int cluster;

    public Grid(boolean v, int c, boolean d) {
        visited = v;
        dense = d;
        cluster = c;
    }

    public boolean isDense() {
        return dense;
    }
}
