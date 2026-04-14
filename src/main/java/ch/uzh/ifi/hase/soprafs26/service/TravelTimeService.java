package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TravelTimeService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ROUTES_API_URL =
        "https://routes.googleapis.com/directions/v2:computeRoutes";

    /**
     * Returns travel time in minutes between two coordinates using the Google Maps Routes API.
     * Returns null if the API call fails or the key is not configured.
     */
    public Integer computeTravelMinutes(double fromLat, double fromLng,
                                        double toLat, double toLng) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        Map<String, Object> body = Map.of(
            "origin", Map.of("location", Map.of("latLng", Map.of(
                "latitude", fromLat, "longitude", fromLng))),
            "destination", Map.of("location", Map.of("latLng", Map.of(
                "latitude", toLat, "longitude", toLng))),
            "travelMode", "DRIVE",
            "routingPreference", "TRAFFIC_UNAWARE"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "routes.duration");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                ROUTES_API_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
            );

            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null) return null;

            List<?> routes = (List<?>) responseBody.get("routes");
            if (routes == null || routes.isEmpty()) return null;

            Map<?, ?> route = (Map<?, ?>) routes.get(0);
            String durationStr = (String) route.get("duration"); // e.g. "259s"
            if (durationStr == null) return null;

            // parse "259s" -> 259 seconds -> round up to minutes
            int seconds = Integer.parseInt(durationStr.replace("s", ""));
            return (int) Math.ceil(seconds / 60.0);

        } catch (Exception e) {
            return null;
        }
    }
}
