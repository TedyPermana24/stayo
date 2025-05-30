package com.stayo.controller;

import com.stayo.model.Booking;
import com.stayo.model.Hotel;
import com.stayo.model.VendorHotel;
import com.stayo.service.BookingService;
import com.stayo.service.HotelService;
import com.stayo.service.VendorHotelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor")
public class VendorHotelController {

    @Autowired
    private VendorHotelService vendorHotelService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("vendor", new VendorHotel());
        return "vendor-register";
    }

    @PostMapping("/register")
    public String registerVendor(@ModelAttribute VendorHotel vendor,
            RedirectAttributes redirectAttributes) {
        try {
            vendorHotelService.registerVendor(vendor);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Your application is under review.");
            return "redirect:/vendor/signin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/register";
        }
    }

    @GetMapping("/signin")
    public String showSignInForm() {
        return "vendor-signin";
    }

    @PostMapping("/signin")
    public String signInVendor(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<VendorHotel> vendorOpt = vendorHotelService.authenticateVendor(email, password);
        if (vendorOpt.isPresent()) {
            session.setAttribute("vendor", vendorOpt.get());
            return "redirect:/vendor/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid credentials or account not approved yet");
            return "redirect:/vendor/signin";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        // Get vendor's hotels
        List<Hotel> hotels = hotelService.getHotelsByVendorId(vendor.getId());
        model.addAttribute("hotels", hotels);

        // Get booking statistics
        int totalBookings = 0;
        double totalRevenue = 0.0;
        for (Hotel hotel : hotels) {
            List<Booking> hotelBookings = bookingService.getBookingsByHotelId(hotel.getId());
            totalBookings += hotelBookings.size();
            for (Booking booking : hotelBookings) {
                totalRevenue += booking.getTotalPrice().doubleValue();
            }
        }

        model.addAttribute("totalHotels", hotels.size());
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("vendor", vendor);

        return "vendor-dashboard";
    }

    // CRUD Operations for Hotels

    @GetMapping("/hotels/add")
    public String showAddHotelForm(HttpSession session, Model model) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Hotel hotel = new Hotel();
        // Initialize default values to prevent null conversion errors
        hotel.setStars(0); // or any default value
        hotel.setAverageRating(0.0); // if this is also a primitive

        model.addAttribute("hotel", hotel);
        model.addAttribute("vendor", vendor);
        return "vendor-hotel-form";
    }

    @PostMapping("/hotels/add")
    public String addHotel(@ModelAttribute Hotel hotel,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            hotel.setVendor(vendor);
            hotelService.saveHotel(hotel);
            redirectAttributes.addFlashAttribute("success", "Hotel added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding hotel: " + e.getMessage());
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            // Check if hotel belongs to this vendor
            if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                // Ensure no null values for primitive fields
                if (hotel.getStars() == null) {
                    hotel.setStars(0);
                }
                if (hotel.getAverageRating() == null) {
                    hotel.setAverageRating(0.0);
                }

                model.addAttribute("hotel", hotel);
                model.addAttribute("vendor", vendor);
                return "vendor-hotel-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this hotel.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Hotel not found.");
        }

        return "redirect:/vendor/dashboard";
    }

    @PostMapping("/hotels/edit/{id}")

    public String updateHotel(@PathVariable Long id,
            @ModelAttribute Hotel hotel,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            Optional<Hotel> existingHotelOpt = hotelService.getHotelById(id);
            if (existingHotelOpt.isPresent()) {
                Hotel existingHotel = existingHotelOpt.get();
                // Check if hotel belongs to this vendor
                if (existingHotel.getVendor() != null && existingHotel.getVendor().getId().equals(vendor.getId())) {
                    hotel.setId(id);
                    hotel.setVendor(vendor);
                    hotelService.saveHotel(hotel);
                    redirectAttributes.addFlashAttribute("success", "Hotel updated successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this hotel.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Hotel not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating hotel: " + e.getMessage());
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
            if (hotelOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                // Check if hotel belongs to this vendor
                if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                    hotelService.deleteHotel(id);
                    redirectAttributes.addFlashAttribute("success", "Hotel deleted successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this hotel.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Hotel not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting hotel: " + e.getMessage());
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/view/{id}")
    public String viewHotel(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            // Check if hotel belongs to this vendor
            if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                List<Booking> hotelBookings = bookingService.getBookingsByHotelId(hotel.getId());

                model.addAttribute("hotel", hotel);
                model.addAttribute("bookings", hotelBookings);
                model.addAttribute("vendor", vendor);
                return "vendor-hotel-details";
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view this hotel.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Hotel not found.");
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/signout")
    public String signOut(HttpSession session) {
        session.removeAttribute("vendor");
        return "redirect:/vendor/signin";
    }
}