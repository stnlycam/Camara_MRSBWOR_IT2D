package com.mrsbwors.ui;

import com.mrsbwors.dao.ActivityLogDao;
import com.mrsbwors.dao.UserDao;
import com.mrsbwors.model.User;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.sc = sc;
    }

    public void start(){
        boolean running = true;
        while (running) {
            System.out.println("\n==========================================================");
            System.out.println(" Motorcycle Repair Service Booking and Work Order System ");
            System.out.println("==========================================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch(choice){
                case "1" -> handleLogin();
                case "2" -> handleRegistration();
                case "3" -> {
                    System.out.println("Exiting System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option! Please try again.");
            }
        }
    }

    private void handleRegistration() {
        System.out.print("Enter new username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        System.out.println("Select role [1] CUSTODIAN | [2] ADMIN: ");
        String roleChoice = sc.nextLine().trim();
        String role = switch (roleChoice) {
            case "1" -> "CUSTODIAN";
            case "2" -> "ADMIN";
            default -> null;
        };

        if(role != null){
            userDao.registerUser(user, pass, role);
        } else {
            System.out.println("Invalid role Selection!");
        }
    }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);

        if(user != null){

            logDao.logAction(user.id(), "USER_LOGIN", "User " + user.username() + " (ID: " + user.id() + ") logged in.");

            System.out.println("\n -> Welcome "+ user.username()+" | ROLE : " +user.roles()+ "");
            switch(user.roles()){
                case "ADMIN" -> {
                    new AdminMenu(userDao, sc).display(user);

                    logDao.logAction(user.id(), "USER_LOGOUT", "User " + user.username() + " (ID: " + user.id() + ") logged out.");
                }
                default -> System.out.println("Account roles not found!");
            }
        }
    }
}