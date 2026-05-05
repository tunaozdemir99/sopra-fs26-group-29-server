package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinTripRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.TripService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ClassName: TripControllerTest
 * Package: ch.uzh.ifi.hase.soprafs26.controller
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 16:07
 * @ version 1.0
 */
@WebMvcTest(TripController.class)
public class TripControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @Test
    public void createTrip_validInput_tripCreated() throws Exception {
        // given
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Paris 2026");
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));
        trip.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        trip.setInviteUrl("abc123");
        trip.setAdmin(admin);

        TripPostDTO tripPostDTO = new TripPostDTO();
        tripPostDTO.setTitle("Paris 2026");
        tripPostDTO.setStartDate(LocalDate.of(2026, 8, 1));
        tripPostDTO.setEndDate(LocalDate.of(2026, 8, 10));

        given(tripService.createTrip(Mockito.any(), Mockito.anyString())).willReturn(trip);

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(tripPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tripId", is(1)))
                .andExpect(jsonPath("$.title", is("Paris 2026")))
                .andExpect(jsonPath("$.adminUsername", is("alice")))
                .andExpect(jsonPath("$.inviteUrl", is("abc123")));
    }

    @Test
    public void createTrip_invalidToken_returnsUnauthorized() throws Exception {
        // given: service throws 401 for bad token
        given(tripService.createTrip(Mockito.any(), Mockito.anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        TripPostDTO tripPostDTO = new TripPostDTO();
        tripPostDTO.setTitle("Paris 2026");
        tripPostDTO.setStartDate(LocalDate.of(2026, 8, 1));
        tripPostDTO.setEndDate(LocalDate.of(2026, 8, 10));

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token")
                .content(asJsonString(tripPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getTrip_validMember_returnsTrip() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Paris 2026");
        trip.setLocation("Paris");
        trip.setStartDate(LocalDate.of(2026, 8, 1));
        trip.setEndDate(LocalDate.of(2026, 8, 10));
        trip.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        trip.setInviteUrl("abc123");
        trip.setAdmin(admin);

        given(tripService.getTripById(Mockito.eq(1L), Mockito.anyString())).willReturn(trip);

        MockHttpServletRequestBuilder getRequest = get("/trips/1")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId", is(1)))
                .andExpect(jsonPath("$.title", is("Paris 2026")))
                .andExpect(jsonPath("$.adminUsername", is("alice")));
    }

    @Test
    public void getTrip_notMember_returnsForbidden() throws Exception {
        given(tripService.getTripById(Mockito.anyLong(), Mockito.anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member"));

        MockHttpServletRequestBuilder getRequest = get("/trips/1")
                .header("Authorization", "Bearer some-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void getTripByInviteCode_validCode_returnsTrip() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Rome 2026");
        trip.setStartDate(LocalDate.of(2026, 9, 1));
        trip.setEndDate(LocalDate.of(2026, 9, 10));
        trip.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        trip.setInviteUrl("invite-abc");
        trip.setAdmin(admin);

        given(tripService.getTripByInviteCode(Mockito.eq("invite-abc"), Mockito.anyString()))
                .willReturn(trip);

        MockHttpServletRequestBuilder getRequest = get("/trips/invite/invite-abc")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId", is(1)))
                .andExpect(jsonPath("$.title", is("Rome 2026")))
                .andExpect(jsonPath("$.inviteUrl", is("invite-abc")));
    }

    @Test
    public void getTripByInviteCode_invalidCode_returnsNotFound() throws Exception {
        given(tripService.getTripByInviteCode(Mockito.anyString(), Mockito.anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired invite link"));

        MockHttpServletRequestBuilder getRequest = get("/trips/invite/bad-code")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinTrip_newMember_returnsSuccessMessage() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Rome 2026");
        trip.setStartDate(LocalDate.of(2026, 9, 1));
        trip.setEndDate(LocalDate.of(2026, 9, 10));
        trip.setInviteUrl("invite-abc");
        trip.setAdmin(admin);

        TripService.JoinResult joinResult = new TripService.JoinResult(trip, false);
        given(tripService.joinTripByInviteCode(Mockito.eq("invite-abc"), Mockito.anyString()))
                .willReturn(joinResult);

        JoinTripRequestDTO requestDTO = new JoinTripRequestDTO();
        requestDTO.setInviteCode("invite-abc");

        MockHttpServletRequestBuilder postRequest = post("/trips/join")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(requestDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId", is(1)))
                .andExpect(jsonPath("$.alreadyMember", is(false)))
                .andExpect(jsonPath("$.message", is("Successfully joined the trip")));
    }

    @Test
    public void joinTrip_alreadyMember_returnsAlreadyMemberMessage() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("alice");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Rome 2026");
        trip.setStartDate(LocalDate.of(2026, 9, 1));
        trip.setEndDate(LocalDate.of(2026, 9, 10));
        trip.setInviteUrl("invite-abc");
        trip.setAdmin(admin);

        TripService.JoinResult joinResult = new TripService.JoinResult(trip, true);
        given(tripService.joinTripByInviteCode(Mockito.eq("invite-abc"), Mockito.anyString()))
                .willReturn(joinResult);

        JoinTripRequestDTO requestDTO = new JoinTripRequestDTO();
        requestDTO.setInviteCode("invite-abc");

        MockHttpServletRequestBuilder postRequest = post("/trips/join")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(requestDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyMember", is(true)))
                .andExpect(jsonPath("$.message", is("You are already a member of this trip")));
    }

    @Test
    public void joinTrip_invalidCode_returnsNotFound() throws Exception {
        given(tripService.joinTripByInviteCode(Mockito.anyString(), Mockito.anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired invite link"));

        JoinTripRequestDTO requestDTO = new JoinTripRequestDTO();
        requestDTO.setInviteCode("bad-code");

        MockHttpServletRequestBuilder postRequest = post("/trips/join")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(requestDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinTrip_invalidToken_returnsUnauthorized() throws Exception {
        given(tripService.joinTripByInviteCode(Mockito.anyString(), Mockito.anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        JoinTripRequestDTO requestDTO = new JoinTripRequestDTO();
        requestDTO.setInviteCode("invite-abc");

        MockHttpServletRequestBuilder postRequest = post("/trips/join")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token")
                .content(asJsonString(requestDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
