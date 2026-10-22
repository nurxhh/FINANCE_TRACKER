package model;


import model.Transactable;
import model.Income;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ReportFrame extends JFrame {

    private final ReportService reportService = new ReportService();
    private final List<Transactable> transactions;

    private JLabel incomeLabel;
    private JLabel expensesLabel;
    private JLabel balanceLabel;

    private JComboBox<String> typeFilter;
    private JTextField categoryField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTextArea reportArea;

    public ReportFrame(List<Transactable> transactions) {
        this.transactions = transactions;

        setTitle("Personal Finance Tracker - Reports");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createSummaryPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        refreshSummary();
        refreshTable(transactions);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 10, 15)
        );

        incomeLabel = new JLabel("", SwingConstants.CENTER);
        expensesLabel = new JLabel("", SwingConstants.CENTER);
        balanceLabel = new JLabel("", SwingConstants.CENTER);

        panel.add(createSummaryCard(
                incomeLabel, new Color(220, 245, 232)));
        panel.add(createSummaryCard(
                expensesLabel, new Color(255, 232, 232)));
        panel.add(createSummaryCard(
                balanceLabel, new Color(225, 237, 255)));

        return panel;
    }

    private JPanel createSummaryCard(JLabel label, Color color) {
        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));

        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(label, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        );

        String[] columns = {
            "ID", "Type", "Amount", "Date", "Category", "Description"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(24);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Transactions", new JScrollPane(transactionTable));
        tabs.addTab("Financial Reports", createReportsPanel());

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 5, 5));

        typeFilter = new JComboBox<>(
                new String[]{"All", "Income", "Expense"}
        );
        categoryField = new JTextField();
        startDateField = new JTextField();
        endDateField = new JTextField();
        searchField = new JTextField();

        JButton applyButton = new JButton("Apply Filters");
        JButton resetButton = new JButton("Reset");

        panel.add(new JLabel("Type:"));
        panel.add(typeFilter);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
        panel.add(endDateField);

        panel.add(new JLabel("Search Description:"));
        panel.add(searchField);
        panel.add(applyButton);
        panel.add(resetButton);

        applyButton.addActionListener(e -> applyFilters());
        resetButton.addActionListener(e -> resetFilters());

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        JButton categoryButton =
                new JButton("Expenses by Category");
        JButton monthlyButton = new JButton("Monthly Report");
        JButton yearlyButton = new JButton("Yearly Report");
        JButton customButton = new JButton("Custom Date Range");

        categoryButton.addActionListener(e -> showCategoryReport());
        monthlyButton.addActionListener(e -> showMonthlyReport());
        yearlyButton.addActionListener(e -> showYearlyReport());
        customButton.addActionListener(e -> showCustomReport());

        buttons.add(categoryButton);
        buttons.add(monthlyButton);
        buttons.add(yearlyButton);
        buttons.add(customButton);

        JTabbedPane reportTabs = new JTabbedPane();

        reportTabs.addTab(
                "Report Details",
                new JScrollPane(reportArea)
        );

        Map<String, Double> categoryTotals =
                reportService.expensesByCategory(transactions);

        reportTabs.addTab(
                "Expense Chart",
                new JScrollPane(new ExpenseChartPanel(categoryTotals))
        );

        panel.add(buttons, BorderLayout.NORTH);
        panel.add(reportTabs, BorderLayout.CENTER);

        return panel;
    }

    private void refreshSummary() {
        ReportService.Summary summary =
                reportService.calculateSummary(transactions);

        incomeLabel.setText(String.format(
                "Total Income: ₹%.2f",
                summary.getTotalIncome()
        ));

        expensesLabel.setText(String.format(
                "Total Expenses: ₹%.2f",
                summary.getTotalExpenses()
        ));

        balanceLabel.setText(String.format(
                "Balance: ₹%.2f",
                summary.getBalance()
        ));
    }

    private void refreshTable(List<Transactable> list) {
        tableModel.setRowCount(0);

        for (Transactable t : list) {
            String type = t instanceof Income ? "Income" : "Expense";

            tableModel.addRow(new Object[]{
                t.getId(),
                type,
                String.format("%.2f", t.getAmount()),
                t.getDate(),
                t.getCategory(),
                t.getDescription()
            });
        }
    }

    private void applyFilters() {
        try {
            String type = (String) typeFilter.getSelectedItem();

            if ("All".equals(type)) {
                type = null;
            }

            String category = categoryField.getText().trim();
            String description = searchField.getText().trim();

            LocalDate start = parseDate(startDateField.getText());
            LocalDate end = parseDate(endDateField.getText());

            if (start != null && end != null && start.isAfter(end)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Start date cannot be after end date."
                );
                return;
            }

            List<Transactable> filtered =
                    reportService.filterTransactions(
                            transactions,
                            type,
                            category,
                            start,
                            end,
                            description
                    );

            refreshTable(filtered);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid date. Use YYYY-MM-DD, for example 2026-10-21."
            );
        }
    }

    private LocalDate parseDate(String value) {
        value = value.trim();

        if (value.isEmpty()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private void resetFilters() {
        typeFilter.setSelectedItem("All");
        categoryField.setText("");
        startDateField.setText("");
        endDateField.setText("");
        searchField.setText("");

        refreshTable(transactions);
    }

    private void showCategoryReport() {
        Map<String, Double> totals =
                reportService.expensesByCategory(transactions);

        StringBuilder result =
                new StringBuilder("EXPENSES BY CATEGORY\n\n");

        if (totals.isEmpty()) {
            result.append("No expense records available.");
        } else {
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                result.append(entry.getKey())
                        .append(": ₹")
                        .append(String.format("%.2f", entry.getValue()))
                        .append("\n");
            }
        }

        reportArea.setText(result.toString());
    }

    private void showMonthlyReport() {
        String input = JOptionPane.showInputDialog(
                this, "Enter month (YYYY-MM):"
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            YearMonth month = YearMonth.parse(input.trim());

            ReportService.Summary summary =
                    reportService.monthlyReport(transactions, month);

            reportArea.setText(
                    "MONTHLY REPORT: " + month + "\n\n"
                    + "Income: ₹" + String.format(
                            "%.2f", summary.getTotalIncome()) + "\n"
                    + "Expenses: ₹" + String.format(
                            "%.2f", summary.getTotalExpenses()) + "\n"
                    + "Balance: ₹" + String.format(
                            "%.2f", summary.getBalance())
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter the month in YYYY-MM format."
            );
        }
    }

    private void showYearlyReport() {
        String input = JOptionPane.showInputDialog(
                this, "Enter year (e.g. 2026):"
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            int year = Integer.parseInt(input.trim());

            ReportService.Summary summary =
                    reportService.yearlyReport(transactions, year);

            reportArea.setText(
                    "YEARLY REPORT: " + year + "\n\n"
                    + "Income: ₹" + String.format(
                            "%.2f", summary.getTotalIncome()) + "\n"
                    + "Expenses: ₹" + String.format(
                            "%.2f", summary.getTotalExpenses()) + "\n"
                    + "Balance: ₹" + String.format(
                            "%.2f", summary.getBalance())
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid year."
            );
        }
    }

    private void showCustomReport() {
        try {
            LocalDate start =
                    LocalDate.parse(startDateField.getText().trim());

            LocalDate end =
                    LocalDate.parse(endDateField.getText().trim());

            if (start.isAfter(end)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Start date cannot be after end date."
                );
                return;
            }

            ReportService.Summary summary =
                    reportService.customDateRangeReport(
                            transactions, start, end
                    );

            reportArea.setText(
                    "CUSTOM DATE RANGE REPORT\n"
                    + start + " to " + end + "\n\n"
                    + "Income: ₹" + String.format(
                            "%.2f", summary.getTotalIncome()) + "\n"
                    + "Expenses: ₹" + String.format(
                            "%.2f", summary.getTotalExpenses()) + "\n"
                    + "Balance: ₹" + String.format(
                            "%.2f", summary.getBalance())
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid start and end dates in YYYY-MM-DD format."
            );
        }
    }
}
