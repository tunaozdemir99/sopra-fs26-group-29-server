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

}
