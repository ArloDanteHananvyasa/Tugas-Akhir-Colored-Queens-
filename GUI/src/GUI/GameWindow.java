package GUI;

import objects.Board;
import objects.Cell;
import solver.backtracking.BacktrackingSolverAC3;
import util.BoardImporter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.Timer;


public class GameWindow extends JFrame {

    private int currentSize;
    private int currentLevel;
    private Board board;
    private int[][] solution; // solution[i] = [row, col] for color i
    private boolean[][] queenPlaced;
    private boolean timerStarted = false;
    private int secondsElapsed = 0;
    private Timer gameTimer;
    private JLabel timerLabel;
    private JLabel sizeLabel;
    private JLabel levelLabel;
    private BoardPanel boardPanel;
    private JComboBox<String> levelDropdown;

    private static final Map<Integer, Integer> MAX_LEVELS = new LinkedHashMap<>();
    static {
        MAX_LEVELS.put(7, 50);
        MAX_LEVELS.put(8, 130);
        MAX_LEVELS.put(9, 110);
        MAX_LEVELS.put(10, 60);
        MAX_LEVELS.put(11, 50);
        MAX_LEVELS.put(20, 1);
        MAX_LEVELS.put(30, 1);
    }

    // hardcoded solutions for 20x20 and 30x30
    // each int[] is {row, col}, ordered by color symbol (A, B, C...)
    private static final int[][] PRELOADED_20x20 = {
        {15,14},{14,1},{17,16},{3,0},{0,12},{10,9},{1,7},{5,8},
        {6,19},{2,3},{11,17},{16,10},{12,2},{18,11},{8,18},{7,13},
        {4,15},{13,4},{19,5},{9,6}
    };

    private static final int[][] PRELOADED_30x30 = {
        {21,22},{15,9},{6,15},{7,10},{26,4},{0,11},{9,3},{19,28},
        {1,0},{27,7},{28,26},{18,23},{22,1},{29,13},{4,16},{11,25},
        {10,21},{20,20},{23,12},{14,19},{3,24},{2,17},{25,2},{24,18},
        {13,27},{12,14},{16,5},{17,29},{5,8},{8,6}
    };

