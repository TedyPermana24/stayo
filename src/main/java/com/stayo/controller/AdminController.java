package com.stayo.controller;

import com.stayo.model.User;
import com.stayo.model.Booking;
import com.stayo.service.BookingService;
import com.stayo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    // Check if user is admin
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/signin";
        }
        
        List<Booking> allBookings = bookingService.getAllBookings();
        List<Booking> pendingBookings = bookingService.getBookingsByStatus("PENDING");
        
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", pendingBookings.size());
        model.addAttribute("confirmedBookings", bookingService.getBookingsByStatus("CONFIRMED").size());
        model.addAttribute("rejectedBookings", bookingService.getBookingsByStatus("REJECTED").size());
        
        return "admin/dashboard";
    }
    
    @GetMapping("/bookings")
    public String viewAllBookings(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/signin";
        }
        
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }
    
    @GetMapping("/bookings/pending")
    public String viewPendingBookings(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/signin";
        }
        
        List<Booking> pendingBookings = bookingService.getBookingsByStatus("PENDING");
        model.addAttribute("bookings", pendingBookings);
        return "admin/pending-bookings";
    }
    
    @PostMapping("/bookings/{id}/approve")
    public String approveBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/signin";
        }
        
        try {
            bookingService.updateBookingStatus(id, "CONFIRMED");
            redirectAttributes.addFlashAttribute("success", "Booking approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve booking: " + e.getMessage());
        }
        
        return "redirect:/admin/bookings/pending";
    }
    
    @PostMapping("/bookings/{id}/reject")
    public String rejectBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
            return "redirect:/signin";
        }
        
        try {
            bookingService.updateBookingStatus(id, "REJECTED");
            redirectAttributes.addFlashAttribute("success", "Booking rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject booking: " + e.getMessage());
        }
        
        return "redirect:/admin/bookings/pending";
    }
}