package com.ArloDante.coloredqueens.solver.backtracking;

import com.ArloDante.coloredqueens.objects.Board;

import java.math.BigInteger;
import java.util.*;

public class BacktrackingSolverAC3 {
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;       // solution[colorIndex] = index in color's cell list
    private boolean[][] occupied; // tracks queen positions
    private BigInteger steps;
    private BigInteger backtracks;
    private long startTime;
    private Map<String, Set<int[]>> invalidCells;

    // Flags for showing extra details
    private boolean showSteps = false;
    private boolean showBoard = false;
    private boolean showConflicts = false;
    private boolean showBacktracks = false;

    public BacktrackingSolverAC3(Board board) {
        this.board = board;
        this.size = board.getSize();
        this.colorCells = board.getColorMap();
        this.colors = new ArrayList<>(colorCells.keySet());
        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);
        this.occupied = new boolean[size][size];
        this.steps = BigInteger.ZERO;
        this.backtracks = BigInteger.ZERO;

        this.invalidCells = new HashMap<>();
        for (String color : colors) {
            invalidCells.put(color, new HashSet<>());
        }
    }

    //memilih logging yang ingin ditampilkan
    public void setDetailOptions(boolean steps, boolean board, boolean conflicts, boolean backtrack) {
        showSteps = steps;
        showBoard = board;
        showConflicts = conflicts;
        showBacktracks = backtrack;
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

        String color = colors.get(colorIndex);
        String symbol = board.getSymbolForColor(color);
        List<int[]> cells = colorCells.get(color);

        if (showSteps) {
            steps = steps.add(BigInteger.ONE);
            System.out.println("Step " + steps + ": menempatkan menteri untuk warna " + symbol);
        } else {
            steps = steps.add(BigInteger.ONE);
        }

        //mencoba setiap sel pada warna ini
        for (int i = 0; i < cells.size(); i++) {
            int row = cells.get(i)[0];
            int col = cells.get(i)[1];

            //jika posisi sekarang ada di invalidCells, lewati sel ini
            boolean skip = false;
            for (int[] inv : invalidCells.get(color)) {
                if (inv[0] == row && inv[1] == col) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;

            //jika posisi valid
            if (isValid(row, col)) {
                solution[colorIndex] = i;  
                occupied[row][col] = true; 

                if (showBoard) {
                    printBoard(); 
                }

                //AC-3 propagation: tandai sel baru yang menjadi tidak valid
                Map<String, Set<int[]>> oldInvalids = cloneInvalidCells(); //simpan snapshot untuk backtracking
                propagateConstraints(row, col, color); //perbarui invalidCells

                //pengecekan kegagalan awal: jika ada warna yang semua selnya invalid, backtrack
                if (anyColorExhausted()) {
                    restoreInvalidCells(oldInvalids); //kembalikan kondisi invalidCells sebelumnya

                    occupied[row][col] = false;
                    solution[colorIndex] = -1;

                    if (showBacktracks) {
                        System.out.println("Backtracking (prune awal) dari [" + row + "," + col + "]");
                    }

                    backtracks = backtracks.add(BigInteger.ONE);
                    continue; //lanjut ke sel berikutnya
                }

                //rekursi ke warna berikutnya
                if (placeQueens(colorIndex + 1)) {
                    return true; //solusi ditemukan di cabang ini
                }

                //backtrack: hapus menteri dan kembalikan kondisi invalidCells
                restoreInvalidCells(oldInvalids);
                occupied[row][col] = false;
                solution[colorIndex] = -1;

                if (showBacktracks) {
                    System.out.println("Backtracking dari [" + row + "," + col + "]");
                }

                backtracks = backtracks.add(BigInteger.ONE);

            } else if (showConflicts) {
                //tampilkan alasan sel tidak valid jika opsi konflik aktif
                System.out.println("Tidak bisa ditempatkan di [" + row + "," + col + "] " + conflictReason(row, col));
            }
        }

        return false; //tidak ada sel valid untuk warna ini di cabang ini
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

    //logging
    private String conflictReason(int row, int col) {
        List<String> reasons = new ArrayList<>();
        if (occupied[row][col]) {
            reasons.add("already occupied");
        }

        for (int i = 0; i < size; i++) {
            if (occupied[row][i]) {
                reasons.add("row conflict");
                break;
            }
            if (occupied[i][col]) {
                reasons.add("column conflict");
                break;
            }
        }

        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];
            if (r >= 0 && r < size && c >= 0 && c < size && occupied[r][c]) {
                reasons.add("adjacent to queen");
                break;
            }
        }

        return "(" + String.join(", ", reasons) + ")";
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

    public BigInteger getSteps() {
        return steps;
    }
    public BigInteger getBacktracks() {
        return backtracks;
    }

    // Mengkopi map invalidCells untuk keperluan backtracking
    private Map<String, Set<int[]>> cloneInvalidCells() {
        Map<String, Set<int[]>> clone = new HashMap<>();
        for (String c : colors) {
            clone.put(c, new HashSet<>(invalidCells.get(c))); // Salin set sel yang tidak valid untuk tiap warna
        }
        return clone;
    }

    // Mengembalikan map invalidCells saat melakukan backtracking
    private void restoreInvalidCells(Map<String, Set<int[]>> oldInvalids) {
        invalidCells.clear(); // Kosongkan map saat ini
        for (String c : colors) {
            invalidCells.put(c, oldInvalids.get(c)); // Kembalikan set sel yang tidak valid dari versi lama
        }
    }

    // Menyebarkan batasan seperti AC-3 setelah menempatkan menteri
    private void propagateConstraints(int row, int col, String placedColor) {
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}}; // 8 arah sekeliling
        for (String color : colors) {
            if (color.equals(placedColor)) continue; // Lewati warna yang baru saja ditempatkan
            for (int[] cell : colorCells.get(color)) {
                if (occupied[cell[0]][cell[1]]) continue; // Lewati sel yang sudah ditempati

                // Tambahkan sel ke invalid jika ada konflik di baris, kolom, atau sel bersebelahan
                if (cell[0] == row || cell[1] == col) {
                    invalidCells.get(color).add(cell);
                }
                for (int[] d : dirs) {
                    int r = row + d[0], c = col + d[1];
                    if (cell[0] == r && cell[1] == c) {
                        invalidCells.get(color).add(cell); // Sel bersebelahan juga menjadi tidak valid
                        break;
                    }
                }
            }
        }
    }

    // Mengecek apakah ada warna yang semua selnya sudah tidak valid
    private boolean anyColorExhausted() {
        for (String color : colors) {
            // Jika warna ini belum ditempatkan dan semua selnya invalid, maka gagal
            if (solution[colors.indexOf(color)] == -1 && invalidCells.get(color).size() == colorCells.get(color).size()) {
                return true;
            }
        }
        return false; // Semua warna masih memiliki sel yang valid
    }

}
