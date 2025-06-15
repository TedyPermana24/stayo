package com.stayo.service;

import com.stayo.model.Booking;
import com.stayo.model.Transaction;
import com.stayo.repository.TransactionRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookingService bookingService;

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Optional<Transaction> getTransactionByOrderId(String orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    public Optional<Transaction> getTransactionByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    public List<Transaction> getTransactionsByBookingId(Long bookingId) {
        return transactionRepository.findByBookingId(bookingId);
    }

    public List<Transaction> getTransactionsByStatus(String status) {
        return transactionRepository.findByTransactionStatus(status);
    }

    public Transaction createTransactionFromNotification(JSONObject notification, Booking booking) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Transaction transaction = new Transaction();
        transaction.setOrderId(notification.getString("order_id"));
        transaction.setTransactionId(notification.getString("transaction_id"));
        transaction.setBooking(booking);
        transaction.setAmount(new BigDecimal(notification.getString("gross_amount")));
        transaction.setPaymentType(notification.getString("payment_type"));
        transaction.setTransactionStatus(notification.getString("transaction_status"));
        transaction.setFraudStatus(notification.getString("fraud_status"));
        transaction.setTransactionTime(LocalDateTime.parse(notification.getString("transaction_time"), formatter));

        if (notification.has("settlement_time")) {
            transaction.setSettlementTime(LocalDateTime.parse(notification.getString("settlement_time"), formatter));
        }

        transaction.setStatusCode(notification.getString("status_code"));
        transaction.setStatusMessage(notification.getString("status_message"));
        transaction.setRawNotification(notification.toString());

        return saveTransaction(transaction);
    }
}