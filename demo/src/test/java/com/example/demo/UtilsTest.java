package com.example.demo;

import javafx.scene.control.Alert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class UtilsTest {
    private Utils utils;
    private static Connection connection;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        utils = new Utils();

        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost/inventory_management";
        String user = "s256945";
        String password = "@RootUser1";
        connection = DriverManager.getConnection(url, user, password);
    }

    @After
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void getCurrentQuantity() {
        String description = "test";
        utils.getCurrentQuantity(connection, description);
    }

    @Test
    public void generateItemId() {
        utils.generateItemId(connection);
    }

    @Test
    public void getItemId() {
        String description = "test";
        utils.getItemId(connection, description);
    }

    @Test
    public void logTransaction() {
        int itemId = 00001;
        String transactionType = "ADD";
        int quantityChanged = 10;
        int stockRemaining = 10;
        double amount = 100.00;
        java.sql.Timestamp transactionDate = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
        utils.logTransaction(connection, itemId, transactionType, quantityChanged, stockRemaining, amount, transactionDate);
    }

    @Test
    public void showAlertTest() {
        String title = "Error";
        String content = "Failed to get current quantity";
        Alert.AlertType alertType = Alert.AlertType.ERROR;

        utils.showAlert(title, content, alertType);
    }
}
