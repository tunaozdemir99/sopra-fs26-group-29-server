package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * ClassName: MemberService
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/5/14 21:47
 * @ version 1.0
 */
@Service
@Transactional
public class MemberService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Autowired
    public MemberService(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public Set<User> getMembers(Long tripId) {
        Trip trip = findTripOrThrow(tripId);
        return trip.getMembers();
    }

    public User addMember(Long tripId, String username, String token) {
        User admin = authenticateAndAuthorize(tripId, token);
        Trip trip = findTripOrThrow(tripId);

        User newMember = userRepository.findByUsername(username);
        if (newMember == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        if (trip.getMembers().contains(newMember)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is already a member");
        }

        trip.addMember(newMember);
        tripRepository.save(trip);
        tripRepository.flush();

        return newMember;
    }

    public void removeMember(Long tripId, Long userId, String token) {
        User requester = authenticateUser(token);
        Trip trip = findTripOrThrow(tripId);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!trip.getMembers().contains(target)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User is not a member of this trip");
        }

        boolean isAdmin = trip.getAdmin().getId().equals(requester.getId());
        boolean isSelf = requester.getId().equals(userId);

        // non-admin can only remove themselves (leave)
        if (!isAdmin && !isSelf) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can remove other members");
        }

        // admin can't leave without transferring admin first
        if (isAdmin && isSelf) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Transfer admin rights before leaving the trip");
        }

        trip.getMembers().remove(target);
        tripRepository.save(trip);
        tripRepository.flush();
    }

    public void transferAdmin(Long tripId, Long targetUserId, String token) {
        User admin = authenticateAndAuthorize(tripId, token);
        Trip trip = findTripOrThrow(tripId);

        if (admin.getId().equals(targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You are already the admin");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!trip.getMembers().contains(target)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User is not a member of this trip");
        }

        trip.setAdmin(target);
        tripRepository.save(trip);
        tripRepository.flush();
    }

    // --- helper methods ---

    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trip not found"));
    }

    private User authenticateUser(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        return user;
    }

    private User authenticateAndAuthorize(Long tripId, String token) {
        User user = authenticateUser(token);
        Trip trip = findTripOrThrow(tripId);
        if (!trip.getAdmin().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can perform this action");
        }
        return user;
    }
}
