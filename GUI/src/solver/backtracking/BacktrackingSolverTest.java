package solver.backtracking;

import java.util.*;

import objects.Board;

public class BacktrackingSolverTest {

    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;
    private boolean[][] occupied;
    private long executionTime;
    
    private Map<String, BitSet> validCells;
    private int[] colorCellCount;
    private Stack<PruneAction> pruneStack;

    private static class PruneAction {
        String color;
        int cellIndex;
        
        PruneAction(String color, int cellIndex) {
            this.color = color;
            this.cellIndex = cellIndex;
        }
    }

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

    public BacktrackingSolverTest(Board board) {
        this.board = board;
        this.size = this.board.getSize();
        this.colorCells = board.getColorMap();
        this.colors = new ArrayList<>(colorCells.keySet());
        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);
        this.occupied = new boolean[size][size];
        
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
        long startTime = System.currentTimeMillis();
        boolean result = placeQueens(0);
        long endTime = System.currentTimeMillis();
        this.executionTime = endTime - startTime;
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

            forwardCheck(row, col, colorIndex);

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
                continue;
            }

            if (placeQueens(colorIndex + 1)) return true;

            undoPrunes(pruneStartPos);
            occupied[row][col] = false;
            solution[colorIndex] = -1;
        }

        return false;
    }

    private void forwardCheck(int row, int col, int placedColorIdx) {
        for (int colorIdx = placedColorIdx + 1; colorIdx < colors.size(); colorIdx++) {
            String color = colors.get(colorIdx);
            BitSet valid = validCells.get(color);
            List<int[]> currentCellList = colorCells.get(color);

            for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                int[] target = currentCellList.get(i);
                int r2 = target[0];
                int c2 = target[1];

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

            for (int j = fromValid.nextSetBit(0); j >= 0; j = fromValid.nextSetBit(j + 1)) {
                int[] fromCell = fromCells.get(j);
                if (!conflicts(fromCell, toCell)) {
                    supported = true;
                    break;
                }
            }

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
            int fromColorIdx = arc.getKey();
            int toColorIdx = arc.getValue();

            if (revise(fromColorIdx, toColorIdx)) {
                if (colorCellCount[toColorIdx] == 0) return;

                for (int k = 0; k < colors.size(); k++) {
                    if (k != toColorIdx && k != fromColorIdx) { 
                        queue.add(new Pair<>(toColorIdx, k)); 
                    }
                }
            }
        }
    }

    private boolean conflicts(int[] cell1, int[] cell2) {
        int r1 = cell1[0], c1 = cell1[1];
        int r2 = cell2[0], c2 = cell2[1];
        
        if (r1 == r2 || c1 == c2) return true;
        
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

    private boolean anyColorExhausted(int currentColorIndex) {
        for (int i = currentColorIndex + 1; i < colors.size(); i++) {
            if (colorCellCount[i] == 0) return true;
        }
        return false;
    }

    public List<int[]> getSolutionCoordinates() {
        List<int[]> coords = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            int[] cell = colorCells.get(color).get(solution[i]);
            coords.add(new int[]{cell[0], cell[1]});
        }
        return coords;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isValid() {
        return solution[0] != -1;
    }
}