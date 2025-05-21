package com.stayo.controller;

import com.stayo.model.Hotel;
import com.stayo.model.Review;
import com.stayo.model.Room;
import com.stayo.service.HotelService;
import com.stayo.service.ReviewService;
import com.stayo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hotels")
public class HotelController {
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping
    public String getAllHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        return "search-results";
    }
    
    @GetMapping("/{id}")
    public String getHotelDetails(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                 @RequestParam(required = false, defaultValue = "2") int guests,
                                 Model model) {
        
        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
        
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            model.addAttribute("hotel", hotel);
            
            // Get all rooms for the hotel
            List<Room> rooms = roomService.getRoomsByHotelId(id);
            model.addAttribute("rooms", rooms);
            
            // Get available rooms if dates are provided
            if (checkIn != null && checkOut != null) {
                List<Room> availableRooms = roomService.getAvailableRooms(id, checkIn, checkOut);
                model.addAttribute("availableRooms", availableRooms);
                model.addAttribute("checkIn", checkIn);
                model.addAttribute("checkOut", checkOut);
                model.addAttribute("guests", guests);
            }
            
            // Get reviews for the hotel
            List<Review> reviews = reviewService.getReviewsByHotelId(id);
            model.addAttribute("reviews", reviews);
            
            return "hotel-details";
        } else {
            return "redirect:/hotels";
        }
    }
    
    @GetMapping("/search")
    public String searchHotels(@RequestParam String keyword, Model model) {
        List<Hotel> hotels = hotelService.searchHotels(keyword);
        model.addAttribute("hotels", hotels);
        model.addAttribute("keyword", keyword);
        return "search-results";
    }
}