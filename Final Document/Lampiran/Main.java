package app;

import javax.swing.SwingUtilities;
import GUI.Homepage;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Homepage game = new Homepage();
            game.setVisible(true);
        });
    }
}
