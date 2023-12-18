package com.example.demo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class IMActionsTest {
    private IMActions imActions;
    private Connection connection;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        imActions = new IMActions();

        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost/inventory_management";
        String user = "s256945";
        String password = "@RootUser1";
        connection = DriverManager.getConnection(url, user, password);
    }

    @Test
    public void addNewItem() {
        String description = "test";
        double unitPrice = 12.5;
        int quantity = 10;
        double totalValue = 125.0;
        imActions.addNewItem(connection, description, unitPrice, quantity, totalValue);

        Assert.assertEquals(imActions.searchForItem(connection, description), true);
    }

    @Test
    public void updateItemQuantity() {
        String description = "test";
        int newQuantity = 20;
        imActions.updateItemQuantity(connection, description, newQuantity);

        Assert.assertEquals(imActions.searchForItem(connection, description), true);
    }

    @Test
    public void searchForItem() {
        String description = "test";
        Assert.assertEquals(imActions.searchForItem(connection, description), true);
    }

    @Test
    public void fetchDailyTransactions() {
        Assert.assertEquals(imActions.fetchDailyTransactions(connection), true);
    }

    @Test
    public void removeItem() {
        String description = "test";
        imActions.removeItem(connection, description);

        Assert.assertEquals(imActions.searchForItem(connection, description), false);
    }
}