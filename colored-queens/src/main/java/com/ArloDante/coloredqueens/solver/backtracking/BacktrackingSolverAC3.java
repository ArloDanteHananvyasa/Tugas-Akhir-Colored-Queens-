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
    if (colorIndex == colors.size()) return true;

    String color = colors.get(colorIndex);
    List<int[]> cells = colorCells.get(color);
    BitSet valid = validCells.get(color);

    if (valid.cardinality() == 0) return false;

    for (int cellIdx = valid.nextSetBit(0); cellIdx >= 0; cellIdx = valid.nextSetBit(cellIdx + 1)) {
        int row = cells.get(cellIdx)[0];
        int col = cells.get(cellIdx)[1];

        solution[colorIndex] = cellIdx;
        occupied[row][col] = true;

        int pruneStartPos = pruneStack.size();

        // Forward-check
        forwardCheck(row, col, colorIndex);

        // AC-3 arc propagation among future colors
        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        for (int i = colorIndex + 1; i < colors.size(); i++) {
            for (int j = colorIndex + 1; j < colors.size(); j++) {
                if (i != j) queue.add(new Pair<>(i, j));
            }
        }
        propagateArcs(queue);

        if (anyColorExhausted(colorIndex)) {
            undoPrunes(pruneStartPos);
            occupied[row][col] = false;
            solution[colorIndex] = -1;
            backtracks++;
            continue;
        }

        steps++;
        if (placeQueens(colorIndex + 1)) return true;

        undoPrunes(pruneStartPos);
        occupied[row][col] = false;
        solution[colorIndex] = -1;
        backtracks++;
    }

    return false;
}

    private void forwardCheck(int row, int col, int placedColorIdx) {
        // Remove attacked cells from all future colors
        for (int colorIdx = placedColorIdx + 1; colorIdx < colors.size(); colorIdx++) {
            String color = colors.get(colorIdx);
            // List<int[]> cells = colorCells.get(color); // Don't need this if we iterate bitset
            BitSet valid = validCells.get(color);
            List<int[]> currentCellList = colorCells.get(color);

            for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                int[] target = currentCellList.get(i);
                int r2 = target[0];
                int c2 = target[1];

                // INLINE CONFLICT CHECK (Replaces attackZone.contains)
                boolean conflict = (row == r2 || col == c2 || 
                                   Math.abs(row - r2) <= 1 && Math.abs(col - c2) <= 1);
                
                if (conflict) {
                    valid.clear(i);
                    colorCellCount[colorIdx]--;
                    pruneStack.push(new PruneAction(color, i));
                }
            }
        }
    }

    private boolean revise(int fromColorIdx, int toColorIdx) {
        boolean revised = false;

        String fromColor = colors.get(fromColorIdx);
        String toColor = colors.get(toColorIdx);

        List<int[]> fromCells = colorCells.get(fromColor);
        List<int[]> toCells = colorCells.get(toColor);

        BitSet fromValid = validCells.get(fromColor);
        BitSet toValid = validCells.get(toColor);

        for (int i = toValid.nextSetBit(0); i >= 0; i = toValid.nextSetBit(i + 1)) {
            int[] toCell = toCells.get(i);
            boolean supported = false;

            // Check support in fromColor: at least one non-conflicting cell
            for (int j = fromValid.nextSetBit(0); j >= 0; j = fromValid.nextSetBit(j + 1)) {
                int[] fromCell = fromCells.get(j);
                if (!conflicts(fromCell, toCell)) {
                    supported = true;
                    break;
                }
            }

            // Also check already placed queens
            if (supported) {
                for (int placedIdx = 0; placedIdx < colors.size(); placedIdx++) {
                    if (solution[placedIdx] != -1 && placedIdx != toColorIdx) {
                        int[] placedCell = colorCells.get(colors.get(placedIdx)).get(solution[placedIdx]);
                        if (conflicts(placedCell, toCell)) {
                            supported = false;
                            break;
                        }
                    }
                }
            }

            // If no support, remove the value from toColor
            if (!supported) {
                toValid.clear(i);
                colorCellCount[toColorIdx]--;
                pruneStack.push(new PruneAction(toColor, i));
                revised = true;
            }
        }

        return revised;
    }

    private void propagateArcs(Queue<Pair<Integer, Integer>> queue) {
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> arc = queue.poll();
            int fromColorIdx = arc.getKey(); // The "Supporter"
            int toColorIdx = arc.getValue(); // The "victim" (being pruned)

            // revise checks if 'to' is supported by 'from'. If 'to' shrinks:
            if (revise(fromColorIdx, toColorIdx)) {
                if (colorCellCount[toColorIdx] == 0) return;

                // We must notify neighbors of 'to' that 'to' has changed.
                // We need to prune the neighbors ('k').
                // So we add (to, k) so that revise(to, k) is called.
                for (int k = 0; k < colors.size(); k++) {
                    // Don't add the one we just came from, and don't add itself
                    if (k != toColorIdx && k != fromColorIdx) { 
                        // CHANGED: Order swapped from (k, to) to (to, k)
                        queue.add(new Pair<>(toColorIdx, k)); 
                    }
                }
            }
        }
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

    private void undoPrunes(int pruneStartPos) {
        while (pruneStack.size() > pruneStartPos) {
            PruneAction action = pruneStack.pop();
            String color = action.color;
            int cellIdx = action.cellIndex;
            validCells.get(color).set(cellIdx);
            colorCellCount[colors.indexOf(color)]++;
        }
    }

    // --- Check if any future color domain is empty ---
    private boolean anyColorExhausted(int currentColorIndex) {
        for (int i = currentColorIndex + 1; i < colors.size(); i++) {
            if (colorCellCount[i] == 0) return true;
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