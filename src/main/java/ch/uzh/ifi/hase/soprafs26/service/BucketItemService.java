package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BucketItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class BucketItemService {

    private final BucketItemRepository bucketItemRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public BucketItemService(BucketItemRepository bucketItemRepository,
                             TripRepository tripRepository,
                             UserRepository userRepository) {
        this.bucketItemRepository = bucketItemRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public List<BucketItem> getBucketItems(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {             // 401 - invalid or missing token
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)     // 404
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {            // 403 - user is not a trip member
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        return bucketItemRepository.findByBucketTrip_TripId(tripId);
    }

    public BucketItem addBucketItem(Long tripId, BucketItem newItem, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {         // 401 - invalid or missing token
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)         // 404
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {            // 403 - user is not a trip member
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        if (newItem.getName() == null || newItem.getName().isBlank()) {         // 400 - missing required field
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        newItem.setAddedBy(user);
        newItem.setBucketTrip(trip);
        return bucketItemRepository.save(newItem);
    }
}
