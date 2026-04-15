package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Activity;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ActivityController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/trips/{tripId}/timeline")
    @ResponseStatus(HttpStatus.OK)
    public List<ActivityGetDTO> getTimeline(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        return activityService.getTimeline(tripId, token);
    }

    @PostMapping("/trips/{tripId}/timeline")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityGetDTO scheduleFromBucket(
            @PathVariable Long tripId,
            @RequestBody ActivityPostDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        Activity activity = activityService.scheduleFromBucket(tripId, dto, token);
        return DTOMapper.INSTANCE.convertEntityToActivityGetDTO(activity);
    }

    @DeleteMapping("/trips/{tripId}/timeline/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivity(
            @PathVariable Long tripId,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        activityService.deleteActivity(tripId, activityId, token);
    }

    @PutMapping("/trips/{tripId}/timeline/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    public ActivityGetDTO updateActivity(
            @PathVariable Long tripId,
            @PathVariable Long activityId,
            @RequestBody ActivityPutDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        Activity activity = activityService.updateActivity(tripId, activityId, dto, token);
        return DTOMapper.INSTANCE.convertEntityToActivityGetDTO(activity);
    }
}
