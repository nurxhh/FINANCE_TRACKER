

package model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FinancetrackerGUI extends JFrame {

    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;

    public FinancetrackerGUI() {
        setTitle("Personal Finance Tracker");
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 30));
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(40, 50, 35, 50));

        JLabel title = new JLabel(
                "Personal Finance Tracker", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(BLACK);

        JLabel subtitle = new JLabel(
                "Manage your income and expenses",
                SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(BLACK);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 8));
        header.setBackground(WHITE);
        header.add(title);
        header.add(subtitle);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 20, 20));
        buttons.setBackground(WHITE);
        buttons.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JButton incomeButton = createButton("Add Income");
        JButton expenseButton = createButton("Add Expense");
        JButton viewButton = createButton("View Transactions");
        JButton reportButton = createButton("Reports");

        buttons.add(incomeButton);
        buttons.add(expenseButton);
        buttons.add(viewButton);
        buttons.add(reportButton);

        incomeButton.addActionListener(e -> showTransactionForm("Income"));
        expenseButton.addActionListener(e -> showTransactionForm("Expense"));
        viewButton.addActionListener(e -> viewTransactions());
        reportButton.addActionListener(e -> openReports());

        JLabel footer = new JLabel(
                "Personal Finance Management",
                SwingConstants.CENTER);
        footer.setForeground(BLACK);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(buttons, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        button.setForeground(BLACK);
        button.setBackground(WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        button.setFocusPainted(false);
        return button;
    }

    private void showTransactionForm(String type) {
        JDialog dialog = new JDialog(this, "Add " + type, true);
        dialog.setSize(430, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 8, 9, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField amountField = new JTextField(15);
        JTextField dateField = new JTextField(LocalDate.now().toString(), 15);
        JTextField descriptionField = new JTextField(15);

        String[] categories = type.equals("Income")
                ? new String[]{"Salary", "Other"}
                : new String[]{"Food", "Rent", "Travel", "Other"};

        JComboBox<String> categoryBox = new JComboBox<>(categories);
        categoryBox.setBackground(WHITE);
        categoryBox.setForeground(BLACK);

        addFormRow(form, gbc, 0, "Amount:", amountField);
        addFormRow(form, gbc, 1, "Date (YYYY-MM-DD):", dateField);
        addFormRow(form, gbc, 2, "Category:", categoryBox);
        addFormRow(form, gbc, 3, "Description:", descriptionField);

        JButton saveButton = createButton("Save " + type);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            Double amount = parseAmount(amountField.getText());

            if (amount == null) {
                showError(dialog, "Enter a valid amount greater than zero.");
                return;
            }

            String date = dateField.getText().trim();
            if (!isValidDate(date)) {
                showError(dialog, "Enter the date as YYYY-MM-DD.");
                return;
            }

            String category = (String) categoryBox.getSelectedItem();
            String description = descriptionField.getText().trim();

            String sql = "INSERT INTO " + type
                    + " (amount, category, transaction_date, description)"
                    + " VALUES (?, ?, ?, ?)";

            try (Connection con = DBconnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDouble(1, amount);
                ps.setString(2, category);
                ps.setString(3, date);
                ps.setString(4, description);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        dialog, type + " added successfully.");
                dialog.dispose();

            } catch (SQLException ex) {
                showError(dialog, "Could not save transaction:\n"
                        + ex.getMessage());
                ex.printStackTrace();
            }
        });

        dialog.add(form);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                            int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(BLACK);
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void viewTransactions() {
        String[] columns = {
            "ID", "Date", "Type", "Category", "Amount", "Description"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            loadTransactions(model);
        } catch (SQLException ex) {
            showError(this, "Could not load transactions:\n"
                    + ex.getMessage());
            return;
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setForeground(BLACK);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(230, 230, 230));
        table.setSelectionForeground(BLACK);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setBackground(WHITE);
        table.getTableHeader().setForeground(BLACK);
        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13));

        // Keep ID in the model for editing/deleting, but hide it from view.
        table.removeColumn(table.getColumnModel().getColumn(0));

        JButton editButton = createButton("Edit Selected");
        JButton deleteButton = createButton("Delete Selected");
        JButton refreshButton = createButton("Refresh");

        editButton.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                showError(table, "Select a transaction to edit.");
                return;
            }
            editTransaction(model, table.convertRowIndexToModel(selected));
        });

        deleteButton.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                showError(table, "Select a transaction to delete.");
                return;
            }
            deleteTransaction(model, table.convertRowIndexToModel(selected));
        });

        refreshButton.addActionListener(e -> {
            model.setRowCount(0);
            try {
                loadTransactions(model);
            } catch (SQLException ex) {
                showError(table, "Could not refresh:\n" + ex.getMessage());
            }
        });

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(WHITE);
        actionPanel.add(refreshButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Your Transactions");
        heading.setForeground(BLACK);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "View Transactions", true);
        dialog.setContentPane(panel);
        dialog.setSize(900, 480);
        dialog.setMinimumSize(new Dimension(700, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadTransactions(DefaultTableModel model)
            throws SQLException {
        String[] tables = {"Income", "Expense"};

        try (Connection con = DBconnection.getConnection()) {
            for (String type : tables) {
                String sql = "SELECT id, transaction_date, category, amount, "
                        + "description FROM " + type
                        + " ORDER BY transaction_date DESC";

                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("transaction_date"),
                            type,
                            rs.getString("category"),
                            rs.getDouble("amount"),
                            rs.getString("description")
                        });
                    }
                }
            }
        }
    }

    private void editTransaction(DefaultTableModel model, int row) {
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        String type = model.getValueAt(row, 2).toString();

        JDialog dialog = new JDialog(this, "Edit " + type, true);
        dialog.setSize(430, 350);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 8, 9, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField amountField = new JTextField(
                model.getValueAt(row, 4).toString(), 15);
        JTextField dateField = new JTextField(
                model.getValueAt(row, 1).toString(), 15);
        JTextField descriptionField = new JTextField(
                model.getValueAt(row, 5) == null ? ""
                        : model.getValueAt(row, 5).toString(), 15);

        String[] categories = type.equals("Income")
                ? new String[]{"Salary", "Other"}
                : new String[]{"Food", "Rent", "Travel", "Other"};

        JComboBox<String> categoryBox = new JComboBox<>(categories);
        String oldCategory = model.getValueAt(row, 3) == null ? ""
                : model.getValueAt(row, 3).toString();

        boolean categoryExists = false;
        for (String category : categories) {
            if (category.equals(oldCategory)) {
                categoryExists = true;
                break;
            }
        }
        if (!categoryExists && !oldCategory.isEmpty()) {
            categoryBox.addItem(oldCategory);
        }
        categoryBox.setSelectedItem(oldCategory);
        categoryBox.setBackground(WHITE);
        categoryBox.setForeground(BLACK);

        addFormRow(form, gbc, 0, "Amount:", amountField);
        addFormRow(form, gbc, 1, "Date (YYYY-MM-DD):", dateField);
        addFormRow(form, gbc, 2, "Category:", categoryBox);
        addFormRow(form, gbc, 3, "Description:", descriptionField);

        JButton updateButton = createButton("Save Changes");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(updateButton, gbc);

        updateButton.addActionListener(e -> {
            Double amount = parseAmount(amountField.getText());
            if (amount == null) {
                showError(dialog, "Enter a valid amount greater than zero.");
                return;
            }

            String date = dateField.getText().trim();
            if (!isValidDate(date)) {
                showError(dialog, "Enter the date as YYYY-MM-DD.");
                return;
            }

            String sql = "UPDATE " + type
                    + " SET amount=?, category=?, transaction_date=?, "
                    + "description=? WHERE id=?";

            try (Connection con = DBconnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDouble(1, amount);
                ps.setString(2, (String) categoryBox.getSelectedItem());
                ps.setString(3, date);
                ps.setString(4, descriptionField.getText().trim());
                ps.setInt(5, id);

                if (ps.executeUpdate() > 0) {
                    model.setValueAt(date, row, 1);
                    model.setValueAt(
                            categoryBox.getSelectedItem(), row, 3);
                    model.setValueAt(amount, row, 4);
                    model.setValueAt(
                            descriptionField.getText().trim(), row, 5);

                    JOptionPane.showMessageDialog(
                            dialog, "Transaction updated successfully.");
                    dialog.dispose();
                } else {
                    showError(dialog, "Transaction was not found.");
                }

            } catch (SQLException ex) {
                showError(dialog, "Could not update transaction:\n"
                        + ex.getMessage());
                ex.printStackTrace();
            }
        });

        dialog.add(form);
        dialog.setVisible(true);
    }

    private void deleteTransaction(DefaultTableModel model, int row) {
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        String type = model.getValueAt(row, 2).toString();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this "
                        + type.toLowerCase() + " transaction?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM " + type + " WHERE id=?";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                model.removeRow(row);
                JOptionPane.showMessageDialog(
                        this, "Transaction deleted successfully.");
            } else {
                showError(this, "Transaction was not found.");
            }

        } catch (SQLException ex) {
            showError(this, "Could not delete transaction:\n"
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openReports() {
        try {
            ReportService service = new ReportService();
            java.util.List<Transactable> transactions =
                    service.loadTransactionsFromDatabase();

            ReportFrame frame = new ReportFrame(transactions);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);

        } catch (Exception ex) {
            showError(this, "Could not open reports:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Double parseAmount(String text) {
        try {
            double amount = Double.parseDouble(text.trim());
            return amount > 0 && Double.isFinite(amount) ? amount : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent, message, "Finance Tracker",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FinancetrackerGUI().setVisible(true);
        });
    }
}
