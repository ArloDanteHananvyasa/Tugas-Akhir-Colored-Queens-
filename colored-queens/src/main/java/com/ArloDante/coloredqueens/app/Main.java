package com.ArloDante.coloredqueens.app;

import com.ArloDante.coloredqueens.objects.Board;
import com.ArloDante.coloredqueens.objects.Cell;
import com.ArloDante.coloredqueens.solver.PSO.PSOSolverTest;
import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverTest;
import com.ArloDante.coloredqueens.util.BoardImporter;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Read parameters from parameters.txt
        String parametersFileName = "optimal.txt";
        PSOParameters params = readParameters(parametersFileName);
        
        // Extract base name from parameters file (e.g., "benchmark" from "benchmark.txt")
        String paramBaseName = parametersFileName.replace(".txt", "");
        
        // Create results directory if it doesn't exist
        File resultsDir = new File(System.getProperty("user.dir") + "/results");
        if (!resultsDir.exists()) {
            resultsDir.mkdir();
        }
        
        // Statistics tracking
        SummaryStats stats = new SummaryStats();
        
        // Test boards from 7x7 to 11x11
        for (int size = 7; size <= 11; size++) {
            System.out.println("Testing " + size + "x" + size + " boards...");
            String outputFile = resultsDir.getPath() + "/results" + size + "x" + size + "_" + paramBaseName + ".txt";
            testBoardSize(size, params, outputFile, stats);
            System.out.println("Results saved to " + outputFile);
        }

        System.out.println("Testing " + 20 + "x" + 20 + " boards...");
        String outputFile = resultsDir.getPath() + "/results" + 20 + "x" + 20 + "_" + paramBaseName + ".txt";
        testBoardSize(20, params, outputFile, stats);
        System.out.println("Results saved to " + outputFile);

        System.out.println("Testing " + 30 + "x" + 30 + " boards...");
        outputFile = resultsDir.getPath() + "/results" + 30 + "x" + 30 + "_" + paramBaseName + ".txt";
        testBoardSize(30, params, outputFile, stats);
        System.out.println("Results saved to " + outputFile);
        
        // Generate summary file
        String summaryFile = resultsDir.getPath() + "/summary_" + paramBaseName + ".txt";
        generateSummary(summaryFile, params, stats);
        System.out.println("\nSummary saved to " + summaryFile);
        
        System.out.println("\nAll tests completed!");
    }
    
    private static void testBoardSize(int size, PSOParameters params, String outputFile, SummaryStats stats) {
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
                    if (size != 20 && size != 30) {
                        BacktrackingSolverTest btSolver = new BacktrackingSolverTest(board);
                        boolean btSolved = btSolver.solve();
                        List<int[]> btCoords = btSolver.getSolutionCoordinates();
                        long btTime = btSolver.getExecutionTime();
                        
                        writer.print("  Backtracking: ");
                        writer.print(formatCoordinates(btCoords));
                        writer.print(", " + btTime + "ms, ");
                        writer.println(btSolved ? "valid" : "no solution");
                        
                        // Collect stats
                        stats.addBacktrackingResult(size, btSolved, btTime);
                    }
                    
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
                    
                    // Collect stats
                    stats.addPSOResult(size, psoValid, psoTime);
                    
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
        
        try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/params/" + filename))) {
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
    
    private static void generateSummary(String outputFile, PSOParameters params, SummaryStats stats) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Extract parameter name from file path
            String paramName = outputFile.substring(outputFile.lastIndexOf("_") + 1, outputFile.lastIndexOf("."));
            
            writer.println("======= PARAMETER SET SUMMARY: " + paramName + " =======");
            writer.println("Parameters: iterations=" + params.iterations + ", particles=" + params.particles + 
                          ", neighborhoods=" + params.neighborhoods + ", c1=" + params.c1 + ", c2=" + params.c2 + 
                          ", inertia=" + params.inertia + ", w1=" + params.w1 + ", w2=" + params.w2 + 
                          ", maxStagnation=" + params.maxStagnation);
            writer.println();
            
            writer.println("=== SUCCESS RATES ===");
            writer.println("Board Size | PSO Valid Solutions | Backtracking Valid Solutions");
            writer.println("-----------+--------------------+-----------------------------");
            
            int[] sizes = {7, 8, 9, 10, 11, 20, 30};
            int totalPSOValid = 0;
            int totalPSOTests = 0;
            int totalBTValid = 0;
            int totalBTTests = 0;
            
            for (int size : sizes) {
                SizeStats sizeStats = stats.getStatsForSize(size);
                if (sizeStats == null) continue;
                
                int psoValid = sizeStats.psoValidCount;
                int psoTotal = sizeStats.psoTotalCount;
                double psoPercent = psoTotal > 0 ? (psoValid * 100.0 / psoTotal) : 0;
                
                totalPSOValid += psoValid;
                totalPSOTests += psoTotal;
                
                String btInfo;
                if (size == 20 || size == 30) {
                    btInfo = "N/A";
                } else {
                    int btValid = sizeStats.btValidCount;
                    int btTotal = sizeStats.btTotalCount;
                    double btPercent = btTotal > 0 ? (btValid * 100.0 / btTotal) : 0;
                    btInfo = String.format("%d/%d (%.2f%%)", btValid, btTotal, btPercent);
                    totalBTValid += btValid;
                    totalBTTests += btTotal;
                }
                
                writer.printf("%-10s | %-18s | %s%n", 
                             size + "x" + size, 
                             String.format("%d/%d (%.2f%%)", psoValid, psoTotal, psoPercent),
                             btInfo);
            }
            
            writer.println("-----------+--------------------+-----------------------------");
            writer.printf("%-10s | %-18s | %s%n",
                         "TOTAL",
                         String.format("%d/%d (%.2f%%)", totalPSOValid, totalPSOTests, 
                                      totalPSOTests > 0 ? (totalPSOValid * 100.0 / totalPSOTests) : 0),
                         totalBTTests > 0 ? String.format("%d/%d (%.2f%%)", totalBTValid, totalBTTests,
                                                         (totalBTValid * 100.0 / totalBTTests)) : "N/A");
            writer.println();
            
            writer.println("=== AVERAGE SOLVE TIMES ===");
            writer.println("Board Size | PSO Avg Time | Backtracking Avg Time | Time Ratio (PSO/BT)");
            writer.println("-----------+-------------+----------------------+--------------------");
            
            for (int size : sizes) {
                SizeStats sizeStats = stats.getStatsForSize(size);
                if (sizeStats == null) continue;
                
                long psoAvg = sizeStats.getPSOAvgTime();
                
                String btAvgStr;
                String ratioStr;
                if (size == 20 || size == 30) {
                    btAvgStr = "N/A";
                    ratioStr = "N/A";
                } else {
                    long btAvg = sizeStats.getBTAvgTime();
                    btAvgStr = btAvg + "ms";
                    if (btAvg > 0) {
                        double ratio = (double) psoAvg / btAvg;
                        ratioStr = String.format("%.1fx slower", ratio);
                    } else {
                        ratioStr = "N/A";
                    }
                }
                
                writer.printf("%-10s | %-12s | %-21s | %s%n",
                             size + "x" + size,
                             psoAvg + "ms",
                             btAvgStr,
                             ratioStr);
            }
            writer.println();
            
            writer.println("End of Summary");
            
        } catch (IOException e) {
            System.err.println("Error writing to summary file: " + e.getMessage());
        }
    }
    
    static class SummaryStats {
        private Map<Integer, SizeStats> statsBySize = new HashMap<>();
        
        public void addPSOResult(int size, boolean valid, long time) {
            statsBySize.computeIfAbsent(size, k -> new SizeStats()).addPSOResult(valid, time);
        }
        
        public void addBacktrackingResult(int size, boolean valid, long time) {
            statsBySize.computeIfAbsent(size, k -> new SizeStats()).addBTResult(valid, time);
        }
        
        public SizeStats getStatsForSize(int size) {
            return statsBySize.get(size);
        }
    }
    
    static class SizeStats {
        int psoValidCount = 0;
        int psoTotalCount = 0;
        long psoTotalTime = 0;
        
        int btValidCount = 0;
        int btTotalCount = 0;
        long btTotalTime = 0;
        
        void addPSOResult(boolean valid, long time) {
            if (valid) psoValidCount++;
            psoTotalCount++;
            psoTotalTime += time;
        }
        
        void addBTResult(boolean valid, long time) {
            if (valid) btValidCount++;
            btTotalCount++;
            btTotalTime += time;
        }
        
        long getPSOAvgTime() {
            return psoTotalCount > 0 ? psoTotalTime / psoTotalCount : 0;
        }
        
        long getBTAvgTime() {
            return btTotalCount > 0 ? btTotalTime / btTotalCount : 0;
        }
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