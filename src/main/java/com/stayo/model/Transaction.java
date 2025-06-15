package com.stayo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId; // Format: STAYO-{bookingId}-{timestamp}

    @Column(nullable = false)
    private String transactionId; // ID dari Midtrans

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentType; // gopay, qris, dll

    @Column(nullable = false)
    private String transactionStatus; // pending, settlement, cancel, deny, expire

    @Column
    private String fraudStatus; // accept, deny, challenge

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    @Column
    private LocalDateTime settlementTime;

    @Column
    private String statusCode;

    @Column
    private String statusMessage;

    @Column(columnDefinition = "TEXT")
    private String rawNotification; // Menyimpan JSON notifikasi asli

    // Constructors
    public Transaction() {
    }

    public Transaction(String orderId, String transactionId, Booking booking, BigDecimal amount,
            String paymentType, String transactionStatus, String fraudStatus,
            LocalDateTime transactionTime, LocalDateTime settlementTime,
            String statusCode, String statusMessage, String rawNotification) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.booking = booking;
        this.amount = amount;
        this.paymentType = paymentType;
        this.transactionStatus = transactionStatus;
        this.fraudStatus = fraudStatus;
        this.transactionTime = transactionTime;
        this.settlementTime = settlementTime;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.rawNotification = rawNotification;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getFraudStatus() {
        return fraudStatus;
    }

    public void setFraudStatus(String fraudStatus) {
        this.fraudStatus = fraudStatus;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public LocalDateTime getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(LocalDateTime settlementTime) {
        this.settlementTime = settlementTime;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getRawNotification() {
        return rawNotification;
    }

    public void setRawNotification(String rawNotification) {
        this.rawNotification = rawNotification;
    }
}