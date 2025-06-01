package com.stayo.controller;

import com.stayo.model.User;
import com.stayo.model.VendorHotel;
import com.stayo.model.VendorStatus;
import com.stayo.service.BookingService;
import com.stayo.service.UserService;
import com.stayo.service.VendorHotelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private VendorHotelService vendorHotelService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        // Menghitung statistik untuk dashboard
        long totalBookings = bookingService.getAllBookings().size();
        long pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> "PENDING".equals(b.getStatus()))
                .count();
        long confirmedBookings = bookingService.getAllBookings().stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .count();
        long rejectedBookings = bookingService.getAllBookings().stream()
                .filter(b -> "REJECTED".equals(b.getStatus()))
                .count();

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("rejectedBookings", rejectedBookings);

        return "admin/dashboard";
    }

    @GetMapping("/vendors")
    public String showVendorApprovalPage(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        // Get all pending vendors
        List<VendorHotel> pendingVendors = vendorHotelService.getAllVendors().stream()
                .filter(v -> v.getStatus() == VendorStatus.PENDING)
                .collect(Collectors.toList());

        model.addAttribute("pendingVendors", pendingVendors);
        return "admin/vendor-approval";
    }

    @PostMapping("/vendors/approve/{id}")
    public String approveVendor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        Optional<VendorHotel> vendorOpt = vendorHotelService.getVendorById(id);
        if (vendorOpt.isPresent()) {
            VendorHotel vendor = vendorOpt.get();
            vendor.setStatus(VendorStatus.APPROVED);
            vendorHotelService.updateVendor(vendor);
            redirectAttributes.addFlashAttribute("success", "Vendor approved successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Vendor not found");
        }

        return "redirect:/admin/vendors";
    }

    @PostMapping("/vendors/reject/{id}")
    public String rejectVendor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        Optional<VendorHotel> vendorOpt = vendorHotelService.getVendorById(id);
        if (vendorOpt.isPresent()) {
            VendorHotel vendor = vendorOpt.get();
            vendor.setStatus(VendorStatus.REJECTED);
            vendorHotelService.updateVendor(vendor);
            redirectAttributes.addFlashAttribute("success", "Vendor rejected successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Vendor not found");
        }

        return "redirect:/admin/vendors";
    }
}