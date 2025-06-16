package com.stayo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.stayo.model.Booking;
import com.stayo.model.User;
import com.stayo.model.User.Role;
import com.stayo.model.VendorHotel;
import com.stayo.model.VendorStatus;
import com.stayo.repository.UserRepository;
import com.stayo.service.BookingService;
import com.stayo.service.UserService;
import com.stayo.service.VendorHotelService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private VendorHotelService vendorHotelService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);

        // Menambahkan totalHotels
        long totalHotels = vendorHotelService.getAllVendors().stream()
                .filter(v -> v.getStatus() == VendorStatus.APPROVED)
                .count();
        model.addAttribute("totalHotels", totalHotels);

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

        // Menambahkan pendingApprovals untuk dashboard
        long pendingApprovals = vendorHotelService.getAllVendors().stream()
                .filter(v -> v.getStatus() == VendorStatus.PENDING)
                .count();
        model.addAttribute("pendingApprovals", pendingApprovals);

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

        // Menambahkan statistik untuk vendor approval
        long todayApplications = 0; // Implementasi sesuai kebutuhan
        long approvedToday = 0; // Implementasi sesuai kebutuhan  
        long rejectedToday = 0; // Implementasi sesuai kebutuhan

        model.addAttribute("pendingVendors", pendingVendors);
        model.addAttribute("todayApplications", todayApplications);
        model.addAttribute("approvedToday", approvedToday);
        model.addAttribute("rejectedToday", rejectedToday);
        
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
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        
        // Menambahkan data vendor users untuk statistik
        List<User> vendorUsers = users.stream()
                .filter(u -> u.getRole() == Role.VENDOR)
                .collect(Collectors.toList());
        
        model.addAttribute("users", users);
        model.addAttribute("vendorUsers", vendorUsers);
        return "admin/users/list";
    }

    @GetMapping("/users/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/users/form";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.createUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/users/form";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User updatedUser) {
        userService.updateUser(id, updatedUser);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // PERBAIKAN: Menambahkan parameter status untuk filter
    @GetMapping("/bookings")
    public String showAllBookings(
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            HttpSession session, 
            Model model) {
        
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }
    
        // Get all bookings
        List<Booking> allBookings = bookingService.getAllBookings();
        List<Booking> filteredBookings;
        
        // Filter berdasarkan status
        if ("ALL".equals(status)) {
            filteredBookings = allBookings;
        } else {
            filteredBookings = allBookings.stream()
                    .filter(b -> status.equals(b.getStatus()))
                    .collect(Collectors.toList());
        }
    
        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("admin", admin);
        model.addAttribute("selectedStatus", status);
        return "admin/bookings";
    }

    @GetMapping("/bookings/pending")
    public String showPendingBookings(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }
    
        // Gunakan pola yang sama seperti di showAllBookings
        List<Booking> pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> "PENDING".equals(b.getStatus()))
                .collect(Collectors.toList());
    
        model.addAttribute("bookings", pendingBookings);
        model.addAttribute("admin", admin);
        model.addAttribute("selectedStatus", "PENDING");
        return "admin/pending-bookings";
    }

    @PostMapping("/bookings/approve/{id}")
    public String approveBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        try {
            bookingService.updateBookingStatus(id, "CONFIRMED");
            redirectAttributes.addFlashAttribute("success", "Booking approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve booking");
        }

        return "redirect:/admin/bookings/pending";
    }

    @PostMapping("/bookings/reject/{id}")
    public String rejectBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }

        try {
            bookingService.updateBookingStatus(id, "REJECTED");
            redirectAttributes.addFlashAttribute("success", "Booking rejected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject booking");
        }

        return "redirect:/admin/bookings/pending";
    }

    @GetMapping("/vendors/management")
    public String showVendorManagement(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/signin";
        }
    
        // Get all vendors
        List<VendorHotel> allVendors = vendorHotelService.getAllVendors();
        
        // Menghitung statistik untuk vendor management
        long totalVendors = allVendors.size();
        long pendingVendors = allVendors.stream()
                .filter(v -> v.getStatus() == VendorStatus.PENDING)
                .count();
        long approvedVendors = allVendors.stream()
                .filter(v -> v.getStatus() == VendorStatus.APPROVED)
                .count();
        long rejectedVendors = allVendors.stream()
                .filter(v -> v.getStatus() == VendorStatus.REJECTED)
                .count();
    
        model.addAttribute("vendors", allVendors);
        model.addAttribute("totalVendors", totalVendors);
        model.addAttribute("pendingVendors", pendingVendors);
        model.addAttribute("approvedVendors", approvedVendors);
        model.addAttribute("rejectedVendors", rejectedVendors);
        
        return "admin/vendors/management";
    }
}