package com.ArloDante.coloredqueens.app;

import com.ArloDante.coloredqueens.objects.Board;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.solver.BacktrackingSolver;
import com.ArloDante.coloredqueens.util.BoardImporter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        int size = 7;
        int level = 1;

        System.out.println("Loading board (size=" + size + ", level=" + level + ")...");

        try {
            List<Cell> cells = BoardImporter.importBoard(size, level);
            Board board = new Board(size, cells);

            System.out.println("\nBoard summary:");
            board.printColorSummary();

            System.out.println("\nColor layout:");
            board.printSymbolBoard();

            BacktrackingSolver solver = new BacktrackingSolver(board);

            boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }

        } catch (Exception e) {
            System.out.println("Error: ");
            System.err.println(e.toString());
        }
    }
}
