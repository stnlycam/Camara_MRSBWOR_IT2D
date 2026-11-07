package com.mrsbwors.ui;

import com.mrsbwors.dao.ActivityLogDao;
import com.mrsbwors.dao.UserDao;
import com.mrsbwors.model.ActivityLog;
import com.mrsbwors.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("       ADMIN DASHBOARD       ");
            System.out.println("=================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Accounts");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "5" -> updateUser(admin);
                case "7" -> viewAuditLogsMenu();
                case "8" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n=============== " + title + " ================");
        if(users.isEmpty()){
            System.out.println("-> No Records Found");
            return;
        }

        System.out.println("+-----+-----------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+-----+-----------------+------------+------------+");
        for(User u : users){
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+-----+-----------------+------------+------------+");
    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");
    }

    private void viewPendingUsers(){
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void approveUser(User admin){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()){
            return;
        }

        int userId = readIntInput("Enter User ID to Approve (or 0 to cancel): ");
        if (userId == 0){
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
        if(!exists){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        userDao.approveUser(admin.id(), userId);
    }

    private void updateUser(User currentAdmin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if(users.isEmpty()){
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if(userId == 0){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if(userId == currentAdmin.id()){
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
        }

        String newRole = readValidRole();
        if(newRole == null){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if(newStatus == null){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        userDao.updateUserAndStatus(currentAdmin.id(), userId, newRole, newStatus);
    }

    private void viewAuditLogsMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("      SYSTEM AUDIT LOGS      ");
            System.out.println("==================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayAuditLogsTable(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case "2" -> {
                    int targetUserId = readIntInput("Enter User ID to filter logs: ");
                    displayAuditLogsTable(logDao.getLogsByUserId(targetUserId), "AUDIT LOGS FOR USER ID: " + targetUserId);
                }
                case "3" -> { return; }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void displayAuditLogsTable(List<ActivityLog> logs, String title) {
        System.out.println("\n==================================================================================================");
        System.out.println(" " + title);
        System.out.println("=================================================================================================");
        if (logs.isEmpty()) {
            System.out.println("-> No Audit Logs Found.");
            return;
        }

        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
        System.out.printf("| %-5s | %-8s | %-18s | %-35s | %-20s |\n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
        for (ActivityLog log : logs) {
            System.out.printf("| %-5s | %-8s | %-18s | %-35s | %-20s |\n",
                    log.id(), log.userId(), log.action_type(), log.description(), log.timestamp());
        }
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
    }

    private String readValidRole(){
        while(true){
            System.out.print("Enter Role (CUSTOMER/MECHANIC/ADMIN) [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if(role.equals("0") || role.equals("CANCEL")){
                return null;
            }
            if(role.equals("CUSTOMER") || role.equals("MECHANIC") || role.equals("ADMIN")){
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("-> [ERROR] Invalid status. Must be APPROVED or PENDING.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("-> [ERROR] Please enter a valid numeric value.");
            }
        }
    }
}