package com.ArloDante.coloredqueens.app;

import com.ArloDante.coloredqueens.objects.Board;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolver;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverAC3;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverBitset;
import com.ArloDante.coloredqueens.util.BoardImporter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        int size = 11;
        int level = 20;

        int solverChoice = 2;

        System.out.println("Loading board (size=" + size + ", level=" + level + ")...");

        try {
            List<Cell> cells = BoardImporter.importBoard(size, level);
            Board board = new Board(size, cells);

            System.out.println("\nBoard summary:");
            board.printColorSummary();

            System.out.println("\nColor layout:");
            board.printSymbolBoard();

            switch (solverChoice) {
                case 1:
                    System.out.println("Solving using pure backtracking");
                    callBacktracking(board);
                    break;

                case 2:
                    System.out.println("Solving using backtracking optimized with bitsets");
                    callBacktrackingBitset(board);
                    break;
                    
                case 3:
                    System.out.println("Solving using backtracking and AC3");
                    callBacktrackingAC3(board);
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            System.out.println("Error: ");
            System.err.println(e.toString());
        }
    }

    public static void callBacktrackingAC3(Board board) {
        BacktrackingSolverAC3 solver = new BacktrackingSolverAC3(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }

    public static void callBacktrackingBitset(Board board) {
        BacktrackingSolverBitset solver = new BacktrackingSolverBitset(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }
    
    public static void callBacktracking(Board board) {
        BacktrackingSolver solver = new BacktrackingSolver(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }
}
