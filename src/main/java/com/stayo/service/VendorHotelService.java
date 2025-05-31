package com.stayo.service;

import com.stayo.model.VendorHotel;
import com.stayo.model.VendorStatus;
import com.stayo.repository.VendorHotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
// Hapus import BCryptPasswordEncoder
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VendorHotelService {

    @Autowired
    private VendorHotelRepository vendorHotelRepository;

    // Hapus deklarasi passwordEncoder

    public VendorHotel registerVendor(VendorHotel vendor) throws Exception {
        // Check if email already exists
        if (vendorHotelRepository.existsByEmail(vendor.getEmail())) {
            throw new Exception("Email already registered");
        }

        // Check if business license already exists
        if (vendorHotelRepository.existsByBusinessLicense(vendor.getBusinessLicense())) {
            throw new Exception("Business license already registered");
        }

        // Tidak perlu enkripsi password
        // vendor.setPassword(passwordEncoder.encode(vendor.getPassword()));
        vendor.setStatus(VendorStatus.PENDING);

        return vendorHotelRepository.save(vendor);
    }

    public Optional<VendorHotel> authenticateVendor(String email, String password) {
        Optional<VendorHotel> vendorOpt = vendorHotelRepository.findByEmail(email);
        if (vendorOpt.isPresent()) {
            VendorHotel vendor = vendorOpt.get();
            // Bandingkan password langsung tanpa dekripsi
            if (password.equals(vendor.getPassword()) &&
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
}