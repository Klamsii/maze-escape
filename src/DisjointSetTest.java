/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        check(!d.union(4, 4), "union of a singleton with itself changes nothing");
        check(!d.union(0, 0), "union of a larger set with itself changes nothing");
        check(d.count() == 3, "self unions preserve the count");
        d.union(4, 0); // The smaller set is deliberately the first argument.
        check(d.connected(4, 2), "a singleton can be joined to a larger set");
        check(d.count() == 2, "joining a singleton reduces the count once");

        int n = 1_000_000;
        DisjointSet chain = new DisjointSet(n);
        boolean allJoined = true;
        for (int i = 1; i < n; i++) allJoined &= chain.union(i - 1, i);
        check(allJoined, "a million-cell chain joins every new cell");
        check(chain.count() == 1, "one set after a million-cell chain");
        boolean oneRoot = true;
        int root = chain.find(0);
        for (int i = n - 1; i >= 0; i--) oneRoot &= chain.find(i) == root;
        check(oneRoot, "all chain cells have the same root without recursion");
        check(!chain.union(n - 1, 0) && chain.count() == 1,
                "closing an existing chain does not reduce the count");
        check(new DisjointSet(0).count() == 0, "an empty DSU has zero sets");
        DisjointSet singleton = new DisjointSet(1);
        check(singleton.find(0) == 0 && !singleton.union(0, 0)
                && singleton.count() == 1, "one-cell DSU remains one set");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
        if (failed != 0) throw new AssertionError(failed + " checks failed");
    }
}
