package com.exampro.util;

import com.exampro.dao.UserDAO;
import com.exampro.model.User;

import java.io.Console;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * AdminProvisioner – Secure CLI Administrator Account Setup Tool
 * ═════════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Provides an offline, administrative command-line utility to provision
 *   the first Administrator account without exposing any public web endpoints
 *   or hardcoding passwords in source control.
 *
 * USAGE (From project root after building WAR):
 *   java -cp "target\ExamPro\WEB-INF\classes;target\ExamPro\WEB-INF\lib\*" com.exampro.util.AdminProvisioner
 *
 * OR via arguments:
 *   java -cp "..." com.exampro.util.AdminProvisioner admin@exampro.com "System Administrator" "SecurePassword123"
 */
public class AdminProvisioner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      ExamPro Administrator Provisioning Tool     ");
        System.out.println("==================================================");

        String email = null;
        String fullName = null;
        String password = null;

        if (args.length >= 3) {
            email = args[0].trim();
            fullName = args[1].trim();
            password = args[2];
        } else {
            Console console = System.console();
            Scanner scanner = (console == null) ? new Scanner(System.in) : null;

            // Prompt Email
            System.out.print("Enter Admin Email (e.g., admin@exampro.com): ");
            email = (console != null) ? console.readLine().trim() : scanner.nextLine().trim();

            // Prompt Full Name
            System.out.print("Enter Admin Full Name: ");
            fullName = (console != null) ? console.readLine().trim() : scanner.nextLine().trim();

            // Prompt Password securely (masked if console available)
            if (console != null) {
                char[] passArray = console.readPassword("Enter Admin Password (min 8 characters): ");
                password = new String(passArray);
            } else {
                System.out.print("Enter Admin Password (min 8 characters): ");
                password = scanner.nextLine();
            }
        }

        // Validate inputs
        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            System.err.println("[ERROR] Invalid email address format.");
            System.exit(1);
        }

        if (fullName == null || fullName.isBlank()) {
            System.err.println("[ERROR] Full name cannot be empty.");
            System.exit(1);
        }

        if (password == null || password.length() < 8) {
            System.err.println("[ERROR] Password must be at least 8 characters long.");
            System.exit(1);
        }

        try {
            UserDAO userDAO = new UserDAO();

            // Check if already registered
            if (userDAO.isEmailRegistered(email)) {
                System.err.println("[ERROR] An account with email [" + email + "] already exists.");
                System.exit(1);
            }

            System.out.println("Generating BCrypt password hash (Cost factor: 12)...");
            String passwordHash = PasswordUtil.hash(password);

            User admin = new User();
            admin.setEmail(email);
            admin.setFullName(fullName);
            admin.setPasswordHash(passwordHash);
            admin.setRole(User.ROLE_ADMIN);
            admin.setActive(true);

            boolean created = userDAO.createUser(admin);

            if (created) {
                System.out.println("\n[SUCCESS] Administrator account created successfully!");
                System.out.println("User ID   : " + admin.getId());
                System.out.println("Email     : " + admin.getEmail());
                System.out.println("Full Name : " + admin.getFullName());
                System.out.println("Role      : " + admin.getRole());
                System.out.println("Status    : Active");
                System.out.println("==================================================");
            } else {
                System.err.println("[FAILED] Could not insert admin record.");
                System.exit(1);
            }

        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage());
            System.exit(1);
        }
    }
}
