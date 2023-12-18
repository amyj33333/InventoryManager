package com.example.demo;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class IMControllerTest {
    private IMController imController;
    private Connection connection;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        imController = new IMController();

        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost/inventory_management";
        String user = "s256945";
        String password = "@RootUser1";
        connection = DriverManager.getConnection(url, user, password);
    }

    @Test
    public void addItemGui() {
    }

    @Test
    public void updateQuantiyGui() {
    }

    @Test
    public void removeItemGui() {
    }

    @Test
    public void searchItemGui() {
    }

    @Test
    public void fetchDailyTransactionsGui() {
    }


}