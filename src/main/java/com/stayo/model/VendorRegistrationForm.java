package com.stayo.model;

import org.springframework.web.multipart.MultipartFile;

public class VendorRegistrationForm {
    private VendorHotel vendor;
    private Hotel hotel;
    private MultipartFile imageFile;

    public VendorRegistrationForm() {
        this.vendor = new VendorHotel();
        this.hotel = new Hotel();
    }

    public VendorHotel getVendor() {
        return vendor;
    }

    public void setVendor(VendorHotel vendor) {
        this.vendor = vendor;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}