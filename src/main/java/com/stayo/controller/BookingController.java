package com.stayo.controller;

import com.stayo.model.Booking;
import com.stayo.model.Hotel;
import com.stayo.model.Room;
import com.stayo.model.User;
import com.stayo.service.BookingService;
import com.stayo.service.HotelService;
import com.stayo.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getUserBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }

        List<Booking> bookings = bookingService.getBookingsByUserId(user.getId());

        // Categorize bookings for the template
        List<Booking> upcomingBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .filter(b -> b.getCheckOutDate().isAfter(LocalDate.now()))
                .toList();

        List<Booking> pastBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .filter(b -> b.getCheckOutDate().isBefore(LocalDate.now()) ||
                        b.getCheckOutDate().isEqual(LocalDate.now()))
                .toList();

        List<Booking> cancelledBookings = bookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .toList();

        // Add pending payment bookings
        List<Booking> pendingPaymentBookings = bookings.stream()
                .filter(b -> "PENDING".equals(b.getStatus()))
                .toList();

        model.addAttribute("bookings", bookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("pastBookings", pastBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("pendingPaymentBookings", pendingPaymentBookings);

        return "bookings";
    }

    @GetMapping("/{id}")
    public String getBookingDetails(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            // Check if the booking belongs to the logged-in user
            if (!booking.getUser().getId().equals(user.getId())) {
                return "redirect:/bookings";
            }

            model.addAttribute("booking", booking);

            // For the bookings template, we need to categorize this single booking
            List<Booking> bookings = List.of(booking);

            boolean isUpcoming = (booking.getStatus() == null || "CONFIRMED".equals(booking.getStatus())) &&
                    booking.getCheckOutDate().isAfter(LocalDate.now());

            boolean isPast = (booking.getStatus() == null || "CONFIRMED".equals(booking.getStatus())) &&
                    (booking.getCheckOutDate().isBefore(LocalDate.now()) ||
                            booking.getCheckOutDate().isEqual(LocalDate.now()));

            boolean isCancelled = "CANCELLED".equals(booking.getStatus());

            model.addAttribute("upcomingBookings", isUpcoming ? bookings : List.of());
            model.addAttribute("pastBookings", isPast ? bookings : List.of());
            model.addAttribute("cancelledBookings", isCancelled ? bookings : List.of());

            return "bookings";
        } else {
            return "redirect:/bookings";
        }
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam Long hotelId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int numberOfGuests,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        if (hotelOpt.isPresent() && roomOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            Room room = roomOpt.get();

            // Validate dates and room availability
            if (checkIn.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Check-in date cannot be in the past");
                return "redirect:/hotels/" + hotelId;
            }

            if (checkOut.isBefore(checkIn)) {
                redirectAttributes.addFlashAttribute("error", "Check-out date must be after check-in date");
                return "redirect:/hotels/" + hotelId;
            }

            if (numberOfGuests > room.getCapacity()) {
                redirectAttributes.addFlashAttribute("error", "Number of guests exceeds room capacity");
                return "redirect:/hotels/" + hotelId;
            }

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setHotel(hotel);
            booking.setRoom(room);
            booking.setCheckInDate(checkIn);
            booking.setCheckOutDate(checkOut);
            booking.setNumberOfGuests(numberOfGuests);
            booking.setStatus("PENDING");

            try {
                Booking savedBooking = bookingService.createBooking(booking);
                // Redirect to payment page
                return "redirect:/payments/process/" + savedBooking.getId();
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to create booking: " + e.getMessage());
                return "redirect:/hotels/" + hotelId;
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid hotel or room");
            return "redirect:/hotels";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            // Check if the booking belongs to the logged-in user
            if (!booking.getUser().getId().equals(user.getId())) {
                return "redirect:/bookings";
            }

            try {
                bookingService.cancelBooking(id);
                redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to cancel booking: " + e.getMessage());
            }

            return "redirect:/bookings";
        } else {
            return "redirect:/bookings";
        }
    }
}