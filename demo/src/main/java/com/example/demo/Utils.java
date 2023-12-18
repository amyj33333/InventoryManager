package com.example.demo;

import javafx.scene.control.Alert;

import java.sql.*;

import static java.lang.Integer.parseInt;

public class Utils {
    // Retrieve the current quantity of an item from the database
    static int getCurrentQuantity(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT quantity FROM items WHERE description = ?")) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to get current quantity: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return 0; // Default to 0 if there's an error
    }

    // Generate a new item ID
    public static int generateItemId(Connection connection) {
        int itemId = 0;
        // Get the current maximum itemId from the database
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT MAX(itemId) FROM items")) {
            if (resultSet.next()) {
                // Increment the maximum itemId by 1
                itemId = resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to generate item ID: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Format the itemId as a 5-digit string with leading zeros
        return parseInt(String.format("%05d", itemId));
    }


    // Retrieve the item ID of an item from the database
    static int getItemId(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT itemId FROM items WHERE description = ?")) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("itemId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to get item ID: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return 0; // Default to 0 if there's an error
    }

    // Log a transaction to the database
    static void logTransaction(Connection connection, int itemId, String transactionType, int quantityChanged, int stockRemaining, double amount, Timestamp transactionDate) {
        // Insert a new row into the transactions table
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO transactions (itemId, transactionType, quantityChanged, stockRemaining, amount, transactionDate) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, itemId);
            statement.setString(2, transactionType);
            statement.setInt(3, quantityChanged);
            statement.setInt(4, stockRemaining);
            statement.setDouble(5, amount);
            statement.setTimestamp(6, transactionDate);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to log transaction: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Show an alert dialog
    static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
