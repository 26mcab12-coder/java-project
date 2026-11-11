package com.smartfoodmess.model;

import java.time.LocalDate;

/**
 * Read-only view of one meal record for the web pages.
 *
 * <p>The plain {@link Meal} class only stores ids. For display we also need
 * the student name, the food name and the total price, which live in other
 * tables. The {@code MealDAO} joins the tables and fills this object - it is
 * never saved back to the database (a "DTO" - data transfer object).</p>
 */
public class MealRecord {

    private long mealId;
    private long studentId;
    private String studentName;
    private long menuId;
    private String foodName;
    private String mealType;
    private LocalDate mealDate;
    private int quantity;
    private String status;
    private double totalPrice;

    public MealRecord() {
    }

    public MealRecord(long mealId, long studentId, String studentName, long menuId,
                      String foodName, String mealType, LocalDate mealDate,
                      int quantity, String status, double totalPrice) {
        this.mealId = mealId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.menuId = menuId;
        this.foodName = foodName;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.quantity = quantity;
        this.status = status;
        this.totalPrice = totalPrice;
    }

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

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "MealRecord{mealId=" + mealId + ", studentName='" + studentName
                + "', foodName='" + foodName + "', quantity=" + quantity
                + ", status='" + status + "', totalPrice=" + totalPrice + "}";
    }
}
