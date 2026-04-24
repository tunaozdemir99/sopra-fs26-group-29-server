package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		userRepository.deleteAll();
	}

	@Test
	public void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

        // when
		User createdUser = userService.createUser(testUser);

		// then
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
		userService.createUser(testUser);

		// attempt to create second user with same username
		User testUser2 = new User();
        testUser2.setUsername("testUsername");
        testUser2.setPassword("testPassword");

        // check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	@Test
	public void loginUser_validCredentials_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		User createdUser = userService.createUser(testUser);
		String tokenAfterRegistration = createdUser.getToken();

		// when
		User loginUser = new User();
		loginUser.setUsername("testUsername");
		loginUser.setPassword("testPassword");
		User loggedInUser = userService.loginUser(loginUser);

		// then
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
		assertNotEquals(tokenAfterRegistration, loggedInUser.getToken()); // token rotated
	}

	@Test
	public void loginUser_invalidPassword_throwsException() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		userService.createUser(testUser);

		// when / then
		User loginUser = new User();
		loginUser.setUsername("testUsername");
		loginUser.setPassword("wrongPassword");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
	}

	@Test
	public void loginUser_nonExistentUser_throwsException() {
		// given - no user created

		// when / then
		User loginUser = new User();
		loginUser.setUsername("ghost");
		loginUser.setPassword("somePassword");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
		}

	@Test
	public void logoutUser_validUser_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		userService.createUser(testUser);

		User loginUser = new User();
		loginUser.setUsername("testUsername");
		loginUser.setPassword("testPassword");
		User loggedInUser = userService.loginUser(loginUser);

		// when
		userService.logoutUser(loggedInUser.getId(), loggedInUser.getToken());

		// then
		User afterLogout = userService.getUserById(loggedInUser.getId());
		assertEquals(UserStatus.OFFLINE, afterLogout.getStatus());
		assertNull(afterLogout.getToken());
	}

	@Test
	public void logoutUser_nonExistentUser_throwsException() {
    	// given - no user exists with this id

    	// when / then
		assertThrows(ResponseStatusException.class, () -> userService.logoutUser(999L, "any-token"));
}
}
