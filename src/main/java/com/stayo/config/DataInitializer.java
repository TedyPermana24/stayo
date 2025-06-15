package com.stayo.config;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.stayo.model.Hotel;
import com.stayo.model.Room;
import com.stayo.model.User;
import com.stayo.repository.HotelRepository;
import com.stayo.repository.RoomRepository;
import com.stayo.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create demo user
        if (userRepository.count() == 0) {
            User user = new User();
            user.setFirstName("Demo");
            user.setLastName("User");
            user.setEmail("demo@example.com");
            user.setPassword("password");
            userRepository.save(user);
        }
        
        // Create hotels
        if (hotelRepository.count() == 0) {
            // Hotel 1
            Hotel hotel1 = new Hotel();
            hotel1.setName("Grand Luxury Resort");
            hotel1.setLocation("Ubud, Bali, Indonesia");
            hotel1.setDescription("Nestled in the lush greenery of Ubud, Grand Luxury Resort offers a perfect blend of traditional Balinese charm and modern luxury.");
            hotel1.setStars(5);
            hotel1.setImageUrl("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            hotel1.setAverageRating(9.2);
            
            // Hotel 2
            Hotel hotel2 = new Hotel();
            hotel2.setName("Beachfront Paradise");
            hotel2.setLocation("Kuta, Bali, Indonesia");
            hotel2.setDescription("Experience the ultimate beachfront getaway at our luxurious resort with direct access to the pristine white sands of Kuta Beach.");
            hotel2.setStars(4);
            hotel2.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            hotel2.setAverageRating(8.7);
            
            // Hotel 3
            Hotel hotel3 = new Hotel();
            hotel3.setName("Tropical Villa Resort");
            hotel3.setLocation("Seminyak, Bali, Indonesia");
            hotel3.setDescription("Indulge in luxury at our exclusive villa resort, featuring private pools, lush gardens, and personalized service in the heart of Seminyak.");
            hotel3.setStars(5);
            hotel3.setImageUrl("https://images.unsplash.com/photo-1582719508461-905c673771fd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1025&q=80");
            hotel3.setAverageRating(9.5);
            
            // Save hotels
            hotelRepository.saveAll(Arrays.asList(hotel1, hotel2, hotel3));
            
            // Create rooms for Hotel 1
            Room room1 = new Room();
            room1.setType("Deluxe Room");
            room1.setDescription("Spacious room with king bed and private balcony");
            room1.setPricePerNight(new BigDecimal("299"));
            room1.setCapacity(2);
            room1.setImageUrl("https://images.unsplash.com/photo-1578683010236-d716f9a3f461?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            room1.setHotel(hotel1);
            
            Room room2 = new Room();
            room2.setType("Premium Suite");
            room2.setDescription("Luxury suite with separate living area and panoramic views");
            room2.setPricePerNight(new BigDecimal("399"));
            room2.setCapacity(2);
            room2.setImageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1074&q=80");
            room2.setHotel(hotel1);
            
            Room room3 = new Room();
            room3.setType("Villa with Private Pool");
            room3.setDescription("Exclusive villa with private pool and garden");
            room3.setPricePerNight(new BigDecimal("599"));
            room3.setCapacity(4);
            room3.setImageUrl("https://images.unsplash.com/photo-1582719508461-905c673771fd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1025&q=80");
            room3.setHotel(hotel1);
            
            // Create rooms for Hotel 2
            Room room4 = new Room();
            room4.setType("Ocean View Room");
            room4.setDescription("Comfortable room with stunning ocean views");
            room4.setPricePerNight(new BigDecimal("259"));
            room4.setCapacity(2);
            room4.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            room4.setHotel(hotel2);
            
            Room room5 = new Room();
            room5.setType("Beachfront Suite");
            room5.setDescription("Luxurious suite with direct beach access");
            room5.setPricePerNight(new BigDecimal("359"));
            room5.setCapacity(3);
            room5.setImageUrl("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            room5.setHotel(hotel2);
            
            // Create rooms for Hotel 3
            Room room6 = new Room();
            room6.setType("Garden Villa");
            room6.setDescription("Private villa surrounded by tropical gardens");
            room6.setPricePerNight(new BigDecimal("349"));
            room6.setCapacity(2);
            room6.setImageUrl("https://images.unsplash.com/photo-1582719508461-905c673771fd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1025&q=80");
            room6.setHotel(hotel3);
            
            Room room7 = new Room();
            room7.setType("Luxury Pool Villa");
            room7.setDescription("Spacious villa with private infinity pool");
            room7.setPricePerNight(new BigDecimal("499"));
            room7.setCapacity(4);
            room7.setImageUrl("https://images.unsplash.com/photo-1540541338287-41700207dee6?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80");
            room7.setHotel(hotel3);
            
            // Save rooms
            roomRepository.saveAll(Arrays.asList(room1, room2, room3, room4, room5, room6, room7));
        }
    }
}