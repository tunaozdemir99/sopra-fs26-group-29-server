package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Activity;
import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ActivityRepository;
import ch.uzh.ifi.hase.soprafs26.repository.BucketItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BucketItemRepository bucketItemRepository;
    @Mock
    private TravelTimeService travelTimeService;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private Trip testTrip;
    private BucketItem testBucketItem;
    private Activity testActivity;

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

        testBucketItem = new BucketItem();
        testBucketItem.setbucketItemId(10L);
        testBucketItem.setName("Eiffel Tower");
        testBucketItem.setBucketTrip(testTrip);

        testActivity = new Activity();
        testActivity.setActivityId(100L);
        testActivity.setName("Eiffel Tower");
        testActivity.setDate(LocalDate.of(2026, 8, 1));
        testActivity.setStartTime(LocalTime.of(9, 0));
        testActivity.setEndTime(LocalTime.of(11, 0));
        testActivity.setActivityTrip(testTrip);

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
        Mockito.when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        Mockito.when(bucketItemRepository.findById(10L)).thenReturn(Optional.of(testBucketItem));
    }

    // --- getTimeline ---

    @Test
    public void getTimeline_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> activityService.getTimeline(1L, "bad-token"));
    }

    @Test
    public void getTimeline_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> activityService.getTimeline(99L, "valid-token"));
    }

    @Test
    public void getTimeline_userNotMember_throwsForbidden() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        Mockito.when(tripRepository.findById(2L)).thenReturn(Optional.of(otherTrip));

        assertThrows(ResponseStatusException.class,
                () -> activityService.getTimeline(2L, "valid-token"));
    }

    @Test
    public void getTimeline_validMember_returnsSortedActivities() {
        Activity early = new Activity();
        early.setActivityId(1L);
        early.setName("Morning");
        early.setDate(LocalDate.of(2026, 8, 1));
        early.setStartTime(LocalTime.of(8, 0));
        early.setEndTime(LocalTime.of(9, 0));

        Activity late = new Activity();
        late.setActivityId(2L);
        late.setName("Afternoon");
        late.setDate(LocalDate.of(2026, 8, 1));
        late.setStartTime(LocalTime.of(14, 0));
        late.setEndTime(LocalTime.of(16, 0));

        // return in wrong order to verify sorting
        Mockito.when(activityRepository.findByActivityTrip_TripId(1L))
                .thenReturn(Arrays.asList(late, early));

        List<ActivityGetDTO> result = activityService.getTimeline(1L, "valid-token");

        assertEquals(2, result.size());
        assertEquals("Morning", result.get(0).getName());
        assertEquals("Afternoon", result.get(1).getName());
    }

    @Test
    public void getTimeline_activitiesWithCoords_setsTravelTime() {
        Activity first = new Activity();
        first.setActivityId(1L);
        first.setName("First");
        first.setDate(LocalDate.of(2026, 8, 1));
        first.setStartTime(LocalTime.of(9, 0));
        first.setEndTime(LocalTime.of(10, 0));
        first.setLatitude(48.8584);
        first.setLongitude(2.2945);

        Activity second = new Activity();
        second.setActivityId(2L);
        second.setName("Second");
        second.setDate(LocalDate.of(2026, 8, 1));
        second.setStartTime(LocalTime.of(12, 0));
        second.setEndTime(LocalTime.of(13, 0));
        second.setLatitude(48.8606);
        second.setLongitude(2.3376);

        Mockito.when(activityRepository.findByActivityTrip_TripId(1L))
                .thenReturn(Arrays.asList(first, second));
        Mockito.when(travelTimeService.computeTravelMinutes(
                48.8584, 2.2945, 48.8606, 2.3376)).thenReturn(15);

        List<ActivityGetDTO> result = activityService.getTimeline(1L, "valid-token");

        assertEquals(15, result.get(0).getTravelTimeToNextActivity());
        assertNull(result.get(1).getTravelTimeToNextActivity());
    }

    @Test
    public void getTimeline_activitiesWithoutCoords_travelTimeIsNull() {
        Activity first = new Activity();
        first.setActivityId(1L);
        first.setName("First");
        first.setDate(LocalDate.of(2026, 8, 1));
        first.setStartTime(LocalTime.of(9, 0));
        first.setEndTime(LocalTime.of(10, 0));
        // no coordinates set

        Activity second = new Activity();
        second.setActivityId(2L);
        second.setName("Second");
        second.setDate(LocalDate.of(2026, 8, 1));
        second.setStartTime(LocalTime.of(12, 0));
        second.setEndTime(LocalTime.of(13, 0));

        Mockito.when(activityRepository.findByActivityTrip_TripId(1L))
                .thenReturn(Arrays.asList(first, second));

        List<ActivityGetDTO> result = activityService.getTimeline(1L, "valid-token");

        assertNull(result.get(0).getTravelTimeToNextActivity());
    }

    // --- scheduleFromBucket ---

    @Test
    public void scheduleFromBucket_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(ResponseStatusException.class,
                () -> activityService.scheduleFromBucket(1L, dto, "bad-token"));
    }

    @Test
    public void scheduleFromBucket_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(ResponseStatusException.class,
                () -> activityService.scheduleFromBucket(99L, dto, "valid-token"));
    }

    @Test
    public void scheduleFromBucket_userNotMember_throwsForbidden() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        Mockito.when(tripRepository.findById(2L)).thenReturn(Optional.of(otherTrip));

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(ResponseStatusException.class,
                () -> activityService.scheduleFromBucket(2L, dto, "valid-token"));
    }

    @Test
    public void scheduleFromBucket_bucketItemNotFound_throwsNotFound() {
        Mockito.when(bucketItemRepository.findById(99L)).thenReturn(Optional.empty());

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(99L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(ResponseStatusException.class,
                () -> activityService.scheduleFromBucket(1L, dto, "valid-token"));
    }

    @Test
    public void scheduleFromBucket_bucketItemBelongsToDifferentTrip_throwsBadRequest() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        testBucketItem.setBucketTrip(otherTrip);

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        assertThrows(ResponseStatusException.class,
                () -> activityService.scheduleFromBucket(1L, dto, "valid-token"));
    }

    @Test
    public void scheduleFromBucket_validInput_createsActivityWithBucketItemData() {
        Mockito.when(activityRepository.save(any())).thenReturn(testActivity);

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));

        Activity result = activityService.scheduleFromBucket(1L, dto, "valid-token");

        Mockito.verify(activityRepository, Mockito.times(1)).save(any(Activity.class));
        assertEquals("Eiffel Tower", result.getName());
    }

    @Test
    public void scheduleFromBucket_locationNameProvided_overridesBucketItemLocation() {
        testBucketItem.setLocation("Old Location");
        Mockito.when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActivityPostDTO dto = new ActivityPostDTO();
        dto.setBucketItemId(10L);
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(11, 0));
        dto.setLocationName("Custom Location");

        Activity result = activityService.scheduleFromBucket(1L, dto, "valid-token");

        assertEquals("Custom Location", result.getLocationName());
    }

    // --- deleteActivity ---

    @Test
    public void deleteActivity_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> activityService.deleteActivity(1L, 100L, "bad-token"));
    }

    @Test
    public void deleteActivity_tripNotFound_throwsNotFound() {
        Mockito.when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> activityService.deleteActivity(99L, 100L, "valid-token"));
    }

    @Test
    public void deleteActivity_userNotMember_throwsForbidden() {
        Trip otherTrip = new Trip();
        otherTrip.setTripId(2L);
        Mockito.when(tripRepository.findById(2L)).thenReturn(Optional.of(otherTrip));

        assertThrows(ResponseStatusException.class,
                () -> activityService.deleteActivity(2L, 100L, "valid-token"));
    }

    @Test
    public void deleteActivity_activityNotFound_throwsNotFound() {
        Mockito.when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> activityService.deleteActivity(1L, 99L, "valid-token"));
    }

    @Test
    public void deleteActivity_validInput_deletesActivity() {
        Mockito.when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));

        activityService.deleteActivity(1L, 100L, "valid-token");

        Mockito.verify(activityRepository, Mockito.times(1)).delete(testActivity);
    }
}
