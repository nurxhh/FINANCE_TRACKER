package model;
import model.Transactable;
import model.Income;
import model.Expense;
import model.DBconnection;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    public static class Summary {
        private final double totalIncome;
        private final double totalExpenses;

        public Summary(double totalIncome, double totalExpenses) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
        }

        public double getTotalIncome() {
            return totalIncome;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public double getBalance() {
            return totalIncome - totalExpenses;
        }
    }

    public List<Transactable> loadTransactionsFromDatabase()
            throws SQLException {

        List<Transactable> transactions = new ArrayList<>();

        String incomeSql =
                "SELECT id, amount, transaction_date, category, description FROM Income";
        String expenseSql =
                "SELECT id, amount, transaction_date, category, description FROM Expense";

        try (Connection con = DBconnection.getConnection();
             Statement stmt = con.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(incomeSql)) {
                while (rs.next()) {
                    transactions.add(new Income(
                            rs.getInt("id"),
                            rs.getDouble("amount"),
                            LocalDate.parse(rs.getString("transaction_date")),
                            rs.getString("category"),
                            rs.getString("description")
                    ));
                }
            }

            try (ResultSet rs = stmt.executeQuery(expenseSql)) {
                while (rs.next()) {
                    transactions.add(new Expense(
                            rs.getInt("id"),
                            rs.getDouble("amount"),
                            LocalDate.parse(rs.getString("transaction_date")),
                            rs.getString("category"),
                            rs.getString("description")
                    ));
                }
            }
        }

        return transactions;
    }

    public Summary calculateSummary(List<Transactable> transactions) {
        double income = 0;
        double expenses = 0;

        for (Transactable t : transactions) {
            if (t instanceof Income) {
                income += t.getAmount();
            } else if (t instanceof Expense) {
                expenses += t.getAmount();
            }
        }

        return new Summary(income, expenses);
    }

    public List<Transactable> filterTransactions(
            List<Transactable> transactions,
            String type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            String searchText) {

        List<Transactable> filtered = new ArrayList<>();

        for (Transactable t : transactions) {
            boolean matches = true;

            if ("Income".equalsIgnoreCase(type)
                    && !(t instanceof Income)) {
                matches = false;
            }

            if ("Expense".equalsIgnoreCase(type)
                    && !(t instanceof Expense)) {
                matches = false;
            }

            if (category != null && !category.trim().isEmpty()
                    && (t.getCategory() == null
                    || !category.trim().equalsIgnoreCase(t.getCategory()))) {
                matches = false;
            }

            if (startDate != null && t.getDate().isBefore(startDate)) {
                matches = false;
            }

            if (endDate != null && t.getDate().isAfter(endDate)) {
                matches = false;
            }

            if (searchText != null && !searchText.trim().isEmpty()
                    && (t.getDescription() == null
                    || !t.getDescription().toLowerCase()
                    .contains(searchText.trim().toLowerCase()))) {
                matches = false;
            }

            if (matches) {
                filtered.add(t);
            }
        }

        return filtered;
    }

    public Map<String, Double> expensesByCategory(
            List<Transactable> transactions) {

        Map<String, Double> totals = new LinkedHashMap<>();

        for (Transactable t : transactions) {
            if (t instanceof Expense) {
                String category = t.getCategory();

                if (category == null || category.trim().isEmpty()) {
                    category = "Uncategorized";
                }

                totals.put(category,
                        totals.getOrDefault(category, 0.0) + t.getAmount());
            }
        }

        return totals;
    }

    public Summary monthlyReport(
            List<Transactable> transactions, YearMonth month) {

        double income = 0;
        double expenses = 0;

        for (Transactable t : transactions) {
            if (YearMonth.from(t.getDate()).equals(month)) {
                if (t instanceof Income) {
                    income += t.getAmount();
                } else if (t instanceof Expense) {
                    expenses += t.getAmount();
                }
            }
        }

        return new Summary(income, expenses);
    }

    public Summary yearlyReport(
            List<Transactable> transactions, int year) {

        double income = 0;
        double expenses = 0;

        for (Transactable t : transactions) {
            if (t.getDate().getYear() == year) {
                if (t instanceof Income) {
                    income += t.getAmount();
                } else if (t instanceof Expense) {
                    expenses += t.getAmount();
                }
            }
        }

        return new Summary(income, expenses);
    }

    public Summary customDateRangeReport(
            List<Transactable> transactions,
            LocalDate startDate,
            LocalDate endDate) {

        double income = 0;
        double expenses = 0;

        for (Transactable t : transactions) {
            LocalDate date = t.getDate();

            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                if (t instanceof Income) {
                    income += t.getAmount();
                } else if (t instanceof Expense) {
                    expenses += t.getAmount();
                }
            }
        }

        return new Summary(income, expenses);
    }
}
