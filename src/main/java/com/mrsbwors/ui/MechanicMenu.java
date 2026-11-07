package com.mrsbwors.ui;

import com.mrsbwors.dao.ServiceDao;
import com.mrsbwors.model.RepairWorkOrder;
import com.mrsbwors.model.User;

import java.util.List;
import java.util.Scanner;

public class MechanicMenu {
    private final ServiceDao serviceDao;
    private final Scanner scanner;

    public MechanicMenu(Scanner scanner) {
        this.serviceDao = new ServiceDao();
        this.scanner = scanner;
    }

    public void display(User mechanic) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("      MECHANIC DASHBOARD       ");
            System.out.println("=================================");
            System.out.println("1. View Available / Assigned Work Orders");
            System.out.println("2. Update Work Order Status");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewWorkOrders(mechanic);
                case "2" -> updateStatus(mechanic);
                case "3" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void viewWorkOrders(User mechanic) {
        List<RepairWorkOrder> orders = serviceDao.getAvailableOrAssignedWorkOrders(mechanic.id());
        System.out.println("\n=============== WORK ORDERS ================");
        if (orders.isEmpty()) {
            System.out.println("-> No Work Orders Found");
            return;
        }
        for (RepairWorkOrder order : orders) {
            System.out.println(order);
        }
    }

    private void updateStatus(User mechanic) {
        viewWorkOrders(mechanic);
        System.out.print("Enter Work Order ID to update: ");
        int orderId;
        try {
            orderId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("-> [ERROR] Invalid numeric value.");
            return;
        }

        System.out.print("Enter New Status (IN_PROGRESS / COMPLETED): ");
        String status = scanner.nextLine().trim().toUpperCase();
        if (!status.equals("IN_PROGRESS") && !status.equals("COMPLETED")) {
            System.out.println("-> [ERROR] Invalid Status.");
            return;
        }

        serviceDao.updateWorkOrderStatus(mechanic.id(), orderId, status);
    }
}