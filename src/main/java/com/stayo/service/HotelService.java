package com.stayo.service;

import com.stayo.model.Hotel;
import com.stayo.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    
    @Autowired
    private HotelRepository hotelRepository;
    
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }
    
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }
    
    public List<Hotel> searchHotels(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return hotelRepository.findAll();
        }
        return hotelRepository.searchHotels(keyword);
    }
    
    public List<Hotel> getHotelsByLocation(String location) {
        return hotelRepository.findByLocationContainingIgnoreCase(location);
    }
    
    public List<Hotel> getHotelsByVendorId(Long vendorId) {
        return hotelRepository.findByVendorId(vendorId);
    }
    
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }
    
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}