package com.ArloDante.coloredqueens.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ArloDante.coloredqueens.objects.Cell;

//Membaca json file untuk mengambil papan hasil scraping
public class BoardImporter {

    private static final String BOARD_FOLDER = "boards";

    public static List<Cell> importBoard(int size, int level) throws IOException {
        String filename = String.format("%s/%dx%d_level%d.json", BOARD_FOLDER, size, size, level);
        File file = new File(filename);

        if (!file.exists()) {
            throw new IOException("Board file not found: " + filename);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(file);
        List<Cell> board = new ArrayList<>();

        for (JsonNode node : root) {
            int row = node.get("row").asInt();
            int col = node.get("col").asInt();
            String colorStr = node.get("color").asText();
            int[] rgb = parseRgba(colorStr);

            board.add(new Cell(row, col, rgb[0], rgb[1], rgb[2]));
        }

        return board;
    }
    
    private static int[] parseRgba(String rgbaString) {
        rgbaString = rgbaString.replace("rgba(", "").replace(")", "");
        String[] parts = rgbaString.split(",");
        int r = Integer.parseInt(parts[0].trim());
        int g = Integer.parseInt(parts[1].trim());
        int b = Integer.parseInt(parts[2].trim());
        return new int[]{r, g, b};
    }

    public static void printBoard(List<Cell> board, int size) {
        String[][] display = new String[size][size];
        for (Cell c : board) {
            display[c.getRow()][c.getCol()] = String.format("(%d,%d,%d)", c.getR(), c.getG(), c.getB());
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print((display[r][c] != null ? display[r][c] : " . ") + "\t");
            }
            System.out.println();
        }
    }
}
