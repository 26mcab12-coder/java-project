package com.smartfoodmess.model;

import com.smartfoodmess.exception.InvalidInputException;

import java.time.LocalDate;

/**
 * One row of the daily food menu.
 *
 * <p>Example: "Chicken Biryani, LUNCH, 120.00, 2026-09-25".</p>
 *
 * <p>An item is considered <i>available</i> when its {@code availableDate}
 * is today or in the future. This keeps the table simple (no extra column)
 * and is easy to explain during a viva.</p>
 */
public class MenuItem {

    // ---- supported meal types ------------------------------------------
    public static final String MEAL_BREAKFAST = "BREAKFAST";
    public static final String MEAL_LUNCH = "LUNCH";
    public static final String MEAL_DINNER = "DINNER";
    public static final String MEAL_SNACK = "SNACK";

    private static final String[] ALL_MEAL_TYPES = {
            MEAL_BREAKFAST, MEAL_LUNCH, MEAL_DINNER, MEAL_SNACK
    };

    private long id;
    private String foodName;
    private String mealType;
    private double price;
    private LocalDate availableDate;

    public MenuItem() {
    }

    public MenuItem(long id, String foodName, String mealType,
                    double price, LocalDate availableDate) {
        this.id = id;
        this.foodName = foodName;
        this.mealType = mealType;
        this.price = price;
        this.availableDate = availableDate;
    }

    // ---- validation -----------------------------------------------------

    /**
     * Validates this menu item.
     *
     * @throws InvalidInputException with a user friendly message.
     */
    public void validate() throws InvalidInputException {
        if (foodName == null || foodName.isBlank()) {
            throw new InvalidInputException("Food name must not be empty.");
        }
        foodName = foodName.trim();
        if (foodName.length() > 100) {
            throw new InvalidInputException("Food name must be 100 characters or less.");
        }

        if (mealType == null || mealType.isBlank()) {
            throw new InvalidInputException("Meal type must not be empty.");
        }
        mealType = mealType.trim().toUpperCase();
        if (!isSupportedMealType(mealType)) {
            throw new InvalidInputException("Meal type must be one of: BREAKFAST, LUNCH, DINNER, SNACK.");
        }

        if (Double.isNaN(price) || price < 0) {
            throw new InvalidInputException("Price must not be negative.");
        }

        if (availableDate == null) {
            throw new InvalidInputException("Available date is required.");
        }
    }

    /** Returns true for the four meal types used by the mess. */
    public static boolean isSupportedMealType(String type) {
        if (type == null) {
            return false;
        }
        for (String t : ALL_MEAL_TYPES) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /** All supported meal types (used for messages / dropdowns). */
    public static String[] allMealTypes() {
        return ALL_MEAL_TYPES.clone();
    }

    /** True when the item can still be booked on the given day. */
    public boolean isAvailableOn(LocalDate day) {
        return availableDate != null && day != null && !availableDate.isBefore(day);
    }

    /** True when the item can be booked today. */
    public boolean isAvailableToday() {
        return isAvailableOn(LocalDate.now());
    }

    // ---- getters / setters ---------------------------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    @Override
    public String toString() {
        return "MenuItem{id=" + id + ", foodName='" + foodName + "', mealType='" + mealType
                + "', price=" + price + ", availableDate=" + availableDate + "}";
    }
}
