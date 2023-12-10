package com.example.demo;

import javafx.scene.control.Alert;

import java.sql.*;

public class IMActions {
    static void addNewItem(Connection connection, String description, double unitPrice, int quantity, double totalValue) {
        int itemId = Utils.generateItemId(connection);

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO items (itemId, description, unitPrice, quantity, totalValue) VALUES (?, ?, ?, ?, ?)")) {
            statement.setInt(1, itemId);
            statement.setString(2, description);
            statement.setDouble(3, unitPrice);
            statement.setInt(4, quantity);
            statement.setDouble(5, totalValue);
            statement.executeUpdate();
            Utils.logTransaction(connection, itemId, "ADD", quantity, quantity, totalValue, Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to add item: " + e.getMessage());
        }
    }

    static void updateItemQuantity(Connection connection, String description, int newQuantity) {
        int oldQuantity = Utils.getCurrentQuantity(connection, description);
        try (PreparedStatement statement = connection.prepareStatement("UPDATE items SET quantity = ? WHERE description = ?")) {
            statement.setInt(1, newQuantity);
            statement.setString(2, description);
            statement.executeUpdate();
            Utils.logTransaction(connection, Utils.getItemId(connection, description), "UPDATE", Math.abs(oldQuantity - newQuantity), newQuantity, 0, Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to update item quantity: " + e.getMessage());
        }
    }

    static void removeItem(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM items WHERE description = ?")) {
            statement.setString(1, description);
            statement.executeUpdate();
            Utils.logTransaction(connection, Utils.getItemId(connection, description), "DELETE", -Utils.getCurrentQuantity(connection, description), 0, 0, Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to remove item: " + e.getMessage());
        }
    }

    static void searchForItem(Connection connection, String description) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM items WHERE description = ?")) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double unitPrice = resultSet.getDouble("unitPrice");
                int quantity = resultSet.getInt("quantity");
                double totalValue = resultSet.getDouble("totalValue");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Item Information");
                alert.setHeaderText(null);
                alert.setContentText("Description: " + description + "\n" + "Unit Price: £" + unitPrice + "\n" + "Quantity: " + quantity + "\n" + "Total Value: £" + totalValue);
                alert.showAndWait();
            } else {
                Utils.showAlert("Item Not Found", "No item found with the given description.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to search for item: " + e.getMessage());
        }
    }

    static void fetchDailyTransactions(Connection connection) {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions WHERE transactionDate >= CURDATE()")) {
            while (resultSet.next()) {
                int itemId = resultSet.getInt("itemId");
                String transactionType = resultSet.getString("transactionType");
                Timestamp transactionDate = resultSet.getTimestamp("transactionDate");
                int quantityChanged = resultSet.getInt("quantityChanged");
                int stockRemaining = resultSet.getInt("stockRemaining");
                double amount = resultSet.getDouble("amount");

                // Process the transaction data as needed
                System.out.println("Item ID: " + itemId + ", Transaction Type: " + transactionType + ", Transaction Date: " + transactionDate + ", Quantity Changed: " + quantityChanged + ", Stock Remaining: " + stockRemaining + ", Amount: " + amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to fetch daily transactions: " + e.getMessage());
        }
    }
}
