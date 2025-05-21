package com.stayo.controller;

import com.stayo.model.Hotel;
import com.stayo.model.Review;
import com.stayo.model.User;
import com.stayo.service.HotelService;
import com.stayo.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private HotelService hotelService;
    
    @PostMapping("/create")
    public String createReview(@RequestParam Long hotelId,
                              @RequestParam int rating,
                              @RequestParam String comment,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }
        
        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            
            Review review = new Review();
            review.setUser(user);
            review.setHotel(hotel);
            review.setRating(rating);
            review.setComment(comment);
            
            try {
                reviewService.createReview(review);
                redirectAttributes.addFlashAttribute("success", "Review submitted successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to submit review: " + e.getMessage());
            }
            
            return "redirect:/hotels/" + hotelId;
        } else {
            return "redirect:/hotels";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                              @RequestParam Long hotelId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }
        
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete review: " + e.getMessage());
        }
        
        return "redirect:/hotels/" + hotelId;
    }
}