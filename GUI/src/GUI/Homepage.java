package GUI;

import javax.swing.*;
import java.awt.*;

public class Homepage extends JFrame {

    public Homepage() {
        setTitle("Colored Queens");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(235, 235, 240));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome to the Colored Queens Game!");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());

        add(panel);

        startButton.addActionListener(e -> {
            new LevelSelectorWindow().setVisible(true);
            dispose();
        });

        
    }


}