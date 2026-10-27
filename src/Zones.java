import java.util.Arrays;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] from = Graphs.slidesFrom(s);
        int[][] into = Graphs.slidesInto(s);
        boolean[] seen = new boolean[n];
        int[] nextEdge = new int[n];
        int[] stack = new int[n];
        int[] finish = new int[n];
        int finished = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            int top = 0;
            stack[0] = start;
            seen[start] = true;
            while (top >= 0) {
                int u = stack[top];
                if (nextEdge[u] < from[u].length) {
                    int v = s.to()[from[u][nextEdge[u]++]];
                    if (!seen[v]) {
                        seen[v] = true;
                        stack[++top] = v;
                    }
                } else {
                    finish[finished++] = u;
                    top--;
                }
            }
        }

        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zones = 0;
        for (int i = finished - 1; i >= 0; i--) {
            int start = finish[i];
            if (zoneOf[start] != -1) continue;
            int size = 0;
            stack[size++] = start;
            zoneOf[start] = zones;
            while (size > 0) {
                int u = stack[--size];
                for (int edge : into[u]) {
                    int v = s.from()[edge];
                    if (zoneOf[v] == -1) {
                        zoneOf[v] = zones;
                        stack[size++] = v;
                    }
                }
            }
            zones++;
        }
        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        if (s.n() == 0) return new long[]{0, 0};
        // Group the cross-zone slides by their source zone using array-based lists.
        int[] head = new int[zoneCount];
        Arrays.fill(head, -1);
        int[] next = new int[s.count()];
        for (int edge = 0; edge < s.count(); edge++) {
            int a = zoneOf[s.from()[edge]], b = zoneOf[s.to()[edge]];
            if (a == b) continue;
            if (a > b) throw new IllegalArgumentException("Zone numbers are not topological");
            next[edge] = head[a];
            head[a] = edge;
        }

        // Build compact zone adjacency lists. One edge per pair, with the minimum time.
        int[] begin = new int[zoneCount + 1];
        int[] to = new int[s.count()];
        int[] cost = new int[s.count()];
        int[] lastSource = new int[zoneCount];
        int[] position = new int[zoneCount];
        Arrays.fill(lastSource, -1);
        int edges = 0;
        for (int a = 0; a < zoneCount; a++) {
            begin[a] = edges;
            for (int edge = head[a]; edge != -1; edge = next[edge]) {
                int b = zoneOf[s.to()[edge]];
                if (lastSource[b] != a) {
                    lastSource[b] = a;
                    position[b] = edges;
                    to[edges] = b;
                    cost[edges++] = s.time()[edge];
                } else {
                    int p = position[b];
                    cost[p] = Math.min(cost[p], s.time()[edge]);
                }
            }
        }
        begin[zoneCount] = edges;

        int[] stages = new int[zoneCount];
        Arrays.fill(stages, 1); // A chain can start in any zone, even an unreachable one.
        long[] distance = new long[zoneCount];
        Arrays.fill(distance, Long.MAX_VALUE);
        distance[zoneOf[0]] = 0;
        int flowStages = 0;
        for (int a = 0; a < zoneCount; a++) {
            flowStages = Math.max(flowStages, stages[a]);
            for (int edge = begin[a]; edge < begin[a + 1]; edge++) {
                int b = to[edge];
                stages[b] = Math.max(stages[b], stages[a] + 1);
                if (distance[a] != Long.MAX_VALUE) {
                    distance[b] = Math.min(distance[b], distance[a] + cost[edge]);
                }
            }
        }
        return new long[]{flowStages, distance[zoneOf[s.n() - 1]]};
    }
}
