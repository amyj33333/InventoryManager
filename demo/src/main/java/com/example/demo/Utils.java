package com.example.demo;

import javafx.scene.control.Alert;

import java.sql.*;

public class Utils {
    static int getCurrentQuantity(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT quantity FROM items WHERE description = ?")) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to get current quantity: " + e.getMessage());
        }
        return 0; // Default to 0 if there's an error
    }

    static int generateItemId(Connection connection) {
        int itemId = 0;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT MAX(itemId) FROM items")) {
            if (resultSet.next()) {
                itemId = resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to generate item ID: " + e.getMessage());
        }
        return itemId;
    }

    static int getItemId(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT itemId FROM items WHERE description = ?")) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("itemId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to get item ID: " + e.getMessage());
        }
        return 0;
    }

    static void logTransaction(Connection connection, int itemId, String transactionType, int quantityChanged, int stockRemaining, double amount, Timestamp transactionDate) {
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
            Utils.showAlert("Error", "Failed to log transaction: " + e.getMessage());
        }
    }

    static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    static void showSuccess(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
