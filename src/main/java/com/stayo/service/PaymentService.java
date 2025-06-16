package com.stayo.service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.midtrans.httpclient.SnapApi;
import com.midtrans.httpclient.error.MidtransError;
import com.stayo.model.Booking;
import com.stayo.model.PaymentResponse;

@Service
public class PaymentService {

    public PaymentResponse createTransaction(Booking booking) throws MidtransError {
        Map<String, Object> params = new HashMap<>();

        Map<String, String> transactionDetails = new HashMap<>();
        // Ubah ini:
        transactionDetails.put("order_id", "STAYO-" + booking.getId());

        // Menjadi ini (tambahkan timestamp):
        String timestamp = String.valueOf(System.currentTimeMillis());
        transactionDetails.put("order_id", "STAYO-" + booking.getId() + "-" + timestamp);
        transactionDetails.put("gross_amount", booking.getTotalPrice().toString());

        Map<String, Object> creditCard = new HashMap<>();
        creditCard.put("secure", true);

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("first_name", booking.getUser().getFirstName());
        customerDetails.put("last_name", booking.getUser().getLastName());
        customerDetails.put("email", booking.getUser().getEmail());
        customerDetails.put("phone", booking.getUser().getPhoneNumber());

        Map<String, String> itemDetails = new HashMap<>();
        itemDetails.put("id", "ROOM-" + booking.getRoom().getId());
        itemDetails.put("price", booking.getRoom().getPricePerNight().toString());
        itemDetails.put("quantity",
                String.valueOf(ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate())));
        itemDetails.put("name", booking.getRoom().getType() + " at " + booking.getHotel().getName().trim().split("\\s+")[0]);

        // Tambahkan callback URL
        Map<String, String> callbackUrls = new HashMap<>();
        callbackUrls.put("finish", "http://localhost:8080/bookings");
        callbackUrls.put("error", "http://localhost:8080/bookings");
        callbackUrls.put("pending", "http://localhost:8080/bookings");

        // Untuk notifikasi, gunakan URL ngrok Anda
        // Contoh: https://abcd-123-456-789-10.ngrok.io/payments/notification
        callbackUrls.put("notification", " https://c906-114-122-110-106.ngrok-free.app/payments/notification");

        params.put("transaction_details", transactionDetails);
        params.put("credit_card", creditCard);
        params.put("customer_details", customerDetails);
        params.put("item_details", itemDetails);
        params.put("callbacks", callbackUrls);

        JSONObject response = SnapApi.createTransaction(params);

        return new PaymentResponse(
                response.getString("token"),
                response.getString("redirect_url"));
    }
}