// package com.ArloDante.coloredqueens.app;

// import com.ArloDante.coloredqueens.objects.Board;
// import com.ArloDante.coloredqueens.objects.Cell;
// import com.ArloDante.coloredqueens.solver.PSO.PSOSolverTest;
// import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverTest;
// import com.ArloDante.coloredqueens.util.BoardImporter;

// import java.io.*;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// public class Main {

//     static int[] sizes = {7, 8, 9, 10, 11, 12};
//     static int[] largeSizes = {20, 30};
//     public static void main(String[] args) {
//         File paramsDir = new File(System.getProperty("user.dir") + "/params");
//         File[] paramFiles = paramsDir.listFiles((dir, name) -> name.endsWith(".txt"));

//         if (paramFiles == null || paramFiles.length == 0) {
//             System.err.println("No parameter files found!");
//             return;
//         }

//         File resultsRoot = new File(System.getProperty("user.dir") + "/results");
//         if (!resultsRoot.exists()) resultsRoot.mkdir();

//         for (File paramFile : paramFiles) {
//             String paramFileName = paramFile.getName();
//             String paramBaseName = paramFileName.replace(".txt", "");

//             System.out.println("\n=== RUNNING PARAM SET: " + paramBaseName + " ===");

//             // Create subfolder
//             File paramDir = new File(resultsRoot, paramBaseName);
//             if (!paramDir.exists()) paramDir.mkdir();

//             PSOParameters params = readParameters(paramFileName);
//             SummaryStats stats = new SummaryStats();
        

//             // Batched sizes
//             for (int size : sizes) {
//                 for (int batch = 1; batch <= 5; batch++) {
//                     String outputFile = paramDir.getPath() +
//                             "/results" + size + "x" + size + "_" + paramBaseName + "_batch" + batch + ".txt";

//                     testBoardBatch(size, batch, params, outputFile, stats);
//                 }
//             }

//             // Large sizes
//             for (int size : largeSizes) {
//                 String outputFile = paramDir.getPath() +
//                         "/results" + size + "x" + size + "_" + paramBaseName + ".txt";

//                 testSingleBoard(size, params, outputFile, stats);
//             }

//             String summaryFile = paramDir.getPath() + "/summary_" + paramBaseName + ".txt";
//             generateSummary(summaryFile, params, stats);

//             System.out.println("Summary saved to " + summaryFile);
//         }

//         System.out.println("\nAll experiments completed!");
//     }

//     private static String formatCoordinates(List<int[]> coords) {
//         StringBuilder sb = new StringBuilder("{");
//         for (int i = 0; i < coords.size(); i++) {
//             int[] coord = coords.get(i);
//             sb.append("[").append(coord[0]).append(",").append(coord[1]).append("]");
//             if (i < coords.size() - 1) {
//                 sb.append(", ");
//             }
//         }
//         sb.append("}");
//         return sb.toString();
//     }

//     // ================= BATCH TEST =================
//     private static void testBoardBatch(int size, int batch, PSOParameters params,
//                                        String outputFile, SummaryStats stats) {

//         int start = (batch - 1) * 50 + 1;
//         int end = batch * 50;

//         try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

//             writer.println("======= " + size + "x" + size + " | Batch " + batch + " =======");

//             for (int level = start; level <= end; level++) {
//                 writer.println("Level " + level + ":");

//                 try {
//                     List<Cell> cells = BoardImporter.importBoard(size, level);
//                     Board board = new Board(size, cells);

//                     // Backtracking
//                     BacktrackingSolverTest btSolver = new BacktrackingSolverTest(board);
//                     boolean btSolved = btSolver.solve();
//                     List<int[]> btCoords = btSolver.getSolutionCoordinates();
//                     long btTime = btSolver.getExecutionTime();

//                     writer.print("  Backtracking: ");
//                     writer.print(formatCoordinates(btCoords));
//                     writer.print(", " + btTime + "ms, ");
//                     writer.println(btSolved ? "valid" : "no solution");

//                     stats.addBTResult(size, batch, btTime);

//                     // PSO
//                     PSOSolverTest psoSolver = new PSOSolverTest(board,
//                             params.iterations, params.particles,
//                             params.c1, params.c2, params.neighborhoods,
//                             params.inertia, params.w1, params.w2,
//                             params.maxStagnation);

