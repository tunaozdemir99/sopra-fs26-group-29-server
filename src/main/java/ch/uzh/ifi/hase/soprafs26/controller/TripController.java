package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinTripRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinTripResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public TripGetDTO getTrip(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String token) {

        String rawToken = token.replace("Bearer ", "");
        Trip trip = tripService.getTripById(tripId, rawToken);

        return DTOMapper.INSTANCE.convertEntityToTripGetDTO(trip);
    }

    @GetMapping("/users/{userId}/trips")
    @ResponseStatus(HttpStatus.OK)
    public List<TripGetDTO> getTripsForUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        String rawToken = token.replace("Bearer ", "");
        List<Trip> trips = tripService.getTripsByUser(userId, rawToken);
        return trips.stream()
                .map(DTOMapper.INSTANCE::convertEntityToTripGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/trips/{tripId}/members")
    @ResponseStatus(HttpStatus.OK)
    public List<UserGetDTO> getTripMembers(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String token) {
        String rawToken = token.replace("Bearer ", "");
        Trip trip = tripService.getTripById(tripId, rawToken);
        return trip.getMembers().stream()
                .map(DTOMapper.INSTANCE::convertEntityToUserGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/trips/invite/{inviteCode}")
    @ResponseStatus(HttpStatus.OK)
    public TripGetDTO getTripByInviteCode(
            @PathVariable String inviteCode,
            @RequestHeader("Authorization") String token) {
        String rawToken = token.replace("Bearer ", "");
        Trip trip = tripService.getTripByInviteCode(inviteCode, rawToken);
        return DTOMapper.INSTANCE.convertEntityToTripGetDTO(trip);
    }

    @PostMapping("/trips/join")
    @ResponseStatus(HttpStatus.OK)
    public JoinTripResponseDTO joinTrip(
            @RequestBody JoinTripRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {
        String rawToken = token.replace("Bearer ", "");
        TripService.JoinResult result = tripService.joinTripByInviteCode(requestDTO.getInviteCode(), rawToken);

        Trip trip = result.trip;
        JoinTripResponseDTO response = new JoinTripResponseDTO();
        response.setTripId(trip.getTripId());
        response.setTitle(trip.getTitle());
        response.setLocation(trip.getLocation());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setAdminUsername(trip.getAdmin().getUsername());
        response.setInviteUrl(trip.getInviteUrl());
        response.setAlreadyMember(result.alreadyMember);
        response.setMessage(result.alreadyMember
                ? "You are already a member of this trip"
                : "Successfully joined the trip");
        return response;
    }
}
