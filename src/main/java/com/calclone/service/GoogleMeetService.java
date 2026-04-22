package com.calclone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GoogleMeetService {

    public String createGoogleMeet(String accessToken) {

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
                "dateTime": "2026-04-22T10:00:00+05:30"
              },
              "end": {
                "dateTime": "2026-04-22T10:30:00+05:30"
              },
              "conferenceData": {
                "createRequest": {
                  "requestId": "%s"
                }
              }
            }
            """.formatted(requestId);

            // send request
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();


            int responseCode = conn.getResponseCode();
            System.out.println("Google API Response Code: " + responseCode);


            BufferedReader br = responseCode >= 200 && responseCode < 300
                    ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            System.out.println("Google Response: " + response);


            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            if (root.has("hangoutLink")) {
                return root.get("hangoutLink").asText();
            }

            JsonNode entryPoints = root
                    .path("conferenceData")
                    .path("entryPoints");

            if (entryPoints.isArray()) {
                for (JsonNode ep : entryPoints) {
                    if ("video".equals(ep.path("entryPointType").asText())) {
                        return ep.path("uri").asText();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}