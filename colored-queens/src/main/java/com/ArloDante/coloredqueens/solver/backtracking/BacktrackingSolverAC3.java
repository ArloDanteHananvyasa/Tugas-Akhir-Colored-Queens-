package com.ArloDante.coloredqueens.solver.backtracking;

import java.util.*;

import com.ArloDante.coloredqueens.objects.Board;

public class BacktrackingSolverAC3 {

    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;
    private boolean[][] occupied;
    private long steps;
    private long backtracks;
    private long startTime;
    
    // OPTIMIZATION: Use bitsets for O(1) operations
    private Map<String, BitSet> validCells;
    private int[] colorCellCount;
    private Stack<PruneAction> pruneStack;

    // Helper class to track what was pruned
    private static class PruneAction {
        String color;
        int cellIndex;
        
        PruneAction(String color, int cellIndex) {
            this.color = color;
            this.cellIndex = cellIndex;
        }
    }

    // Simple Pair class for queue
    private static class Pair<K, V> {
        private K key;
        private V value;
        
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() { return key; }
        public V getValue() { return value; }
    }

    public BacktrackingSolverAC3(Board board) {
        this.board = board;
        this.size = board.getSize();
        this.colorCells = board.getColorMap();
        this.colors = new ArrayList<>(colorCells.keySet());
        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);
        this.occupied = new boolean[size][size];
        this.steps = 0;
        this.backtracks = 0;
        
        // Initialize bitsets
        this.validCells = new HashMap<>();
        this.colorCellCount = new int[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            int cellCount = colorCells.get(color).size();
            BitSet bs = new BitSet(cellCount);
            bs.set(0, cellCount);
            validCells.put(color, bs);
            colorCellCount[i] = cellCount;
        }
        
