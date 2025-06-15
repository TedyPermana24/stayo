package com.stayo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {

    @Value("${midtrans.server.key:SB-Mid-server-9GO_1t9yB0CIgAFwdtt_xHZ3}")
    private String serverKey;

    @Value("${midtrans.client.key:SB-Mid-client-5yXhC7zkffeExYWE}")
    private String clientKey;

    @Value("${midtrans.is.production:false}")
    private boolean isProduction;

    @Bean
    public String initMidtransSdk() {
        com.midtrans.Midtrans.serverKey = serverKey;
        com.midtrans.Midtrans.clientKey = clientKey;
        com.midtrans.Midtrans.isProduction = isProduction;
        return "Midtrans SDK initialized";
    }
}