package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ClassName: TripServiceTest
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 16:06
 * @ version 1.0
 */
public class TripServiceTest {
    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    private User testUser;
    private Trip testTrip;

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
        testTrip.setStartDate(LocalDate.of(2026, 8, 1));
        testTrip.setEndDate(LocalDate.of(2026, 8, 10));

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
        Mockito.when(tripRepository.save(Mockito.any())).thenReturn(testTrip);
    }

    @Test
    public void createTrip_validInput_success() {
        Trip input = new Trip();
        input.setTitle("Paris 2026");
        input.setStartDate(LocalDate.of(2026, 8, 1));
        input.setEndDate(LocalDate.of(2026, 8, 10));

        Trip created = tripService.createTrip(input, "valid-token");

        Mockito.verify(tripRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals("Paris 2026", created.getTitle());
        assertEquals(1L, created.getTripId());
    }

    @Test
    public void createTrip_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        Trip input = new Trip();
        input.setTitle("Paris 2026");
        input.setStartDate(LocalDate.of(2026, 8, 1));
        input.setEndDate(LocalDate.of(2026, 8, 10));

        assertThrows(ResponseStatusException.class,
                () -> tripService.createTrip(input, "bad-token"));
    }

    @Test
    public void createTrip_endDateBeforeStartDate_throwsBadRequest() {
        Trip input = new Trip();
        input.setTitle("Bad Trip");
        input.setStartDate(LocalDate.of(2026, 8, 10));
        input.setEndDate(LocalDate.of(2026, 8, 1));

        assertThrows(ResponseStatusException.class,
                () -> tripService.createTrip(input, "valid-token"));
    }

    @Test
    public void getTripById_validAdmin_success() {
        testTrip.setAdmin(testUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(java.util.Optional.of(testTrip));

        Trip found = tripService.getTripById(1L, "valid-token");

        assertEquals(testTrip.getTripId(), found.getTripId());
    }

    @Test
    public void getTripById_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> tripService.getTripById(1L, "bad-token"));
    }

    @Test
    public void getTripById_notMember_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("bob");
        otherUser.setToken("bob-token");

        testTrip.setAdmin(otherUser); // trip belongs to bob, not testUser

        Mockito.when(tripRepository.findById(1L)).thenReturn(java.util.Optional.of(testTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.getTripById(1L, "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
