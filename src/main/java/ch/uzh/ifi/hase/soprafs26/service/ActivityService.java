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
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ActivityService {

    private static final String TRIP_NOT_FOUND = "Trip not found";

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

    public List<ActivityGetDTO> getTimeline(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRIP_NOT_FOUND));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }

        List<Activity> activities = activityRepository.findByActivityTrip_TripId(tripId);
        activities.sort(Comparator.comparing(Activity::getDate)
            .thenComparing(Activity::getStartTime));

        List<ActivityGetDTO> dtos = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            Activity current = activities.get(i);
            ActivityGetDTO dto = DTOMapper.INSTANCE.convertEntityToActivityGetDTO(current);

            dto.setDurationMinutes((int) Duration.between(current.getStartTime(), current.getEndTime()).toMinutes());
            dto.setGapToNextActivityMinutes(null);
            dto.setTravelTimeToNextActivity(null);
            dto.setHasOverlapConflict(false);
            dto.setHasTravelTimeConflict(false);

            if (i < activities.size() - 1) {
                applyNextActivityMetrics(dto, current, activities.get(i + 1));
            }

            dtos.add(dto);
        }
        return dtos;
    }

    private void applyNextActivityMetrics(ActivityGetDTO dto, Activity current, Activity next) {
        if (!current.getDate().equals(next.getDate())) {
            return;
        }

        int gapMinutes = (int) Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();
        dto.setGapToNextActivityMinutes(gapMinutes);
        dto.setHasOverlapConflict(gapMinutes < 0);

        if (current.getLatitude() == null || current.getLongitude() == null
            || next.getLatitude() == null || next.getLongitude() == null) {
            return;
        }

        Integer travelMinutes = travelTimeService.computeTravelMinutes(
            current.getLatitude(), current.getLongitude(),
            next.getLatitude(), next.getLongitude());
        dto.setTravelTimeToNextActivity(travelMinutes);

        if (travelMinutes != null && gapMinutes >= 0) {
            dto.setHasTravelTimeConflict(gapMinutes < travelMinutes);
        }
    }

    public Activity scheduleFromBucket(Long tripId, ActivityPostDTO activityPostDTO, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRIP_NOT_FOUND));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        validateActivityTimes(activityPostDTO.getDate(), activityPostDTO.getStartTime(), activityPostDTO.getEndTime());

        Activity activity = new Activity();
        activity.setDate(activityPostDTO.getDate());
        activity.setStartTime(activityPostDTO.getStartTime());
        activity.setEndTime(activityPostDTO.getEndTime());
        activity.setActivityTrip(trip);
        activity.setLatitude(activityPostDTO.getLatitude());
        activity.setLongitude(activityPostDTO.getLongitude());

        if (activityPostDTO.getBucketItemId() != null) {
            BucketItem bucketItem = bucketItemRepository.findById(activityPostDTO.getBucketItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));
            if (!bucketItem.getBucketTrip().getTripId().equals(tripId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket item does not belong to this trip");
            }
            activity.setName(bucketItem.getName());
            activity.setFromBucketItem(true);
            activity.setBucketItem(bucketItem);
            activity.setLocationName(activityPostDTO.getLocationName() != null ? activityPostDTO.getLocationName() : bucketItem.getLocation());
        } else {
            if (activityPostDTO.getName() == null || activityPostDTO.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required when not scheduling from bucket");
            }
            activity.setName(activityPostDTO.getName());
            activity.setFromBucketItem(false);
            activity.setLocationName(activityPostDTO.getLocationName());
        }

        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long tripId, Long activityId, ActivityPutDTO activityPutDTO, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRIP_NOT_FOUND));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }

        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getActivityTrip().getTripId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity does not belong to this trip");
        }

        validateActivityTimes(activityPutDTO.getDate(), activityPutDTO.getStartTime(), activityPutDTO.getEndTime());

        activity.setDate(activityPutDTO.getDate());
        activity.setStartTime(activityPutDTO.getStartTime());
        activity.setEndTime(activityPutDTO.getEndTime());
        activity.setLocationName(activityPutDTO.getLocationName());
        activity.setLatitude(activityPutDTO.getLatitude());
        activity.setLongitude(activityPutDTO.getLongitude());
        return activityRepository.save(activity);
    }

    public void deleteActivity(Long tripId, Long activityId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRIP_NOT_FOUND));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getActivityTrip().getTripId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity does not belong to this trip");
        }
        activityRepository.delete(activity);
    }

    private void validateActivityTimes(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null || startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date, startTime and endTime are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
    }
}
