package com.mess.model;

import java.math.BigDecimal;

/**
 * Represents a single menu entry (one meal of one day).
 * Mirrors the {@code menu} table in the mess_db database.
 */
public class Menu {

    private int menuId;
    private String dayOfWeek;
    private String mealType;
    private String items;
    private BigDecimal price;

    public Menu() {
    }

    public Menu(int menuId, String dayOfWeek, String mealType, String items) {
        this(menuId, dayOfWeek, mealType, items, BigDecimal.ZERO);
    }

    public Menu(int menuId, String dayOfWeek, String mealType, String items, BigDecimal price) {
        this.menuId = menuId;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.items = items;
        this.price = price == null ? BigDecimal.ZERO : price;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price == null ? BigDecimal.ZERO : price;
    }

    @Override
    public String toString() {
        return "Menu{menuId=" + menuId + ", dayOfWeek='" + dayOfWeek + '\''
                + ", mealType='" + mealType + '\'' + ", items='" + items + '\''
                + ", price=" + price + '}';
    }
}