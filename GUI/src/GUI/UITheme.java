package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class UITheme {
    // palette
    public static final Color CREAM       = new Color(245, 239, 228);
    public static final Color CARD        = new Color(251, 247, 240);
    public static final Color PLUM        = new Color(142, 106, 138);
    public static final Color PLUM_DARK   = new Color(112,  82, 108);
    public static final Color ROSE        = new Color(217, 166, 160);
    public static final Color TAUPE       = new Color(168, 156, 138);
    public static final Color TEXT_DARK   = new Color( 62,  46,  46);
    public static final Color TEXT_MUTED  = new Color(120, 104,  94);

    // fonts (Swing falls through to first installed)
    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }
    public static Font serif(int style, int size) {
        return new Font("Georgia", style, size);
    }

    // a reusable cream-gradient background panel
    public static class GradientPanel extends JPanel {
        public GradientPanel() {
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(
                0, 0,           new Color(248, 242, 232),
                0, getHeight(), new Color(238, 228, 215));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // pretty rounded button — pass primary=true for plum, false for outline
    public static JButton roundedButton(String text, boolean primary) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg;
                if (primary) {
                    bg = getModel().isPressed() ? PLUM_DARK
                       : getModel().isRollover() ? ROSE
                       : PLUM;
                } else {
                    bg = getModel().isRollover() ? new Color(232, 220, 205)
                                                 : CARD;
                }
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth(), getHeight(), 22, 22));

                if (!primary) {
                    g2.setColor(TAUPE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1,
                            getWidth() - 2, getHeight() - 2, 22, 22));
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        b.setForeground(primary ? Color.WHITE : TEXT_DARK);
        b.setFont(font(Font.BOLD, 18));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        return b;
    }

    // a soft card panel (for the level selector groups)
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(
            new LineBorder(TAUPE, 1, true),
            new EmptyBorder(20, 28, 20, 28)
        ));
        return p;
    }
}