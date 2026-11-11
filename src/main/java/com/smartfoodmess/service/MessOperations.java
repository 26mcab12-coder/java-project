package com.smartfoodmess.service;

import com.smartfoodmess.exception.AuthenticationException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.Booking;
import com.smartfoodmess.model.BookingRecord;
import com.smartfoodmess.model.MenuItem;
import com.smartfoodmess.model.MealRecord;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.model.User;

import java.util.List;
import java.util.Map;

/**
 * Contract of the business layer.
 *
 * <p>Every important operation of the mess is declared here once.
 * {@link MessService} is the (only) implementation, so servlets depend on
 * this interface - a classic OOP example of "program to an interface".
 * If we ever rewrite the logic, only the implementation changes.</p>
 */
public interface MessOperations {

    // ---- accounts -------------------------------------------------------

    /** Checks username/password in the database and returns the account. */
    User login(String username, String password)
            throws InvalidInputException, AuthenticationException;

    /** Creates a student account (users + students tables). */
    Student registerStudent(String name, String email, String phone,
                            String username, String password) throws InvalidInputException;

    // ---- student management (admin) -------------------------------------

    /** Updates a student profile; optional new password (blank = keep old). */
    Student updateStudent(long studentId, String name, String email,
                          String phone, String newPassword) throws InvalidInputException;

    /** Deletes a student together with their login account and records. */
    void deleteStudent(long studentId) throws InvalidInputException;

    List<Student> getAllStudents();

    /** Profile of a logged-in student (by username), or {@code null}. */
    Student getStudentByUsername(String username);

    // ---- menu management ------------------------------------------------

    List<MenuItem> getAllMenu();

    MenuItem addMenuItem(String foodName, String mealType,
                         String priceText, String dateText) throws InvalidInputException;

    MenuItem updateMenuItem(long menuId, String foodName, String mealType,
                            String priceText, String dateText) throws InvalidInputException;

    void deleteMenuItem(long menuId) throws InvalidInputException;

    // ---- bookings and meal records --------------------------------------

    /**
     * Books a menu item: calculates {@code total = price x quantity} and
     * stores both a booking row and a meal record.
     */
    Booking bookMeal(long studentId, long menuId, int quantity) throws InvalidInputException;

    List<MealRecord> getAllMealRecords();

    List<MealRecord> getMealRecordsOf(long studentId) throws InvalidInputException;

    /** Changes a meal status (and syncs the linked booking). */
    void updateMealStatus(long mealId, String status) throws InvalidInputException;

    /** Deletes a meal record (and its linked booking). */
    void deleteMealRecord(long mealId) throws InvalidInputException;

    List<BookingRecord> getAllBookings();

    List<BookingRecord> getBookingsOf(long studentId) throws InvalidInputException;

    // ---- dashboard ------------------------------------------------------

    /** Counts for the admin dashboard: students / menu / meals. */
    Map<String, Long> getDashboardCounts();
}
