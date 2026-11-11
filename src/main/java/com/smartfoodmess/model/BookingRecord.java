package com.smartfoodmess.model;

import java.time.LocalDate;

/**
 * Read-only view of one booking for the web pages.
 *
 * <p>Like {@link MealRecord}, this joins data from several tables
 * (booking + menu + student) so the browser can show names and food titles
 * instead of only numeric ids.</p>
 */
public class BookingRecord {

    private long bookingId;
    private long studentId;
    private String studentName;
    private long menuId;
    private String foodName;
    private String mealType;
    private LocalDate availableDate;
    private LocalDate bookingDate;
    private int quantity;
    private double totalPrice;
    private String status;

    public BookingRecord() {
    }

    public BookingRecord(long bookingId, long studentId, String studentName, long menuId,
                         String foodName, String mealType, LocalDate availableDate,
                         LocalDate bookingDate, int quantity, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.menuId = menuId;
        this.foodName = foodName;
        this.mealType = mealType;
        this.availableDate = availableDate;
        this.bookingDate = bookingDate;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public long getMenuId() {
        return menuId;
    }

    public void setMenuId(long menuId) {
        this.menuId = menuId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
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
        return "BookingRecord{bookingId=" + bookingId + ", studentName='" + studentName
                + "', foodName='" + foodName + "', quantity=" + quantity
                + ", totalPrice=" + totalPrice + ", status='" + status + "'}";
    }
}
