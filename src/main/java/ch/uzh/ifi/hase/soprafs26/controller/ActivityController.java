package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Activity;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/trips/{tripId}/timeline")
    @ResponseStatus(HttpStatus.OK)
    public List<ActivityGetDTO> getTimeline(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return activityService.getTimeline(tripId, token);
    }

    @PostMapping("/trips/{tripId}/timeline")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityGetDTO scheduleFromBucket(
            @PathVariable Long tripId,
            @RequestBody ActivityPostDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Activity activity = activityService.scheduleFromBucket(
            tripId, dto.getBucketItemId(), dto.getDate(), dto.getStartTime(), dto.getEndTime(),
            dto.getLocationName(), dto.getLatitude(), dto.getLongitude(), token);
        return DTOMapper.INSTANCE.convertEntityToActivityGetDTO(activity);
    }

    @DeleteMapping("/trips/{tripId}/timeline/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivity(
            @PathVariable Long tripId,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        activityService.deleteActivity(tripId, activityId, token);
    }
}
