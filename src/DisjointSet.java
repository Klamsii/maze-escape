/**
 * Mission 0. Disjoint set union (DSU).
 * Cells are numbered 0 .. n-1. Write the whole class here.
 * Needed: union by size (the smaller tree goes under the larger one) and path compression in find.
 */
public class DisjointSet {

    public DisjointSet(int n) {
        // TODO
    }

    /** The root of the set that contains x. */
    public int find(int x) {
        // TODO
        return x;
    }

    /** Joins the sets of a and b. Returns true if they were different sets, false if they were already one set. */
    public boolean union(int a, int b) {
        // TODO
        return false;
    }

    public boolean connected(int a, int b) {
        // TODO
        return false;
    }

    /** Number of sets now. */
    public int count() {
        // TODO
        return 0;
    }
}
