package com.stayo.controller;

import com.stayo.model.Booking;
import com.stayo.model.Hotel;
import com.stayo.model.VendorHotel;
import com.stayo.model.*;
import com.stayo.service.BookingService;
import com.stayo.service.HotelService;
import com.stayo.service.RoomService;
import com.stayo.service.VendorHotelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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

    @Autowired
    private RoomService roomService;

    // Define the upload directory
    private final String UPLOAD_DIR = "src/main/resources/static/images/hotels/";

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("vendorForm", new VendorRegistrationForm());
        return "vendor-register";
    }

    @PostMapping("/register")
    public String registerVendor(@ModelAttribute VendorRegistrationForm vendorForm,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Register vendor first
            VendorHotel vendor = vendorForm.getVendor();
            vendor = vendorHotelService.registerVendor(vendor);

            // Then create hotel
            Hotel hotel = vendorForm.getHotel();
            hotel.setVendor(vendor);
            hotel.setStars(5); // Default value
            hotel.setAverageRating(0.0); // Default value

            // Handle file upload if a file was selected
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                // Create directory if it doesn't exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save the file
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath);

                // Set the image URL to the file name
                hotel.setImageUrl(fileName);
            }

            hotelService.saveHotel(hotel);

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
        int totalRooms = 0;
        double totalRating = 0.0;
        int hotelWithRatings = 0;
        
        for (Hotel hotel : hotels) {
            List<Booking> hotelBookings = bookingService.getBookingsByHotelId(hotel.getId());
            totalBookings += hotelBookings.size();
            for (Booking booking : hotelBookings) {
                totalRevenue += booking.getTotalPrice().doubleValue();
            }
            
            // Count total rooms
            List<Room> hotelRooms = roomService.getRoomsByHotelId(hotel.getId());
            totalRooms += hotelRooms.size();
            
            // Calculate average rating
            if (hotel.getAverageRating() != null && hotel.getAverageRating() > 0) {
                totalRating += hotel.getAverageRating();
                hotelWithRatings++;
            }
        }
        
        // Calculate occupancy rate
        double occupancyRate = 0.0;
        if (totalRooms > 0) {
            occupancyRate = (double) totalBookings / totalRooms * 100;
            if (occupancyRate > 100) occupancyRate = 100.0; // Cap at 100%
        }
        
        // Calculate average rating across all hotels
        double averageRating = 0.0;
        if (hotelWithRatings > 0) {
            averageRating = totalRating / hotelWithRatings;
        }

        model.addAttribute("totalHotels", hotels.size());
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupancyRate", occupancyRate);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("vendor", vendor);

        return "vendor-dashboard";
    }

    // CRUD Operations for Hotels

    // Modify the updateHotel method to handle file upload
    @PostMapping("/hotels/edit/{id}")
    public String updateHotel(@PathVariable Long id,
            @ModelAttribute Hotel hotel,
            @RequestParam("imageFile") MultipartFile imageFile,
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
                    // Handle file upload if a file was selected
                    if (!imageFile.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                        Path uploadPath = Paths.get(UPLOAD_DIR);

                        // Create directory if it doesn't exist
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }

                        // Save the file
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(imageFile.getInputStream(), filePath);

                        // Delete old image if exists
                        if (existingHotel.getImageUrl() != null && !existingHotel.getImageUrl().isEmpty()) {
                            try {
                                Path oldFilePath = uploadPath.resolve(existingHotel.getImageUrl());
                                Files.deleteIfExists(oldFilePath);
                            } catch (IOException e) {
                                // Log error but continue
                                System.err.println("Could not delete old image: " + e.getMessage());
                            }
                        }

                        // Set the image URL to the file name
                        hotel.setImageUrl(fileName);
                    } else if (hotel.getImageUrl() == null || hotel.getImageUrl().isEmpty()) {
                        // Keep the existing image if no new image was uploaded
                        hotel.setImageUrl(existingHotel.getImageUrl());
                    }

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
                List<Room> hotelRooms = roomService.getRoomsByHotelId(hotel.getId());
                
                // Calculate occupancy rate for this hotel
                double occupancyRate = 0.0;
                if (hotelRooms.size() > 0) {
                    occupancyRate = (double) hotelBookings.size() / hotelRooms.size() * 100;
                    if (occupancyRate > 100) occupancyRate = 100.0; // Cap at 100%
                }
                
                // Calculate total revenue for this hotel
                double totalRevenue = 0.0;
                for (Booking booking : hotelBookings) {
                    totalRevenue += booking.getTotalPrice().doubleValue();
                }

                model.addAttribute("hotel", hotel);
                model.addAttribute("bookings", hotelBookings);
                model.addAttribute("occupancyRate", occupancyRate);
                model.addAttribute("totalRevenue", totalRevenue);
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
    // CRUD Operations for Rooms

    @GetMapping("/hotels/{hotelId}/rooms")
    public String showRooms(@PathVariable Long hotelId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            // Check if hotel belongs to this vendor
            if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                List<Room> rooms = roomService.getRoomsByHotelId(hotelId);

                model.addAttribute("hotel", hotel);
                model.addAttribute("rooms", rooms);
                model.addAttribute("vendor", vendor);
                return "vendor-rooms";
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view these rooms.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Hotel not found.");
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/{hotelId}/rooms/add")
    public String showAddRoomForm(@PathVariable Long hotelId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            // Check if hotel belongs to this vendor
            if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                Room room = new Room();
                room.setHotel(hotel);

                model.addAttribute("room", room);
                model.addAttribute("hotel", hotel);
                model.addAttribute("vendor", vendor);
                return "vendor-room-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to add rooms to this hotel.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Hotel not found.");
        }

        return "redirect:/vendor/dashboard";
    }

    @PostMapping("/hotels/{hotelId}/rooms/add")
    public String addRoom(@PathVariable Long hotelId,
            @ModelAttribute Room room,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
            if (hotelOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                // Check if hotel belongs to this vendor
                if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId())) {
                    room.setHotel(hotel);

                    // Handle file upload if a file was selected
                    if (!imageFile.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                        Path uploadPath = Paths.get(UPLOAD_DIR);

                        // Create directory if it doesn't exist
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }

                        // Save the file
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(imageFile.getInputStream(), filePath);

                        // Set the image URL to the file name
                        room.setImageUrl(fileName);
                    }

                    roomService.saveRoom(room);
                    redirectAttributes.addFlashAttribute("success", "Room added successfully!");
                    return "redirect:/vendor/hotels/" + hotelId + "/rooms";
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "You don't have permission to add rooms to this hotel.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Hotel not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding room: " + e.getMessage());
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/{hotelId}/rooms/edit/{roomId}")
    public String showEditRoomForm(@PathVariable Long hotelId,
            @PathVariable Long roomId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        if (hotelOpt.isPresent() && roomOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            Room room = roomOpt.get();

            // Check if hotel belongs to this vendor and room belongs to this hotel
            if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId()) &&
                    room.getHotel().getId().equals(hotelId)) {

                model.addAttribute("room", room);
                model.addAttribute("hotel", hotel);
                model.addAttribute("vendor", vendor);
                return "vendor-room-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this room.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Hotel or room not found.");
        }

        return "redirect:/vendor/dashboard";
    }

    @PostMapping("/hotels/{hotelId}/rooms/edit/{roomId}")
    public String updateRoom(@PathVariable Long hotelId,
            @PathVariable Long roomId,
            @ModelAttribute Room room,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
            Optional<Room> existingRoomOpt = roomService.getRoomById(roomId);

            if (hotelOpt.isPresent() && existingRoomOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                Room existingRoom = existingRoomOpt.get();

                // Check if hotel belongs to this vendor and room belongs to this hotel
                if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId()) &&
                        existingRoom.getHotel().getId().equals(hotelId)) {

                    // Handle file upload if a file was selected
                    if (!imageFile.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                        Path uploadPath = Paths.get(UPLOAD_DIR);

                        // Create directory if it doesn't exist
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }

                        // Save the file
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(imageFile.getInputStream(), filePath);

                        // Delete old image if exists
                        if (existingRoom.getImageUrl() != null && !existingRoom.getImageUrl().isEmpty()) {
                            try {
                                Path oldFilePath = uploadPath.resolve(existingRoom.getImageUrl());
                                Files.deleteIfExists(oldFilePath);
                            } catch (IOException e) {
                                // Log error but continue
                                System.err.println("Could not delete old image: " + e.getMessage());
                            }
                        }

                        // Set the image URL to the file name
                        room.setImageUrl(fileName);
                    } else if (room.getImageUrl() == null || room.getImageUrl().isEmpty()) {
                        // Keep the existing image if no new image was uploaded
                        room.setImageUrl(existingRoom.getImageUrl());
                    }

                    room.setId(roomId);
                    room.setHotel(hotel);
                    roomService.saveRoom(room);
                    redirectAttributes.addFlashAttribute("success", "Room updated successfully!");
                    return "redirect:/vendor/hotels/" + hotelId + "/rooms";
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this room.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Hotel or room not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating room: " + e.getMessage());
        }

        return "redirect:/vendor/dashboard";
    }

    @GetMapping("/hotels/{hotelId}/rooms/delete/{roomId}")
    public String deleteRoom(@PathVariable Long hotelId,
            @PathVariable Long roomId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        VendorHotel vendor = (VendorHotel) session.getAttribute("vendor");
        if (vendor == null) {
            return "redirect:/vendor/signin";
        }

        try {
            Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
            Optional<Room> roomOpt = roomService.getRoomById(roomId);

            if (hotelOpt.isPresent() && roomOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                Room room = roomOpt.get();

                // Check if hotel belongs to this vendor and room belongs to this hotel
                if (hotel.getVendor() != null && hotel.getVendor().getId().equals(vendor.getId()) &&
                        room.getHotel().getId().equals(hotelId)) {

                    roomService.deleteRoom(roomId);
                    redirectAttributes.addFlashAttribute("success", "Room deleted successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this room.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Hotel or room not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting room: " + e.getMessage());
        }

        return "redirect:/vendor/hotels/" + hotelId + "/rooms";
    }

    @GetMapping("/signout")
    public String signOut(HttpSession session) {
        session.removeAttribute("vendor");
        return "redirect:/vendor/signin";
    }
}