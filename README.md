# Assignment 3. Maze Escape

Student ID: **250117**  
Name: **Amir Romanov**  
Group: **SE-2519**  
Maze size: **11 x 8** (88 cells, 157 inner walls)

Java 17 or newer. The implementation uses only the Java standard library and its own DSU and graph algorithms. The original `Main`, `Input`, `Graphs`, `Render`, `Generator`, `Bench` and generator/checking tools are unchanged.

## Run

Run commands from the project folder (the folder containing `src` and this README):

```sh
javac --release 17 -d out src/*.java
java -cp out Main
```

Open [the interactive maze](output/maze.html) in a browser. Use arrow keys or WASD to walk; the checkboxes show the solution, cell numbers, costs, zones and slides. `Main` reads `data/walls.txt` and `data/slides.txt`, prints its results without asking for input, and recreates both maze pictures.

The personal input files were generated with the unchanged tool:

```sh
java tools/MakeVariant.java 250117 data
```

## Program output

```text
Broken: 87
Kept: 70
Last broken wall: 123
MST cost: 29455
Heaviest broken wall: 830
Path length: 23
DFS visited: 53
Dead ends: 23
Zones: 10
Largest zone: 17
Is DAG: 0
Flow stages: 6
Fastest route cost: 8
```

![Personal maze: start in green, exit in blue, solution in red](output/maze.png)

## Questions

### Mission 0. What do find and union do, and what makes them fast?

`find(x)` returns the root representing the set containing `x`; `union(a, b)` joins two different sets and returns whether a merge happened. Union by size places the smaller tree under the larger root, while the iterative `find` compresses the whole visited path to its root, giving an amortized O(alpha(n)) cost per operation. `count()` stores the current number of sets, so it takes O(1).

### Mission 1. Why are Broken = R*C - 1 and Kept = (R-1)*(C-1)?

There are initially R*C components, and every successful union reduces their number by one; since the full rectangular grid is connected, exactly R*C - 1 walls are broken. A grid has R*(C-1) + C*(R-1) inner walls, so subtracting the broken walls leaves (R-1)*(C-1). Here this gives 87 broken walls and 70 kept walls.

### Mission 2. What are the MST costs, and why are they equal?

Kruskal and Prim both return **29455** for the personal maze; the heaviest edge in the minimum spanning tree costs **830**. Kruskal adds the cheapest edge joining different components, while Prim adds the cheapest edge crossing from its current tree to an unvisited cell; both choices are safe by the cut property. All wall costs are distinct, so the minimum spanning tree is unique and both algorithms have the same total cost.

### Mission 3. Why is there exactly one path? How is stack overflow avoided?

The broken walls connect every cell without making a cycle, so they form a tree; two different simple paths between the same cells would create a cycle. The DFS uses arrays on the heap as an explicit stack, with each frame remembering its next direction in the order up, right, down, left, instead of using recursive Java calls. It counts cells when they are first marked and stops as soon as the exit is marked: here 53 cells are visited and the resulting path has 23 moves.

### Mission 4. Why reverse the graph? Why does Zones = n mean no cycle?

The first DFS records finishing order in the original graph; taking vertices in reverse finishing order selects a source component among the components that remain. On the reversed graph, the second DFS can reach every vertex of that component without escaping into another unassigned component, so each pass collects exactly one zone and the resulting zone numbers are topological. Because the input forbids self-loops, a directed cycle must contain at least two vertices and places them in the same zone; therefore the graph is acyclic exactly when every zone is a singleton.

### Mission 5. Why is zone-number order sufficient, and what does the pass cost?

Every edge between zones goes from a smaller number to a larger number, so all incoming results are final when a zone is processed; one forward pass can relax both minimum route cost and maximum chain length. Parallel slides between the same two zones are merged using their minimum time, and movement within a zone is free. Building this condensed graph takes O(m+k) time and space with array-based lists and markers, and the dynamic programming pass takes O(k+q) time, where k is the number of zones and q the number of distinct edges between them; no priority queue is used.

## AI use

**OpenAI Codex (ChatGPT)** generated the algorithm implementations, additional tests and this report, and executed input generation, verification and timing. This is a version made with substantial AI assistance; it does not claim independent authorship, review or modification by the student. Before submission, review and adapt the code and explanations to meet the course rules, and keep this disclosure accurate.

Git commits made during this work identify `Codex (AI assistant)` as the author. No external books, websites, graph libraries or DSU libraries were used; the supplied starter project and assignment were the source of the requirements.

## Verification

```sh
java tools/Check.java
javac --release 17 -Xlint:all -d out src/*.java
java -Xss256k -cp out DisjointSetTest
java -Xmx768m -Xss256k -cp out AlgorithmTest --stress
```

- All 13 output lines match the expected values for `tiny`, `small` and `dag`.
- The sources compile for Java 17 without compiler warnings.
- DSU checks include self-unions, an empty DSU, a singleton and a million-cell chain.
- `AlgorithmTest` performs 17,464 small-instance assertions, including 80 random grids and 400 random slide graphs with a fixed seed of 250117. The independent oracles use BFS for connectivity, exhaustive enumeration for tiny spanning trees, recursive DFS only on tiny test graphs, transitive closure for SCC membership, exhaustive DAG paths for flow stages, and Floyd-Warshall for zone route costs.
- Edge cases include one cell, thin mazes, parallel slides, disconnected slide graphs, one-zone graphs, a longest chain outside the start's reachable region, and totals greater than 32-bit integers.
- Stress checks pass for a 1000 x 1000 snake maze, a million-vertex directed cycle, and a million-vertex directed chain. The Java call stack is limited to 256 KiB for this run.

The recursive methods in the test oracle are used only on graphs of at most nine vertices. All DFS traversals in the mission implementation are iterative.

## Timing and complexity (Mission 7)

The measured results and complexity table are added in the Mission 7 commit.

## Git and submission

The local repository is on `main`, with an initial starter commit and separate commits for the missions. These are actual commits made during this session, not a reconstruction of earlier student work. Inspect them with `git log --oneline`.

- [x] Source code, personal data and maze picture are included.
- [x] The program compiles and the supplied examples pass.
- [x] The output, answers and accurate AI-use note are recorded.
- [ ] The student has reviewed and adapted the code and explanations.
- [ ] The repository has been published as a public GitHub repository with its commit history.
- [ ] The public repository link has been submitted in Moodle.

The last three items require the student's action; they have not been marked as completed by the assistant. The setup and assignment remain available in `START_HERE.md` and `docs/Assignment3.pdf`.
