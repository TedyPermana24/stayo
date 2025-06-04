package com.stayo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stayo.model.Hotel;
import com.stayo.model.Review;
import com.stayo.repository.HotelRepository;
import com.stayo.repository.ReviewRepository;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    public List<Review> getAllReviews() { 
        return reviewRepository.findAll();
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public List<Review> getReviewsByHotelId(Long hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }
    
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    public Review createReview(Review review) {
        review.setReviewDate(LocalDate.now());
        Review savedReview = reviewRepository.save(review);
        
        // Update hotel average rating
        updateHotelAverageRating(review.getHotel().getId());
        
        return savedReview;
    }
    
    public void deleteReview(Long id) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            Long hotelId = review.getHotel().getId();
            
            reviewRepository.deleteById(id);
            
            // Update hotel average rating
            updateHotelAverageRating(hotelId);
        }
    }
    
    private void updateHotelAverageRating(Long hotelId) {
        List<Review> hotelReviews = reviewRepository.findByHotelId(hotelId);
        if (!hotelReviews.isEmpty()) {
            double averageRating = hotelReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
            if (hotelOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                hotel.setAverageRating(averageRating);
                hotelRepository.save(hotel);
            }
        }
    }
}