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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        long totalUsers = userRepository.count();
        User admin = (User) session.getAttribute("user");
        model.addAttribute("totalUsers", totalUsers);
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
    
  @GetMapping ("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list"; // resources/templates/user/list.html
    }

    // Show form to add user
    @GetMapping("/users/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/users/form"; // resources/templates/user/form.html
    }

    // Handle create user
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.createUser(user);
        return "redirect:/admin/users";
    }

    // Show form to edit user
    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/users/form";
    }

    // Handle update user
    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User updatedUser) {
        userService.updateUser(id, updatedUser);
        return "redirect:/admin/users";
    }

    // Delete user
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}