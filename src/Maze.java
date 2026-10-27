/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int used = 0;
        for (int k = 0; k < w.count() && used < broken.length; k++) {
            if (sets.union(w.a()[k], w.b()[k])) broken[used++] = k;
        }
        return Arrays.copyOf(broken, used);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];
        for (int k = 0; k < order.length; k++) order[k] = k;
        // Sort indexes, keeping the input arrays and wall numbers unchanged.
        Arrays.sort(order, Comparator.comparingInt(k -> w.cost()[k]));
        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int used = 0;
        for (int k : order) {
            if (used == broken.length) break;
            if (sets.union(w.a()[k], w.b()[k])) broken[used++] = k;
        }
        return Arrays.copyOf(broken, used);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n == 0) return 0;
        // Adjacency lists hold wall indexes, so each undirected wall occurs twice.
        int[] degree = new int[n];
        for (int k = 0; k < w.count(); k++) {
            degree[w.a()[k]]++;
            degree[w.b()[k]]++;
        }
        int[][] incident = new int[n][];
        for (int u = 0; u < n; u++) incident[u] = new int[degree[u]];
        int[] filled = new int[n];
        for (int k = 0; k < w.count(); k++) {
            int a = w.a()[k], b = w.b()[k];
            incident[a][filled[a]++] = k;
            incident[b][filled[b]++] = k;
        }

        boolean[] inTree = new boolean[n];
        long[] best = new long[n];
        Arrays.fill(best, Long.MAX_VALUE);
        PriorityQueue<Connection> queue = new PriorityQueue<>(
                Comparator.comparingInt(Connection::cost));
        best[0] = 0;
        queue.add(new Connection(0, 0));
        long total = 0;
        int reached = 0;
        while (!queue.isEmpty() && reached < n) {
            Connection next = queue.remove();
            int u = next.cell();
            if (inTree[u]) continue; // Ignore old entries for an already chosen cell.
            inTree[u] = true;
            reached++;
            total += next.cost();
            for (int k : incident[u]) {
                int v = w.a()[k] == u ? w.b()[k] : w.a()[k];
                if (!inTree[v] && w.cost()[k] < best[v]) {
                    best[v] = w.cost()[k];
                    queue.add(new Connection(v, w.cost()[k]));
                }
            }
        }
        if (reached != n) throw new IllegalArgumentException("The maze is disconnected");
        return total;
    }

    private record Connection(int cell, int cost) {}
}
