import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;

/**
 * Compiles your program and checks it on the three examples.
 * Run from the project folder:   java tools/Check.java
 */
public class Check {

    static final String[] MISSION = {"1", "1", "1", "2", "2", "3", "3", "3", "4", "4", "4", "5", "5"};

    public static void main(String[] args) throws Exception {
        // 1. compile
        List<String> files = new ArrayList<>();
        for (String dir : new String[]{"src"}) {
            try (var s = Files.list(Path.of(dir))) { s.filter(p -> p.toString().endsWith(".java")).forEach(p -> files.add(p.toString())); }
        }
        List<String> javac = new ArrayList<>(List.of("-d", "out"));
        javac.addAll(files);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) { System.out.println("No compiler found. You need a JDK (javac), not only a JRE."); return; }
        if (compiler.run(null, null, null, javac.toArray(new String[0])) != 0) {
            System.out.println("The program does not compile. Fix the errors above.");
            return;
        }
        System.out.println("Compiles.");

        // 2. Mission 0
        System.out.println("\nMission 0, DisjointSetTest:");
        for (String line : run("DisjointSetTest")) System.out.println("  " + line);

        // 3. examples
        System.out.println("\nExamples:");
        boolean all = true;
        for (String name : new String[]{"tiny", "small", "dag"}) {
            List<String> expected = Files.readAllLines(Path.of("examples", name, "expected.txt"));
            List<String> got = run("Main", "examples/" + name + "/walls.txt", "examples/" + name + "/slides.txt");
            got.removeIf(l -> !l.contains(": ") || l.startsWith("WARNING"));
            int wrong = 0;
            StringBuilder report = new StringBuilder();
            for (int i = 0; i < expected.size(); i++) {
                String g = i < got.size() ? got.get(i) : "(missing)";
                if (!g.equals(expected.get(i))) {
                    wrong++;
                    if (wrong <= 3) report.append("    Mission ").append(MISSION[i]).append(": expected \"").append(expected.get(i)).append("\", got \"").append(g).append("\"\n");
                }
            }
            if (wrong == 0) System.out.println("  " + name + ": all 13 lines are right");
            else { all = false; System.out.println("  " + name + ": " + wrong + " of 13 lines are wrong"); System.out.print(report); }
        }
        System.out.println(all ? "\nAll examples pass. Now run it on your own data: java -cp out Main" : "\nFix the first mission in the list and run this check again.");
    }

    static List<String> run(String main, String... args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", "out", main));
        cmd.addAll(List.of(args));
        Path tmp = Files.createTempFile("check", ".txt");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.redirectOutput(tmp.toFile());
        Process p = pb.start();
        if (!p.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)) {
            p.destroyForcibly();
            System.out.println("  WARNING: " + main + " did not finish in 15 seconds (an endless loop?) and was stopped.");
        }
        List<String> lines = new ArrayList<>(Files.readAllLines(tmp));
        Files.deleteIfExists(tmp);
        return lines;
    }
}
