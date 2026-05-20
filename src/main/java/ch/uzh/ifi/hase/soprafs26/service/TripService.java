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

import java.time.LocalDate;
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

        if (newTrip.getStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Start date cannot be in the past");
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

    public Trip getTripById(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        boolean isMember = trip.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User is not a member of this trip");
        }

        return trip;
    }

    public void deleteTrip(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        // only admin can delete
        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the trip admin can delete this trip");
        }

        // clear the members join table before deleting
        trip.getMembers().clear();

        tripRepository.delete(trip);
        tripRepository.flush();
    }

    public Trip getTripByInviteCode(String inviteCode, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        return tripRepository.findByInviteUrl(inviteCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid or expired invite link"));
    }

    public JoinResult joinTripByInviteCode(String inviteCode, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findByInviteUrl(inviteCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.isInviteActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invite link has been deactivated");
        }

        boolean alreadyMember = trip.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));

        if (!alreadyMember) {
            trip.addMember(user);
            tripRepository.save(trip);
            tripRepository.flush();
        }

        return new JoinResult(trip, alreadyMember);
    }

    public static class JoinResult {
        public final Trip trip;
        public final boolean alreadyMember;

        public JoinResult(Trip trip, boolean alreadyMember) {
            this.trip = trip;
            this.alreadyMember = alreadyMember;
        }
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

    public String getInviteUrl(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can view the invite link");
        }

        return trip.getInviteUrl();
    }

    public String regenerateInviteUrl(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can regenerate the invite link");
        }

        trip.setInviteUrl(UUID.randomUUID().toString());
        tripRepository.save(trip);
        tripRepository.flush();

        return trip.getInviteUrl();
    }

    public Trip updateTrip(Long tripId, String token, Trip updates) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the trip admin can edit trip details");
        }

        if (updates.getTitle() != null && !updates.getTitle().isBlank()) {
            trip.setTitle(updates.getTitle());
        }
        if (updates.getLocation() != null) {
            trip.setLocation(updates.getLocation());
        }

        LocalDate newStart = updates.getStartDate() != null ? updates.getStartDate() : trip.getStartDate();
        LocalDate newEnd = updates.getEndDate() != null ? updates.getEndDate() : trip.getEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        trip.setStartDate(newStart);
        trip.setEndDate(newEnd);

        tripRepository.save(trip);
        tripRepository.flush();
        return trip;
    }

    public void setInviteActive(Long tripId, boolean active, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can manage the invite link");
        }

        trip.setInviteActive(active);
        tripRepository.save(trip);
        tripRepository.flush();
    }
}
