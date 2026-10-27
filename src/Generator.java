import java.util.*;

/** Makes a random maze of any size, for the timing in Mission 7. Ready to use. All costs are different, the order of the walls is shuffled. */
public class Generator {

    public static Input.Walls generate(int rows, int cols, long seed) {
        int w = rows * (cols - 1) + cols * (rows - 1);
        int[] a = new int[w], b = new int[w], cost = new int[w];
        int k = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cell = r * cols + c;
                if (c + 1 < cols) { a[k] = cell; b[k] = cell + 1; k++; }
                if (r + 1 < rows) { a[k] = cell; b[k] = cell + cols; k++; }
            }
        }
        Random rnd = new Random(seed);
        int[] costs = new int[w];
        for (int i = 0; i < w; i++) costs[i] = i + 1;
        for (int i = w - 1; i > 0; i--) { int j = rnd.nextInt(i + 1); int t = costs[i]; costs[i] = costs[j]; costs[j] = t; }
        for (int i = w - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
            t = b[i]; b[i] = b[j]; b[j] = t;
        }
        System.arraycopy(costs, 0, cost, 0, w);
        return new Input.Walls(rows, cols, a, b, cost);
    }
}
