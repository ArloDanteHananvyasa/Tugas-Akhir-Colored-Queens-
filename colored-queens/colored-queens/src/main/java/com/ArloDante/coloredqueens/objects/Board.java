package com.ArloDante.coloredqueens.objects;

import java.util.*;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.util.BoardImporter;

public class Board {
    private int size;
    private Map<String, List<int[]>> colorMap = new HashMap<>();
    private Map<String, String> colorSymbolMap = new HashMap<>();

    public Board(int size, List<Cell> cells) {
        this.size = size;
        buildColorMap(cells);
        buildColorSymbols();
    }

    //memetakan setiap posisi warna ke dalam map
    private void buildColorMap(List<Cell> cells) {
        for (Cell cell : cells) {
            String colorKey = String.format("%d,%d,%d", cell.getR(), cell.getG(), cell.getB());
            colorMap.computeIfAbsent(colorKey, k -> new ArrayList<>())
                    .add(new int[]{cell.getRow(), cell.getCol()});
        }
    }

    //menggunakan huruf (A,B,C,D, dst.) untuk memodelkan warna RGB yang unik
    private void buildColorSymbols() {
        char symbol = 'A';
        for (String color : colorMap.keySet()) {
            colorSymbolMap.put(color, String.valueOf(symbol));
            symbol++;
        }
    }

    public int getSize() {
        return size;
    }

    public Map<String, List<int[]>> getColorMap() {
        return colorMap;
    }

    public Map<String, String> getColorSymbolMap() {
        return colorSymbolMap;
    }

    //mengembalikan semua cell yang merupakan suatu warna
    public List<int[]> getCellsByColor(String colorKey) {
        return colorMap.getOrDefault(colorKey, Collections.emptyList());
    }

    //mengembalikan huruf yang merepresentasikan suatu key RGB
    public String getSymbolForColor(String colorKey) {
        return colorSymbolMap.getOrDefault(colorKey, "?");
    }

    //print dalam format huruf
    public void printSymbolBoard() {
        String[][] grid = new String[size][size];
        for (Map.Entry<String, List<int[]>> entry : colorMap.entrySet()) {
            String symbol = getSymbolForColor(entry.getKey());
            for (int[] cell : entry.getValue()) {
                grid[cell[0]][cell[1]] = symbol;
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print((grid[r][c] != null ? grid[r][c] : ".") + " ");
            }
            System.out.println();
        }
    }

    //logging
    public void printColorSummary() {
        System.out.println("Board color distribution:");
        for (String color : colorMap.keySet()) {
            String symbol = getSymbolForColor(color);
            System.out.printf("Color %s (%s) -> %d cells%n",
                    color, symbol, colorMap.get(color).size());
        }
        System.out.println("Board size: " + size);
        System.out.println("Unique colors: " + colorMap.size());
    }
}
