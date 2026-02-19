package com.ArloDante.coloredqueens.app;

import com.ArloDante.coloredqueens.objects.Board;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.solver.PSO.PSOSolverTest;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverTest;
import com.ArloDante.coloredqueens.util.BoardImporter;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Read parameters from parameters.txt
        String parametersFileName = "parameters1.txt";
        PSOParameters params = readParameters(parametersFileName);
        
        // Extract base name from parameters file (e.g., "parameters1" from "parameters1.txt")
        String paramBaseName = parametersFileName.replace(".txt", "");
        
        // Create results directory if it doesn't exist
        File resultsDir = new File(System.getProperty("user.dir") + "/results");
        if (!resultsDir.exists()) {
            resultsDir.mkdir();
        }
        
        // Test boards from 7x7 to 11x11
        for (int size = 7; size <= 11; size++) {
            System.out.println("Testing " + size + "x" + size + " boards...");
            String outputFile = resultsDir.getPath() + "/results" + size + "x" + size + "_" + paramBaseName + ".txt";
            testBoardSize(size, params, outputFile);
            System.out.println("Results saved to " + outputFile);
        }
        
        System.out.println("\nAll tests completed!");
    }
    
    private static void testBoardSize(int size, PSOParameters params, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("======= " + size + "x" + size + " BOARDS =======");
            writer.println("PSO Parameters: iterations=" + params.iterations + ", particles=" + params.particles + 
                          ", neighborhoods=" + params.neighborhoods + ", c1=" + params.c1 + ", c2=" + params.c2 + 
                          ", inertia=" + params.inertia + ", w1=" + params.w1 + ", w2=" + params.w2 + 
                          ", maxStagnation=" + params.maxStagnation);
            writer.println();
            
            for (int level = 1; level <= 30; level++) {
                writer.println("Level " + level + ":");
                
                try {
                    List<Cell> cells = BoardImporter.importBoard(size, level);
                    Board board = new Board(size, cells);
                    
                    // Test Backtracking
                    BacktrackingSolverTest btSolver = new BacktrackingSolverTest(board);
                    boolean btSolved = btSolver.solve();
                    List<int[]> btCoords = btSolver.getSolutionCoordinates();
                    long btTime = btSolver.getExecutionTime();
                    
                    writer.print("  Backtracking: ");
                    writer.print(formatCoordinates(btCoords));
                    writer.print(", " + btTime + "ms, ");
                    writer.println(btSolved ? "valid" : "no solution");
                    
                    // Test PSO
                    PSOSolverTest psoSolver = new PSOSolverTest(board, params.iterations, params.particles, 
                                                                 params.c1, params.c2, params.neighborhoods, 
                                                                 params.inertia, params.w1, params.w2, 
                                                                 params.maxStagnation);
                    psoSolver.solve();
                    List<int[]> psoCoords = psoSolver.getSolutionCoordinates();
                    long psoTime = psoSolver.getExecutionTime();
                    boolean psoValid = psoSolver.isValid();
                    double psoFitness = psoSolver.getFitness();
                    int psoAdj = psoSolver.getAdjacencyViolations();
                    int psoAtt = psoSolver.getAttackingViolations();
                    
                    writer.print("  PSO: ");
                    writer.print(formatCoordinates(psoCoords));
                    writer.print(", " + psoTime + "ms, ");
                    if (psoValid) {
                        writer.println("valid");
                    } else {
                        writer.println("not valid, fitness = " + psoFitness + " (adj = " + psoAdj + ", att = " + psoAtt + ")");
                    }
                    
                    writer.println();
                    
                } catch (Exception e) {
                    writer.println("  Error loading board: " + e.getMessage());
                    writer.println();
                }
                
                writer.flush(); // Flush after each level
            }
            
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }
    
    private static String formatCoordinates(List<int[]> coords) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < coords.size(); i++) {
            int[] coord = coords.get(i);
            sb.append("[").append(coord[0]).append(",").append(coord[1]).append("]");
            if (i < coords.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static PSOParameters readParameters(String filename) {
        PSOParameters params = new PSOParameters();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/" + filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length != 2) continue;
                
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "iterations":
                        params.iterations = Integer.parseInt(value);
                        break;
                    case "particles":
                        params.particles = Integer.parseInt(value);
                        break;
                    case "neighborhoods":
                        params.neighborhoods = Integer.parseInt(value);
                        break;
                    case "c1":
                        params.c1 = Double.parseDouble(value);
                        break;
                    case "c2":
                        params.c2 = Double.parseDouble(value);
                        break;
                    case "inertia":
                        params.inertia = Double.parseDouble(value);
                        break;
                    case "w1":
                        params.w1 = Double.parseDouble(value);
                        break;
                    case "w2":
                        params.w2 = Double.parseDouble(value);
                        break;
                    case "maxStagnation":
                        params.maxStagnation = Integer.parseInt(value);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading parameters file: " + e.getMessage());
            System.exit(1);
        }
        
        return params;
    }
    
    static class PSOParameters {
        int iterations;
        int particles;
        int neighborhoods;
        double c1;
        double c2;
        double inertia;
        double w1;
        double w2;
        int maxStagnation;
    }
}