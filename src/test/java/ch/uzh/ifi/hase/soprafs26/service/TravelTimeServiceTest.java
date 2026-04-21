package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class TravelTimeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TravelTimeService travelTimeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        travelTimeService = new TravelTimeService();
        ReflectionTestUtils.setField(travelTimeService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(travelTimeService, "apiKey", "test-api-key");
    }

    @Test
    public void computeTravelMinutes_noApiKey_returnsNull() {
        ReflectionTestUtils.setField(travelTimeService, "apiKey", "");

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertNull(result);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void computeTravelMinutes_validResponse_returnsMinutes() {
        Map<String, Object> responseBody = Map.of(
                "routes", List.of(Map.of("duration", "120s")));
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertEquals(2, result);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void computeTravelMinutes_roundsUpSeconds_returnsCorrectMinutes() {
        // 61 seconds -> ceil(61/60) = 2 minutes
        Map<String, Object> responseBody = Map.of(
                "routes", List.of(Map.of("duration", "61s")));
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertEquals(2, result);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void computeTravelMinutes_emptyRoutes_returnsNull() {
        Map<String, Object> responseBody = Map.of("routes", List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertNull(result);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void computeTravelMinutes_nullResponseBody_returnsNull() {
        ResponseEntity<Map> response = new ResponseEntity<>((Map) null, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertNull(result);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void computeTravelMinutes_apiCallThrowsException_returnsNull() {
        Mockito.when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        Integer result = travelTimeService.computeTravelMinutes(48.0, 2.0, 48.1, 2.1);

        assertNull(result);
    }
}
