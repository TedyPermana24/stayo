package com.stayo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    /**
     * Display the user profile page
     */
    @GetMapping
    public String showProfilePage(Model model, HttpSession session) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/signin";
        }

        // Get user information from session with default values
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String email = (String) session.getAttribute("email");
        String phone = (String) session.getAttribute("phone");

        // Set default values if null
        if (firstName == null || firstName.trim().isEmpty()) {
            firstName = "User";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            lastName = "";
        }
        if (email == null || email.trim().isEmpty()) {
            email = "user@example.com"; // Default email or get from database
        }
        if (phone == null) {
            phone = "";
        }

        // Add user information to model
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);

        return "profile";
    }

    /**
     * Update personal information
     */
    @PostMapping("/update-personal-info")
    public String updatePersonalInfo(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam String address,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "First name is required");
            return "redirect:/profile";
        }

        // Update session with new user information
        session.setAttribute("firstName", firstName.trim());
        session.setAttribute("lastName", lastName != null ? lastName.trim() : "");
        session.setAttribute("phone", phone != null ? phone.trim() : "");

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Personal information updated successfully");

        return "redirect:/profile";
    }

    /**
     * Update security settings (password)
     */
    @PostMapping("/update-security")
    public String updateSecurity(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validate passwords
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long");
            return "redirect:/profile";
        }

        // In a real application, you would validate the current password against the
        // stored password and update the password in the database

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully");

        return "redirect:/profile";
    }

    /**
     * Update user preferences
     */
    @PostMapping("/update-preferences")
    public String updatePreferences(
            @RequestParam(required = false, defaultValue = "false") boolean emailNotifications,
            @RequestParam String language,
            @RequestParam String currency,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Update session with new preferences
        session.setAttribute("emailNotifications", emailNotifications);
        session.setAttribute("language", language);
        session.setAttribute("currency", currency);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Preferences updated successfully");

        return "redirect:/profile";
    }

    /**
     * Handle logout
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate session
        session.invalidate();

        return "redirect:/signin";
    }
}