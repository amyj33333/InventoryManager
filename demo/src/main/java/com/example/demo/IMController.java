package com.example.demo;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IMController {
    @FXML
    public void showAddItemDialog(Connection connection) {
        Dialog<Pair<String, Double>> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Enter Item Details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField description = new TextField();
        TextField unitPrice = new TextField();
        TextField quantity = new TextField();

        // Set up real-time input validation
        description.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9]*")) {
                description.setText(oldValue);
            }
        });

        unitPrice.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                unitPrice.setText(oldValue);
            }
        });

        quantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantity.setText(oldValue);
            }
        });

        grid.add(new Label("Description:"), 0, 0);
        grid.add(description, 1, 0);
        grid.add(new Label("Unit Price: £"), 0, 1);
        grid.add(unitPrice, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantity, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    // Validate inputs
                    Utils.validateInputs(description.getText(), unitPrice.getText(), quantity.getText());

                    return new Pair<>(description.getText(), Double.parseDouble(unitPrice.getText()));
                } catch (NumberFormatException e) {
                    Utils.showAlert("Error", "Invalid input for unit price or quantity.");
                } catch (IllegalArgumentException e) {
                    Utils.showAlert("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            InventoryManager.addNewItem(connection, result.getKey(), result.getValue(), Integer.parseInt(quantity.getText()), result.getValue() * Integer.parseInt(quantity.getText()));
            InventoryManager.fetchDailyTransactions(connection);
        });
    }


    @FXML
    public void showUpdateQuantityDialog(Connection connection) {
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Update Quantity");
        dialog.setHeaderText("Enter Item description");

        ButtonType searchButton = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField description = new TextField();

        grid.add(new Label("Item description:"), 0, 0);
        grid.add(description, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButton) {
                String itemDescription = description.getText();
                int currentQty = Utils.getCurrentQuantity(connection, itemDescription);
                if (currentQty >= 0) {
                    // Item found, now prompt for the new quantity
                    Dialog<Integer> updateDialog = new Dialog<>();
                    updateDialog.setTitle("Update Quantity");
                    Label currentQuantityLabel = new Label("Current quantity:");
                    Label currentQuantityValue = new Label(String.valueOf(currentQty));
                    updateDialog.setHeaderText("Enter the new quantity");

                    ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
                    updateDialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

                    GridPane updateGrid = new GridPane();
                    updateGrid.setHgap(10);
                    updateGrid.setVgap(10);
                    updateGrid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

                    TextField newQuantity = new TextField();
                    updateGrid.add(currentQuantityLabel, 0, 0);
                    updateGrid.add(currentQuantityValue, 1, 0);
                    updateGrid.add(new Label("New quantity:"), 0, 1);
                    updateGrid.add(newQuantity, 1, 1);

                    updateDialog.getDialogPane().setContent(updateGrid);

                    updateDialog.setResultConverter(updateDialogButton -> {
                        if (updateDialogButton == updateButton) {
                            try {
                                int updatedQty = Integer.parseInt(newQuantity.getText());
                                return updatedQty;
                            } catch (NumberFormatException e) {
                                Utils.showAlert("Error", "Invalid input for new quantity.");
                            }
                        }
                        return null;
                    });

                    Optional<Integer> newQtyResult = updateDialog.showAndWait();
                    return newQtyResult.map(newQty -> new Pair<>(itemDescription, newQty)).orElse(null);
                } else {
                    Utils.showAlert("Item Not Found", "No item found with the given description.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            InventoryManager.updateItemQuantity(connection, result.getKey(), result.getValue());
            InventoryManager.fetchDailyTransactions(connection);
        });
    }


    @FXML
    public void showRemoveItemDialog(Connection connection) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Remove Item");
        dialog.setHeaderText("Enter Item description to Remove");

        ButtonType removeButton = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(removeButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField description = new TextField();

        grid.add(new Label("Item description:"), 0, 0);
        grid.add(description, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == removeButton) {
                return description.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            InventoryManager.removeItem(connection, result);
            InventoryManager.fetchDailyTransactions(connection);
        });
    }

    @FXML
    public void showSearchItemDialog(Connection connection) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Search for Item");
        dialog.setHeaderText("Enter Item description to Search");

        ButtonType searchButton = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField description = new TextField();

        grid.add(new Label("Item description:"), 0, 0);
        grid.add(description, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButton) {
                return description.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> InventoryManager.searchForItem(connection, result));
    }

    @FXML
    public void showDailyTransactionReportDialog(Connection connection) {
        // Create a new Stage
        Stage stage = new Stage();
        stage.setTitle("Daily Transactions Report");

        // Create a TableView
        TableView<Map<String, Object>> tableView = new TableView<>();

        // Create TableColumn instances for each field in the ResultSet
        TableColumn<Map<String, Object>, Integer> itemIdColumn = new TableColumn<>("Item ID");
        itemIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("itemId")));

        TableColumn<Map<String, Object>, String> transactionTypeColumn = new TableColumn<>("Transaction Type");
        transactionTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue().get("transactionType")));

        TableColumn<Map<String, Object>, Integer> quantityChangedColumn = new TableColumn<>("Quantity Changed");
        quantityChangedColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("quantityChanged")));

        TableColumn<Map<String, Object>, Integer> stockRemainingColumn = new TableColumn<>("Stock Remaining");
        stockRemainingColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("stockRemaining")));

        TableColumn<Map<String, Object>, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("amount")));

        TableColumn<Map<String, Object>, Timestamp> transactionDateColumn = new TableColumn<>("Transaction Date");
        transactionDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Timestamp) data.getValue().get("transactionDate")));

        // Add columns to the TableView
        tableView.getColumns().addAll(itemIdColumn, transactionTypeColumn, quantityChangedColumn, stockRemainingColumn, amountColumn, transactionDateColumn);

        // Fetch and add daily transactions to the TableView using Map
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions WHERE transactionDate >= CURDATE()")) {
            while (resultSet.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("itemId", resultSet.getInt("itemId"));
                data.put("transactionType", resultSet.getString("transactionType"));
                data.put("transactionDate", resultSet.getTimestamp("transactionDate"));
                data.put("quantityChanged", resultSet.getInt("quantityChanged"));
                data.put("stockRemaining", resultSet.getInt("stockRemaining"));
                data.put("amount", resultSet.getDouble("amount"));

                // Add data to the TableView
                tableView.getItems().add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Utils.showAlert("Error", "Failed to fetch daily transactions: " + e.getMessage());
        }

        // Create a Scene with the TableView and set it to the Stage
        Scene scene = new Scene(new BorderPane(tableView), 800, 600);
        stage.setScene(scene);

        // Show the Stage
        stage.show();
    }
}