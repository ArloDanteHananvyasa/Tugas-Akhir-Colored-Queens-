package com.ArloDante.coloredqueens.solver.PSO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ArloDante.coloredqueens.objects.Board;

class Particle {
    private int[] candidate;
    private double[] velocity;
    private double fitness;
    private int[] pBest;
    private double pBestFitness;
    
    public Particle(int[] candidate, double[] velocity) {
        this.candidate = candidate;
        this.velocity = velocity;
        this.pBest = candidate.clone();
        this.pBestFitness = Double.MAX_VALUE;
    }

    public int[] getCandidate() {
        return candidate;
    }

    public void setCandidate(int[] candidate) {
        this.candidate = candidate;
    }

    public double[] getVelocity() {
        return velocity;
    }

    public void setVelocity(double[] velocity) {
        this.velocity = velocity;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int[] getPBest() {
        return pBest;
    }

    public void setPBest(int[] pBest) {
        this.pBest = pBest;
    }

    public double getPBestFitness() {
        return pBestFitness;
    }

    public void setPBestFitness(double pBestFitness) {
        this.pBestFitness = pBestFitness;
    }

}

public class PSOSolver {
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;
    private int[] solution;

    private int nIterations;
    private int nParticles;
    private List<Particle> particles;
    
    private int nNeighborhood;
    private List<List<Integer>> neighborhoods;
    private int[] nBests;

    private double c1;
    private double c2;
    private double inertia;

    private double w1;
    private double w2;
    
    private Random random;

    private int maxStagnation;
    
    public PSOSolver(Board board, int nIterations, int nParticles, double c1, double c2, int nNeighborhood, double inertia, double w1, double w2, int maxStagnation) {
        this.board = board;
        this.size = this.board.getSize();
        
        this.colorCells = this.board.getColorMap();
        this.colors = new ArrayList<>(this.colorCells.keySet());

        this.solution = new int[colors.size()];
        Arrays.fill(solution, -1);

        //main hyperparameters
        this.nIterations = nIterations;
        this.nParticles = nParticles;
        this.nNeighborhood = nNeighborhood;

        //create the initial 
        this.particles = new ArrayList<>();

        //coeficients
        this.c1 = c1;
        this.c2 = c2;
        this.inertia = inertia;

        //fitness weights
        this.w1 = w1;
        this.w2 = w2;
        
        this.random = new Random();

        this.maxStagnation = maxStagnation;
    }

    private void generateParticles() {
        for (int i = 0; i < nParticles; i++) {
            int[] newSolution = generatePossibleSolution();
            double[] newVelocity = generateRandomVelocity();

            this.particles.add(new Particle(newSolution, newVelocity));
        }
    }

    private int[] generatePossibleSolution() {
        int[] newSolution = new int[this.colors.size()];

        int idx = 0;
        for (String s : this.colors) {
            int domain = colorCells.get(s).size();

            newSolution[idx] = random.nextInt(domain);
            idx++;
        }

        return newSolution;
    }

    private double[] generateRandomVelocity() {
        double[] newVelocity = new double[this.colors.size()];

        for (int i = 0; i < this.colors.size(); i++) {
            newVelocity[i] = random.nextDouble(1.0);
        }

        return newVelocity;
    }

    private void initialize() {
        
        //1. generate the initial particles
        this.generateParticles();

        //2. separate into neighborhoods
        this.generateNeighborhoods();

        //3. intial fitness calculation
        this.calculateInitialFitness();
    }

    private void generateNeighborhoods() {
        
        this.neighborhoods = new ArrayList<List<Integer>>(nNeighborhood);
        this.nBests = new int[this.nNeighborhood];

        List<Integer> indexes = IntStream.range(0, nParticles).boxed().collect(Collectors.toList());
        
        Collections.shuffle(indexes);

        this.neighborhoods = partitionList(indexes, nNeighborhood);
    }

    public List<List<Integer>> partitionList(List<Integer> list, int n) {
        //split the particles into neighborhoods
        //set equal amounts of particles in each neighborhood
        List<List<Integer>> result = new ArrayList<>(n);

        int size = list.size();
        int baseSize = size / n;
        int remainder = size % n;

        int index = 0;

        for (int i = 0; i < n; i++) {
            int groupSize = baseSize + (i < remainder ? 1:0);

            List<Integer> neighborhood = new ArrayList<>();

            for (int j = 0; j < groupSize && index < size; j++) {
                neighborhood.add(list.get(index++));
            }

            result.add(neighborhood);
        }

        return result;
    }

