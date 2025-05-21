package com.stayo.repository;

import com.stayo.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByLocationContainingIgnoreCase(String location);
    
    @Query("SELECT h FROM Hotel h WHERE h.name LIKE %?1% OR h.location LIKE %?1%")
    List<Hotel> searchHotels(String keyword);
}