package com.calclone.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(name = "app.ping.enabled", havingValue = "true")
public class SelfPingScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.base-url}")
    private String baseUrl;

    @Scheduled(fixedRateString = "${app.ping.rate:300000}")
    public void pingSelf() {
        if (baseUrl == null || baseUrl.isBlank()) {
            System.out.println("Base URL not set, skipping ping...");
            return;
        }

        try {
            String url = baseUrl + "/ping";
            restTemplate.getForObject(url, String.class);
            System.out.println("Pinged: " + url);
        } catch (Exception e) {
            System.out.println("Ping failed: " + e.getMessage());
        }
    }
}