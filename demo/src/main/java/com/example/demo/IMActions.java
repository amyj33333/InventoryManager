package com.example.demo;

import javafx.scene.control.Alert;

import java.sql.*;

public class IMActions {
    // Add a new item to the database
    static void addNewItem(Connection connection, String description, double unitPrice, int quantity, double totalValue) {
        int itemId = Utils.generateItemId(connection);
        // Insert the item into the database
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO items (itemId, description, unitPrice, quantity, totalValue) VALUES (?, ?, ?, ?, ?)")) {
            statement.setInt(1, itemId);
            statement.setString(2, description);
            statement.setDouble(3, unitPrice);
            statement.setInt(4, quantity);
            statement.setDouble(5, totalValue);
            statement.executeUpdate();
            // Log the transaction
            Utils.logTransaction(connection, itemId, description, "ADD", quantity, quantity, totalValue, Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to add item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Update the quantity of an item in the database
    static void updateItemQuantity(Connection connection, String description, int newQuantity) {
        int oldQuantity = Utils.getCurrentQuantity(connection, description);

        // Select the item from the database
        try (PreparedStatement statement = connection.prepareStatement("UPDATE items SET quantity = ? WHERE description = ?")) {
            // Update the item in the database
            statement.setInt(1, newQuantity);
            statement.setString(2, description);
            statement.executeUpdate();

            // Fetch the unit price
            double unitPrice;
            try (PreparedStatement selectStatement = connection.prepareStatement("SELECT unitPrice FROM items WHERE description = ?")) {
                selectStatement.setString(1, description);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        unitPrice = resultSet.getDouble("unitPrice");
                    } else {
                        unitPrice = 0.0;
                    }
                }
            }

            int quantityChange = Math.abs(oldQuantity - newQuantity);
            double transactionAmount = quantityChange * unitPrice;

            // Log the transaction
            Utils.logTransaction(connection, Utils.getItemId(connection, description), description, "UPDATE", quantityChange, newQuantity, transactionAmount, Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to update item quantity: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    // Remove an item from the database
    static int removeItem(Connection connection, String description) {
        int removedQuantity;

        // Select the item from the database to check if it exists
        try (PreparedStatement selectStatement = connection.prepareStatement("SELECT quantity FROM items WHERE description = ?")) {
            selectStatement.setString(1, description);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Item exists in the database, get the quantity to be removed for logging
                    removedQuantity = resultSet.getInt("quantity");
                    
                } else {
                    return 0; // Item not found, return 0
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        // Delete the item from the database
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM items WHERE description = ?")) {
            deleteStatement.setString(1, description);
            int rowsAffected = deleteStatement.executeUpdate();

            // Check if the deletion was successful before logging the transaction
            if (rowsAffected > 0) {
                // Log the transaction
                Utils.logTransaction(connection, Utils.getItemId(connection, description), description, "DELETE", -removedQuantity, 0, 0.00, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }

            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


    // search for items by description in the database and display the results
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
                Utils.showAlert("Item Not Found", "No item found with the given description.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to search for item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
