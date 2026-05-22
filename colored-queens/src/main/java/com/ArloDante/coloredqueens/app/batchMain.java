// package com.ArloDante.coloredqueens.app;

// import com.ArloDante.coloredqueens.objects.Board;
// import com.ArloDante.coloredqueens.objects.Cell;
// import com.ArloDante.coloredqueens.solver.PSO.PSOSolverTest;
// import com.ArloDante.coloredqueens.solver.backtracking.BacktrackingSolverTest;
// import com.ArloDante.coloredqueens.util.BoardImporter;

// import java.io.*;
// import java.util.*;
// import java.util.concurrent.*;

// public class Main {

//     static int[] sizes = {7, 8, 9, 10, 11, 12};
//     static int[] largeSizes = {20, 30};

//     public static void main(String[] args) throws Exception {

//         File paramsDir = new File(System.getProperty("user.dir") + "/params");
//         File[] paramFiles = paramsDir.listFiles((dir, name) -> name.endsWith(".txt"));

//         if (paramFiles == null || paramFiles.length == 0) {
//             System.err.println("No parameter files found!");
//             return;
//         }

//         File resultsRoot = new File(System.getProperty("user.dir") + "/results");
//         if (!resultsRoot.exists()) resultsRoot.mkdir();

//         int threads = 6; // your CPU cores
//         ExecutorService executor = Executors.newFixedThreadPool(threads);

//         for (File paramFile : paramFiles) {
//             String paramFileName = paramFile.getName();
//             String paramBaseName = paramFileName.replace(".txt", "");

//             System.out.println("\n=== RUNNING PARAM SET: " + paramBaseName + " ===");

//             File paramDir = new File(resultsRoot, paramBaseName);
//             if (!paramDir.exists()) paramDir.mkdir();

//             PSOParameters params = readParameters(paramFileName);

//             List<Future<BatchResult>> futures = new ArrayList<>();

//             // ===== MULTITHREADED BATCHES =====
//             for (int size : sizes) {
//                 for (int batch = 1; batch <= 5; batch++) {

//                     int finalSize = size;
//                     int finalBatch = batch;

//                     futures.add(executor.submit(() ->
//                             testBoardBatch(finalSize, finalBatch, params, paramDir, paramBaseName)
//                     ));
//                 }
//             }

//             // ===== LARGE SIZES (single thread is fine) =====
//             SummaryStats stats = new SummaryStats();

//             for (Future<BatchResult> f : futures) {
//                 BatchResult result = f.get();
//                 stats.merge(result);
//             }

//             for (int size : largeSizes) {
//                 BatchResult result = testSingleBoard(size, params, paramDir, paramBaseName);
//                 stats.merge(result);
//             }

//             String summaryFile = paramDir.getPath() + "/summary_" + paramBaseName + ".txt";
//             generateSummary(summaryFile, stats);

//             System.out.println("Summary saved to " + summaryFile);
//         }

//         executor.shutdown();
//         System.out.println("\nAll experiments completed!");
//     }

//     // ================= BATCH =================
//     private static BatchResult testBoardBatch(int size, int batch, PSOParameters params,
//                                               File paramDir, String paramBaseName) {

//         String outputFile = paramDir.getPath() +
//                 "/results" + size + "x" + size + "_" + paramBaseName + "_batch" + batch + ".txt";

//         BatchResult result = new BatchResult(size, batch);

//         int start = (batch - 1) * 50 + 1;
//         int end = batch * 50;

//         try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

//             writer.println("======= " + size + "x" + size + " | Batch " + batch + " =======");

//             for (int level = start; level <= end; level++) {
//                 writer.println("Level " + level + ":");

//                 try {
//                     List<Cell> cells = BoardImporter.importBoard(size, level);
//                     Board board = new Board(size, cells);

//                     // ===== BACKTRACKING =====
//                     BacktrackingSolverTest btSolver = new BacktrackingSolverTest(board);
//                     boolean btSolved = btSolver.solve();
//                     List<int[]> btCoords = btSolver.getSolutionCoordinates();
//                     long btTime = btSolver.getExecutionTime();

//                     writer.print("  Backtracking: ");
//                     writer.print(formatCoordinates(btCoords));
//                     writer.print(", " + btTime + "ms, ");
//                     writer.println(btSolved ? "valid" : "no solution");

//                     result.addBT(btTime);

//                     // ===== PSO =====
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

//                     result.addPSO(valid, psoTime);

//                 } catch (Exception e) {
//                     writer.println("  Error: " + e.getMessage());
//                 }

//                 writer.println();
//                 writer.flush();
//             }

//         } catch (IOException e) {
//             System.err.println("Error writing file: " + e.getMessage());
//         }

//         return result;
//     }

//     // ================= SINGLE =================
//     private static BatchResult testSingleBoard(int size, PSOParameters params,
//                                                File paramDir, String paramBaseName) {

