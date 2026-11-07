package com.mrsbwors.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        createTables();
        new UserDao();
        System.out.println("-> Database table verified");
    }

    private static void createTables() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "roles TEXT NOT NULL, " +
                "status TEXT NOT NULL)";

        String createLogTable = "CREATE TABLE IF NOT EXISTS activity_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "action_type TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";

        String createMotorcyclesTable = "CREATE TABLE IF NOT EXISTS motorcycles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "plate_number TEXT UNIQUE NOT NULL, " +
                "FOREIGN KEY(customer_id) REFERENCES users(id) ON DELETE CASCADE)";

        String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "motorcycle_id INTEGER NOT NULL, " +
                "booking_date TEXT NOT NULL, " +
                "service_type TEXT NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'PENDING', " +
                "FOREIGN KEY(customer_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(motorcycle_id) REFERENCES motorcycles(id) ON DELETE CASCADE)";

        String createWorkOrdersTable = "CREATE TABLE IF NOT EXISTS repair_work_orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER NOT NULL, " +
                "mechanic_id INTEGER, " +
                "repair_details TEXT NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'PENDING', " +
                "completion_date TEXT, " +
                "FOREIGN KEY(booking_id) REFERENCES bookings(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(mechanic_id) REFERENCES users(id) ON DELETE SET NULL)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createLogTable);
            stmt.execute(createMotorcyclesTable);
            stmt.execute(createBookingsTable);
            stmt.execute(createWorkOrdersTable);
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
        }
    }
}