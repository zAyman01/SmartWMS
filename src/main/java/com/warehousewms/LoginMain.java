package com.warehousewms;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.LoginResult;
import com.warehousewms.service.LoginService;

import java.io.Console;

public class LoginMain {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        java.io.Console console = System.console();

        System.out.println("=== Smart WMS Login Test ===");

        String username;
        String password;

        if (console != null) {
            username = console.readLine("Username: ");
            password = new String(console.readPassword("Password: "));
        } else {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("Username: ");
            username = scanner.nextLine();
            System.out.print("Password: ");
            password = scanner.nextLine();
            scanner.close();
        }

        try (LoginService loginService = new LoginService(dbManager.getDataSourceWithFallback())) {
            LoginResult result = loginService.login(username, password, false);

            if (result.isSuccess()) {
                System.out.println("Login successful!");
                System.out.println("  User ID : " + result.getUser().getUserId());
                System.out.println("  Username: " + result.getUser().getUsername());
                System.out.println("  Full Name: " + result.getUser().getFullName());
                System.out.println("  Role    : " + result.getUser().getRole());
            } else {
                System.out.println("Login failed: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
