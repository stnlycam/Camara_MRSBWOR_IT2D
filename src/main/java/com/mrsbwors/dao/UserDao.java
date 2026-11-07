package com.mrsbwors.dao;

import com.mrsbwors.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE roles = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, password, roles, status) VALUES('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created (Username: admin | Password: admin123)");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("-> Successful Registration, Waiting for Approval!");
            return true;

        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserByAdmin(int adminId, String username, String password, String role, String status) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.setString(4, status.toUpperCase());
            pstmt.executeUpdate();

            System.out.println("-> User '" + username + "' created successfully!");
            logDao.logAction(adminId, "USER_CREATED", "Admin ID " + adminId + " created user '" + username + "' with role " + role.toUpperCase());
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int adminId, int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("-> User ID:" + userId + " deleted successfully!");
                logDao.logAction(adminId, "USER_DELETED", "Admin ID " + adminId + " deleted User ID " + userId);
                return true;
            } else {
                System.out.println("-> User ID not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("Account is pending, waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );
                } else {
                    System.out.println("--> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUser() {
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }

        return usersList;
    }

    public List<User> getPendingUser() {
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pendingList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        return pendingList;
    }

    public boolean approveUser(int adminId, int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("-> User ID:" + userId + " approved successfully!");
                logDao.logAction(adminId, "USER_APPROVED", "Admin ID " + adminId + " approved account for User ID " + userId);
                return true;
            } else {
                System.out.println("-> ID not found");
            }

        } catch (Exception e) {
            System.err.println("Error approving users: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserAndStatus(int adminId, int userId, String newRoles, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRoles.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(" -> User ID: " + userId + " updated successfully!");
                logDao.logAction(adminId, "USER_UPDATED", "Admin ID " + adminId + " updated User ID " + userId + " to Role: " + newRoles + ", Status: " + newStatus);
                return true;
            } else {
                System.out.println(" -> User ID not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    public boolean logRepairBooking(int userId, String serviceDetails) {
        return logDao.logAction(userId, "REPAIR_BOOKED", "User ID " + userId + " booked repair service: " + serviceDetails);
    }

    public boolean updateWorkOrderStatus(int userId, int workOrderId, String oldStatus, String newStatus) {
        return logDao.logAction(userId, "STATUS_UPDATED", "Updated Ticket #" + workOrderId + " status from " + oldStatus + " to " + newStatus);
    }
}