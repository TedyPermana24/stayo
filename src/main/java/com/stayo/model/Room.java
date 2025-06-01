package com.stayo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    private String description;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    private int capacity;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    // Room amenities/features
    private boolean hasPrivateBathroom;
    private boolean hasKingBed;
    private boolean hasQueenBed;
    private boolean hasTwinBeds;
    private boolean hasTV;
    private boolean hasMinibar;
    private boolean hasBalcony;
    private boolean hasAirConditioning;
    private boolean hasWifi;
    private boolean hasSafetyDeposit;
    
    // Getters and Setters for amenities
    public boolean isHasPrivateBathroom() {
        return hasPrivateBathroom;
    }

    public void setHasPrivateBathroom(boolean hasPrivateBathroom) {
        this.hasPrivateBathroom = hasPrivateBathroom;
    }

    public boolean isHasKingBed() {
        return hasKingBed;
    }

    public void setHasKingBed(boolean hasKingBed) {
        this.hasKingBed = hasKingBed;
    }

    public boolean isHasQueenBed() {
        return hasQueenBed;
    }

    public void setHasQueenBed(boolean hasQueenBed) {
        this.hasQueenBed = hasQueenBed;
    }

    public boolean isHasTwinBeds() {
        return hasTwinBeds;
    }

    public void setHasTwinBeds(boolean hasTwinBeds) {
        this.hasTwinBeds = hasTwinBeds;
    }

    public boolean isHasTV() {
        return hasTV;
    }

    public void setHasTV(boolean hasTV) {
        this.hasTV = hasTV;
    }

    public boolean isHasMinibar() {
        return hasMinibar;
    }

    public void setHasMinibar(boolean hasMinibar) {
        this.hasMinibar = hasMinibar;
    }

    public boolean isHasBalcony() {
        return hasBalcony;
    }

    public void setHasBalcony(boolean hasBalcony) {
        this.hasBalcony = hasBalcony;
    }

    public boolean isHasAirConditioning() {
        return hasAirConditioning;
    }

    public void setHasAirConditioning(boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHasSafetyDeposit() {
        return hasSafetyDeposit;
    }

    public void setHasSafetyDeposit(boolean hasSafetyDeposit) {
        this.hasSafetyDeposit = hasSafetyDeposit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}