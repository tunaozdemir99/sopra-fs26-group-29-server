package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PinRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
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

public class PinServiceTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PinService pinService;

    private Trip testTrip;
    private Pin testPin;
    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("alice");
        testUser.setToken("valid-token");

        testTrip = new Trip();
        testTrip.setTripId(1L);
        testTrip.setTitle("Paris 2026");
        testTrip.addMember(testUser);

        testPin = new Pin();
        testPin.setPinId(1L);
        testPin.setName("Eiffel Tower");
        testPin.setLatitude(48.8584);
        testPin.setLongitude(2.2945);
        testPin.setTrip(testTrip);

        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
    }

    // --- Auth tests ---

    @Test
    public void getPins_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.getPinsByTripId(1L, "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void getPins_notMember_throwsForbidden() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setUsername("outsider");
        Mockito.when(userRepository.findByToken("outsider-token")).thenReturn(outsider);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.getPinsByTripId(1L, "outsider-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void createPin_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);
        Pin input = new Pin();
        input.setName("Louvre");
        input.setLatitude(48.8606);
        input.setLongitude(2.3376);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.createPin(1L, input, "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void deletePin_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.deletePin(1L, 1L, "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    // --- GET tests ---

    @Test
    public void getPins_validTrip_returnsList() {
        Mockito.when(pinRepository.findByTrip_TripId(1L)).thenReturn(List.of(testPin));

        List<Pin> pins = pinService.getPinsByTripId(1L, "valid-token");

        assertEquals(1, pins.size());
        assertEquals("Eiffel Tower", pins.get(0).getName());
    }

    @Test
    public void getPins_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.getPinsByTripId(99L, "valid-token"));
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

        Pin created = pinService.createPin(1L, input, "valid-token");

        Mockito.verify(pinRepository, Mockito.times(1)).save(Mockito.any());
        assertNotNull(created.getPinId());
    }

    @Test
    public void createPin_missingName_throwsBadRequest() {
        Pin input = new Pin();
        input.setLatitude(48.8606);
        input.setLongitude(2.3376);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.createPin(1L, input, "valid-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void createPin_missingCoordinates_throwsBadRequest() {
        Pin input = new Pin();
        input.setName("Louvre");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.createPin(1L, input, "valid-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- DELETE tests ---

    @Test
    public void deletePin_validInput_success() {
        Mockito.when(pinRepository.findById(1L)).thenReturn(Optional.of(testPin));

        assertDoesNotThrow(() -> pinService.deletePin(1L, 1L, "valid-token"));
        Mockito.verify(pinRepository, Mockito.times(1)).delete(testPin);
    }

    @Test
    public void deletePin_pinNotFound_throwsNotFound() {
        Mockito.when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.deletePin(1L, 99L, "valid-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void deletePin_pinBelongsToDifferentTrip_throwsBadRequest() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        testPin.setTrip(otherTrip);

        Mockito.when(pinRepository.findById(1L)).thenReturn(Optional.of(testPin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pinService.deletePin(1L, 1L, "valid-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
