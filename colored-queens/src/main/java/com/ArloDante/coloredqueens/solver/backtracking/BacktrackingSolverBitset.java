package com.ArloDante.coloredqueens.solver.backtracking;

import com.ArloDante.coloredqueens.objects.Board;
import java.util.*;

public class BacktrackingSolverBitset {
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;
    
    // --- HYBRID STATE TRACKING ---
    // 1. BitSets for instant Row/Col checks (O(1))
    private BitSet rows;
    private BitSet cols; 
    
    // 2. 2D Array for checking "Touching Neighbors" (O(1))
    private boolean[][] occupied; 
    
    private long steps;
    private long backtracks;
    private long startTime;

    public BacktrackingSolverBitset(Board board) {
        this.board = board;
        this.size = board.getSize();
        this.colorCells = board.getColorMap();
        this.colors = new ArrayList<>(colorCells.keySet());
        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);
        
        this.occupied = new boolean[size][size];
        
        // Initialize BitSets
        this.rows = new BitSet(size);
        this.cols = new BitSet(size);
        
        this.steps = 0;
        this.backtracks = 0;
    }

    public boolean solve() {
        System.out.println("Starting backtracking solver optimized with bitsets for " + size + "x" + size + " board with " + colors.size() + " colors.");
        startTime = System.currentTimeMillis();
        boolean result = placeQueens(0);
        long endTime = System.currentTimeMillis();

        System.out.println("\nSolver stats:");
        System.out.println("Steps: " + steps);
        System.out.println("Backtracks: " + backtracks);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Solution found: " + result);

        return result;
    }

    private boolean placeQueens(int colorIndex) {
        // Base Case: All colors have a queen
        if (colorIndex == colors.size()) {
            return true;
        }

        String color = colors.get(colorIndex);
        List<int[]> cells = colorCells.get(color);

        steps++;

        // Try every cell in this color region
        for (int i = 0; i < cells.size(); i++) {
            int row = cells.get(i)[0];
            int col = cells.get(i)[1];

            if (isValid(row, col)) {
                // DO: Place queen
                solution[colorIndex] = i;
                occupied[row][col] = true;
                rows.set(row); 
                cols.set(col); 

                // RECURSE
                if (placeQueens(colorIndex + 1)) {
                    return true;
                }

                // UNDO (Backtrack)
                backtracks++;
                solution[colorIndex] = -1;
                occupied[row][col] = false;
                rows.clear(row);
                cols.clear(col);
            }
        }

        return false;
    }

    // O(1) Check
    private boolean isValid(int row, int col) {
        // 1. Check Row and Column (BitSet Speedup)
        if (rows.get(row) || cols.get(col)) {
            return false;
        }

        // 2. Check 8 Neighbors (King's Move)
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];
            
            // Boundary check + Occupancy check
            if (r >= 0 && r < size && c >= 0 && c < size && occupied[r][c]) {
                return false;
            }
        }

        return true;
    }

    // --- Printing Logic Preserved ---
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
    
    public long getSteps() { return steps; }
    public long getBacktracks() { return backtracks; }
    public long getExecutionTime() { return System.currentTimeMillis() - startTime; }
}