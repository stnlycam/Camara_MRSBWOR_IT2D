package com.mrsbwors.dao;

import com.mrsbwors.model.Booking;
import com.mrsbwors.model.Motorcycle;
import com.mrsbwors.model.RepairWorkOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean addMotorcycle(int customerId, String brand, String model, String plateNumber) {
        String sql = "INSERT INTO motorcycles(customer_id, brand, model, plate_number) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            pstmt.setString(2, brand);
            pstmt.setString(3, model);
            pstmt.setString(4, plateNumber.toUpperCase());
            pstmt.executeUpdate();

            System.out.println("-> Motorcycle added successfully!");
            logDao.logAction(customerId, "MOTO_ADDED", "Customer ID " + customerId + " added motorcycle " + plateNumber.toUpperCase());
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding motorcycle: " + e.getMessage());
            return false;
        }
    }

    public List<Motorcycle> getMotorcyclesByCustomer(int customerId) {
        List<Motorcycle> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, brand, model, plate_number FROM motorcycles WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Motorcycle(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getString("plate_number")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching motorcycles: " + e.getMessage());
        }
        return list;
    }

    public boolean createBooking(int customerId, int motorcycleId, String bookingDate, String serviceType) {
        String sql = "INSERT INTO bookings(customer_id, motorcycle_id, booking_date, service_type, status) VALUES(?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, motorcycleId);
            pstmt.setString(3, bookingDate);
            pstmt.setString(4, serviceType);
            pstmt.executeUpdate();

            int bookingId = 0;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bookingId = keys.getInt(1);
                }
            }

            if (bookingId > 0) {
                createWorkOrderForBooking(bookingId, serviceType);
            }

            System.out.println("-> Booking created successfully!");
            logDao.logAction(customerId, "BOOKING_CREATED", "Customer ID " + customerId + " booked " + serviceType + " on " + bookingDate);
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
            return false;
        }
    }

    private void createWorkOrderForBooking(int bookingId, String serviceType) {
        String sql = "INSERT INTO repair_work_orders(booking_id, mechanic_id, repair_details, status, completion_date) VALUES(?, NULL, ?, 'PENDING', NULL)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            pstmt.setString(2, serviceType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating work order: " + e.getMessage());
        }
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, motorcycle_id, booking_date, service_type, status FROM bookings WHERE customer_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Booking(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("motorcycle_id"),
                            rs.getString("booking_date"),
                            rs.getString("service_type"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
        return list;
    }

    public boolean cancelBooking(int customerId, int bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ? AND customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, customerId);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                System.out.println("-> Booking #" + bookingId + " cancelled.");
                logDao.logAction(customerId, "BOOKING_CANCELLED", "Customer ID " + customerId + " cancelled Booking #" + bookingId);
                return true;
            } else {
                System.out.println("-> Booking not found or not owned by you.");
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
        }
        return false;
    }

    public List<RepairWorkOrder> getAvailableOrAssignedWorkOrders(int mechanicId) {
        List<RepairWorkOrder> list = new ArrayList<>();
        String sql = "SELECT id, booking_id, mechanic_id, repair_details, status, completion_date FROM repair_work_orders WHERE mechanic_id IS NULL OR mechanic_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mechanicId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int mechId = rs.getInt("mechanic_id");
                    Integer mechObj = rs.wasNull() ? null : mechId;
                    list.add(new RepairWorkOrder(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            mechObj,
                            rs.getString("repair_details"),
                            rs.getString("status"),
                            rs.getString("completion_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching work orders: " + e.getMessage());
        }
        return list;
    }

    public boolean updateWorkOrderStatus(int mechanicId, int workOrderId, String newStatus) {
        String selectSql = "SELECT booking_id, status FROM repair_work_orders WHERE id = ?";
        String updateSql = "UPDATE repair_work_orders SET mechanic_id = ?, status = ?, completion_date = CASE WHEN ? = 'COMPLETED' THEN datetime('now') ELSE completion_date END WHERE id = ?";
        String bookingSql = "UPDATE bookings SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selStmt = conn.prepareStatement(selectSql)) {

            selStmt.setInt(1, workOrderId);
            String oldStatus = "UNKNOWN";
            int bookingId = 0;

            try (ResultSet rs = selStmt.executeQuery()) {
                if (rs.next()) {
                    oldStatus = rs.getString("status");
                    bookingId = rs.getInt("booking_id");
                } else {
                    System.out.println("-> Work Order not found.");
                    return false;
                }
            }

            try (PreparedStatement updStmt = conn.prepareStatement(updateSql)) {
                updStmt.setInt(1, mechanicId);
                updStmt.setString(2, newStatus.toUpperCase());
                updStmt.setString(3, newStatus.toUpperCase());
                updStmt.setInt(4, workOrderId);
                updStmt.executeUpdate();
            }

            if (bookingId > 0) {
                try (PreparedStatement bStmt = conn.prepareStatement(bookingSql)) {
                    bStmt.setString(1, newStatus.toUpperCase());
                    bStmt.setInt(2, bookingId);
                    bStmt.executeUpdate();
                }
            }

            System.out.println("-> Work Order #" + workOrderId + " updated to " + newStatus.toUpperCase());
            logDao.logAction(mechanicId, "STATUS_UPDATED", "Mechanic ID " + mechanicId + " updated Work Order #" + workOrderId + " from " + oldStatus + " to " + newStatus.toUpperCase());
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating work order: " + e.getMessage());
            return false;
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, motorcycle_id, booking_date, service_type, status FROM bookings ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Booking(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("motorcycle_id"),
                        rs.getString("booking_date"),
                        rs.getString("service_type"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteBooking(int adminId, int bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            int deleted = pstmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("-> Booking #" + bookingId + " deleted successfully!");
                logDao.logAction(adminId, "BOOKING_DELETED", "Admin ID " + adminId + " deleted Booking #" + bookingId);
                return true;
            } else {
                System.out.println("-> Booking #" + bookingId + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
        }
        return false;
    }
}