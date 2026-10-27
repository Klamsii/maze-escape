# Start here

Read this page once, from top to bottom. It explains the setup, your files, the missions and how to submit.

## 1. What you need

- Java 17 or newer. Check in a terminal: `java -version` and `javac -version`. Both must show 17 or more. If `javac` is not found, you have only a JRE: install a JDK (for example Temurin 17 or 21).
- git. Check: `git --version`.
- An editor. IntelliJ IDEA Community or VS Code with the Java extension both work. You can also use only a terminal.

## 2. Open the right folder

Unzip the project. Open the folder `maze-escape`: the one that contains `src`, `tools` and `README.md`. Not the folder above it. All commands below are run in this folder.

If your editor complains about the project: in IntelliJ choose File, New, Project from Existing Sources, and mark `src` as the Sources root (blue folder). In VS Code open the folder and let the Java extension load it. If the editor still does not cooperate, you do not need it for checking: `java tools/Check.java` works from any terminal.

## 3. Make your data

    java tools/MakeVariant.java YOUR_STUDENT_ID data

This creates `data/walls.txt` and `data/slides.txt`. They are yours: made from your ID. Type the ID exactly as it is in Moodle, with all its digits: `12345` and `012345` give different mazes.

## 4. What the numbers in your files mean

A maze is a graph: cells are points, walls are lines between two neighbouring cells. Take a tiny maze of 2 rows and 2 columns. The cells are numbered row by row from 0:

    0 1
    2 3

Its `walls.txt`:

    2 2 4
    0 1 17
    0 2 5
    1 3 9
    2 3 23

- First line: 2 rows, 2 columns, 4 walls inside the maze.
- Every other line is one wall. `0 1 17` is the wall between cell 0 and cell 1, and breaking it costs 17. `0 2 5` is the wall between cell 0 and cell 2 (the one below it), cost 5.
- The line number is the number of the wall: `0 1 17` is wall 1, `0 2 5` is wall 2. "Last broken wall: 3" means line 3 of the file.
- Your own maze is the same, only larger (about 8 to 12 rows and columns), and the costs are all different.

Its `slides.txt`:

    4 4
    0 1 3
    1 0 2
    1 2 4
    2 3 5

- First line: 4 cells, 4 slides.
- `0 1 3` is a one-way slide from cell 0 to cell 1 that takes 3 seconds. `1 0 2` is a different slide, back from 1 to 0. Slides ignore the walls.

In the program the walls are in `Input.Walls` (arrays a, b and cost; the wall on line k of the file is at index k - 1) and the slides are in `Input.Slides` (arrays from, to and time). Open `Input.java`, it is short.

## 5. See your maze

Compile and run once:

    javac -d out src/*.java
    java -cp out Main

Then open `output/maze.html` in a browser by double click. You will see your own maze with every wall standing: nothing is broken yet. You can switch on the cell numbers and the wall costs, and if you move the mouse over a wall, the page shows its line in `walls.txt`. That is the way to connect the numbers in the file with the picture.

The maze comes to life mission by mission:

- After Mission 1 the broken walls disappear, the button "Watch the maze being built" plays your Kruskal wall by wall, and you can walk through your maze with the arrow keys, from the green dot to the blue one.
- After Mission 3 the box "way out" shows the red path, and when you walk out the page tells you how many moves you made and the shortest possible way.
- After Mission 4 the box "zones" colours the cells by zone, and "slides" shows the slides.

The same picture is saved as `output/maze.png`: put it in your repository.

## 6. Where you write code

Every mission has its own file in `src`. Open the file, read the comment on top and replace the `TODO`.

    Mission  File                What you write
    0        DisjointSet.java    find, union, connected, count
    1, 2     Maze.java           breakWalls, cheapestMaze (Kruskal), primCost (Prim)
    3        Escape.java         DFS: the way out, visited cells, dead ends
    4, 5     Zones.java          Kosaraju (zones), flow stages, fastest route
    6        no code             commit after every mission, fill in README.md
    7        no code             run Bench, fill in the timing and complexity tables

You do not change `Main.java`, `Input.java`, `Graphs.java`, `Render.java`, `Generator.java` and `Bench.java`: they are ready (`Graphs` gives you the lists of open neighbours in the maze and the lists of slides for each cell). `Main` calls your methods and prints the 13 lines. Add your own helper classes if you want.

## 7. Check yourself

    java tools/Check.java

It compiles everything, runs the Mission 0 checks and runs your program on three examples in `examples/`. For a wrong line it tells you which mission to look at. Run it after every mission. At the beginning almost everything is wrong, that is normal.

The examples: `tiny` is 2 by 2 and you can solve it on paper. `small` is 5 by 5 with all the values meaningful. `dag` has no cycle in the slides.

## 8. Run on your data

    javac -d out src/*.java
    java -cp out Main

In an IDE run `Main`: without arguments it uses `data/walls.txt` and `data/slides.txt`. The program prints the 13 lines and writes `output/maze.html` and `output/maze.png`. Paste the lines into `README.md`.

For Mission 7 run `java -cp out Bench` and copy the numbers into `README.md`.

## 9. Git

Do not leave this for the last night. The repository shows how you worked.

1. Create an empty public repository on GitHub, for example `maze-escape`.
2. In the project folder:

        git init
        git add .
        git commit -m "Add starter project"
        git branch -M main
        git remote add origin https://github.com/YOUR_NAME/maze-escape.git
        git push -u origin main

3. Work on `main`. After every mission make a commit with a message that says what changed, and push it:

        git add -A
        git commit -m "Add DSU with path compression"
        git push

   Good messages: "Add Kruskal", "Fix wrong cost in Prim", "Add Kosaraju". "update" tells nobody anything. At least one commit per mission, so you will have at least 7 commits. `main` must compile.

Logging in. GitHub does not accept your password in the terminal. The easiest ways: GitHub Desktop, or sign in to GitHub from IntelliJ or VS Code (it opens a browser). With plain git you need a personal access token: GitHub, Settings, Developer settings, Personal access tokens, create one with the `repo` right, and use it instead of the password.

## 10. Submit

Fill in `README.md`: it is your report, with the section "AI use" (the syllabus requires you to say which AI tools you used and for what; write "none" if you used none). Check that `data/walls.txt`, `data/slides.txt` and `output/maze.png` are in the repository. Open the link in a private browser window to see that it works without your login. The deadline is set in Moodle: upload the link there before it.
