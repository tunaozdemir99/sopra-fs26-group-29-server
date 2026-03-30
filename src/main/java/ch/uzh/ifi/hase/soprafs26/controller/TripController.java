package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: TripController
 * Package: ch.uzh.ifi.hase.soprafs26.controller
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 15:55
 * @ version 1.0
 */
@RestController
public class TripController {
    private final TripService tripService;

    TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public TripGetDTO createTrip(
            @RequestBody TripPostDTO tripPostDTO,
            @RequestHeader("Authorization") String token) {

        // convert DTO to entity
        Trip tripInput = DTOMapper.INSTANCE.convertTripPostDTOtoEntity(tripPostDTO);

        // strip "Bearer " prefix to get raw token
        String rawToken = token.replace("Bearer ", "");

        // create trip
        Trip createdTrip = tripService.createTrip(tripInput, rawToken);

        return DTOMapper.INSTANCE.convertEntityToTripGetDTO(createdTrip);
    }

    @GetMapping("/trips/{tripId}")
    @ResponseStatus(HttpStatus.OK)
    public TripGetDTO getTrip(@PathVariable Long tripId) {

        Trip trip = tripService.getTripById(tripId);

        return DTOMapper.INSTANCE.convertEntityToTripGetDTO(trip);
    }
}
