package com.mrsbwors.ui;

import com.mrsbwors.dao.ServiceDao;
import com.mrsbwors.model.Booking;
import com.mrsbwors.model.Motorcycle;
import com.mrsbwors.model.User;

import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private final ServiceDao serviceDao;
    private final Scanner scanner;

    public CustomerMenu(Scanner scanner) {
        this.serviceDao = new ServiceDao();
        this.scanner = scanner;
    }

    public void display(User customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("      CUSTOMER DASHBOARD       ");
            System.out.println("=================================");
            System.out.println("1. Add Motorcycle");
            System.out.println("2. View My Motorcycles");
            System.out.println("3. Create Service Booking");
            System.out.println("4. View My Bookings");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addMotorcycle(customer);
                case "2" -> viewMotorcycles(customer);
                case "3" -> createBooking(customer);
                case "4" -> viewBookings(customer);
                case "5" -> cancelBooking(customer);
                case "6" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void addMotorcycle(User customer) {
        System.out.print("Enter Brand: ");
        String brand = scanner.nextLine().trim();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine().trim();
        System.out.print("Enter Plate Number: ");
        String plate = scanner.nextLine().trim();

        serviceDao.addMotorcycle(customer.id(), brand, model, plate);
    }

    private void viewMotorcycles(User customer) {
        List<Motorcycle> list = serviceDao.getMotorcyclesByCustomer(customer.id());
        System.out.println("\n=============== MY MOTORCYCLES ================");
        if (list.isEmpty()) {
            System.out.println("-> No Motorcycles Found");
            return;
        }
        for (Motorcycle m : list) {
            System.out.println(m);
        }
    }

    private void createBooking(User customer) {
        List<Motorcycle> motos = serviceDao.getMotorcyclesByCustomer(customer.id());
        if (motos.isEmpty()) {
            System.out.println("-> Please add a motorcycle first before booking.");
            return;
        }

        viewMotorcycles(customer);
        System.out.print("Enter Motorcycle ID: ");
        int motoId;
        try {
            motoId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("-> [ERROR] Invalid numeric value.");
            return;
        }

        System.out.print("Enter Booking Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();
        System.out.print("Enter Service Type: ");
        String serviceType = scanner.nextLine().trim();

        serviceDao.createBooking(customer.id(), motoId, date, serviceType);
    }

    private void viewBookings(User customer) {
        List<Booking> bookings = serviceDao.getBookingsByCustomer(customer.id());
        System.out.println("\n=============== MY BOOKINGS ================");
        if (bookings.isEmpty()) {
            System.out.println("-> No Bookings Found");
            return;
        }
        for (Booking b : bookings) {
            System.out.println(b);
        }
    }

    private void cancelBooking(User customer) {
        viewBookings(customer);
        System.out.print("Enter Booking ID to cancel (or 0 to cancel): ");
        int bookingId;
        try {
            bookingId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("-> [ERROR] Invalid numeric value.");
            return;
        }
        if (bookingId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }
        serviceDao.cancelBooking(customer.id(), bookingId);
    }
}