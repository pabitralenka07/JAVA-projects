package com.portfolio.passwordgen.ui;

import com.portfolio.passwordgen.model.PasswordStrength;
import com.portfolio.passwordgen.model.PasswordStrengthLevel;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Custom modern Swing component rendering a rounded progress bar
 * representing password entropy strength with dynamic color transitions.
 */
public class StrengthBar extends JComponent {

    private int percentage = 0;
    private Color fillColor = PasswordStrengthLevel.VERY_WEAK.getColor();
    private static final Color TRACK_COLOR = new Color(55, 65, 81); // Slate gray (#374151)

    public StrengthBar() {
        setPreferredSize(new Dimension(320, 10));
        setMinimumSize(new Dimension(100, 10));
    }

    public void updateStrength(PasswordStrength strength) {
        if (strength == null) {
            this.percentage = 0;
            this.fillColor = PasswordStrengthLevel.VERY_WEAK.getColor();
        } else {
            this.percentage = strength.getPercentage();
            this.fillColor = strength.level().getColor();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable high-quality anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = height; // Fully rounded pill shape

        // Draw track background
        g2.setColor(TRACK_COLOR);
        g2.fillRoundRect(0, 0, width, height, arc, arc);

        // Draw active fill
        if (percentage > 0) {
            int fillWidth = Math.max(arc, (int) (width * (percentage / 100.0)));
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, fillWidth, height, arc, arc);
        }

        g2.dispose();
    }
}
