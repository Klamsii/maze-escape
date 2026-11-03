import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Independent small-instance oracles, plus optional iterative-DFS stress checks. */
public class AlgorithmTest {
    private static final long INF = Long.MAX_VALUE / 4;
    private static int checks;

    private static void require(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Random random = new Random(250117);
        for (int run = 0; run < 80; run++) {
            checkMaze(Generator.generate(1 + random.nextInt(3),
                    1 + random.nextInt(3), random.nextLong()));
        }
        Input.Walls expensive = new Input.Walls(1, 4, new int[]{0, 1, 2},
                new int[]{1, 2, 3}, new int[]{1_500_000_000, 1_500_000_001, 1_500_000_002});
        require(Maze.primCost(expensive) == 4_500_000_003L, "Prim needs a long total");
        require(cost(expensive, Maze.cheapestMaze(expensive)) == 4_500_000_003L,
                "Kruskal needs a long total");

        for (int run = 0; run < 400; run++) {
            int n = 1 + random.nextInt(9);
            List<int[]> edges = new ArrayList<>();
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) {
                if (a != b && random.nextInt(4) == 0) {
                    edges.add(new int[]{a, b, 1 + random.nextInt(50)});
                    if (random.nextBoolean()) edges.add(new int[]{a, b, 1 + random.nextInt(50)});
                }
            }
            checkSlides(slides(n, edges));
        }
        // The longest chain can be outside the part reachable from cell zero.
        checkSlides(slides(6, List.of(new int[]{0, 5, 7}, new int[]{1, 2, 2},
                new int[]{2, 3, 3}, new int[]{3, 4, 4})));
        checkSlides(slides(4, List.of(new int[]{0, 1, 2_000_000_000},
                new int[]{1, 2, 1_500_000_000}, new int[]{2, 3, 1_000_000_000})));
        checkSlides(slides(4, List.of(new int[]{0, 1, 3}, new int[]{1, 2, 4},
                new int[]{2, 3, 5}, new int[]{3, 0, 6})));
        System.out.println("Small-instance checks passed: " + checks);

