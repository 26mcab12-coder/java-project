package com.smartfoodmess.model;

import com.smartfoodmess.exception.InvalidInputException;

import java.time.LocalDate;

/**
 * A meal booking made by a student.
 *
 * <p>When a student books a menu item we store the chosen quantity and the
 * total price: {@code total = price x quantity}. The booking row keeps the
 * money information while the linked {@link Meal} row keeps the food
 * record.</p>
 */
public class Booking {

    // ---- status values (kept in sync with the meal record) --------------
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private long bookingId;
    private long studentId;
    private long menuId;
    private LocalDate bookingDate;
    private int quantity;
    private double totalPrice;
    private String status;

    public Booking() {
    }

    public Booking(long bookingId, long studentId, long menuId,
                   LocalDate bookingDate, int quantity, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.menuId = menuId;
        this.bookingDate = bookingDate;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // ---- business logic -------------------------------------------------

    /**
     * Calculates the total price of a booking: {@code price x quantity}.
     *
     * <p>The result is rounded to 2 decimal places so that money values
     * never show long floating point tails (120.05 instead of 120.049999).</p>
     *
     * @param price     unit price of the menu item (must not be negative)
     * @param quantity  number of portions (must be greater than zero)
     * @return total price rounded to 2 decimals
     * @throws InvalidInputException if price is negative or quantity &lt;= 0
     */
    public static double calculateTotal(double price, int quantity) throws InvalidInputException {
        if (Double.isNaN(price) || price < 0) {
            throw new InvalidInputException("Price must not be negative.");
        }
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }
        double total = price * quantity;
        return Math.round(total * 100.0) / 100.0;
    }

    // ---- validation -----------------------------------------------------

    /**
     * Validates this booking.
     *
     * @throws InvalidInputException with a user friendly message.
     */
    public void validate() throws InvalidInputException {
        if (studentId <= 0) {
            throw new InvalidInputException("A valid student is required for a booking.");
        }
        if (menuId <= 0) {
            throw new InvalidInputException("A valid menu item is required for a booking.");
        }
        if (bookingDate == null) {
            throw new InvalidInputException("Booking date is required.");
        }
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }
        if (Double.isNaN(totalPrice) || totalPrice < 0) {
            throw new InvalidInputException("Total price must not be negative.");
        }
        if (!Meal.isValidStatus(status)) {
            throw new InvalidInputException("Status must be BOOKED, CANCELLED or COMPLETED.");
        }
    }

    // ---- getters / setters ---------------------------------------------

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
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

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking{bookingId=" + bookingId + ", studentId=" + studentId
                + ", menuId=" + menuId + ", bookingDate=" + bookingDate
                + ", quantity=" + quantity + ", totalPrice=" + totalPrice
                + ", status='" + status + "'}";
    }
}
