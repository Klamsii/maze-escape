/** Helpers for Missions 3, 4 and 5. Ready to use. */
public class Graphs {

    /**
     * The maze as a graph: result[cell] is the list of the cells you can walk to from this cell
     * (the neighbours behind a broken wall). 'broken' holds the indexes of the broken walls.
     * The order of the cells inside one list is not defined.
     */
    public static int[][] open(Input.Walls w, int[] broken) {
        int n = w.cells();
        int[] degree = new int[n];
        for (int k : broken) { degree[w.a()[k]]++; degree[w.b()[k]]++; }
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) g[i] = new int[degree[i]];
        int[] filled = new int[n];
        for (int k : broken) {
            int a = w.a()[k], b = w.b()[k];
            g[a][filled[a]++] = b;
            g[b][filled[b]++] = a;
        }
        return g;
    }

    /** The slides leaving each cell: result[u] holds the numbers i of the slides that start in u (the cell they lead to is s.to()[i]). */
    public static int[][] slidesFrom(Input.Slides s) { return group(s.n(), s.from()); }

    /** The slides entering each cell: result[v] holds the numbers i of the slides that end in v (the cell they start in is s.from()[i]). */
    public static int[][] slidesInto(Input.Slides s) { return group(s.n(), s.to()); }

    private static int[][] group(int n, int[] key) {
        int[] count = new int[n];
        for (int k : key) count[k]++;
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) g[i] = new int[count[i]];
        int[] filled = new int[n];
        for (int i = 0; i < key.length; i++) g[key[i]][filled[key[i]]++] = i;
        return g;
    }
}
