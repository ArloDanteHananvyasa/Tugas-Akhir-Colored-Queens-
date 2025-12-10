package com.ArloDante.coloredqueens.solver.backtracking;

import com.ArloDante.coloredqueens.objects.Board;

import java.util.*;

public class BacktrackingSolver {
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;       //solution[colorIndex] = index in color's cell list
    private boolean[][] occupied; //tracks queen positions
    private long steps;
    private long backtracks;
    private long startTime;

    public BacktrackingSolver(Board board) {
        this.board = board;
        this.size = board.getSize();
        this.colorCells = board.getColorMap();
        this.colors = new ArrayList<>(colorCells.keySet());
        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);
        this.occupied = new boolean[size][size];
        this.steps = 0;
        this.backtracks = 0;
    }

    //memanggil fungsi rekursif
    public boolean solve() {
        System.out.println("Starting solver for " + size + "x" + size + " board with " + colors.size() + " colors.");
        startTime = System.currentTimeMillis();
        boolean result = placeQueens(0); //hasil fungsi rekursif disimpan di sini
        long endTime = System.currentTimeMillis();

        System.out.println("\nSolver stats:");
        System.out.println("Steps: " + steps);
        System.out.println("Backtracks: " + backtracks);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Solution found: " + result);

        return result;
    }

    //fungsi backtracking utama
    private boolean placeQueens(int colorIndex) {
        if (colorIndex == colors.size()) {
            return true; //base case
        }

        //berbeda dengan N-Queens, domain 1 queen merupakan daerah warna itu sendiri, bukan satu kolom/baris agar tidak perlu mengecek "color constraint" di setiap step
        String color = colors.get(colorIndex);
        List<int[]> cells = colorCells.get(color);

        steps++;


        //mencoba setiap cell yang berwarna sama
        for (int i = 0; i < cells.size(); i++) {
            int row = cells.get(i)[0];
            int col = cells.get(i)[1];


            //jika posisi valid
            if (isValid(row, col)) {
                solution[colorIndex] = i;
                occupied[row][col] = true;


                //lanjutkan untuk warna sebelumnya
                if (placeQueens(colorIndex + 1)) {
                    return true;
                }

                backtracks++;
                solution[colorIndex] = -1;
                occupied[row][col] = false;
            }
        }

        return false;
    }

    private boolean isValid(int row, int col) {
        //mengecek apakah cell tersebut sudah ada menteri atau belum
        if (occupied[row][col]) {
            return false;
        }

        //mengecek semua cell horizontal dan vertikal
        for (int i = 0; i < size; i++) {
            if (occupied[row][i] || occupied[i][col]) {
                return false;
            }
        }

        //mengecek apakah ada bidak yang bersebelahan (8 mata angin)
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];
            if (r >= 0 && r < size && c >= 0 && c < size && occupied[r][c]) {
                return false;
            }
        }

        return true;
    }

    //print hasil akhir papan dengan Q menandakan Bidak menteri
    private void printBoard() {
        System.out.println("Board state:");
        String[][] grid = new String[size][size];

        for (Map.Entry<String, List<int[]>> entry : colorCells.entrySet()) {
            String symbol = board.getSymbolForColor(entry.getKey());
            for (int[] cell : entry.getValue()) {
                grid[cell[0]][cell[1]] = symbol;
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (occupied[r][c]) {
                    grid[r][c] = "Q";
                }
            }
        }

        System.out.print("   ");
        for (int c = 0; c < size; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.print(r + "  ");
            for (int c = 0; c < size; c++) {
                System.out.print((grid[r][c] != null ? grid[r][c] : ".") + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    //Print posisi akhir semua bidak menteri dalam bentuk per baris
    public void printSolution() {
        if (solution[0] == -1) {
            System.out.println("No solution found!");
            return;
        }

        System.out.println("Final solution:");
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            String symbol = board.getSymbolForColor(color);
            int[] cell = colorCells.get(color).get(solution[i]);
            System.out.println("Color " + symbol + " at [" + cell[0] + "," + cell[1] + "]");
        }

        printBoard();
    }

    public long getSteps() {
        return steps;
    }
    public long getBacktracks() {
        return backtracks;
    }
}


// package com.ArloDante.coloredqueens.solver.backtracking;

// import com.ArloDante.coloredqueens.objects.Board;

// import java.util.*;

// public class BacktrackingSolver {
//     private final Board board;
//     private final int size;
//     private final Map<String, List<int[]>> colorCells;
//     private final List<String> colors;
//     private final int[] solution;       // solution[colorIndex] = index in color's cell list

//     private final boolean[][] occupied; // tracks queen positions (for printing & direct checks)
//     private final boolean[] rowUsed;    // true if row has a queen
//     private final boolean[] colUsed;    // true if column has a queen
//     private final int[][] adjacentCount; // reference count of adjacency constraints

//     private final int[][] dirs = {
//             {-1, -1}, {-1, 0}, {-1, 1},
//             { 0, -1},          { 0, 1},
//             { 1, -1}, { 1, 0}, { 1, 1}
//     };

//     private long steps;
//     private long backtracks;
//     private long startTime;

//     // Flags for showing extra details
//     private boolean showSteps = false;
//     private boolean showBoard = false;
//     private boolean showConflicts = false;
//     private boolean showBacktracks = false;

//     public BacktrackingSolver(Board board) {
//         this.board = board;
//         this.size = board.getSize();
//         this.colorCells = board.getColorMap();
//         this.colors = new ArrayList<>(colorCells.keySet());
//         this.solution = new int[colors.size()];
//         Arrays.fill(solution, -1);

//         this.occupied = new boolean[size][size];
//         this.rowUsed = new boolean[size];
//         this.colUsed = new boolean[size];
//         this.adjacentCount = new int[size][size];

//         this.steps = 0;
//         this.backtracks = 0;
//     }

//     // memilih logging yang ingin ditampilkan
//     public void setDetailOptions(boolean steps, boolean board, boolean conflicts, boolean backtrack) {
//         showSteps = steps;
//         showBoard = board;
//         showConflicts = conflicts;
//         showBacktracks = backtrack;
//     }

//     // memanggil fungsi rekursif
//     public boolean solve() {
//         System.out.println("Starting solver for " + size + "x" + size + " board with " + colors.size() + " colors.");
//         startTime = System.currentTimeMillis();
//         boolean result = placeQueens(0);
//         long endTime = System.currentTimeMillis();

//         System.out.println("\nSolver stats:");
//         System.out.println("Steps: " + steps);
//         System.out.println("Backtracks: " + backtracks);
//         System.out.println("Time: " + (endTime - startTime) + " ms");
//         System.out.println("Solution found: " + result);

//         return result;
//     }

//     // fungsi backtracking utama
//     private boolean placeQueens(int colorIndex) {
//         if (colorIndex == colors.size()) return true; // semua warna terpasang

//         String color = colors.get(colorIndex);
//         String symbol = board.getSymbolForColor(color);
//         List<int[]> cells = colorCells.get(color);

//         // counting step
//         steps++;
//         if (showSteps) {
//             System.out.println("Step " + steps + ": placing queen for color " + symbol);
//         }

//         for (int i = 0; i < cells.size(); i++) {
//             int row = cells.get(i)[0];
//             int col = cells.get(i)[1];

//             if (showSteps) {
//                 System.out.println("Trying [" + row + "," + col + "] for color " + symbol);
//             }

//             if (isValid(row, col)) {
//                 // place queen (updates all helpers)
//                 placeQueen(row, col);
//                 solution[colorIndex] = i;

//                 if (showBoard) printBoard();

//                 if (placeQueens(colorIndex + 1)) {
//                     return true;
//                 }

//                 // backtrack
//                 if (showBacktracks) {
//                     System.out.println("Backtracking from [" + row + "," + col + "] for color " + symbol);
//                 }
//                 backtracks++;
//                 solution[colorIndex] = -1;
//                 removeQueen(row, col);
//             } else if (showConflicts) {
//                 System.out.println("Cannot place at [" + row + "," + col + "] " + conflictReason(row, col));
//             }
//         }

//         return false;
//     }

//     // O(1) validity check
//     private boolean isValid(int row, int col) {
//         // 1) cell must not already be occupied
//         if (occupied[row][col]) return false;

//         // 2) row and column must be unused
//         if (rowUsed[row] || colUsed[col]) return false;

//         // 3) adjacency must be zero (no adjacent queens)
//         if (adjacentCount[row][col] > 0) return false;

//         return true;
//     }

//     // place queen and update helper structures (O(1), up to 8 neighbor updates)
//     private void placeQueen(int row, int col) {
//         occupied[row][col] = true;
//         rowUsed[row] = true;
//         colUsed[col] = true;

//         // increment adjacency counts on neighbors
//         for (int[] d : dirs) {
//             int r2 = row + d[0];
//             int c2 = col + d[1];
//             if (r2 >= 0 && r2 < size && c2 >= 0 && c2 < size) {
//                 adjacentCount[r2][c2] += 1;
//             }
//         }
//     }

//     // remove queen and undo updates (O(1), up to 8 neighbor updates)
//     private void removeQueen(int row, int col) {
//         occupied[row][col] = false;
//         rowUsed[row] = false;
//         colUsed[col] = false;

//         // decrement adjacency counts on neighbors
//         for (int[] d : dirs) {
//             int r2 = row + d[0];
//             int c2 = col + d[1];
//             if (r2 >= 0 && r2 < size && c2 >= 0 && c2 < size) {
//                 adjacentCount[r2][c2] -= 1;
//                 // safety: ensure non-negative (shouldn't go below 0 if logic correct)
//                 if (adjacentCount[r2][c2] < 0) adjacentCount[r2][c2] = 0;
//             }
//         }
//     }

//     // logging reason konflik menggunakan helper O(1)
//     private String conflictReason(int row, int col) {
//         List<String> reasons = new ArrayList<>();
//         if (occupied[row][col]) reasons.add("already occupied");
//         if (rowUsed[row]) reasons.add("row conflict");
//         if (colUsed[col]) reasons.add("column conflict");
//         if (adjacentCount[row][col] > 0) reasons.add("adjacent to queen");
//         if (reasons.isEmpty()) reasons.add("unknown reason");
//         return "(" + String.join(", ", reasons) + ")";
//     }

//     // print hasil akhir papan dengan Q menandakan Bidak menteri
//     private void printBoard() {
//         System.out.println("Board state:");
//         String[][] grid = new String[size][size];

//         // fill with color symbol (non-queen)
//         for (Map.Entry<String, List<int[]>> entry : colorCells.entrySet()) {
//             String symbol = board.getSymbolForColor(entry.getKey());
//             for (int[] cell : entry.getValue()) {
//                 grid[cell[0]][cell[1]] = symbol;
//             }
//         }

//         // override queen positions
//         for (int r = 0; r < size; r++) {
//             for (int c = 0; c < size; c++) {
//                 if (occupied[r][c]) grid[r][c] = "Q";
//             }
//         }

//         System.out.print("   ");
//         for (int c = 0; c < size; c++) {
//             System.out.print(c + " ");
//         }
//         System.out.println();

//         for (int r = 0; r < size; r++) {
//             System.out.print(r + "  ");
//             for (int c = 0; c < size; c++) {
//                 System.out.print((grid[r][c] != null ? grid[r][c] : ".") + " ");
//             }
//             System.out.println();
//         }
//         System.out.println();
//     }

//     // Print posisi akhir semua bidak menteri dalam bentuk per baris
//     public void printSolution() {
//         if (solution.length == 0 || solution[0] == -1) {
//             System.out.println("No solution found!");
//             return;
//         }

//         System.out.println("Final solution:");
//         for (int i = 0; i < colors.size(); i++) {
//             String color = colors.get(i);
//             String symbol = board.getSymbolForColor(color);
//             int[] cell = colorCells.get(color).get(solution[i]);
//             System.out.println("Color " + symbol + " at [" + cell[0] + "," + cell[1] + "]");
//         }

//         printBoard();
//     }

//     public long getSteps() {
//         return steps;
//     }
//     public long getBacktracks() {
//         return backtracks;
//     }
// }
