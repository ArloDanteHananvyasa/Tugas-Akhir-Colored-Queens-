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

        UITheme.GradientPanel main = new UITheme.GradientPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));

        // page header
        JLabel header = new JLabel("Choose Your Challenge");
        header.setFont(UITheme.serif(Font.BOLD, 48));
        header.setForeground(UITheme.PLUM);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Section 1: Pick a Difficulty ---
        JLabel diffLabel = new JLabel("Pick a Difficulty");
        diffLabel.setFont(UITheme.font(Font.BOLD, 28));
        diffLabel.setForeground(UITheme.TEXT_DARK);
        diffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        diffPanel.setOpaque(false);
        diffPanel.add(makeDifficultyGroup("Easy",   new int[]{7}));
        diffPanel.add(makeDifficultyGroup("Medium", new int[]{8, 9}));
        diffPanel.add(makeDifficultyGroup("Hard",   new int[]{10, 11, 12}));

        // --- Section 2: Challenge Yourself ---
        JLabel challengeLabel = new JLabel("Challenge Yourself!");
        challengeLabel.setFont(UITheme.font(Font.BOLD, 28));
        challengeLabel.setForeground(UITheme.TEXT_DARK);
        challengeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel challengeSub = new JLabel("Only one level each");
        challengeSub.setFont(UITheme.font(Font.ITALIC, 16));
        challengeSub.setForeground(UITheme.TEXT_MUTED);
        challengeSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel challengePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        challengePanel.setOpaque(false);
        challengePanel.add(makeSizeButton(20));
        challengePanel.add(makeSizeButton(30));

        main.add(header);
        main.add(Box.createRigidArea(new Dimension(0, 35)));
        main.add(diffLabel);
        main.add(Box.createRigidArea(new Dimension(0, 18)));
        main.add(diffPanel);
        main.add(Box.createRigidArea(new Dimension(0, 35)));
        main.add(challengeLabel);
        main.add(Box.createRigidArea(new Dimension(0, 4)));
        main.add(challengeSub);
        main.add(Box.createRigidArea(new Dimension(0, 14)));
        main.add(challengePanel);

        setContentPane(main);
    }

    private JPanel makeDifficultyGroup(String label, int[] sizes) {
        JPanel group = UITheme.card();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel groupLabel = new JLabel(label);
        groupLabel.setFont(UITheme.serif(Font.BOLD, 24));
        groupLabel.setForeground(UITheme.PLUM);
        groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.add(groupLabel);
        group.add(Box.createRigidArea(new Dimension(0, 14)));

        for (int size : sizes) {
            JButton btn = makeSizeButton(size);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            group.add(btn);
            group.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return group;
    }

    private JButton makeSizeButton(int size) {
        JButton btn = UITheme.roundedButton(size + " × " + size, true);
        btn.setPreferredSize(new Dimension(150, 52));
        btn.addActionListener(e -> {
            new GameWindow(size, 1).setVisible(true);
            dispose();
        });
        return btn;
    }
}