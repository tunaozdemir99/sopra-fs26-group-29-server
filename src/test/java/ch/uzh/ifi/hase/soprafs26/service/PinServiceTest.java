package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.repository.PinRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: PinServiceTest
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/4/21 22:36
 * @ version 1.0
 */
public class PinServiceTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private PinService pinService;

    private Trip testTrip;
    private Pin testPin;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testTrip = new Trip();
        testTrip.setTripId(1L);
        testTrip.setTitle("Paris 2026");

        testPin = new Pin();
        testPin.setPinId(1L);
        testPin.setName("Eiffel Tower");
        testPin.setLatitude(48.8584);
        testPin.setLongitude(2.2945);
        testPin.setTrip(testTrip);

        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
    }

    // --- GET tests ---

    @Test
    public void getPins_validTrip_returnsList() {
        Mockito.when(pinRepository.findByTrip_TripId(1L)).thenReturn(List.of(testPin));

        List<Pin> pins = pinService.getPinsByTripId(1L);

        assertEquals(1, pins.size());
        assertEquals("Eiffel Tower", pins.get(0).getName());
    }

    @Test
    public void getPins_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.getPinsByTripId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- POST tests ---

    @Test
    public void createPin_validInput_success() {
        Pin input = new Pin();
        input.setName("Louvre");
        input.setLatitude(48.8606);
        input.setLongitude(2.3376);

        Mockito.when(pinRepository.save(Mockito.any())).thenReturn(testPin);

        Pin created = pinService.createPin(1L, input);

        Mockito.verify(pinRepository, Mockito.times(1)).save(Mockito.any());
        assertNotNull(created.getPinId());
    }

    @Test
    public void createPin_missingName_throwsBadRequest() {
        Pin input = new Pin();
        input.setLatitude(48.8606);
        input.setLongitude(2.3376);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.createPin(1L, input));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void createPin_missingCoordinates_throwsBadRequest() {
        Pin input = new Pin();
        input.setName("Louvre");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.createPin(1L, input));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- DELETE tests ---

    @Test
    public void deletePin_validInput_success() {
        Mockito.when(pinRepository.findById(1L)).thenReturn(Optional.of(testPin));

        assertDoesNotThrow(() -> pinService.deletePin(1L, 1L));
        Mockito.verify(pinRepository, Mockito.times(1)).delete(testPin);
    }

    @Test
    public void deletePin_pinNotFound_throwsNotFound() {
        Mockito.when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.deletePin(1L, 99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void deletePin_pinBelongsToDifferentTrip_throwsBadRequest() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        testPin.setTrip(otherTrip);

        Mockito.when(pinRepository.findById(1L)).thenReturn(Optional.of(testPin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.deletePin(1L, 1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
