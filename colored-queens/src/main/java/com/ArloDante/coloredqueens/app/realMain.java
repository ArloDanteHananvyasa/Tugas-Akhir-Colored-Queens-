package com.ArloDante.coloredqueens.app;

import com.ArloDante.coloredqueens.objects.Board;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.solver.PSO.PSOSolver;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolver;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverAC3;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverBitset;
import com.ArloDante.coloredqueens.util.BoardImporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class realMain {
    public static void main(String[] args) {
        int size = 7;
        int level = 1;

        int solverChoice = 3;

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

                case 4:
                    System.out.println("Solving using Discrete PSO");
                    callPSO(board);

                default:
                    break;
            }

        } catch (Exception e) {
            System.out.println("Error: ");
            System.err.println(e.toString());
        }
    }

    private static void callBacktrackingAC3(Board board) {
        BacktrackingSolverAC3 solver = new BacktrackingSolverAC3(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }

    private static void callBacktrackingBitset(Board board) {
        BacktrackingSolverBitset solver = new BacktrackingSolverBitset(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }
    
    private static void callBacktracking(Board board) {
        BacktrackingSolver solver = new BacktrackingSolver(board);

        boolean solved = solver.solve();

            if (solved) {
                solver.printSolution();
            } else {
                System.out.println("No solution found for this board.");
            }
    }

    private static void callPSO(Board board) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/parameters.txt"));

        int iterations = 0;
        int particles = 0;
        int neighborhoods = 0;
        double c1 = 0;
        double c2 = 0;
        double inertia = 0;
        double w1 = 0;
        double w2 = 0;
        int maxStagnation = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=");
            String key = parts[0].trim();
            String value = parts[1].trim();

            switch (key) {
                case "iterations":
                    iterations = Integer.parseInt(value);
                    break;
                case "particles":
                    particles = Integer.parseInt(value);
                    break;
                case "neighborhoods":
                    neighborhoods = Integer.parseInt(value);
                    break;
                case "c1":
                    c1 = Double.parseDouble(value);
                    break;
                case "c2":
                    c2 = Double.parseDouble(value);
                    break;
                case "inertia":
                    inertia = Double.parseDouble(value);
                    break;
                case "w1":
                    w1 = Double.parseDouble(value);
                    break;
                case "w2":
                    w2 = Double.parseDouble(value);
                    break;
                case "maxStagnation":
                    maxStagnation = Integer.parseInt(value);
                break;
            }
        }

        reader.close();

        PSOSolver pso = new PSOSolver(board, iterations, particles, c1, c2, neighborhoods, inertia, w1, w2, maxStagnation);

        pso.solve();
    }
}
