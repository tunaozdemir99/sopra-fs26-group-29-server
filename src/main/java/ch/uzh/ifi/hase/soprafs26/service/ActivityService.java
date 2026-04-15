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
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final BucketItemRepository bucketItemRepository;
    private final TravelTimeService travelTimeService;

    public ActivityService(ActivityRepository activityRepository,
                           TripRepository tripRepository,
                           UserRepository userRepository,
                           BucketItemRepository bucketItemRepository,
                           TravelTimeService travelTimeService) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.bucketItemRepository = bucketItemRepository;
        this.travelTimeService = travelTimeService;
    }

    // GET /trips/{tripId}/timeline
    public List<ActivityGetDTO> getTimeline(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }

        List<Activity> activities = activityRepository.findByActivityTrip_TripId(tripId);
        activities.sort(Comparator.comparing(Activity::getDate)
            .thenComparing(Activity::getStartTime));

        List<ActivityGetDTO> dtos = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            ActivityGetDTO dto = DTOMapper.INSTANCE.convertEntityToActivityGetDTO(activities.get(i));

            if (i < activities.size() - 1) {
                Activity curr = activities.get(i);
                Activity next = activities.get(i + 1);
                if (curr.getLatitude() != null && next.getLatitude() != null) {
                    Integer minutes = travelTimeService.computeTravelMinutes(
                        curr.getLatitude(), curr.getLongitude(),
                        next.getLatitude(), next.getLongitude());
                    dto.setTravelTimeToNextActivity(minutes);
                }
            }

            dtos.add(dto);
        }
        return dtos;
    }

    // POST /trips/{tripId}/timeline
    public Activity scheduleFromBucket(Long tripId, Long bucketItemId,
                                       LocalDate date, LocalTime startTime,
                                       LocalTime endTime, String locationName,
                                       Double latitude, Double longitude, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        BucketItem bucketItem = bucketItemRepository.findById(bucketItemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));

        if (!bucketItem.getBucketTrip().getTripId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket item does not belong to this trip");
        }

        Activity activity = new Activity();
        activity.setName(bucketItem.getName());
        activity.setDate(date);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setFromBucketItem(true);
        activity.setActivityTrip(trip);
        activity.setBucketItem(bucketItem);
        activity.setLocationName(locationName != null ? locationName : bucketItem.getLocation());
        activity.setLatitude(latitude);
        activity.setLongitude(longitude);
        return activityRepository.save(activity);
    }

    // DELETE /trips/{tripId}/timeline/{activityId}
    public void deleteActivity(Long tripId, Long activityId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        activityRepository.delete(activity);
    }
}
