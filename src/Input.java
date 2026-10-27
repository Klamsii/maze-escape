import java.io.*;
import java.util.*;

/** Reading of walls.txt and slides.txt. Ready to use, you do not need to change it. */
public class Input {

    /** The maze: rows x cols cells, W inner walls. Wall number k (counting from 1 as in the file) is stored at index k - 1. */
    public record Walls(int rows, int cols, int[] a, int[] b, int[] cost) {
        public int cells() { return rows * cols; }
        public int count() { return a.length; }
    }

    /** The slides: n cells, S one-way slides from[i] -> to[i] that take time[i]. */
    public record Slides(int n, int[] from, int[] to, int[] time) {
        public int count() { return from.length; }
    }

    public static Walls readWalls(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int rows = Integer.parseInt(st.nextToken()), cols = Integer.parseInt(st.nextToken()), w = Integer.parseInt(st.nextToken());
            int[] a = new int[w], b = new int[w], c = new int[w];
            for (int i = 0; i < w; i++) {
                st = new StringTokenizer(br.readLine());
                a[i] = Integer.parseInt(st.nextToken()); b[i] = Integer.parseInt(st.nextToken()); c[i] = Integer.parseInt(st.nextToken());
            }
            return new Walls(rows, cols, a, b, c);
        }
    }

    public static Slides readSlides(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken()), s = Integer.parseInt(st.nextToken());
            int[] f = new int[s], t = new int[s], w = new int[s];
            for (int i = 0; i < s; i++) {
                st = new StringTokenizer(br.readLine());
                f[i] = Integer.parseInt(st.nextToken()); t[i] = Integer.parseInt(st.nextToken()); w[i] = Integer.parseInt(st.nextToken());
            }
            return new Slides(n, f, t, w);
        }
    }
}
