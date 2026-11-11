package com.smartfoodmess.model;

import com.smartfoodmess.exception.InvalidInputException;

import java.time.LocalDate;

/**
 * A meal record: shows which food a student has taken on a given day.
 *
 * <p>A meal record is created automatically when a student books a menu item.
 * Its date and meal type are copied from the menu item so the record always
 * matches what was actually served.</p>
 */
public class Meal {

    // ---- status values --------------------------------------------------
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private static final String[] ALL_STATUSES = {
            STATUS_BOOKED, STATUS_CANCELLED, STATUS_COMPLETED
    };

    private long mealId;
    private long studentId;
    private long menuId;
    private long bookingId;      // 0 when not linked to a booking (yet)
    private LocalDate mealDate;
    private String mealType;
    private int quantity;
    private String status;

    public Meal() {
    }

    public Meal(long mealId, long studentId, long menuId, long bookingId,
                LocalDate mealDate, String mealType, int quantity, String status) {
        this.mealId = mealId;
        this.studentId = studentId;
        this.menuId = menuId;
        this.bookingId = bookingId;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.quantity = quantity;
        this.status = status;
    }

    // ---- validation -----------------------------------------------------

    /**
     * Validates this meal record.
     *
     * @throws InvalidInputException with a user friendly message.
     */
    public void validate() throws InvalidInputException {
        if (studentId <= 0) {
            throw new InvalidInputException("A valid student is required for a meal record.");
        }
        if (menuId <= 0) {
            throw new InvalidInputException("A valid menu item is required for a meal record.");
        }
        if (bookingId < 0) {
            throw new InvalidInputException("Invalid booking reference.");
        }
        if (mealDate == null) {
            throw new InvalidInputException("Meal date is required.");
        }
        if (!MenuItem.isSupportedMealType(mealType)) {
            throw new InvalidInputException("Meal type must be one of: BREAKFAST, LUNCH, DINNER, SNACK.");
        }
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }
        if (!isValidStatus(status)) {
            throw new InvalidInputException("Status must be BOOKED, CANCELLED or COMPLETED.");
        }
    }

    /** Returns true for BOOKED / CANCELLED / COMPLETED. */
    public static boolean isValidStatus(String value) {
        if (value == null) {
            return false;
        }
        for (String s : ALL_STATUSES) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /** All valid statuses (used for messages / dropdowns). */
    public static String[] allStatuses() {
        return ALL_STATUSES.clone();
    }

    // ---- getters / setters ---------------------------------------------

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getMenuId() {
        return menuId;
    }

    public void setMenuId(long menuId) {
        this.menuId = menuId;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Meal{mealId=" + mealId + ", studentId=" + studentId + ", menuId=" + menuId
                + ", mealDate=" + mealDate + ", mealType='" + mealType
                + "', quantity=" + quantity + ", status='" + status + "'}";
    }
}
