package com.stayo.model;

public class VendorRegistrationForm {
    private User user;
    private VendorHotel vendor;

    public VendorRegistrationForm() {
        this.user = new User();
        this.vendor = new VendorHotel();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VendorHotel getVendor() {
        return vendor;
    }

    public void setVendor(VendorHotel vendor) {
        this.vendor = vendor;
    }
}