    public GameWindow(int size, int level) {
        this.currentSize = size;
        this.currentLevel = level;

        setTitle("Colored Queens");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- Top Panel ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        sizeLabel = new JLabel(size + " x " + size);
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        levelLabel = new JLabel("Level " + level);
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(sizeLabel);
        topPanel.add(levelLabel);

        // --- Bottom Panel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // size dropdown
        String[] sizeOptions = {"7x7", "8x8", "9x9", "10x10", "11x11", "20x20", "30x30"};
        JComboBox<String> sizeDropdown = new JComboBox<>(sizeOptions);
        sizeDropdown.setSelectedItem(size + "x" + size);
        sizeDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        sizeDropdown.addActionListener(e -> {
            String selected = (String) sizeDropdown.getSelectedItem();
            int newSize = Integer.parseInt(selected.replace("x" + selected.split("x")[1], ""));
            loadBoard(newSize, 1);
        });

        // level dropdown
        levelDropdown = new JComboBox<>();
        populateLevelDropdown(size);
        levelDropdown.setSelectedItem("Level " + level);
        levelDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        levelDropdown.addActionListener(e -> {
            if (levelDropdown.getSelectedItem() == null) return;
            String selected = (String) levelDropdown.getSelectedItem();
            int newLevel = Integer.parseInt(selected.replace("Level ", ""));
            if (newLevel != currentLevel) {
                loadBoard(currentSize, newLevel);
            }
        });

        // timer
        timerLabel = new JLabel("00:00:000");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));

        gameTimer = new Timer(100, e -> {
            secondsElapsed += 100;
            timerLabel.setText(formatTime(secondsElapsed));
        });

        // hint button
        JButton hintButton = new JButton("Hint");
        hintButton.setFont(new Font("Arial", Font.PLAIN, 16));
        hintButton.addActionListener(e -> giveHint());

        // solve button
        JButton solveButton = new JButton("Solve");
        solveButton.setFont(new Font("Arial", Font.PLAIN, 16));
        solveButton.addActionListener(e -> applySolution());

        bottomPanel.add(sizeDropdown);
        bottomPanel.add(levelDropdown);
        bottomPanel.add(timerLabel);
        bottomPanel.add(hintButton);
        bottomPanel.add(solveButton);

        // --- Board Panel (placeholder until board loads) ---
        boardPanel = new BoardPanel();
        boardPanel.setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // load the board
        loadBoard(size, level);
    }

    private void loadBoard(int size, int level) {
        this.currentSize = size;
        this.currentLevel = level;

        // stop timer
        if (gameTimer.isRunning()) gameTimer.stop();
        timerStarted = false;
        secondsElapsed = 0;
        timerLabel.setText("00:00:000");

        // update labels
        sizeLabel.setText(size + " x " + size);
        levelLabel.setText("Level " + level);

        // update level dropdown
        populateLevelDropdown(size);
        levelDropdown.setSelectedItem("Level " + level);

        try {
            List<Cell> cells = BoardImporter.importBoard(size, level);
            board = new Board(size, cells);
            queenPlaced = new boolean[size][size];
            solution = null;

            boardPanel.setBoard(board, queenPlaced);
            boardPanel.repaint();

            // run solver in background (not for 20x20 and 30x30)
            if (size == 20) {
                solution = PRELOADED_20x20;
            } else if (size == 30) {
                solution = PRELOADED_30x30;
            } else {
                //multithreading so the window doesn't freeze during the solver call
                SwingWorker<int[][], Void> worker = new SwingWorker<>() {
                    @Override
                    protected int[][] doInBackground() {
                        BacktrackingSolverAC3 solver = new BacktrackingSolverAC3(board);
                        solver.solve();
                        return solver.getSolutionAsGrid();
                    }

                    @Override
                    protected void done() {
                        try {
                            solution = get();
                        } catch (Exception e) {
                            System.err.println("Solver error: " + e.getMessage());
                        }
                    }
                };
                worker.execute();
            }

        } catch (Exception e) {
            System.err.println("Failed to load board: " + e.getMessage());
        }
    }

    private void applySolution() {
        if (solution == null) {
            JOptionPane.showMessageDialog(this, "Solution not ready yet, please wait.");
            return;
        }
        // stop timer
        if (gameTimer.isRunning()) gameTimer.stop();

        // apply solution to board
        queenPlaced = new boolean[currentSize][currentSize];
        for (int[] pos : solution) {
            queenPlaced[pos[0]][pos[1]] = true;
        }
        boardPanel.setQueens(queenPlaced);
        boardPanel.repaint();
    }

    private void giveHint() {
        if (solution == null) {
            JOptionPane.showMessageDialog(this, "Hint not ready yet, please wait.");
            return;
        }
        // find a color that doesn't have a queen placed yet and reveal one correct placement
        for (int[] pos : solution) {
            if (!queenPlaced[pos[0]][pos[1]]) {
                queenPlaced[pos[0]][pos[1]] = true;
                if (!timerStarted) {
                    timerStarted = true;
                    gameTimer.start();
                }
                boardPanel.setQueens(queenPlaced);
                boardPanel.repaint();
                return;
            }
        }
    }

    private void populateLevelDropdown(int size) {
        levelDropdown.removeAllItems();
        int max = MAX_LEVELS.getOrDefault(size, 1);
        for (int i = 1; i <= max; i++) {
            levelDropdown.addItem("Level " + i);
        }
    }

    private String formatTime(int ms) {
        int minutes = ms / 60000;
        int seconds = (ms % 60000) / 1000;
        int millis = ms % 1000;
        return String.format("%02d:%02d:%03d", minutes, seconds, millis);
    }

    // --- Board Panel ---
    private class BoardPanel extends JPanel {

        private Color[][] errorHighlight;

        private Image queenImageBlack;
        private Image queenImageWhite;
        private Image queenImageRed;

        private Board board;
        private Color[][] colorGrid;
        private boolean[][] queens;
        private int size;

        public BoardPanel() {}

        public void setBoard(Board board, boolean[][] queens) {
            this.board = board;
            this.size = board.getSize();
            this.queens = queens;
            buildColorGrid();

            // remove old listeners first
            for (MouseListener ml : getMouseListeners()) {
                removeMouseListener(ml);
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int cellSize = Math.min(getWidth(), getHeight()) / size;
                    int totalSize = cellSize * size;
                    int xOffset = (getWidth() - totalSize) / 2;
                    int yOffset = (getHeight() - totalSize) / 2;

                    int col = (e.getX() - xOffset) / cellSize;
                    int row = (e.getY() - yOffset) / cellSize;

                    if (row < 0 || row >= size || col < 0 || col >= size) return;

                    // toggle queen
                    queens[row][col] = !queens[row][col];

                    // start timer on first placement
                    if (!timerStarted && queens[row][col]) {
                        timerStarted = true;
                        gameTimer.start();
                    }

                    repaint();
                    checkViolations();
                }
            });

            try {
                queenImageBlack = ImageIO.read(new File("assets/queen_black.png"));
                queenImageWhite = ImageIO.read(new File("assets/queen_white.png"));
                queenImageRed = ImageIO.read(new File("assets/queen_red.png"));
                System.out.println("Queen image loaded: " + queenImageBlack);
            } catch (Exception e) {
                System.out.println("Queen image failed: " + e.getMessage());
                queenImageBlack = null;
                queenImageWhite = null;
                queenImageRed = null;
            }
        
        }

        public void setQueens(boolean[][] queens) {
            this.queens = queens;
        }

        private void buildColorGrid() {
            colorGrid = new Color[size][size];
            for (Map.Entry<String, List<int[]>> entry : board.getColorMap().entrySet()) {
                String[] rgb = entry.getKey().split(",");
                Color color = new Color(
                    Integer.parseInt(rgb[0].trim()),
                    Integer.parseInt(rgb[1].trim()),
                    Integer.parseInt(rgb[2].trim())
                );
                for (int[] cell : entry.getValue()) {
                    colorGrid[cell[0]][cell[1]] = color;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cellSize = Math.min(getWidth(), getHeight()) / size;
            int totalSize = cellSize * size;

            // center the board
            int xOffset = (getWidth() - totalSize) / 2;
            int yOffset = (getHeight() - totalSize) / 2;

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int x = xOffset + col * cellSize;
                    int y = yOffset + row * cellSize;

                    // draw cell color
                    Color color = colorGrid[row][col];
                    if (color != null) {
                        g2.setColor(color);
                        g2.fillRect(x, y, cellSize, cellSize);
                    }

                    // draw grid lines
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, cellSize, cellSize);

                    // draw queen
                    if (queens != null && queens[row][col]) {
                        boolean isError = errorHighlight != null && errorHighlight[row][col] != null;
                        Image img;
                        if (isError) {
                            img = queenImageRed;
                        } else {
                            img = isDark(colorGrid[row][col]) ? queenImageWhite : queenImageBlack;
                        }
                        if (img != null) {
                            int padding = cellSize / 6;
                            g2.drawImage(img, x + padding, y + padding,
                                        cellSize - padding * 2, cellSize - padding * 2, null);
                        }
                    }
                }
            }
        }

        private void checkViolations() {
            errorHighlight = new Color[currentSize][currentSize];

            // collect all placed queens
            List<int[]> placed = new ArrayList<>();
            for (int r = 0; r < currentSize; r++) {
                for (int c = 0; c < currentSize; c++) {
                    if (queenPlaced[r][c]) placed.add(new int[]{r, c});
                }
            }

            // check each pair for conflicts
            for (int i = 0; i < placed.size(); i++) {
                for (int j = i + 1; j < placed.size(); j++) {
                    int r1 = placed.get(i)[0], c1 = placed.get(i)[1];
                    int r2 = placed.get(j)[0], c2 = placed.get(j)[1];

                    boolean conflict = (r1 == r2 || c1 == c2 ||
                        (Math.abs(r1 - r2) <= 1 && Math.abs(c1 - c2) <= 1));

                    if (conflict) {
                        errorHighlight[r1][c1] = new Color(255, 0, 0, 100);
                        errorHighlight[r2][c2] = new Color(255, 0, 0, 100);
                    }
                }
            }

            boardPanel.setErrorHighlight(errorHighlight);

            // check win condition
            if (solution != null && placed.size() == currentSize) {
                boolean matchesSolution = true;
                for (int[] pos : solution) {
                    if (!queenPlaced[pos[0]][pos[1]]) {
                        matchesSolution = false;
                        break;
                    }
                }
                if (matchesSolution) {
                    gameTimer.stop();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Congratulations! You solved it!");
                    });
                }
            }
        }

        public void setErrorHighlight(Color[][] errorHighlight) {
            this.errorHighlight = errorHighlight;
        }

        private boolean isDark(Color c) {
            // standard luminance formula
            double luminance = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255;
            return luminance < 0.5;
        }
    }
}