//                     psoSolver.solve();

//                     List<int[]> psoCoords = psoSolver.getSolutionCoordinates();
//                     long psoTime = psoSolver.getExecutionTime();
//                     boolean valid = psoSolver.isValid();
//                     double fitness = psoSolver.getFitness();
//                     int adj = psoSolver.getAdjacencyViolations();
//                     int att = psoSolver.getAttackingViolations();

//                     writer.print("  PSO: ");
//                     writer.print(formatCoordinates(psoCoords));
//                     writer.print(", " + psoTime + "ms, ");

//                     if (valid) {
//                         writer.println("valid");
//                     } else {
//                         writer.println("not valid, fitness = " + fitness +
//                                 " (adj = " + adj + ", att = " + att + ")");
//                     }

//                     stats.addPSOResult(size, batch, valid, psoTime);

//                 } catch (Exception e) {
//                     writer.println("  Error: " + e.getMessage());
//                 }

//                 writer.println();
//                 writer.flush();
//             }

//         } catch (IOException e) {
//             System.err.println("Error writing file: " + e.getMessage());
//         }
//     }

//     // ================= SINGLE BOARD =================
//     private static void testSingleBoard(int size, PSOParameters params,
//                                         String outputFile, SummaryStats stats) {

//         try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

//             writer.println("======= " + size + "x" + size + " =======");

//             try {
//                 List<Cell> cells = BoardImporter.importBoard(size, 1);
//                 Board board = new Board(size, cells);

//                 PSOSolverTest psoSolver = new PSOSolverTest(board,
//                         params.iterations, params.particles,
//                         params.c1, params.c2, params.neighborhoods,
//                         params.inertia, params.w1, params.w2,
//                         params.maxStagnation);

//                 psoSolver.solve();

//                 long psoTime = psoSolver.getExecutionTime();
//                 boolean valid = psoSolver.isValid();

//                 writer.println("PSO: " + psoTime + "ms, " + (valid ? "valid" : "not valid"));

//                 stats.addPSOResult(size, 1, valid, psoTime);

//             } catch (Exception e) {
//                 writer.println("Error: " + e.getMessage());
//             }

//         } catch (IOException e) {
//             System.err.println(e.getMessage());
//         }
//     }

//     // ================= SUMMARY =================
//     private static void generateSummary(String outputFile, PSOParameters params, SummaryStats stats) {

//         try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

//             writer.println("=== SUMMARY ===\n");

//             for (int size : stats.getSizes()) {
//                 writer.println(size + "x" + size + ":");

//                 List<BatchStats> batches = stats.getBatches(size);

//                 double totalValid = 0;
//                 double totalRuns = 0;
//                 long totalTime = 0;
//                 long totalBTTime = 0;
//                 int totalBTRuns = 0;

//                 for (int i = 0; i < batches.size(); i++) {
//                     BatchStats b = batches.get(i);

//                     double rate = b.psoTotal > 0 ? (b.psoValid * 100.0 / b.psoTotal) : 0;

//                     writer.printf("  Batch %d: %.2f%%, PSO Avg = %dms, BT Avg = %s%n",
//                             (i + 1),
//                             rate,
//                             b.getPSOAvg(),
//                             b.btTotal > 0 ? b.getBTAvg() + "ms" : "N/A"
//                     );

//                     totalValid += b.psoValid;
//                     totalRuns += b.psoTotal;
//                     totalTime += b.psoTime;

//                     totalBTTime += b.btTime;
//                     totalBTRuns += b.btTotal;
//                 }

//                 // Mean of batch means
//                 double meanBatchRate = batches.stream()
//                         .mapToDouble(b -> b.psoTotal > 0 ? (b.psoValid * 1.0 / b.psoTotal) : 0)
//                         .average().orElse(0) * 100;

//                 long meanBatchTime = (long) batches.stream()
//                         .mapToLong(BatchStats::getPSOAvg)
//                         .average().orElse(0);

//                 // Overall mean
//                 double overallRate = totalRuns > 0 ? (totalValid * 100.0 / totalRuns) : 0;
//                 long overallTime = totalRuns > 0 ? totalTime / (long) totalRuns : 0;
//                 long overallBTTime = totalBTRuns > 0 ? totalBTTime / totalBTRuns : 0;

