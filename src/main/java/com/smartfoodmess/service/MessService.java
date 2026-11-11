package com.smartfoodmess.service;

import com.smartfoodmess.dao.BookingDAO;
import com.smartfoodmess.dao.MealDAO;
import com.smartfoodmess.dao.MenuDAO;
import com.smartfoodmess.dao.StudentDAO;
import com.smartfoodmess.dao.UserDAO;
import com.smartfoodmess.exception.AuthenticationException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.Booking;
import com.smartfoodmess.model.BookingRecord;
import com.smartfoodmess.model.MenuItem;
import com.smartfoodmess.model.Meal;
import com.smartfoodmess.model.MealRecord;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.model.User;
import com.smartfoodmess.util.PasswordUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The business layer of the application (implements {@link MessOperations}).
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li><b>Validate</b> all input before it reaches the database
 *       ({@link InvalidInputException} with user friendly messages),</li>
 *   <li><b>Coordinate</b> several DAO calls (e.g. registration writes two
 *       tables, booking writes two tables),</li>
 *   <li>Apply simple <b>business rules</b> (no double bookings, meals can
 *       only be booked while they are available, ...).</li>
 * </ul>
 *
 * <p>Safety note: this class never builds SQL strings itself - all database
 * work happens in the DAOs with PreparedStatements.</p>
 */
public class MessService implements MessOperations {

    private final UserDAO userDao = new UserDAO();
    private final StudentDAO studentDao = new StudentDAO();
    private final MenuDAO menuDao = new MenuDAO();
    private final MealDAO mealDao = new MealDAO();
    private final BookingDAO bookingDao = new BookingDAO();

    // ====================================================================
    // Accounts
    // ====================================================================

    /**
     * Verifies username and password against the database.
     *
     * @throws InvalidInputException  when a field is empty
     * @throws AuthenticationException when no account matches (generic message)
     */
    @Override
    public User login(String username, String password)
            throws InvalidInputException, AuthenticationException {
        validateLoginInput(username, password);
        User user = userDao.findByUsername(username.trim());
        // verify() hashes the typed password with the stored salt and compares
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return user;
    }

    /** Shared login validation (also used by unit tests). */
    public static void validateLoginInput(String username, String password)
            throws InvalidInputException {
        if (username == null || username.isBlank()) {
            throw new InvalidInputException("Username must not be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidInputException("Password must not be empty.");
        }
    }

    /**
     * Registers a new student: validates everything, rejects duplicate
     * username/email, hashes the password and writes BOTH tables
     * ({@code users} then {@code students}). If the second insert fails,
     * the first one is undone so no orphan account stays behind.
     */
    @Override
    public Student registerStudent(String name, String email, String phone,
                                   String username, String password) throws InvalidInputException {
        // 1) build + validate the objects (never trust the browser)
        User user = new User(username, password, User.ROLE_STUDENT);
        user.validate(); // username not empty, password length, role

        Student student = new Student(0, 0, name, email, phone, user.getUsername());
        student.validate(); // name, email format, phone digits

        // 2) reject duplicates before inserting
        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new InvalidInputException("That username is already taken. Please choose another one.");
        }
        if (studentDao.findByEmail(student.getEmail()) != null) {
            throw new InvalidInputException("That email is already registered.");
        }

        // 3) store the HASHED password, never the plain text
        user.setPassword(PasswordUtil.encode(password));
        userDao.insert(user);

        student.setUserId(user.getId());
        try {
            studentDao.insert(student);
        } catch (RuntimeException ex) {
            userDao.delete(user.getId()); // manual compensation - keep data consistent
            throw ex;
        }
        return student;
    }

    // ====================================================================
    // Student management (admin)
    // ====================================================================

