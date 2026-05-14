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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: MemberServiceTest
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/5/14 22:18
 * @ version 1.0
 */
public class MemberServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemberService memberService;

    private User admin;
    private User member;
    private Trip trip;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");
        admin.setToken("admin-token");

        member = new User();
        member.setId(2L);
        member.setUsername("bob");
        member.setToken("member-token");

        trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Paris 2026");
        trip.setAdmin(admin);
        trip.setMembers(new HashSet<>(Set.of(admin, member)));

        Mockito.when(userRepository.findByToken("admin-token")).thenReturn(admin);
        Mockito.when(userRepository.findByToken("member-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
    }

    // --- #118: Add member ---

    @Test
    public void addMember_validInput_success() {
        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("charlie");

        Mockito.when(userRepository.findByUsername("charlie")).thenReturn(newUser);

        User added = memberService.addMember(1L, "charlie", "admin-token");

        assertEquals("charlie", added.getUsername());
        assertTrue(trip.getMembers().contains(newUser));
    }

    @Test
    public void addMember_notAdmin_throwsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.addMember(1L, "charlie", "member-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void addMember_userNotFound_throwsNotFound() {
        Mockito.when(userRepository.findByUsername("ghost")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.addMember(1L, "ghost", "admin-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void addMember_alreadyMember_throwsBadRequest() {
        Mockito.when(userRepository.findByUsername("bob")).thenReturn(member);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.addMember(1L, "bob", "admin-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- #118: Remove member ---

    @Test
    public void removeMember_adminRemovesOther_success() {
        assertDoesNotThrow(() -> memberService.removeMember(1L, 2L, "admin-token"));
        assertFalse(trip.getMembers().contains(member));
    }

    @Test
    public void removeMember_memberLeavesSelf_success() {
        assertDoesNotThrow(() -> memberService.removeMember(1L, 2L, "member-token"));
        assertFalse(trip.getMembers().contains(member));
    }

    @Test
    public void removeMember_nonAdminRemovesOther_throwsForbidden() {
        User other = new User();
        other.setId(3L);
        other.setUsername("charlie");
        trip.getMembers().add(other);

        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(other));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.removeMember(1L, 3L, "member-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void removeMember_adminLeavesSelf_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.removeMember(1L, 1L, "admin-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- #120: Removed member loses access ---

    @Test
    public void removedMember_cannotAccessTrip() {
        // remove bob
        memberService.removeMember(1L, 2L, "admin-token");

        // verify bob is no longer in members
        assertFalse(trip.getMembers().contains(member));
        // getTripById would throw 403 for bob since he's not in members anymore
    }

    // --- #121: Transfer admin ---

    @Test
    public void transferAdmin_validInput_success() {
        memberService.transferAdmin(1L, 2L, "admin-token");

        assertEquals(member, trip.getAdmin());
    }

    @Test
    public void transferAdmin_notAdmin_throwsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.transferAdmin(1L, 1L, "member-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void transferAdmin_toSelf_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.transferAdmin(1L, 1L, "admin-token"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void transferAdmin_targetNotMember_throwsNotFound() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setUsername("stranger");

        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.transferAdmin(1L, 99L, "admin-token"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}