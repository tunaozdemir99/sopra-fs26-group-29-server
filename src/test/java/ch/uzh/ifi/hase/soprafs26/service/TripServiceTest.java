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
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    public void deleteTrip_validAdmin_success() {
        testTrip.setAdmin(testUser);
        testTrip.setMembers(new HashSet<>());
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        assertDoesNotThrow(() -> tripService.deleteTrip(1L, "valid-token"));
        Mockito.verify(tripRepository, Mockito.times(1)).delete(testTrip);
    }

    @Test
    public void deleteTrip_notAdmin_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("bob");
        testTrip.setAdmin(otherUser);

        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.deleteTrip(1L, "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void deleteTrip_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.deleteTrip(1L, "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void deleteTrip_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.deleteTrip(99L, "valid-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void getTripById_validMember_success() {
        testTrip.setAdmin(testUser);
        testTrip.addMember(testUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

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
        testTrip.setAdmin(otherUser);
        // testUser is NOT in members

        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.getTripById(1L, "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void getTripByInviteCode_validCode_returnsTrip() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteUrl("invite-abc");
        when(tripRepository.findByInviteUrl("invite-abc")).thenReturn(Optional.of(testTrip));

        Trip found = tripService.getTripByInviteCode("invite-abc", "valid-token");

        assertEquals(testTrip.getTripId(), found.getTripId());
    }

    @Test
    public void getTripByInviteCode_invalidCode_throwsNotFound() {
        when(tripRepository.findByInviteUrl("bad-code")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.getTripByInviteCode("bad-code", "valid-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void getTripByInviteCode_invalidToken_throwsUnauthorized() {
        when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.getTripByInviteCode("invite-abc", "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void joinTripByInviteCode_newMember_joinsSuccessfully() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteUrl("invite-abc");
        // testUser is NOT yet a member
        when(tripRepository.findByInviteUrl("invite-abc")).thenReturn(Optional.of(testTrip));

        TripService.JoinResult result = tripService.joinTripByInviteCode("invite-abc", "valid-token");

        assertFalse(result.alreadyMember);
        assertTrue(result.trip.getMembers().stream()
                .anyMatch(m -> m.getId().equals(testUser.getId())));
    }

    @Test
    public void joinTripByInviteCode_alreadyMember_returnsAlreadyMemberFlag() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteUrl("invite-abc");
        testTrip.addMember(testUser);
        when(tripRepository.findByInviteUrl("invite-abc")).thenReturn(Optional.of(testTrip));

        TripService.JoinResult result = tripService.joinTripByInviteCode("invite-abc", "valid-token");

        assertTrue(result.alreadyMember);
    }

    @Test
    public void joinTripByInviteCode_invalidCode_throwsNotFound() {
        when(tripRepository.findByInviteUrl("bad-code")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.joinTripByInviteCode("bad-code", "valid-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void joinTripByInviteCode_invalidToken_throwsUnauthorized() {
        when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.joinTripByInviteCode("invite-abc", "bad-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void getInviteUrl_admin_success() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteUrl("abc123");
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        String url = tripService.getInviteUrl(1L, "valid-token");

        assertEquals("abc123", url);
    }

    @Test
    public void getInviteUrl_notAdmin_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        testTrip.setAdmin(otherUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.getInviteUrl(1L, "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void regenerateInviteUrl_admin_success() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteUrl("old-url");
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        String newUrl = tripService.regenerateInviteUrl(1L, "valid-token");

        assertNotEquals("old-url", newUrl);
        assertNotNull(newUrl);
    }

    @Test
    public void setInviteActive_admin_success() {
        testTrip.setAdmin(testUser);
        testTrip.setInviteActive(true);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        tripService.setInviteActive(1L, false, "valid-token");

        assertFalse(testTrip.isInviteActive());
    }

    @Test
    public void setInviteActive_notAdmin_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        testTrip.setAdmin(otherUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tripService.setInviteActive(1L, false, "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