//                 writer.printf("  Mean (batch avg): %.2f%%, %dms%n", meanBatchRate, meanBatchTime);
//                 writer.printf("  Overall (all runs): %.2f%%, %dms, BT Avg = %s%n%n",
//                         overallRate,
//                         overallTime,
//                         totalBTRuns > 0 ? overallBTTime + "ms" : "N/A"
//                 );
//             }

//         } catch (IOException e) {
//             System.err.println(e.getMessage());
//         }
//     }

//     private static PSOParameters readParameters(String filename) {
//         PSOParameters params = new PSOParameters();
        
//         try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/params/" + filename))) {
//             String line;
//             while ((line = reader.readLine()) != null) {
//                 String[] parts = line.split("=");
//                 if (parts.length != 2) continue;
                
//                 String key = parts[0].trim();
//                 String value = parts[1].trim();

//                 switch (key) {
//                     case "iterations":
//                         params.iterations = Integer.parseInt(value);
//                         break;
//                     case "particles":
//                         params.particles = Integer.parseInt(value);
//                         break;
//                     case "neighborhoods":
//                         params.neighborhoods = Integer.parseInt(value);
//                         break;
//                     case "c1":
//                         params.c1 = Double.parseDouble(value);
//                         break;
//                     case "c2":
//                         params.c2 = Double.parseDouble(value);
//                         break;
//                     case "inertia":
//                         params.inertia = Double.parseDouble(value);
//                         break;
//                     case "w1":
//                         params.w1 = Double.parseDouble(value);
//                         break;
//                     case "w2":
//                         params.w2 = Double.parseDouble(value);
//                         break;
//                     case "maxStagnation":
//                         params.maxStagnation = Integer.parseInt(value);
//                         break;
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("Error reading parameters file: " + e.getMessage());
//             System.exit(1);
//         }
        
//         return params;
//     }
    
//     static class SummaryStats {
//         Map<Integer, List<BatchStats>> data = new HashMap<>();

//         void addPSOResult(int size, int batch, boolean valid, long time) {
//             BatchStats b = getBatch(size, batch);
//             b.psoTotal++;
//             if (valid) b.psoValid++;
//             b.psoTime += time;
//         }

//         void addBTResult(int size, int batch, long time) {
//             BatchStats b = getBatch(size, batch);
//             b.btTotal++;
//             b.btTime += time;
//         }

//         private BatchStats getBatch(int size, int batch) {
//             data.computeIfAbsent(size, k -> new ArrayList<>());
//             List<BatchStats> list = data.get(size);

//             while (list.size() < batch) {
//                 list.add(new BatchStats());
//             }

//             return list.get(batch - 1);
//         }

//         List<Integer> getSizes() {
//             return new ArrayList<>(data.keySet());
//         }

//         List<BatchStats> getBatches(int size) {
//             return data.get(size);
//         }
//     }

//     static class BatchStats {
//         int psoValid = 0;
//         int psoTotal = 0;
//         long psoTime = 0;

//         int btTotal = 0;
//         long btTime = 0;

//         long getPSOAvg() {
//             return psoTotal > 0 ? psoTime / psoTotal : 0;
//         }

//         long getBTAvg() {
//             return btTotal > 0 ? btTime / btTotal : 0;
//         }
//     }
    
//     static class SizeStats {
//         int psoValidCount = 0;
//         int psoTotalCount = 0;
//         long psoTotalTime = 0;
        
//         int btValidCount = 0;
//         int btTotalCount = 0;
//         long btTotalTime = 0;
        
//         void addPSOResult(boolean valid, long time) {
//             if (valid) psoValidCount++;
//             psoTotalCount++;
//             psoTotalTime += time;
//         }
        
//         void addBTResult(boolean valid, long time) {
//             if (valid) btValidCount++;
//             btTotalCount++;
//             btTotalTime += time;
//         }
        
//         long getPSOAvgTime() {
//             return psoTotalCount > 0 ? psoTotalTime / psoTotalCount : 0;
//         }
        
//         long getBTAvgTime() {
//             return btTotalCount > 0 ? btTotalTime / btTotalCount : 0;
//         }
//     }
    
//     static class PSOParameters {
//         int iterations;
//         int particles;
//         int neighborhoods;
//         double c1;
//         double c2;
//         double inertia;
//         double w1;
//         double w2;
//         int maxStagnation;
//     }
// }