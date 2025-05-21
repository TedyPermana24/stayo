package com.stayo.controller;

import com.stayo.model.Hotel;
import com.stayo.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/")
    public String home(Model model) {
        List<Hotel> featuredHotels = hotelService.getAllHotels();
        model.addAttribute("featuredHotels", featuredHotels);
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String location,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false, defaultValue = "2") int guests,
            Model model) {

        List<Hotel> hotels;
        if (location != null && !location.isEmpty()) {
            hotels = hotelService.getHotelsByLocation(location);
        } else {
            hotels = hotelService.getAllHotels();
        }

        model.addAttribute("hotels", hotels);
        model.addAttribute("location", location);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("guests", guests);

        return "search-results";
    }
}