package com.stayo.repository;

import com.stayo.model.VendorHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorHotelRepository extends JpaRepository<VendorHotel, Long> {
    Optional<VendorHotel> findByUserEmail(String email);
    boolean existsByUserEmail(String email);
    boolean existsByBusinessLicense(String businessLicense);
}