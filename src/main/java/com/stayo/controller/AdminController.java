package com.stayo.controller;

import com.stayo.model.Role;
import com.stayo.model.User;
import com.stayo.model.VendorHotel;
import com.stayo.model.VendorStatus;
import com.stayo.service.UserService;
import com.stayo.service.VendorHotelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/vendors")
    public String showVendorApprovalPage(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/signin";
        }

        // Get all pending vendors
        List<VendorHotel> pendingVendors = vendorHotelService.getAllVendors().stream()
                .filter(v -> v.getStatus() == VendorStatus.PENDING)
                .collect(Collectors.toList());

        model.addAttribute("pendingVendors", pendingVendors);
        return "admin-vendor-approval";
    }

    @PostMapping("/vendors/approve/{id}")
    public String approveVendor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || admin.getRole() != Role.ADMIN) {
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
        if (admin == null || admin.getRole() != Role.ADMIN) {
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