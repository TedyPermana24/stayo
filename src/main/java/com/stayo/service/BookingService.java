package com.stayo.service;

import com.stayo.model.Booking;
import com.stayo.model.Room;
import com.stayo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getBookingsByHotelId(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId);
    }

    public Booking createBooking(Booking booking) {
        // Calculate total price
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        Room room = booking.getRoom();
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(days));
        booking.setTotalPrice(totalPrice);

        // Set booking date
        booking.setBookingDate(LocalDate.now());

        // Set initial status
        booking.setStatus("CONFIRMED");

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long id, String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Booking not found");
    }

    public void cancelBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
}