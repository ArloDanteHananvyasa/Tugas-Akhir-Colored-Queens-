package com.ArloDante.coloredqueens.solver.backtracking;

import com.ArloDante.coloredqueens.objects.Board;

import java.math.BigInteger;
import java.util.*;

public class BacktrackingSolverAC3{
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;
    private boolean[][] occupied;
    private BigInteger steps;
    private BigInteger backtracks;
    private long startTime;
    
    // OPTIMIZATION 1: Use bitsets instead of HashSet for O(1) operations
    private Map<String, BitSet> validCells; // Track which cells are still valid
    private int[] colorCellCount; // Track how many valid cells each color has
    
    // OPTIMIZATION 2: Stack-based undo instead of cloning entire map
    private Stack<PruneAction> pruneStack;
    
    private boolean showSteps = false;
    private boolean showBoard = false;
    private boolean showBacktracks = false;

    // Helper class to track what was pruned (for efficient undo)
    private static class PruneAction {
        String color;
        int cellIndex;
        
        PruneAction(String color, int cellIndex) {
            this.color = color;
            this.cellIndex = cellIndex;
        }
    }

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
        
        // Initialize bitsets - all cells start as valid
        this.validCells = new HashMap<>();
        this.colorCellCount = new int[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            int cellCount = colorCells.get(color).size();
            BitSet bs = new BitSet(cellCount);
            bs.set(0, cellCount); // Set all bits to true (valid)
            validCells.put(color, bs);
            colorCellCount[i] = cellCount;
        }
        
        this.pruneStack = new Stack<>();
    }

    public void setDetailOptions(boolean steps, boolean board, boolean backtrack) {
        showSteps = steps;
        showBoard = board;
        showBacktracks = backtrack;
    }

    public boolean solve() {
        System.out.println("Starting optimized solver for " + size + "x" + size + " board with " + colors.size() + " colors.");
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
        if (colorIndex == colors.size()) {
            return true;
        }

        String color = colors.get(colorIndex);
        String symbol = board.getSymbolForColor(color);
        List<int[]> cells = colorCells.get(color);
        BitSet valid = validCells.get(color);

        if (showSteps) {
            steps = steps.add(BigInteger.ONE);
            System.out.println("Step " + steps + ": placing queen for color " + symbol);
        } else {
            steps = steps.add(BigInteger.ONE);
        }

        // OPTIMIZATION 3: Iterate only over valid cells using BitSet
        for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
            int row = cells.get(i)[0];
            int col = cells.get(i)[1];

            solution[colorIndex] = i;
            occupied[row][col] = true;

            if (showBoard) {
                printBoard();
            }

            // Mark the position where we start pruning (for undo)
            int pruneStartPos = pruneStack.size();

            // Propagate constraints and track changes
            propagateConstraints(row, col, colorIndex);

            // Early pruning check
            if (anyColorExhausted(colorIndex)) {
                // Undo all prunes from this placement
                undoPrunes(pruneStartPos);
                occupied[row][col] = false;
                solution[colorIndex] = -1;

                if (showBacktracks) {
                    System.out.println("Backtracking (prune awal) dari [" + row + "," + col + "]");
                }

                backtracks = backtracks.add(BigInteger.ONE);
                continue;
            }

            // Recurse
            if (placeQueens(colorIndex + 1)) {
                return true;
            }

            // Backtrack: undo prunes
            undoPrunes(pruneStartPos);
            occupied[row][col] = false;
            solution[colorIndex] = -1;

            if (showBacktracks) {
                System.out.println("Backtracking dari [" + row + "," + col + "]");
            }

            backtracks = backtracks.add(BigInteger.ONE);
        }

        return false;
    }

    // OPTIMIZATION 4: Only propagate to colors not yet placed
    private void propagateConstraints(int row, int col, int placedColorIndex) {
        // Pre-compute attack positions for efficiency
        boolean[] attackRow = new boolean[size];
        boolean[] attackCol = new boolean[size];
        boolean[][] attackAdjacent = new boolean[size][size];
        
        attackRow[row] = true;
        attackCol[col] = true;
        
        // Mark all 8 adjacent positions
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (r >= 0 && r < size && c >= 0 && c < size) {
                attackAdjacent[r][c] = true;
            }
        }

        // Only check colors that haven't been placed yet
        for (int colorIdx = placedColorIndex + 1; colorIdx < colors.size(); colorIdx++) {
            String color = colors.get(colorIdx);
            List<int[]> cells = colorCells.get(color);
            BitSet valid = validCells.get(color);

            // Iterate only over currently valid cells
            for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                int[] cell = cells.get(i);
                int r = cell[0], c = cell[1];
                
                // Check if this cell is attacked
                if (attackRow[r] || attackCol[c] || attackAdjacent[r][c]) {
                    valid.clear(i); // Mark as invalid
                    colorCellCount[colorIdx]--;
                    pruneStack.push(new PruneAction(color, i));
                }
            }
        }
    }

    private void undoPrunes(int pruneStartPos) {
        while (pruneStack.size() > pruneStartPos) {
            PruneAction action = pruneStack.pop();
            int colorIdx = colors.indexOf(action.color);
            validCells.get(action.color).set(action.cellIndex); // Restore validity
            colorCellCount[colorIdx]++;
        }
    }

    private boolean anyColorExhausted(int currentColorIndex) {
        // Only check colors that haven't been placed yet
        for (int i = currentColorIndex + 1; i < colors.size(); i++) {
            if (colorCellCount[i] == 0) {
                return true;
            }
        }
        return false;
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
}