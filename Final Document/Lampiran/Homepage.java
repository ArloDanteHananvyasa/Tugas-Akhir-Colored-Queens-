package GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Homepage extends JFrame {

    public Homepage() {
        setTitle("Colored Queens");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        UITheme.GradientPanel panel = new UITheme.GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // queen icon
        JLabel queenIcon = new JLabel();
        queenIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            Image img = ImageIO.read(new File("assets/queen_black.png"));
            Image scaled = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            queenIcon.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            // fall through silently if asset missing
        }

        // main welcome title (big + bold, your main ask)
        JLabel welcomeLabel = new JLabel("Welcome to Colored Queens");
        welcomeLabel.setFont(UITheme.serif(Font.BOLD, 64));
        welcomeLabel.setForeground(UITheme.PLUM);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // subtitle / tagline
        JLabel subtitle = new JLabel("A puzzle of queens, colors, and constraints");
        subtitle.setFont(UITheme.font(Font.PLAIN, 22));
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // start button
        JButton startButton = UITheme.roundedButton("Start Game", true);
        startButton.setFont(UITheme.font(Font.BOLD, 22));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(260, 60));
        startButton.addActionListener(e -> {
            new LevelSelectorWindow().setVisible(true);
            dispose();
        });

        panel.add(Box.createVerticalGlue());
        panel.add(queenIcon);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        setContentPane(panel);
    }
}