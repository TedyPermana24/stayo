package com.stayo.repository;

import com.stayo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderId(String orderId);
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByBookingId(Long bookingId);
    List<Transaction> findByTransactionStatus(String transactionStatus);
}