    @Override
    public Student updateStudent(long studentId, String name, String email,
                                 String phone, String newPassword) throws InvalidInputException {
        Student existing = studentDao.findById(studentId);
        if (existing == null) {
            throw new InvalidInputException("Student not found.");
        }

        Student updated = new Student(existing.getStudentId(), existing.getUserId(),
                name, email, phone, existing.getUsername());
        updated.validate();

        // email must stay unique (ignore the record we are editing)
        Student byEmail = studentDao.findByEmail(updated.getEmail());
        if (byEmail != null && byEmail.getStudentId() != updated.getStudentId()) {
            throw new InvalidInputException("That email is already used by another student.");
        }

        if (!studentDao.update(updated)) {
            throw new InvalidInputException("Student not found.");
        }

        // optional password reset from the admin edit form
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < User.MIN_PASSWORD_LENGTH) {
                throw new InvalidInputException("New password must be at least "
                        + User.MIN_PASSWORD_LENGTH + " characters long.");
            }
            userDao.updatePassword(existing.getUserId(), PasswordUtil.encode(newPassword));
        }
        return studentDao.findById(studentId);
    }

    /**
     * Deletes a student in child-first order:
     * profile row (meal/booking rows cascade away) then the login account.
     */
    @Override
    public void deleteStudent(long studentId) throws InvalidInputException {
        Student existing = studentDao.findById(studentId);
        if (existing == null) {
            throw new InvalidInputException("Student not found.");
        }
        studentDao.delete(studentId);         // meals & bookings removed by FK cascade
        userDao.delete(existing.getUserId()); // then the login account
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    @Override
    public Student getStudentByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return studentDao.findByUsername(username.trim());
    }

    // ====================================================================
    // Menu management
    // ====================================================================

    @Override
    public List<MenuItem> getAllMenu() {
        return menuDao.findAll();
    }

    @Override
    public MenuItem addMenuItem(String foodName, String mealType,
                                String priceText, String dateText) throws InvalidInputException {
        MenuItem item = buildMenuItem(0, foodName, mealType, priceText, dateText);
        return menuDao.insert(item);
    }

    @Override
    public MenuItem updateMenuItem(long menuId, String foodName, String mealType,
                                   String priceText, String dateText) throws InvalidInputException {
        if (menuDao.findById(menuId) == null) {
            throw new InvalidInputException("Menu item not found.");
        }
        MenuItem item = buildMenuItem(menuId, foodName, mealType, priceText, dateText);
        if (!menuDao.update(item)) {
            throw new InvalidInputException("Menu item not found.");
        }
        return item;
    }

    @Override
    public void deleteMenuItem(long menuId) throws InvalidInputException {
        if (!menuDao.delete(menuId)) {
            throw new InvalidInputException("Menu item not found.");
        }
    }

    /**
     * Converts the raw text coming from the HTML form (price and date are
     * strings there) into a validated {@link MenuItem}.
     */
    private MenuItem buildMenuItem(long id, String foodName, String mealType,
                                   String priceText, String dateText) throws InvalidInputException {
        if (priceText == null || priceText.isBlank()) {
            throw new InvalidInputException("Price must not be empty.");
        }
        double price;
        try {
            price = Double.parseDouble(priceText.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Price must be a valid number, for example 120.50.");
        }

        if (dateText == null || dateText.isBlank()) {
            throw new InvalidInputException("Available date is required.");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateText.trim()); // expects YYYY-MM-DD
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date must be in YYYY-MM-DD format.");
        }

        MenuItem item = new MenuItem(id, foodName, mealType, price, date);
        item.validate();
        return item;
    }

    // ====================================================================
    // Bookings and meal records
    // ====================================================================

    /**
     * Books a menu item for a student.
     *
     * <p>Rules enforced here:</p>
     * <ol>
     *   <li>quantity must be greater than zero,</li>
     *   <li>the menu item must exist and still be available (today or later),</li>
     *   <li>the same student cannot book the same item twice,</li>
     *   <li>total price = price x quantity (via {@link Booking#calculateTotal}).</li>
     * </ol>
     *
     * <p>Two rows are written: the <b>booking</b> (money) and the linked
     * <b>meal record</b> (food). If the meal insert fails, the booking is
     * removed again.</p>
     */
    @Override
    public Booking bookMeal(long studentId, long menuId, int quantity) throws InvalidInputException {
        if (studentId <= 0) {
            throw new InvalidInputException("Your student profile could not be found. Please login again.");
        }
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }

        MenuItem item = menuDao.findById(menuId);
        if (item == null) {
            throw new InvalidInputException("Menu item not found.");
        }
        if (!item.isAvailableToday()) {
            throw new InvalidInputException("This meal is no longer available for booking.");
        }
        if (bookingDao.findIdByStudentAndMenu(studentId, menuId) != null) {
            throw new InvalidInputException("You have already booked this meal.");
        }

        double total = Booking.calculateTotal(item.getPrice(), quantity);

        Booking booking = new Booking(0, studentId, menuId, LocalDate.now(),
                quantity, total, Booking.STATUS_BOOKED);
        booking.validate();
        bookingDao.insert(booking);

        // the meal copies date + type from the menu so records always match
        Meal meal = new Meal(0, studentId, menuId, booking.getBookingId(),
                item.getAvailableDate(), item.getMealType(), quantity, Meal.STATUS_BOOKED);
        meal.validate();
        try {
            mealDao.insert(meal);
        } catch (RuntimeException ex) {
            bookingDao.delete(booking.getBookingId()); // undo the booking
            throw ex;
        }
        return booking;
    }

    @Override
    public List<MealRecord> getAllMealRecords() {
        return mealDao.findAllRecords();
    }

    @Override
    public List<MealRecord> getMealRecordsOf(long studentId) throws InvalidInputException {
        if (studentId <= 0) {
            throw new InvalidInputException("Invalid student profile. Please login again.");
        }
        return mealDao.findRecordsByStudent(studentId);
    }

    /** Changes the status of a meal and of its linked booking (if any). */
    @Override
    public void updateMealStatus(long mealId, String status) throws InvalidInputException {
        if (!Meal.isValidStatus(status)) {
            throw new InvalidInputException("Status must be BOOKED, CANCELLED or COMPLETED.");
        }
        Long bookingId = mealDao.getBookingId(mealId);
        if (!mealDao.updateStatus(mealId, status)) {
            throw new InvalidInputException("Meal record not found.");
        }
        if (bookingId != null) {
            bookingDao.updateStatus(bookingId, status); // keep both tables in sync
        }
    }

    /** Deletes the meal record and its linked booking (if any). */
    @Override
    public void deleteMealRecord(long mealId) throws InvalidInputException {
        Long bookingId = mealDao.getBookingId(mealId);
        if (!mealDao.delete(mealId)) {
            throw new InvalidInputException("Meal record not found.");
        }
        if (bookingId != null) {
            bookingDao.delete(bookingId);
        }
    }

    @Override
    public List<BookingRecord> getAllBookings() {
        return bookingDao.findAllRecords();
    }

    @Override
    public List<BookingRecord> getBookingsOf(long studentId) throws InvalidInputException {
        if (studentId <= 0) {
            throw new InvalidInputException("Invalid student profile. Please login again.");
        }
        return bookingDao.findRecordsByStudent(studentId);
    }

    // ====================================================================
    // Dashboard
    // ====================================================================

    @Override
    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("students", studentDao.count());
        counts.put("menu", menuDao.count());
        counts.put("meals", mealDao.count());
        return counts;
    }
}