        if (args.length > 0 && args[0].equals("--stress")) {
            stressEscape();
            stressZones();
            System.out.println("Million-cell DFS and SCC/DAG checks passed.");
        }
    }

    private static long cost(Input.Walls w, int[] indexes) {
        long result = 0;
        for (int k : indexes) result += w.cost()[k];
        return result;
    }

    private static boolean reaches(boolean[][] graph, int start, int target) {
        boolean[] seen = new boolean[graph.length];
        int[] queue = new int[graph.length];
        int head = 0, tail = 0;
        queue[tail++] = start;
        seen[start] = true;
        while (head < tail) {
            int u = queue[head++];
            if (u == target) return true;
            for (int v = 0; v < graph.length; v++) if (graph[u][v] && !seen[v]) {
                seen[v] = true;
                queue[tail++] = v;
            }
        }
        return false;
    }

    private static void checkMaze(Input.Walls w) {
        int n = w.cells();
        int[] originalCosts = w.cost().clone();
        boolean[][] open = new boolean[n][n];
        int[] expectedBroken = new int[n - 1];
        int used = 0;
        // A BFS connectivity oracle avoids using the DSU under test.
        for (int k = 0; k < w.count(); k++) {
            int a = w.a()[k], b = w.b()[k];
            if (!reaches(open, a, b)) {
                expectedBroken[used++] = k;
                open[a][b] = open[b][a] = true;
            }
        }
        int[] broken = Maze.breakWalls(w);
        require(Arrays.equals(broken, expectedBroken), "input-order maze differs from BFS oracle");

        long minimum = INF;
        // At most 12 walls (3 by 3), so all candidate spanning trees can be enumerated.
        for (int mask = 0; mask < (1 << w.count()); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            boolean[][] candidate = new boolean[n][n];
            long sum = 0;
            for (int k = 0; k < w.count(); k++) if ((mask & (1 << k)) != 0) {
                candidate[w.a()[k]][w.b()[k]] = candidate[w.b()[k]][w.a()[k]] = true;
                sum += w.cost()[k];
            }
            boolean connected = true;
            for (int v = 0; v < n; v++) connected &= reaches(candidate, 0, v);
            if (connected) minimum = Math.min(minimum, sum);
        }
        int[] cheapest = Maze.cheapestMaze(w);
        require(cheapest.length == n - 1 && cost(w, cheapest) == minimum,
                "Kruskal differs from exhaustive MST oracle");
        require(Maze.primCost(w) == minimum, "Prim differs from exhaustive MST oracle");
        require(Arrays.equals(originalCosts, w.cost()), "MST algorithms changed input costs");

        Escape.Result result = Escape.escape(w, broken);
        List<Integer> path = new ArrayList<>();
        int[] visited = {0};
        referenceDfs(0, w.cols(), open, new boolean[n], path, visited);
        require(Arrays.equals(result.path(), path.stream().mapToInt(Integer::intValue).toArray()),
                "escape path differs from the small recursive oracle");
        require(result.visited() == visited[0], "DFS discovery count differs");
        int deadEnds = 0;
        for (boolean[] row : open) {
            int degree = 0;
            for (boolean edge : row) if (edge) degree++;
            if (degree == 1) deadEnds++;
        }
        require(result.deadEnds() == deadEnds, "dead-end count differs");
    }

    // Recursion is deliberately limited to tiny test graphs; the solution uses stacks.
    private static boolean referenceDfs(int u, int cols, boolean[][] open, boolean[] seen,
                                        List<Integer> path, int[] visited) {
        seen[u] = true;
        visited[0]++;
        path.add(u);
        if (u == open.length - 1) return true;
        int[] neighbours = {u - cols, u + 1, u + cols, u - 1};
        for (int v : neighbours) {
            if (v >= 0 && v < open.length && open[u][v] && !seen[v]
                    && referenceDfs(v, cols, open, seen, path, visited)) return true;
        }
        path.remove(path.size() - 1);
        return false;
    }

    private static Input.Slides slides(int n, List<int[]> edges) {
        int[] from = new int[edges.size()], to = new int[edges.size()], time = new int[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            from[i] = edges.get(i)[0];
            to[i] = edges.get(i)[1];
            time[i] = edges.get(i)[2];
        }
        return new Input.Slides(n, from, to, time);
    }

    private static void checkSlides(Input.Slides s) {
        int n = s.n();
        boolean[][] reach = new boolean[n][n];
        for (int u = 0; u < n; u++) reach[u][u] = true;
        for (int i = 0; i < s.count(); i++) reach[s.from()[i]][s.to()[i]] = true;
        for (int k = 0; k < n; k++) for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) {
            reach[u][v] |= reach[u][k] && reach[k][v];
        }
        int[] zone = Zones.kosaraju(s);
        int count = Arrays.stream(zone).max().orElse(-1) + 1;
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) {
            require((zone[u] == zone[v]) == (reach[u][v] && reach[v][u]),
                    "SCC partition differs from transitive closure");
        }

        long[][] graph = new long[count][count];
        for (long[] row : graph) Arrays.fill(row, INF);
        for (int i = 0; i < s.count(); i++) {
            int a = zone[s.from()[i]], b = zone[s.to()[i]];
            require(a == b || a < b, "SCC numbering must be topological");
            if (a != b) graph[a][b] = Math.min(graph[a][b], s.time()[i]);
        }
        int longest = 0;
        for (int u = 0; u < count; u++) longest = Math.max(longest, chainLength(graph, u));
        long[][] shortest = new long[count][];
        for (int u = 0; u < count; u++) {
            shortest[u] = graph[u].clone();
            shortest[u][u] = 0;
        }
        for (int k = 0; k < count; k++) for (int u = 0; u < count; u++) for (int v = 0; v < count; v++) {
            if (shortest[u][k] != INF && shortest[k][v] != INF) {
                shortest[u][v] = Math.min(shortest[u][v], shortest[u][k] + shortest[k][v]);
            }
        }
        long expectedCost = shortest[zone[0]][zone[n - 1]];
        if (expectedCost == INF) expectedCost = Long.MAX_VALUE;
        long[] result = Zones.routes(s, zone, count);
        require(result[0] == longest, "flow stages differ from exhaustive path oracle");
        require(result[1] == expectedCost, "zone route differs from Floyd-Warshall oracle");
    }

    private static int chainLength(long[][] graph, int u) {
        int best = 1;
        for (int v = 0; v < graph.length; v++) if (graph[u][v] != INF) {
            best = Math.max(best, 1 + chainLength(graph, v));
        }
        return best;
    }

    private static int snakeCell(int i, int cols) {
        int row = i / cols, col = i % cols;
        return row * cols + (row % 2 == 0 ? col : cols - 1 - col);
    }

    private static void stressEscape() {
        int side = 1000, n = side * side;
        int[] a = new int[n - 1], b = new int[n - 1], cost = new int[n - 1], broken = new int[n - 1];
        for (int i = 0; i < n - 1; i++) {
            a[i] = snakeCell(i, side);
            b[i] = snakeCell(i + 1, side);
            cost[i] = i + 1;
            broken[i] = i;
        }
        Escape.Result result = Escape.escape(new Input.Walls(side, side, a, b, cost), broken);
        int expectedLength = n - side + 1;
        require(result.path().length == expectedLength && result.visited() == expectedLength,
                "million-cell snake must reach the exit without a recursive call stack");
        require(result.deadEnds() == 2, "a snake has two dead ends");
        for (int i = 0; i < result.path().length; i++) {
            require(result.path()[i] == snakeCell(i, side), "snake path is not continuous");
        }
    }

    private static void stressZones() {
        int n = 1_000_000;
        int[] from = new int[n], to = new int[n], time = new int[n];
        Arrays.fill(time, 1);
        for (int i = 0; i < n; i++) {
            from[i] = i;
            to[i] = (i + 1) % n;
        }
        Input.Slides cycle = new Input.Slides(n, from, to, time);
        int[] zones = Zones.kosaraju(cycle);
        for (int z : zones) require(z == 0, "a million-cell directed cycle is one SCC");
        require(Arrays.equals(Zones.routes(cycle, zones, 1), new long[]{1, 0}),
                "walking inside a zone is free");
        Input.Slides chain = new Input.Slides(n, Arrays.copyOf(from, n - 1),
                Arrays.copyOf(to, n - 1), Arrays.copyOf(time, n - 1));
        zones = Zones.kosaraju(chain);
        for (int i = 0; i < n; i++) require(zones[i] == i, "chain SCC order differs");
        require(Arrays.equals(Zones.routes(chain, zones, n), new long[]{n, n - 1}),
                "million-zone DAG route differs");
    }
}
