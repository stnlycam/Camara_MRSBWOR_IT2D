package com.mrsbwors.dao;

public class DatabaseInitializer {
    public static void initialize(){
        new UserDao();
        System.out.println("-> Database table verified");
    }
}