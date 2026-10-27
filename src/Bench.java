/**
 * Mission 7. Timing of Kruskal and Prim. Ready: you do not change it, you only run it and copy the numbers to README.md.
 * Run:  java -cp out Bench
 */
public class Bench {

    public static void main(String[] args) {
        System.out.println("size   kruskal_ms (5 runs, then average)   prim_ms (5 runs, then average)");
        for (int size : new int[]{100, 300, 1000}) {
            Input.Walls w = Generator.generate(size, size, 1);
            StringBuilder k = new StringBuilder(), p = new StringBuilder();
            double ks = 0, ps = 0;
            for (int run = 0; run < 5; run++) {
                double kt = timeMs(() -> Maze.cheapestMaze(w));
                double pt = timeMs(() -> Maze.primCost(w));
                k.append(String.format("%.0f ", kt)); p.append(String.format("%.0f ", pt));
                ks += kt; ps += pt;
            }
            System.out.printf("%-6d %s avg %.0f    %s avg %.0f%n", size, k, ks / 5, p, ps / 5);
        }
    }

    static double timeMs(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1e6;
    }
}
