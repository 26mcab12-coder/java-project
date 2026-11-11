package com.smartfoodmess.web;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.Booking;
import com.smartfoodmess.model.BookingRecord;
import com.smartfoodmess.model.User;
import com.smartfoodmess.service.MessOperations;
import com.smartfoodmess.service.MessService;
import com.smartfoodmess.util.AuthUtil;
import com.smartfoodmess.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Meal booking.
 *
 * <pre>
 *   GET  /bookings - STUDENT -> own bookings, ADMIN -> all bookings (JSON)
 *   POST /bookings - STUDENT only: menuId + quantity
 *                    creates booking + meal record, total = price x quantity
 *                    answers 201 with the calculated total price
 * </pre>
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/bookings"})
public class BookingServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireLogin(req, resp);
        if (user == null) {
            return;
        }

        try {
            List<BookingRecord> records;
            if (user.isAdmin()) {
                records = service.getAllBookings();
            } else {
                records = service.getBookingsOf(AuthUtil.getLoggedStudentId(req));
            }

            List<String> jsonItems = new ArrayList<>();
            for (BookingRecord b : records) {
                jsonItems.add(JsonUtil.object(
                        JsonUtil.field("bookingId", b.getBookingId()),
                        JsonUtil.field("studentId", b.getStudentId()),
                        JsonUtil.field("studentName", b.getStudentName()),
                        JsonUtil.field("menuId", b.getMenuId()),
                        JsonUtil.field("foodName", b.getFoodName()),
                        JsonUtil.field("mealType", b.getMealType()),
                        JsonUtil.field("availableDate", b.getAvailableDate()),
                        JsonUtil.field("bookingDate", b.getBookingDate()),
                        JsonUtil.field("quantity", b.getQuantity()),
                        JsonUtil.field("totalPrice", b.getTotalPrice()),
                        JsonUtil.field("status", b.getStatus())));
            }
            JsonUtil.write(resp, 200, JsonUtil.array(jsonItems));
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading bookings.");
        }
    }

    /** Book a meal - students only (admins get 403). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireStudent(req, resp);
        if (user == null) {
            return;
        }

        long menuId = parseId(req.getParameter("menuId"), "menu item");
        int quantity = parseQuantity(req.getParameter("quantity"));

        try {
            Booking booking = service.bookMeal(
                    AuthUtil.getLoggedStudentId(req), menuId, quantity);

            JsonUtil.write(resp, 201, JsonUtil.object(
                    JsonUtil.field("success", true),
                    JsonUtil.field("bookingId", booking.getBookingId()),
                    JsonUtil.field("totalPrice", booking.getTotalPrice()),
                    JsonUtil.field("message", String.format(Locale.ENGLISH,
                            "Meal booked successfully! Total: Rs. %.2f",
                            booking.getTotalPrice()))));
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while booking the meal.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private long parseId(String text, String name) throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Missing " + name + " id.");
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid " + name + " id.");
        }
    }

    private int parseQuantity(String text) throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Quantity must not be empty.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Quantity must be a whole number.");
        }
    }
}
