/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBconnection {

    private static Connection connection;

    private DBconnection() {
    }

    public static Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {

            String url = "jdbc:sqlite:finance_tracker.db";

            connection = DriverManager.getConnection(url);

            createTables();

            System.out.println("Connected database: " + url);
        }

        return connection;
    }

    private static void createTables() throws SQLException {

        String incomeSQL = "CREATE TABLE IF NOT EXISTS Income ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "amount REAL NOT NULL, "
                + "category TEXT, "
                + "transaction_date TEXT, "
                + "description TEXT)";

        String expenseSQL = "CREATE TABLE IF NOT EXISTS Expense ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "amount REAL NOT NULL, "
                + "category TEXT, "
                + "transaction_date TEXT, "
                + "description TEXT)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(incomeSQL);
            stmt.execute(expenseSQL);
        }
    }
}
