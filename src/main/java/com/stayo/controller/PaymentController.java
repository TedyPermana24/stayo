package com.stayo.controller;

import com.midtrans.httpclient.error.MidtransError;
import com.stayo.model.Booking;
import com.stayo.model.PaymentResponse;
import com.stayo.model.Transaction;
import com.stayo.service.BookingService;
import com.stayo.service.PaymentService;
import com.stayo.service.TransactionService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TransactionService transactionService;

    // Metode ini sudah ada dan sudah benar
    @GetMapping("/process/{bookingId}")
    public String processPayment(@PathVariable Long bookingId, Model model) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Periksa status booking
            if ("CONFIRMED".equals(booking.getStatus())) {
                model.addAttribute("error", "This booking has already been paid");
                return "error";
            }

            try {
                booking.setStatus("PENDING");
                bookingService.updateBookingStatus(bookingId, "PENDING");

                PaymentResponse response = paymentService.createTransaction(booking);
                model.addAttribute("token", response.getToken());
                model.addAttribute("booking", booking);
                return "payment";
            } catch (MidtransError e) {
                model.addAttribute("error", "Payment processing failed: " + e.getMessage());
                return "error";
            }
        } else {
            model.addAttribute("error", "Booking not found");
            return "error";
        }
    }

    @PostMapping("/notification")
    @ResponseBody
    public ResponseEntity<String> handleNotification(@RequestBody String notificationJson) {
        try {
            JSONObject notification = new JSONObject(notificationJson);

            String orderId = notification.getString("order_id");
            String transactionStatus = notification.getString("transaction_status");
            String fraudStatus = notification.getString("fraud_status");

            // Extract booking ID from order ID (format: STAYO-{bookingId}-{timestamp})
            String[] orderIdParts = orderId.split("-");
            if (orderIdParts.length < 2) {
                return ResponseEntity.status(400).body("Invalid order ID format");
            }

            String bookingIdStr = orderIdParts[1]; // Ambil bagian kedua setelah split
            Long bookingId = Long.parseLong(bookingIdStr);

            // Get booking
            Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);

            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();

                // Simpan transaksi ke database
                Transaction transaction = transactionService.createTransactionFromNotification(notification, booking);

                // Handle transaction status
                if (transactionStatus.equals("capture") || transactionStatus.equals("settlement")) {
                    if (fraudStatus.equals("accept")) {
                        // Payment success and accepted
                        booking.setStatus("CONFIRMED");
                        bookingService.updateBookingStatus(bookingId, "CONFIRMED");
                    }
                } else if (transactionStatus.equals("cancel") ||
                        transactionStatus.equals("deny") ||
                        transactionStatus.equals("expire")) {
                    // Payment failed
                    booking.setStatus("PAYMENT_FAILED");
                    bookingService.updateBookingStatus(bookingId, "PAYMENT_FAILED");
                } else if (transactionStatus.equals("pending")) {
                    // Payment pending
                    booking.setStatus("PENDING");
                    bookingService.updateBookingStatus(bookingId, "PENDING");
                }

                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.status(404).body("Booking not found");
            }
        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing notification: " + e.getMessage());
        }
    }

    @PostMapping("/update-status/{bookingId}")
    @ResponseBody
    public ResponseEntity<String> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            String transactionId = payload.get("transactionId");

            Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);

            if (bookingOpt.isPresent()) {
                bookingService.updateBookingStatus(bookingId, status);

                // Jika ada transactionId, cari transaksi dan update statusnya
                if (transactionId != null && !transactionId.isEmpty()) {
                    Optional<Transaction> transactionOpt = transactionService
                            .getTransactionByTransactionId(transactionId);
                    if (transactionOpt.isPresent()) {
                        Transaction transaction = transactionOpt.get();
                        transaction.setTransactionStatus(status);
                        transactionService.saveTransaction(transaction);
                    }
                }

                return ResponseEntity.ok("{\"success\": true}");
            } else {
                return ResponseEntity.status(404).body("{\"error\": \"Booking not found\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}