package com.calclone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleMeetService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public String createGoogleMeet(String accessToken, String startDateTime, String endDateTime, String guestEmail) {

        try {
            String urlStr =
                    "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+05:30");

            String requestId = "req-" + System.currentTimeMillis();

            String json = """
        {
          "summary": "Meeting",
          "start": {
            "dateTime": "%s"
          },
          "end": {
            "dateTime": "%s"
          },
          "attendees": [
            { "email": "%s" }
          ],
          "conferenceData": {
            "createRequest": {
              "requestId": "%s",
              "conferenceSolutionKey": {
                "type": "hangoutsMeet"
              }
            }
          }
        }
        """.formatted(startDateTime, endDateTime, guestEmail, requestId);

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            return root.path("hangoutLink").asText();


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<String[]> getBusySlots(String accessToken, String date) {
        try {
            String urlStr = "https://www.googleapis.com/calendar/v3/freeBusy";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = """
        {
          "timeMin": "%sT00:00:00+05:30",
          "timeMax": "%sT23:59:59+05:30",
          "items": [{"id": "primary"}]
        }
        """.formatted(date, date);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            List<String[]> busy = new ArrayList<>();
            JsonNode arr = root.path("calendars").path("primary").path("busy");

            for (JsonNode node : arr) {
                busy.add(new String[]{
                        node.get("start").asText(),
                        node.get("end").asText()
                });
            }

            return busy;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public String refreshAccessToken(String refreshToken) {
        try {
            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&refresh_token=" + refreshToken
                    + "&grant_type=refresh_token";

            conn.getOutputStream().write(body.getBytes());

            int status = conn.getResponseCode();
            InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);

            System.out.println("Token refresh response: " + response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            String newToken = root.path("access_token").asText();
            return newToken.isEmpty() ? null : newToken;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}