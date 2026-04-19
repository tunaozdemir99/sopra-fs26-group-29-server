package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Activity;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.ActivityService;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    // --- GET /trips/{tripId}/timeline ---

    @Test
    public void getTimeline_validToken_returns200WithList() throws Exception {
        ActivityGetDTO dto = new ActivityGetDTO();
        dto.setActivityId(1L);
        dto.setName("Eiffel Tower");
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        given(activityService.getTimeline(anyLong(), anyString()))
                .willReturn(Collections.singletonList(dto));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Eiffel Tower")));
    }

    @Test
    public void getTimeline_invalidToken_returns401() throws Exception {
        given(activityService.getTimeline(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getTimeline_userNotMember_returns403() throws Exception {
        given(activityService.getTimeline(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    // --- POST /trips/{tripId}/timeline ---

    @Test
    public void scheduleFromBucket_validInput_returns201() throws Exception {
        Activity activity = new Activity();
        activity.setActivityId(1L);
        activity.setName("Eiffel Tower");
        activity.setDate(LocalDate.of(2026, 8, 1));
        activity.setStartTime(LocalTime.of(9, 0));
        activity.setEndTime(LocalTime.of(11, 0));
        activity.setFromBucketItem(true);

        given(activityService.scheduleFromBucket(
                anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyString()))
                .willReturn(activity);

        ActivityPostDTO postDTO = new ActivityPostDTO();
        postDTO.setBucketItemId(10L);
        postDTO.setDate(LocalDate.of(2026, 8, 1));
        postDTO.setStartTime(LocalTime.of(9, 0));
        postDTO.setEndTime(LocalTime.of(11, 0));

        MockHttpServletRequestBuilder postRequest = post("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Eiffel Tower")))
                .andExpect(jsonPath("$.fromBucketItem", is(true)));
    }

    @Test
    public void scheduleFromBucket_invalidToken_returns401() throws Exception {
        given(activityService.scheduleFromBucket(
                anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        ActivityPostDTO postDTO = new ActivityPostDTO();
        postDTO.setBucketItemId(10L);
        postDTO.setDate(LocalDate.of(2026, 8, 1));
        postDTO.setStartTime(LocalTime.of(9, 0));
        postDTO.setEndTime(LocalTime.of(11, 0));

        MockHttpServletRequestBuilder postRequest = post("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void scheduleFromBucket_bucketItemNotFound_returns404() throws Exception {
        given(activityService.scheduleFromBucket(
                anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));

        ActivityPostDTO postDTO = new ActivityPostDTO();
        postDTO.setBucketItemId(99L);
        postDTO.setDate(LocalDate.of(2026, 8, 1));
        postDTO.setStartTime(LocalTime.of(9, 0));
        postDTO.setEndTime(LocalTime.of(11, 0));

        MockHttpServletRequestBuilder postRequest = post("/trips/1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    // --- DELETE /trips/{tripId}/timeline/{activityId} ---

    @Test
    public void deleteActivity_validToken_returns204() throws Exception {
        Mockito.doNothing().when(activityService)
                .deleteActivity(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/timeline/100")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteActivity_invalidToken_returns401() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"))
                .when(activityService).deleteActivity(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/timeline/100")
                .header("Authorization", "Bearer bad-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteActivity_activityNotFound_returns404() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"))
                .when(activityService).deleteActivity(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/timeline/99")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound());
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
