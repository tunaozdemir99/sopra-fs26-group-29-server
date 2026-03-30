package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
