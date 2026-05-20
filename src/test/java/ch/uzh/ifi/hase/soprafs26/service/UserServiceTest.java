package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being saved in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	// --- logoutUser ---

	@Test
	public void logoutUser_userNotFound_throwsNotFound() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.logoutUser(99L, "valid-token"));
	}

	@Test
	public void logoutUser_wrongToken_throwsUnauthorized() {
		testUser.setToken("correct-token");
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		assertThrows(ResponseStatusException.class, () -> userService.logoutUser(1L, "wrong-token"));
	}

	@Test
	public void logoutUser_validToken_setsOfflineAndNullsToken() {
		testUser.setToken("valid-token");
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);

		userService.logoutUser(1L, "valid-token");

		assertNull(testUser.getToken());
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
		Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
	}

	// --- getUserById ---

	@Test
	public void getUserById_userNotFound_throwsNotFound() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUserById(99L));
	}

	@Test
	public void getUserById_validId_returnsUser() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		User found = userService.getUserById(1L);

		assertEquals(testUser.getId(), found.getId());
		assertEquals(testUser.getUsername(), found.getUsername());
	}


    // --- createUser validation ---

    @Test
    public void createUser_missingUsername_throwsBadRequest() {
        User noUsername = new User();
        noUsername.setPassword("pass");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(noUsername));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void createUser_missingPassword_throwsBadRequest() {
        User noPassword = new User();
        noPassword.setUsername("alice");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(noPassword));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- updateUser ---

    @Test
    public void updateUser_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, "bad-token", new User()));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void updateUser_wrongOwner_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setToken("other-token");
        Mockito.when(userRepository.findByToken("other-token")).thenReturn(otherUser);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, "other-token", new User()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void updateUser_validInput_success() {
        testUser.setToken("valid-token");
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User updates = new User();
        updates.setBio("New bio");

        User updated = userService.updateUser(1L, "valid-token", updates);

        assertEquals("New bio", updated.getBio());
        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
    }
}
