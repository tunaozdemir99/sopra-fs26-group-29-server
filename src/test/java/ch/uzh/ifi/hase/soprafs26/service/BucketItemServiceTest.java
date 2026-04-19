package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BucketItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPatchDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class BucketItemServiceTest {

    @Mock
    private BucketItemRepository bucketItemRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BucketItemService bucketItemService;

    private User testUser;
    private Trip testTrip;
    private BucketItem testItem;

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
        testTrip.addMember(testUser);

        testItem = new BucketItem();
        testItem.setbucketItemId(10L);
        testItem.setName("Eiffel Tower");
        testItem.setAddedBy(testUser);
        testItem.setBucketTrip(testTrip);

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        Mockito.when(bucketItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
    }

    // getBucketItems 

    @Test
    public void getBucketItems_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.getBucketItems(1L, "bad-token"));
    }

    @Test
    public void getBucketItems_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.getBucketItems(99L, "valid-token"));
    }

    @Test
    public void getBucketItems_userNotMember_throwsForbidden() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        Mockito.when(tripRepository.findById(2L)).thenReturn(Optional.of(otherTrip));

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.getBucketItems(2L, "valid-token"));
    }

    @Test
    public void getBucketItems_validMember_returnsItems() {
        Mockito.when(bucketItemRepository.findByBucketTrip_TripId(1L))
                .thenReturn(Collections.singletonList(testItem));

        List<BucketItemGetDTO> result = bucketItemService.getBucketItems(1L, "valid-token");

        assertEquals(1, result.size());
        assertEquals("Eiffel Tower", result.get(0).getName());
    }

    // --- addBucketItem ---

    @Test
    public void addBucketItem_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.addBucketItem(1L, new BucketItem(), "bad-token"));
    }

    @Test
    public void addBucketItem_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.addBucketItem(99L, new BucketItem(), "valid-token"));
    }

    @Test
    public void addBucketItem_userNotMember_throwsForbidden() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        Mockito.when(tripRepository.findById(2L)).thenReturn(Optional.of(otherTrip));

        BucketItem item = new BucketItem();
        item.setName("Louvre");

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.addBucketItem(2L, item, "valid-token"));
    }

    @Test
    public void addBucketItem_blankName_throwsBadRequest() {
        BucketItem item = new BucketItem();
        item.setName("   ");

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.addBucketItem(1L, item, "valid-token"));
    }

    @Test
    public void addBucketItem_nullName_throwsBadRequest() {
        BucketItem item = new BucketItem();
        item.setName(null);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.addBucketItem(1L, item, "valid-token"));
    }

    @Test
    public void addBucketItem_validInput_savesAndReturns() {
        BucketItem item = new BucketItem();
        item.setName("Louvre");
        Mockito.when(bucketItemRepository.save(any())).thenReturn(item);

        BucketItem saved = bucketItemService.addBucketItem(1L, item, "valid-token");

        Mockito.verify(bucketItemRepository, Mockito.times(1)).save(any());
        assertEquals("Louvre", saved.getName());
        assertEquals(testUser, item.getAddedBy());
        assertEquals(testTrip, item.getBucketTrip());
    }

    // --- updateBucketItem ---

    @Test
    public void updateBucketItem_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.updateBucketItem(1L, 10L, new BucketItemPatchDTO(), "bad-token"));
    }

    @Test
    public void updateBucketItem_itemNotFound_throwsNotFound() {
        Mockito.when(bucketItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.updateBucketItem(1L, 99L, new BucketItemPatchDTO(), "valid-token"));
    }

    @Test
    public void updateBucketItem_itemBelongsToDifferentTrip_throwsBadRequest() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        testItem.setBucketTrip(otherTrip);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.updateBucketItem(1L, 10L, new BucketItemPatchDTO(), "valid-token"));
    }

    @Test
    public void updateBucketItem_notAuthor_throwsForbidden() {
        User other = new User();
        other.setId(2L);
        testItem.setAddedBy(other);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.updateBucketItem(1L, 10L, new BucketItemPatchDTO(), "valid-token"));
    }

    @Test
    public void updateBucketItem_blankName_throwsBadRequest() {
        BucketItemPatchDTO patch = new BucketItemPatchDTO();
        patch.setName("  ");

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.updateBucketItem(1L, 10L, patch, "valid-token"));
    }

    @Test
    public void updateBucketItem_validInput_updatesFields() {
        BucketItemPatchDTO patch = new BucketItemPatchDTO();
        patch.setName("Arc de Triomphe");
        patch.setDescription("Famous arch");
        Mockito.when(bucketItemRepository.save(any())).thenReturn(testItem);

        bucketItemService.updateBucketItem(1L, 10L, patch, "valid-token");

        assertEquals("Arc de Triomphe", testItem.getName());
        assertEquals("Famous arch", testItem.getDescription());
        Mockito.verify(bucketItemRepository, Mockito.times(1)).save(testItem);
    }

    // --- deleteBucketItem ---

    @Test
    public void deleteBucketItem_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.deleteBucketItem(1L, 10L, "bad-token"));
    }

    @Test
    public void deleteBucketItem_itemNotFound_throwsNotFound() {
        Mockito.when(bucketItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.deleteBucketItem(1L, 99L, "valid-token"));
    }

    @Test
    public void deleteBucketItem_itemBelongsToDifferentTrip_throwsBadRequest() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        testItem.setBucketTrip(otherTrip);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.deleteBucketItem(1L, 10L, "valid-token"));
    }

    @Test
    public void deleteBucketItem_notAuthor_throwsForbidden() {
        User other = new User();
        other.setId(2L);
        testItem.setAddedBy(other);

        assertThrows(ResponseStatusException.class,
                () -> bucketItemService.deleteBucketItem(1L, 10L, "valid-token"));
    }

    @Test
    public void deleteBucketItem_validAuthor_deletesItem() {
        bucketItemService.deleteBucketItem(1L, 10L, "valid-token");

        Mockito.verify(bucketItemRepository, Mockito.times(1)).delete(testItem);
    }
}
