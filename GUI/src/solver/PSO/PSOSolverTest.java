package solver.PSO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import objects.Board;

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

public class PSOSolverTest {
    private Board board;
    private int size;
    private Map<String, List<int[]>> colorCells;
    private List<String> colors;

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
    
    private int maxStagnation;
    
    private Random random;
    
    private long executionTime;
    private int finalAttackingViolations;
    private int finalAdjacencyViolations;
    
    public PSOSolverTest(Board board, int nIterations, int nParticles, double c1, double c2, int nNeighborhood, double inertia, double w1, double w2, int maxStagnation) {
        this.board = board;
        this.size = this.board.getSize();
        
        this.colorCells = this.board.getColorMap();
        this.colors = new ArrayList<>(this.colorCells.keySet());

        this.nIterations = nIterations;
        this.nParticles = nParticles;
        this.nNeighborhood = nNeighborhood;

        this.particles = new ArrayList<>();

        this.c1 = c1;
        this.c2 = c2;
        this.inertia = inertia;

        this.w1 = w1;
        this.w2 = w2;
        
        this.maxStagnation = maxStagnation;
        
        this.random = new Random();
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
        this.generateParticles();
        this.generateNeighborhoods();
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
        long startTime = System.currentTimeMillis();
        
        initialize();

        double globalBestFitness = getLowestNBestFitness();
        int stagnationCounter = 0;

        for (int i = 1; i <= this.nIterations; i++) {
            updateVelocity();
            updateParticles();

            if (checkNbest() != -1) {
                long endTime = System.currentTimeMillis();
                this.executionTime = endTime - startTime;
                storeFinalViolations(checkNbest());
                return;
            }

            double currentBestFitness = getLowestNBestFitness();
            if (currentBestFitness < globalBestFitness) {
                globalBestFitness = currentBestFitness;
                stagnationCounter = 0;
            } else {
                stagnationCounter++;
            }

            if (stagnationCounter >= maxStagnation) {
                long endTime = System.currentTimeMillis();
                this.executionTime = endTime - startTime;
                storeFinalViolations(checkLowestNBest());
                return;
            }
        }

        long endTime = System.currentTimeMillis();
        this.executionTime = endTime - startTime;
        storeFinalViolations(checkLowestNBest());
    }

    private void storeFinalViolations(int particleIdx) {
        Particle p = this.particles.get(particleIdx);
        
        boolean[][] occupied = new boolean[this.size][this.size];
        int[] positions = p.getCandidate();
        
        for (int i = 0; i < this.colors.size(); i++) {
            String color = this.colors.get(i);
            List<int[]> cells = this.colorCells.get(color);
            int cellIdx = positions[i];
            int[] cell = cells.get(cellIdx);
            occupied[cell[0]][cell[1]] = true;
        }
        
        this.finalAttackingViolations = calculateAttackingViolation(p);
        this.finalAdjacencyViolations = calculateAdjacencyViolation(p, occupied);
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

    public List<int[]> getSolutionCoordinates() {
        int bestIdx = checkNbest();
        if (bestIdx == -1) {
            bestIdx = checkLowestNBest();
        }
        
        Particle p = this.particles.get(bestIdx);
        int[] positions = p.getCandidate();
        
        List<int[]> coords = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);
            int[] cell = colorCells.get(color).get(positions[i]);
            coords.add(new int[]{cell[0], cell[1]});
        }
        return coords;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isValid() {
        int bestIdx = checkNbest();
        return bestIdx != -1;
    }

    public double getFitness() {
        int bestIdx = checkNbest();
        if (bestIdx == -1) {
            bestIdx = checkLowestNBest();
        }
        return this.particles.get(bestIdx).getFitness();
    }

    public int getAttackingViolations() {
        return finalAttackingViolations;
    }

    public int getAdjacencyViolations() {
        return finalAdjacencyViolations;
    }
}