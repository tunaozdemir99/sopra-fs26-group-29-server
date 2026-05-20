package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PinPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.PinService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PinController.class)
public class PinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PinService pinService;

    private Pin createTestPin() {
        Trip trip = new Trip();
        trip.setTripId(1L);

        Pin pin = new Pin();
        pin.setPinId(1L);
        pin.setName("Eiffel Tower");
        pin.setLatitude(48.8584);
        pin.setLongitude(2.2945);
        pin.setTrip(trip);
        return pin;
    }

    // --- GET tests ---

    @Test
    public void getPins_returnsList() throws Exception {
        given(pinService.getPinsByTripId(1L, "valid-token")).willReturn(List.of(createTestPin()));

        mockMvc.perform(get("/trips/1/pins")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pinId", is(1)))
                .andExpect(jsonPath("$[0].name", is("Eiffel Tower")))
                .andExpect(jsonPath("$[0].latitude", is(48.8584)))
                .andExpect(jsonPath("$[0].longitude", is(2.2945)));
    }

    @Test
    public void getPins_tripNotFound_returns404() throws Exception {
        given(pinService.getPinsByTripId(99L, "valid-token"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

        mockMvc.perform(get("/trips/99/pins")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPins_unauthorized_returns401() throws Exception {
        given(pinService.getPinsByTripId(1L, "bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        mockMvc.perform(get("/trips/1/pins")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPins_notMember_returns403() throws Exception {
        given(pinService.getPinsByTripId(1L, "outsider-token"))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        mockMvc.perform(get("/trips/1/pins")
                        .header("Authorization", "Bearer outsider-token"))
                .andExpect(status().isForbidden());
    }

    // --- POST tests ---

    @Test
    public void createPin_validInput_returns201() throws Exception {
        given(pinService.createPin(Mockito.eq(1L), Mockito.any(), Mockito.eq("valid-token")))
                .willReturn(createTestPin());

        PinPostDTO dto = new PinPostDTO();
        dto.setName("Eiffel Tower");
        dto.setLatitude(48.8584);
        dto.setLongitude(2.2945);

        mockMvc.perform(post("/trips/1/pins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto))
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pinId", is(1)))
                .andExpect(jsonPath("$.name", is("Eiffel Tower")));
    }

    @Test
    public void createPin_unauthorized_returns401() throws Exception {
        given(pinService.createPin(Mockito.eq(1L), Mockito.any(), Mockito.eq("bad-token")))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        PinPostDTO dto = new PinPostDTO();
        dto.setName("Eiffel Tower");
        dto.setLatitude(48.8584);
        dto.setLongitude(2.2945);

        mockMvc.perform(post("/trips/1/pins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto))
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE tests ---

    @Test
    public void deletePin_validInput_returns204() throws Exception {
        mockMvc.perform(delete("/trips/1/pins/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        Mockito.verify(pinService, Mockito.times(1)).deletePin(1L, 1L, "valid-token");
    }

    @Test
    public void deletePin_pinNotFound_returns404() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found"))
                .when(pinService).deletePin(1L, 99L, "valid-token");

        mockMvc.perform(delete("/trips/1/pins/99")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletePin_unauthorized_returns401() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"))
                .when(pinService).deletePin(1L, 1L, "bad-token");

        mockMvc.perform(delete("/trips/1/pins/1")
                        .header("Authorization", "Bearer bad-token"))
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
