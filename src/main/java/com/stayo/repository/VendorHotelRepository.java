package com.stayo.repository;

import com.stayo.model.VendorHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorHotelRepository extends JpaRepository<VendorHotel, Long> {
    Optional<VendorHotel> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByBusinessLicense(String businessLicense);
}