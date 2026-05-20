package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PinRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * ClassName: PinService
 * Package: ch.uzh.ifi.hase.soprafs26.service
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/4/21 22:19
 * @ version 1.0
 */
@Service
@Transactional
public class PinService {

    private final PinRepository pinRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Autowired
    public PinService(PinRepository pinRepository, TripRepository tripRepository, UserRepository userRepository) {
        this.pinRepository = pinRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public List<Pin> getPinsByTripId(Long tripId, String token) {
        validateTokenAndMembership(tripId, token);
        return pinRepository.findByTrip_TripId(tripId);
    }

    public Pin createPin(Long tripId, Pin newPin, String token) {
        validateTokenAndMembership(tripId, token);
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (newPin.getName() == null || newPin.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pin name is required");
        }
        if (newPin.getLatitude() == null || newPin.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }
        newPin.setTrip(trip);
        newPin = pinRepository.save(newPin);
        pinRepository.flush();
        return newPin;
    }

    public void deletePin(Long tripId, Long pinId, String token) {
        validateTokenAndMembership(tripId, token);
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found"));
        if (!pin.getTrip().getTripId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pin does not belong to this trip");
        }
        pinRepository.delete(pin);
        pinRepository.flush();
    }

    // helper function
    private User validateTokenAndMembership(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        return user;
    }
}