    private void calculateInitialFitness() {
        for (int i = 0; i < this.nNeighborhood; i++) {
            int curNBestIdx = -1;
            double curNBestFitness = Double.MAX_VALUE;
            
            for (int index : this.neighborhoods.get(i)) {
                Particle p = this.particles.get(index);
                double fitness = calculateFitness(p);
                p.setFitness(fitness);
                p.setPBestFitness(fitness);
                p.setPBest(p.getCandidate().clone());
                
                if (fitness < curNBestFitness) {
                    curNBestFitness = fitness;
                    curNBestIdx = index;
                }
            }
            this.nBests[i] = curNBestIdx;
        }
    }

    private double calculateFitness(Particle candidate) {
        boolean[][] occupied = new boolean[this.size][this.size];

        int[] positions = candidate.getCandidate();
        
        for (int i = 0; i < this.colors.size(); i++) {
            String color = this.colors.get(i);
            List<int[]> cells = this.colorCells.get(color);
            int cellIdx = positions[i];
            
            int[] cell = cells.get(cellIdx);
            occupied[cell[0]][cell[1]] = true;
        }

        int attackingViolations = calculateAttackingViolation(candidate);
        int adjacencyViolations = calculateAdjacencyViolation(candidate, occupied);
        
        double fitness = w1 * attackingViolations + w2 * adjacencyViolations;

        return fitness;
    }

    private int calculateAttackingViolation(Particle candidate) {
        int count = 0;
        int[] positions = candidate.getCandidate();
        
        List<int[]> queenPositions = new ArrayList<>();
        
        for (int i = 0; i < this.colors.size(); i++) {
            String color = this.colors.get(i);
            List<int[]> cells = this.colorCells.get(color);
            int cellIdx = positions[i];
            queenPositions.add(cells.get(cellIdx));
        }
        
        for (int i = 0; i < queenPositions.size(); i++) {
            for (int j = i + 1; j < queenPositions.size(); j++) {
                int[] pos1 = queenPositions.get(i);
                int[] pos2 = queenPositions.get(j);
                
                if (pos1[0] == pos2[0] || pos1[1] == pos2[1]) {
                    count++;
                }
            }
        }

        return count;
    }

    private int calculateAdjacencyViolation(Particle candidate, boolean[][] occupied) {
        int count = 0;
        int[] positions = candidate.getCandidate();
        
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        
        for (int i = 0; i < this.colors.size(); i++) {
            String color = this.colors.get(i);
            List<int[]> cells = this.colorCells.get(color);
            int cellIdx = positions[i];
            int[] cell = cells.get(cellIdx);
            
            int row = cell[0];
            int col = cell[1];
            
            for (int[] d : dirs) {
                int r = row + d[0];
                int c = col + d[1];
                
                if (r >= 0 && r < this.size && c >= 0 && c < this.size) {
                    if (occupied[r][c]) {
                        count++;
                    }
                }
            }
        }

        return count/2;
    }