//         String outputFile = paramDir.getPath() +
//                 "/results" + size + "x" + size + "_" + paramBaseName + ".txt";

//         BatchResult result = new BatchResult(size, 1);

//         try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

//             writer.println("======= " + size + "x" + size + " =======");

//             List<Cell> cells = BoardImporter.importBoard(size, 1);
//             Board board = new Board(size, cells);

//             PSOSolverTest psoSolver = new PSOSolverTest(board,
//                     params.iterations, params.particles,
//                     params.c1, params.c2, params.neighborhoods,
//                     params.inertia, params.w1, params.w2,
//                     params.maxStagnation);

//             psoSolver.solve();

//             long psoTime = psoSolver.getExecutionTime();
//             boolean valid = psoSolver.isValid();

//             writer.println("PSO: " + psoTime + "ms, " + (valid ? "valid" : "not valid"));

//             result.addPSO(valid, psoTime);

//         } catch (Exception e) {
//             System.err.println(e.getMessage());
//         }

//         return result;
//     }

//     // ================= SUMMARY =================
//     private static void generateSummary(String outputFile, SummaryStats stats) {

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

//                 double meanBatchRate = batches.stream()
//                         .mapToDouble(b -> b.psoTotal > 0 ? (b.psoValid * 1.0 / b.psoTotal) : 0)
//                         .average().orElse(0) * 100;

//                 long meanBatchTime = (long) batches.stream()
//                         .mapToLong(BatchStats::getPSOAvg)
//                         .average().orElse(0);

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

//     // ================= UTILS =================
//     private static String formatCoordinates(List<int[]> coords) {
//         StringBuilder sb = new StringBuilder("{");
//         for (int i = 0; i < coords.size(); i++) {
//             int[] c = coords.get(i);
//             sb.append("[").append(c[0]).append(",").append(c[1]).append("]");
//             if (i < coords.size() - 1) sb.append(", ");
//         }
//         sb.append("}");
//         return sb.toString();
//     }

//     private static PSOParameters readParameters(String filename) {
//         PSOParameters params = new PSOParameters();

//         try (BufferedReader reader = new BufferedReader(
//                 new FileReader(System.getProperty("user.dir") + "/params/" + filename))) {

//             String line;
//             while ((line = reader.readLine()) != null) {
//                 String[] parts = line.split("=");
//                 if (parts.length != 2) continue;

//                 String key = parts[0].trim();
//                 String value = parts[1].trim();

//                 switch (key) {
//                     case "iterations": params.iterations = Integer.parseInt(value); break;
//                     case "particles": params.particles = Integer.parseInt(value); break;
//                     case "neighborhoods": params.neighborhoods = Integer.parseInt(value); break;
//                     case "c1": params.c1 = Double.parseDouble(value); break;
//                     case "c2": params.c2 = Double.parseDouble(value); break;
//                     case "inertia": params.inertia = Double.parseDouble(value); break;
//                     case "w1": params.w1 = Double.parseDouble(value); break;
//                     case "w2": params.w2 = Double.parseDouble(value); break;
//                     case "maxStagnation": params.maxStagnation = Integer.parseInt(value); break;
//                 }
//             }

//         } catch (Exception e) {
//             System.err.println("Error reading parameters file: " + e.getMessage());
//             System.exit(1);
//         }

//         return params;
//     }

//     // ================= DATA =================
//     static class BatchResult {
//         int size, batch;
//         int psoValid = 0, psoTotal = 0;
//         long psoTime = 0;
//         int btTotal = 0;
//         long btTime = 0;

//         BatchResult(int size, int batch) {
//             this.size = size;
//             this.batch = batch;
//         }

//         void addPSO(boolean valid, long time) {
//             psoTotal++;
//             if (valid) psoValid++;
//             psoTime += time;
//         }

//         void addBT(long time) {
//             btTotal++;
//             btTime += time;
//         }
//     }

//     static class SummaryStats {
//         Map<Integer, List<BatchStats>> data = new HashMap<>();

//         void merge(BatchResult r) {
//             data.computeIfAbsent(r.size, k -> new ArrayList<>());
//             List<BatchStats> list = data.get(r.size);

//             while (list.size() < r.batch) list.add(new BatchStats());

//             BatchStats b = list.get(r.batch - 1);

//             b.psoValid += r.psoValid;
//             b.psoTotal += r.psoTotal;
//             b.psoTime += r.psoTime;
//             b.btTotal += r.btTotal;
//             b.btTime += r.btTime;
//         }

//         List<Integer> getSizes() {
//             return new ArrayList<>(data.keySet());
//         }

//         List<BatchStats> getBatches(int size) {
//             return data.get(size);
//         }
//     }

//     static class BatchStats {
//         int psoValid = 0, psoTotal = 0;
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

//     static class PSOParameters {
//         int iterations;
//         int particles;
//         int neighborhoods;
//         double c1, c2, inertia, w1, w2;
//         int maxStagnation;
//     }
// }