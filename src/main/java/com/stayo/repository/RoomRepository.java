package com.stayo.repository;

import com.stayo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = ?1 AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE b.hotel.id = ?1 AND " +
           "((b.checkInDate <= ?3 AND b.checkOutDate >= ?2) OR " +
           "(b.checkInDate >= ?2 AND b.checkInDate < ?3)))")
    List<Room> findAvailableRoomsByHotelAndDateRange(Long hotelId, LocalDate checkIn, LocalDate checkOut);
}