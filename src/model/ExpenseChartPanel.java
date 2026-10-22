package model;


import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ExpenseChartPanel extends JPanel {

    private final Map<String, Double> expenses;

    public ExpenseChartPanel(Map<String, Double> expenses) {
        this.expenses = expenses;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 450));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int left = 80;
        int top = 65;
        int right = getWidth() - 40;
        int bottom = getHeight() - 70;

        g2.setColor(new Color(35, 55, 85));
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2.drawString("Expenses by Category", left, 35);

        if (expenses == null || expenses.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.drawString("No expense data available.", left, 100);
            g2.dispose();
            return;
        }

        double max = 0;

        for (double amount : expenses.values()) {
            max = Math.max(max, amount);
        }

        if (max <= 0) {
            max = 1;
        }

        int chartWidth = right - left;
        int chartHeight = bottom - top;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        for (int i = 0; i <= 4; i++) {
            int y = bottom - (i * chartHeight / 4);

            g2.setColor(new Color(220, 225, 230));
            g2.drawLine(left, y, right, y);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString(
                    String.format("%.0f", max * i / 4),
                    35, y + 5
            );
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(left, top, left, bottom);
        g2.drawLine(left, bottom, right, bottom);

        int count = expenses.size();
        int slotWidth = Math.max(1, chartWidth / count);
        int barWidth = Math.max(8, Math.min(55, slotWidth / 2));

        Color[] colors = {
            new Color(66, 133, 244),
            new Color(52, 168, 83),
            new Color(251, 188, 5),
            new Color(171, 71, 188),
            new Color(234, 67, 53)
        };

        int index = 0;

        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            double amount = entry.getValue();

            int barHeight = (int) ((amount / max) * chartHeight);
            int x = left + index * slotWidth
                    + (slotWidth - barWidth) / 2;
            int y = bottom - barHeight;

            g2.setColor(colors[index % colors.length]);
            g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(
                    String.format("%.0f", amount),
                    x, Math.max(top - 5, y - 5)
            );

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            String category = entry.getKey();

            if (category == null) {
                category = "Other";
            }

            if (category.length() > 10) {
                category = category.substring(0, 10);
            }

            g2.drawString(category, x - 5, bottom + 20);
            index++;
        }

        g2.dispose();
    }
}