    public void solve() {
        System.out.println("Starting PSO solver for " + size + "x" + size + " board with " + colors.size() + " colors.");
        System.out.println("Parameters: iterations=" + nIterations + ", particles=" + nParticles + 
                         ", neighborhoods=" + nNeighborhood + ", c1=" + c1 + ", c2=" + c2 + 
                         ", inertia=" + inertia + ", w1=" + w1 + ", w2=" + w2 +
                         ", maxStagnation=" + maxStagnation);
        
        long startTime = System.currentTimeMillis();
        
        //initialize the population
        initialize();

        //track the global best fitness across all neighborhoods for stagnation detection
        double globalBestFitness = getLowestNBestFitness();
        int stagnationCounter = 0;

        for (int i = 1; i <= this.nIterations; i++) {
            //update the velocity
            updateVelocity();

            //update particles and their fitness
            updateParticles();

            //check if solution exists
            if (checkNbest() != -1) {
                long endTime = System.currentTimeMillis();
                System.out.println("\nSolution found at iteration " + i);
                System.out.println("Time: " + (endTime - startTime) + " ms");
                printSolution(checkNbest());
                return;
            }

            //check for stagnation across all neighborhoods
            double currentBestFitness = getLowestNBestFitness();
            if (currentBestFitness < globalBestFitness) {
                globalBestFitness = currentBestFitness;
                stagnationCounter = 0;
            } else {
                stagnationCounter++;
            }

            if (stagnationCounter >= maxStagnation) {
                long endTime = System.currentTimeMillis();
                System.out.println("\nEarly termination: no improvement for " + maxStagnation + " iterations");
                System.out.println("Time: " + (endTime - startTime) + " ms");
                printSolution(checkLowestNBest());
                return;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\nNo perfect solution found after " + nIterations + " iterations");
        System.out.println("Time: " + (endTime - startTime) + " ms");
        printSolution(checkLowestNBest());
    }

    private double getLowestNBestFitness() {
        double minFitness = Double.MAX_VALUE;

        for (int n : this.nBests) {
            double fitness = this.particles.get(n).getFitness();
            if (fitness < minFitness) {
                minFitness = fitness;
            }
        }
        return minFitness;
    }

    private int checkLowestNBest() {
        int solution = -1;
        double minFitness = Double.MAX_VALUE;

        for (int n : this.nBests) {
            double fitness = this.particles.get(n).getFitness();
            if (fitness < minFitness) {
                minFitness = fitness;
                solution = n;
            }   
        }
        return solution;
    }

    private int checkNbest() {
        int solution = -1;

        for (int n : this.nBests) {
            if (this.particles.get(n).getFitness() == 0) {
                solution = n;
                break;
            }   
        }
        return solution;
    }

    private void updateParticles() {
        for (int i = 0; i < this.nNeighborhood; i++) {
            int nBestIdx = this.nBests[i];
            int[] nBestPosition = this.particles.get(nBestIdx).getCandidate();
            
            double curNBestFitness = this.particles.get(nBestIdx).getFitness();
            
            for (int particleIdx : this.neighborhoods.get(i)) {
                Particle p = this.particles.get(particleIdx);
                int[] currentPosition = p.getCandidate();
                double[] velocity = p.getVelocity();
                int[] newPosition = currentPosition.clone();
                
                for (int j = 0; j < newPosition.length; j++) {
                    double rand = random.nextDouble(1.0);
                    
                    if (rand < velocity[j]) {
                        newPosition[j] = nBestPosition[j];
                    }
                }
                
                p.setCandidate(newPosition);
                
                double newFitness = calculateFitness(p);
                p.setFitness(newFitness);
                
                if (newFitness < p.getPBestFitness()) {
                    p.setPBestFitness(newFitness);
                    p.setPBest(newPosition.clone());
                }
                
                if (newFitness < curNBestFitness) {
                    curNBestFitness = newFitness;
                    this.nBests[i] = particleIdx;
                }
            }
        }
    }

    private void updateVelocity() {
        for (int i = 0; i < this.nNeighborhood; i++) {
            int nBestIdx = this.nBests[i];
            int[] nBestPosition = this.particles.get(nBestIdx).getCandidate();
            
            for (int particleIdx : this.neighborhoods.get(i)) {
                Particle p = this.particles.get(particleIdx);
                int[] currentPosition = p.getCandidate();
                int[] pBestPosition = p.getPBest();
                double[] velocity = p.getVelocity();
                double[] newVelocity = new double[velocity.length];
                
                for (int j = 0; j < velocity.length; j++) {
                    double r1 = random.nextDouble(1.0);
                    double r2 = random.nextDouble(1.0);
                    
                    int pBestDiff = (currentPosition[j] != pBestPosition[j]) ? 1 : 0;
                    int nBestDiff = (currentPosition[j] != nBestPosition[j]) ? 1 : 0;
                    
                    newVelocity[j] = this.inertia * velocity[j] + this.c1 * r1 * pBestDiff + this.c2 * r2 * nBestDiff;
                    
                    if (newVelocity[j] < 0.0) newVelocity[j] = 0.0;
                    if (newVelocity[j] > 1.0) newVelocity[j] = 1.0;
                }
                
                p.setVelocity(newVelocity);
            }
        }
    }

    private void printBoard(boolean[][] occupied) {
        System.out.println("Board state:");
        String[][] grid = new String[size][size];

        for (Map.Entry<String, List<int[]>> entry : colorCells.entrySet()) {
            String symbol = board.getSymbolForColor(entry.getKey());
            for (int[] cell : entry.getValue()) {
                grid[cell[0]][cell[1]] = symbol;
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (occupied[r][c]) {
                    grid[r][c] = "Q";
                }
            }
        }

        System.out.print("   ");
        for (int c = 0; c < size; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.print(r + "  ");
            for (int c = 0; c < size; c++) {
                System.out.print((grid[r][c] != null ? grid[r][c] : ".") + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printSolution(int particleIdx) {
        if (particleIdx == -1) {
            System.out.println("No solution found!");
            return;
        }

        Particle p = this.particles.get(particleIdx);
        int[] positions = p.getCandidate();
        double fitness = p.getFitness();
        
        boolean isValid = (fitness == 0.0);
        
        if (isValid) {
            System.out.println("\n=== VALID SOLUTION FOUND ===");
        } else {
            System.out.println("\n=== BEST SOLUTION (NOT VALID) ===");
            System.out.println("Fitness: " + fitness);
        }
        
        System.out.println("Final solution:");
        
        boolean[][] occupied = new boolean[this.size][this.size];
        
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            String symbol = board.getSymbolForColor(color);
            int[] cell = colorCells.get(color).get(positions[i]);
            System.out.println("Color " + symbol + " at [" + cell[0] + "," + cell[1] + "]");
            occupied[cell[0]][cell[1]] = true;
        }

        printBoard(occupied);
    }
}