package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.BucketItemService;
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

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BucketItemController.class)
public class BucketItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BucketItemService bucketItemService;

    // --- GET /trips/{tripId}/bucketItems ---

    @Test
    public void getBucketItems_validToken_returns200WithList() throws Exception {
        BucketItemGetDTO dto = new BucketItemGetDTO();
        UserGetDTO addedByDTO = new UserGetDTO();
        addedByDTO.setId(1L);
        addedByDTO.setUsername("alice");

        dto.setBucketItemId(10L);
        dto.setName("Eiffel Tower");
        dto.setAddedBy(addedByDTO);
        dto.setVoteScore(0);

        given(bucketItemService.getBucketItems(anyLong(), anyString()))
                .willReturn(Collections.singletonList(dto));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Eiffel Tower")))
                .andExpect(jsonPath("$[0].addedBy.username", is("alice")));
    }

    @Test
    public void getBucketItems_invalidToken_returns401() throws Exception {
        given(bucketItemService.getBucketItems(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getBucketItems_userNotMember_returns403() throws Exception {
        given(bucketItemService.getBucketItems(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        MockHttpServletRequestBuilder getRequest = get("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    // --- POST /trips/{tripId}/bucketItems ---

    @Test
    public void addBucketItem_validInput_returns201() throws Exception {
        User author = new User();
        author.setId(1L);
        author.setUsername("alice");

        BucketItem saved = new BucketItem();
        saved.setBucketItemId(10L);
        saved.setName("Eiffel Tower");
        saved.setAddedBy(author);

        given(bucketItemService.addBucketItem(anyLong(), any(), anyString())).willReturn(saved);

        BucketItemPostDTO postDTO = new BucketItemPostDTO();
        postDTO.setName("Eiffel Tower");

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Eiffel Tower")))
                .andExpect(jsonPath("$.addedBy.username", is("alice")));
    }

    @Test
    public void addBucketItem_invalidToken_returns401() throws Exception {
        given(bucketItemService.addBucketItem(anyLong(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        BucketItemPostDTO postDTO = new BucketItemPostDTO();
        postDTO.setName("Eiffel Tower");

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void addBucketItem_blankName_returns400() throws Exception {
        given(bucketItemService.addBucketItem(anyLong(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required"));

        BucketItemPostDTO postDTO = new BucketItemPostDTO();
        postDTO.setName("  ");

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
    }

        @Test
        public void addBucketItem_blankLocation_returns400() throws Exception {
        given(bucketItemService.addBucketItem(anyLong(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required"));

        BucketItemPostDTO postDTO = new BucketItemPostDTO();
        postDTO.setName("Eiffel Tower");
        postDTO.setLocation("  ");

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
        }


    // --- PATCH /trips/{tripId}/bucketItems/{itemId} ---

    @Test
    public void updateBucketItem_validInput_returns200() throws Exception {
        User author = new User();
        author.setId(1L);
        author.setUsername("alice");

        BucketItem updated = new BucketItem();
        updated.setBucketItemId(10L);
        updated.setName("Louvre");
        updated.setAddedBy(author);

        given(bucketItemService.updateBucketItem(anyLong(), anyLong(), any(), anyString()))
                .willReturn(updated);

        BucketItemPatchDTO patchDTO = new BucketItemPatchDTO();
        patchDTO.setName("Louvre");

        MockHttpServletRequestBuilder patchRequest = patch("/trips/1/bucketItems/10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(patchDTO));

        mockMvc.perform(patchRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Louvre")));
    }

    @Test
    public void updateBucketItem_notAuthor_returns403() throws Exception {
        given(bucketItemService.updateBucketItem(anyLong(), anyLong(), any(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this item"));

        BucketItemPatchDTO patchDTO = new BucketItemPatchDTO();
        patchDTO.setName("Louvre");

        MockHttpServletRequestBuilder patchRequest = patch("/trips/1/bucketItems/10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(patchDTO));

        mockMvc.perform(patchRequest)
                .andExpect(status().isForbidden());
    }

    // --- DELETE /trips/{tripId}/bucketItems/{itemId} ---

    @Test
    public void deleteBucketItem_validAuthor_returns204() throws Exception {
        Mockito.doNothing().when(bucketItemService)
                .deleteBucketItem(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/bucketItems/10")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteBucketItem_notAuthor_returns403() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this item"))
                .when(bucketItemService).deleteBucketItem(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/bucketItems/10")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteBucketItem_invalidToken_returns401() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"))
                .when(bucketItemService).deleteBucketItem(anyLong(), anyLong(), anyString());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/1/bucketItems/10")
                .header("Authorization", "Bearer bad-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isUnauthorized());
    }

        // --- POST /trips/{tripId}/bucketItems/{itemId}/vote ---

        @Test
        public void vote_validUpvote_returns200WithUpdatedScore() throws Exception {
        UserGetDTO addedByDTO = new UserGetDTO();
        addedByDTO.setId(1L);
        addedByDTO.setUsername("alice");

        BucketItemGetDTO dto = new BucketItemGetDTO();
        dto.setBucketItemId(10L);
        dto.setName("Eiffel Tower");
        dto.setAddedBy(addedByDTO);
        dto.setVoteScore(1);
        dto.setMyVote(1);

        given(bucketItemService.vote(anyLong(), anyLong(), anyInt(), anyString()))
                .willReturn(dto);

        VotePostDTO voteDTO = new VotePostDTO();
        voteDTO.setValue(1);

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems/10/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(voteDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteScore", is(1)))
                .andExpect(jsonPath("$.myVote", is(1)));
        }

        @Test
        public void vote_retractVote_returns200WithZeroScore() throws Exception {
        UserGetDTO addedByDTO = new UserGetDTO();
        addedByDTO.setId(1L);
        addedByDTO.setUsername("alice");

        BucketItemGetDTO dto = new BucketItemGetDTO();
        dto.setBucketItemId(10L);
        dto.setName("Eiffel Tower");
        dto.setAddedBy(addedByDTO);
        dto.setVoteScore(0);
        dto.setMyVote(0);

        given(bucketItemService.vote(anyLong(), anyLong(), anyInt(), anyString()))
                .willReturn(dto);

        VotePostDTO voteDTO = new VotePostDTO();
        voteDTO.setValue(0);

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems/10/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(voteDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteScore", is(0)))
                .andExpect(jsonPath("$.myVote", is(0)));
        }

        @Test
        public void vote_invalidToken_returns401() throws Exception {
        given(bucketItemService.vote(anyLong(), anyLong(), anyInt(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token"));

        VotePostDTO voteDTO = new VotePostDTO();
        voteDTO.setValue(1);

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems/10/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer bad-token")
                .content(asJsonString(voteDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
        }

        @Test
        public void vote_userNotMember_returns403() throws Exception {
        given(bucketItemService.vote(anyLong(), anyLong(), anyInt(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        VotePostDTO voteDTO = new VotePostDTO();
        voteDTO.setValue(1);

        MockHttpServletRequestBuilder postRequest = post("/trips/1/bucketItems/10/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(voteDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden());
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
