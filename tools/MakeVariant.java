import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Makes the personal files of one student: walls.txt (the maze) and slides.txt (one-way slides between cells).
 *  Usage: java MakeVariant.java STUDENT_ID [outdir]   (or: javac MakeVariant.java, then java MakeVariant STUDENT_ID)
 *  walls.txt:  first line "R C W", then W lines "a b w": a wall between the neighbouring cells a and b, w = cost of breaking it.
 *  slides.txt: first line "n S", then S lines "a b w": a one-way slide from cell a to cell b, w = time.
 *  Cells are numbered 0 .. R*C-1 row by row. Start = cell 0, exit = cell R*C-1. */
public class MakeVariant {
    static final String SALT = "AITU-DAA-A3-MAZE-2026";

    static class Rng {
        long x;
        Rng(String sid, String tag) throws Exception {
            byte[] h = MessageDigest.getInstance("SHA-256").digest((SALT + "|" + sid + "|" + tag).getBytes(StandardCharsets.UTF_8));
            long v = 0; for (int i = 0; i < 8; i++) v = (v << 8) | (h[i] & 0xFFL);
            x = v;
        }
        long next() {
            x += 0x9E3779B97F4A7C15L;
            long z = x;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }
        int below(int n) { return (int) Long.remainderUnsigned(next(), n); }
        int randint(int a, int b) { return a + below(b - a + 1); }
        <T> T choice(List<T> l) { return l.get(below(l.size())); }
        <T> void shuffle(List<T> l) { for (int i = l.size() - 1; i > 0; i--) { int j = below(i + 1); T t = l.get(i); l.set(i, l.get(j)); l.set(j, t); } }
        List<Integer> sample(List<Integer> src, int k) {
            List<Integer> l = new ArrayList<>(src);
            for (int i = 0; i < k; i++) { int j = i + below(l.size() - i); int t = l.get(i); l.set(i, l.get(j)); l.set(j, t); }
            return new ArrayList<>(l.subList(0, k));
        }
    }

    public static void main(String[] args) throws Exception {
        String sid = args[0]; File out = new File(args.length > 1 ? args[1] : "."); out.mkdirs();
        Rng d = new Rng(sid, "dims"); int R = d.randint(8, 12), C = d.randint(8, 12), n = R * C;
        // walls
        Rng r = new Rng(sid, "walls"); List<int[]> ws = new ArrayList<>();
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) {
            int u = i * C + j;
            if (j < C - 1) ws.add(new int[]{u, u + 1});
            if (i < R - 1) ws.add(new int[]{u, u + C});
        }
        r.shuffle(ws);
        List<Integer> pool = new ArrayList<>(); for (int v = 1; v < 1000; v++) pool.add(v);
        List<Integer> cost = r.sample(pool, ws.size());
        try (PrintWriter w = new PrintWriter(new File(out, "walls.txt"))) {
            w.println(R + " " + C + " " + ws.size());
            for (int i = 0; i < ws.size(); i++) w.println(ws.get(i)[0] + " " + ws.get(i)[1] + " " + cost.get(i));
        }
        // slides
        Rng s = new Rng(sid, "slides"); boolean dag = s.below(10) < 2; int k = dag ? n : s.randint(8, 12);
        int[] sizes = new int[k]; Arrays.fill(sizes, 1);
        if (!dag) {
            List<Integer> open = new ArrayList<>(); for (int z = 0; z < k; z++) if (z != 1 && z != k - 2) open.add(z);
            for (int i = 0; i < n - k; i++) sizes[s.choice(open)]++;
        }
        List<Integer> labels = new ArrayList<>(); for (int i = 0; i < n; i++) labels.add(i);
        s.shuffle(labels);
        List<List<Integer>> zones = new ArrayList<>(); int p = 0;
        for (int z = 0; z < k; z++) { zones.add(new ArrayList<>(labels.subList(p, p + sizes[z]))); p += sizes[z]; }
        int[] f = find(zones, 0);
        if (f[0] != 0) swap(zones, f[0], f[1], 0, 0);
        f = find(zones, n - 1);
        if (f[0] != k - 1) swap(zones, f[0], f[1], k - 1, 0);
        List<int[]> es = new ArrayList<>(); Set<Long> seen = new HashSet<>();
        for (int z = 0; z < k; z++) {
            List<Integer> zz = zones.get(z);
            if (zz.size() >= 2) {
                List<Integer> order = new ArrayList<>(zz); s.shuffle(order);
                for (int i = 0; i < order.size(); i++) add(es, seen, order.get(i), order.get((i + 1) % order.size()), s.randint(1, 20), n);
                int ch = s.below(zz.size() / 2 + 1);
                for (int q = 0; q < ch; q++) { int a = s.choice(zz), b = s.choice(zz), w = s.randint(1, 20); add(es, seen, a, b, w, n); }
            }
        }
        for (int j = 1; j < k; j++) {
            int cnt = s.randint(1, 3);
            for (int q = 0; q < cnt; q++) { int i = s.below(j); int a = s.choice(zones.get(i)), b = s.choice(zones.get(j)), w = s.randint(1, 20); add(es, seen, a, b, w, n); }
        }
        for (int q = 0; q < k / 2; q++) {
            int i = s.below(k - 1); int j = i + 1 + s.below(k - 1 - i);
            int a = s.choice(zones.get(i)), b = s.choice(zones.get(j)), w = s.randint(1, 20); add(es, seen, a, b, w, n);
        }
        s.shuffle(es);
        try (PrintWriter w = new PrintWriter(new File(out, "slides.txt"))) {
            w.println(n + " " + es.size());
            for (int[] e : es) w.println(e[0] + " " + e[1] + " " + e[2]);
        }
    }
    static int[] find(List<List<Integer>> zones, int c) {
        for (int z = 0; z < zones.size(); z++) { int i = zones.get(z).indexOf(c); if (i >= 0) return new int[]{z, i}; }
        return null;
    }
    static void swap(List<List<Integer>> zones, int z1, int i1, int z2, int i2) {
        int t = zones.get(z1).get(i1); zones.get(z1).set(i1, zones.get(z2).get(i2)); zones.get(z2).set(i2, t);
    }
    static void add(List<int[]> es, Set<Long> seen, int a, int b, int w, int n) {
        if (a != b && seen.add((long) a * n + b)) es.add(new int[]{a, b, w});
    }
}
