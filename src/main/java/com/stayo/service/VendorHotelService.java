package com.stayo.service;

import com.stayo.model.VendorHotel;
import com.stayo.model.VendorStatus;
import com.stayo.repository.VendorHotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorHotelService {

    @Autowired
    private VendorHotelRepository vendorHotelRepository;

    public VendorHotel registerVendor(VendorHotel vendor) throws Exception {
        // Check if email already exists
        if (vendorHotelRepository.existsByUserEmail(vendor.getUser().getEmail())) {
            throw new Exception("Email already registered");
        }

        // Check if business license already exists
        if (vendorHotelRepository.existsByBusinessLicense(vendor.getBusinessLicense())) {
            throw new Exception("Business license already registered");
        }

        vendor.setStatus(VendorStatus.PENDING);

        return vendorHotelRepository.save(vendor);
    }

    public Optional<VendorHotel> authenticateVendor(String email, String password) {
        Optional<VendorHotel> vendorOpt = vendorHotelRepository.findByUserEmail(email);
        if (vendorOpt.isPresent()) {
            VendorHotel vendor = vendorOpt.get();
            // Bandingkan password langsung tanpa dekripsi
            if (password.equals(vendor.getUser().getPassword()) &&
                    vendor.getStatus() == VendorStatus.APPROVED) {
                return Optional.of(vendor);
            }
        }
        return Optional.empty();
    }

    public Optional<VendorHotel> getVendorById(Long id) {
        return vendorHotelRepository.findById(id);
    }

    public VendorHotel updateVendor(VendorHotel vendor) {
        return vendorHotelRepository.save(vendor);
    }
    
    // Tambahkan metode getAllVendors
    public List<VendorHotel> getAllVendors() {
        return vendorHotelRepository.findAll();
    }
}