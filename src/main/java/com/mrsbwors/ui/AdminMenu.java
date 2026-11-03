package com.mrsbwors.ui;



import com.mrsbwors.dao.DatabaseConnection;
import com.mrsbwors.dao.UserDao;
import com.mrsbwors.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }


    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("     ADMIN DASHBOARD     ");
            System.out.println("===================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" ->viewAllUser();
                case "2" ->viewPendingUsers();
                case "3" ->approveUser();
                case "5" ->updateUser(admin);
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }

        }
    }
    private void displayUserTable(List<User> users, String title){
        System.out.println("\n==================== " +title+ " ====================");
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
    private void approveUser(){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()){
            return;
        }


        System.out.print("Enter User ID to Approve (or 0 to cancel):  ");
        int userId = scanner.nextInt();



        if (userId == 0){
            System.out.println(" -> Operation Cancelled by the user. ");
        }
        boolean exits = pendingUsers.stream().anyMatch(u ->u.id() == userId);
        if(!exits){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        userDao.approveUser(userId);
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
            System.out.println(" ->Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if(newStatus == null){
            System.out.println(" ->Operation Cancelled.");
            return;
        }


        userDao.updateUserAndStatus(userId, newRole, newStatus);
    }

    private String readValidRole(){
        while(true){
            System.out.print("Enter Role (STUDENT/TEACHER/ADMIN [0 to cancel]: )");
            String role = scanner.nextLine().trim().toUpperCase();
            if(role.equals("0") || role.equals("CANCEL")){
                return null;
            }

            if(role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN")){
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("-> [ERROR] Please enter a valid numeric ID.");
            }
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



}