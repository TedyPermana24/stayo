package com.stayo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stayo.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByHotelId(Long hotelId);
    
    List<Booking> findByStatus(String status);
    
    // Add new methods for monthly data
    @Query("SELECT MONTH(b.bookingDate) as month, COUNT(b) as bookingCount, SUM(b.totalPrice) as totalRevenue " +
           "FROM Booking b WHERE b.room.hotel.vendor.id = :vendorId " +
           "AND YEAR(b.bookingDate) = :year " +
           "AND b.status = 'CONFIRMED' " +
           "GROUP BY MONTH(b.bookingDate) " +
           "ORDER BY MONTH(b.bookingDate)")
    List<Object[]> getMonthlyBookingStats(@Param("vendorId") Long vendorId, @Param("year") int year);
}