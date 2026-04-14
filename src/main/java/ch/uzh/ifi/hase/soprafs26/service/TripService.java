package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: TripService
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 15:38
 * @ version 1.0
 */
@Service
@Transactional
public class TripService {
    private final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Autowired
    public TripService(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public Trip createTrip(Trip newTrip, String token) {
        // find creator by token
        User creator = userRepository.findByToken(token);
        if (creator == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        // validate required fields
        if (newTrip.getTitle() == null || newTrip.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (newTrip.getStartDate() == null || newTrip.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }
        if (newTrip.getEndDate().isBefore(newTrip.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        // set auto-generated fields
        newTrip.setAdmin(creator);
        newTrip.setCreatedAt(LocalDateTime.now());
        newTrip.setInviteUrl(UUID.randomUUID().toString());
        newTrip.addMember(creator);     // adding the admin to the set of trip members

        newTrip = tripRepository.save(newTrip);
        tripRepository.flush();

        log.debug("Created trip: {}", newTrip.getTripId());
        return newTrip;
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));
    }

    public List<Trip> getTripsByUser(Long userId, String token) {
        User requester = userRepository.findByToken(token);
        if (requester == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        if (!requester.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own trips");
        }
        return tripRepository.findByMembers_Id(userId);
    }
}
