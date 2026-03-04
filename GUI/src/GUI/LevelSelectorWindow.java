package GUI;

import javax.swing.*;
import java.awt.*;

public class LevelSelectorWindow extends JFrame {

    public LevelSelectorWindow() {
        setTitle("Colored Queens");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setBackground(Color.WHITE);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // --- Section 1: Pick a Difficulty ---
        JLabel diffLabel = new JLabel("Pick a Difficulty");
        diffLabel.setFont(new Font("Arial", Font.BOLD, 36));
        diffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 20));
        diffPanel.setBackground(Color.WHITE);

        diffPanel.add(makeDifficultyGroup("Easy", new int[]{7}));
        diffPanel.add(makeDifficultyGroup("Medium", new int[]{8, 9}));
        diffPanel.add(makeDifficultyGroup("Hard", new int[]{10, 11}));

        // --- Section 2: Challenge Yourself ---
        JLabel challengeLabel = new JLabel("Challenge Yourself!");
        challengeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        challengeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel challengeSub = new JLabel("Only one level each");
        challengeSub.setFont(new Font("Arial", Font.PLAIN, 16));
        challengeSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel challengePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 20));
        challengePanel.setBackground(Color.WHITE);
        challengePanel.add(makeSizeButton(20));
        challengePanel.add(makeSizeButton(30));

        main.add(diffLabel);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(diffPanel);
        main.add(Box.createRigidArea(new Dimension(0, 30)));
        main.add(challengeLabel);
        main.add(Box.createRigidArea(new Dimension(0, 5)));
        main.add(challengeSub);
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        main.add(challengePanel);

        add(main);
    }

    // creates a labeled group of size buttons (e.g. "Easy" with 7x7)
    private JPanel makeDifficultyGroup(String label, int[] sizes) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(Color.WHITE);

        JLabel groupLabel = new JLabel(label);
        groupLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.add(groupLabel);
        group.add(Box.createRigidArea(new Dimension(0, 8)));

        for (int size : sizes) {
            group.add(makeSizeButton(size));
            group.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        return group;
    }

    // creates a single clickable size button (e.g. "8 x 8")
    private JButton makeSizeButton(int size) {
        JButton btn = new JButton(size + " x " + size);
        btn.setFont(new Font("Arial", Font.PLAIN, 20));
        btn.setPreferredSize(new Dimension(120, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            new GameWindow(size, 1).setVisible(true);
            dispose();
        });
        return btn;
    }
}