        this.pruneStack = new Stack<>();
    }

    public boolean solve() {
        System.out.println("Starting AC-3 solver for " + size + "x" + size + " board with " + colors.size() + " colors.");
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
        List<int[]> cells = colorCells.get(color);
        BitSet valid = validCells.get(color);

        steps++;

        for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
            int row = cells.get(i)[0];
            int col = cells.get(i)[1];

            solution[colorIndex] = i;
            occupied[row][col] = true;

            int pruneStartPos = pruneStack.size();

            // AC-3 propagation
            propagateConstraints(row, col, colorIndex);

            if (anyColorExhausted(colorIndex)) {
                undoPrunes(pruneStartPos);
                occupied[row][col] = false;
                solution[colorIndex] = -1;

                backtracks++;
                continue;
            }

            if (placeQueens(colorIndex + 1)) {
                return true;
            }

            undoPrunes(pruneStartPos);
            occupied[row][col] = false;
            solution[colorIndex] = -1;

            backtracks++;
        }

        return false;
    }

    // Full AC-3 implementation
    private void propagateConstraints(int row, int col, int placedColorIndex) {
        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        
        // Step 1: Initial forward checking - prune values conflicting with placed queen
        Set<Long> attackZone = computeAttackZone(row, col);
        
        for (int colorIdx = placedColorIndex + 1; colorIdx < colors.size(); colorIdx++) {
            String color = colors.get(colorIdx);
            List<int[]> cells = colorCells.get(color);
            BitSet valid = validCells.get(color);
            
            for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                int[] cell = cells.get(i);
                long pos = encodePosition(cell[0], cell[1]);
                
                if (attackZone.contains(pos)) {
                    valid.clear(i);
                    colorCellCount[colorIdx]--;
                    pruneStack.push(new PruneAction(color, i));
                    
                    // Add to queue for AC-3 propagation
                    queue.add(new Pair<>(colorIdx, i));
                }
            }
        }
        
        // Step 2: AC-3 propagation - check if removals cause more removals
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> removed = queue.poll();
            int removedColorIdx = removed.getKey();
            int removedCellIdx = removed.getValue();
            
            // Get the removed cell's attack zone
            int[] removedCell = colorCells.get(colors.get(removedColorIdx)).get(removedCellIdx);
            Set<Long> removedAttackZone = computeAttackZone(removedCell[0], removedCell[1]);
            
            // Check all other future colors
            for (int colorIdx = placedColorIndex + 1; colorIdx < colors.size(); colorIdx++) {
                if (colorIdx == removedColorIdx) continue;
                
                String color = colors.get(colorIdx);
                List<int[]> cells = colorCells.get(color);
                BitSet valid = validCells.get(color);
                
                // Check each valid cell in this color
                for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                    int[] cell = cells.get(i);
                    long pos = encodePosition(cell[0], cell[1]);
                    
                    // If this cell is in the removed cell's attack zone
                    if (removedAttackZone.contains(pos)) {
                        // Check if this cell has lost all support
                        if (!hasAnySupport(cell, colorIdx, removedColorIdx, placedColorIndex)) {
                            valid.clear(i);
                            colorCellCount[colorIdx]--;
                            pruneStack.push(new PruneAction(color, i));
                            queue.add(new Pair<>(colorIdx, i));
                        }
                    }
                }
            }
        }
    }

    // Check if a cell has support from at least one value in another color
    private boolean hasAnySupport(int[] cell, int cellColorIdx, int excludeColorIdx, int placedColorIndex) {
        // A cell has support if there exists at least one compatible value
        // in every other color's domain
        
        for (int otherColorIdx = placedColorIndex + 1; otherColorIdx < colors.size(); otherColorIdx++) {
            if (otherColorIdx == cellColorIdx || otherColorIdx == excludeColorIdx) continue;
            
            String otherColor = colors.get(otherColorIdx);
            BitSet otherValid = validCells.get(otherColor);
            
            if (otherValid.cardinality() == 0) {
                return false; // No values left in this color
            }
            
            // Check if there's at least one value that doesn't conflict
            boolean foundSupport = false;
            List<int[]> otherCells = colorCells.get(otherColor);
            
            for (int j = otherValid.nextSetBit(0); j >= 0; j = otherValid.nextSetBit(j + 1)) {
                int[] otherCell = otherCells.get(j);
                
                if (!conflicts(cell, otherCell)) {
                    foundSupport = true;
                    break;
                }
            }
            
            if (!foundSupport) {
                return false; // No support from this color
            }
        }
        
        return true; // Has support from all colors
    }

    // Check if two cells conflict (same row/col or adjacent)
    private boolean conflicts(int[] cell1, int[] cell2) {
        int r1 = cell1[0], c1 = cell1[1];
        int r2 = cell2[0], c2 = cell2[1];
        
        // Same row or column
        if (r1 == r2 || c1 == c2) return true;
        
        // Adjacent (including diagonal)
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        if (dr <= 1 && dc <= 1) return true;
        
        return false;
    }

    // Compute attack zone for a cell (all conflicting positions)
    private Set<Long> computeAttackZone(int row, int col) {
        Set<Long> attackZone = new HashSet<>();
        
        // Add entire row and column
        for (int c = 0; c < size; c++) {
            attackZone.add(encodePosition(row, c));
        }
        for (int r = 0; r < size; r++) {
            attackZone.add(encodePosition(r, col));
        }
        
        // Add all 8 adjacent positions
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : dirs) {
            int r = row + d[0];
            int c = col + d[1];
            if (r >= 0 && r < size && c >= 0 && c < size) {
                attackZone.add(encodePosition(r, c));
            }
        }
        
        return attackZone;
    }

    private long encodePosition(int row, int col) {
        return ((long) row << 32) | col;
    }

    private void undoPrunes(int pruneStartPos) {
        while (pruneStack.size() > pruneStartPos) {
            PruneAction action = pruneStack.pop();
            int colorIdx = colors.indexOf(action.color);
            validCells.get(action.color).set(action.cellIndex);
            colorCellCount[colorIdx]++;
        }
    }

    private boolean anyColorExhausted(int currentColorIndex) {
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

    public int[] getSolution() {
        return solution.clone();
    }

    public long getSteps() {
        return steps;
    }
    
    public long getBacktracks() {
        return backtracks;
    }
    
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }
}