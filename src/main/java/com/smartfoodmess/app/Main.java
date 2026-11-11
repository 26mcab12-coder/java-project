package com.smartfoodmess.app;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.MenuItem;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.service.MessOperations;
import com.smartfoodmess.service.MessService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Small CONSOLE demonstration of the business layer.
 *
 * <p>The main product of this project is the web application (servlets +
 * HTML). This class simply proves that the same {@link MessService} can be
 * reused from anywhere - a classic example of separating the business layer
 * from the user interface.</p>
 *
 * <p>Run (after the database is set up):</p>
 * <pre>
 *   mvn -q compile exec:java -Dexec.mainClass=com.smartfoodmess.app.Main
 * </pre>
 * or run the class directly from your IDE. All operations are read-only.
 */
public class Main {

    public static void main(String[] args) {
        MessOperations service = new MessService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==============================================");
        System.out.println(" Smart Food Mess - Console Demo (read-only)");
        System.out.println("==============================================");

        while (running) {
            System.out.println();
            System.out.println("1) Show today's food menu");
            System.out.println("2) Show all students");
            System.out.println("3) Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        printTodayMenu(service);
                        break;
                    case "2":
                        printStudents(service);
                        break;
                    case "3":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Please enter 1, 2 or 3.");
                }
            } catch (DatabaseException e) {
                // Friendly message only - technical SQL details are logged
                // inside DBConnection/DAOs for the developer.
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Is MySQL running? Check src/main/resources/db.properties.");
            }
        }
        scanner.close();
    }

    private static void printTodayMenu(MessOperations service) {
        LocalDate today = LocalDate.now();
        List<MenuItem> items = service.getAllMenu();
        boolean found = false;
        for (MenuItem item : items) {
            if (today.equals(item.getAvailableDate())) {
                found = true;
                System.out.printf("  [%-9s] %-30s Rs. %.2f%n",
                        item.getMealType(), item.getFoodName(), item.getPrice());
            }
        }
        if (!found) {
            System.out.println("  No menu items for today (" + today + ").");
        }
    }

    private static void printStudents(MessOperations service) {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("  No students registered yet.");
            return;
        }
        for (Student s : students) {
            System.out.printf("  #%d  %-25s %-30s %s%n",
                    s.getStudentId(), s.getName(), s.getEmail(), s.getPhone());
        }
    }
}
