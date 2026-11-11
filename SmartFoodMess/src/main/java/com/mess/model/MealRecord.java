package com.mess.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a student's meal record (opt-in / opt-out / consumed status).
 * Mirrors the {@code meal_records} table in the mess_db database.
 * The {@code studentName} field is only populated by JOIN queries for display.
 */
public class MealRecord {

    private int recordId;
    private int studentId;
    private LocalDate mealDate;
    private String mealType;
    private String status;
    private String studentName;
    private BigDecimal expense;

    public MealRecord() {
    }

    public MealRecord(int recordId, int studentId, LocalDate mealDate, String mealType, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.status = status;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    @Override
    public String toString() {
        return "MealRecord{recordId=" + recordId + ", studentId=" + studentId
                + ", mealDate=" + mealDate + ", mealType='" + mealType + '\''
                + ", status='" + status + '\'' + ", studentName='" + studentName
                + '\'' + ", expense=" + expense + '}';
    }
}