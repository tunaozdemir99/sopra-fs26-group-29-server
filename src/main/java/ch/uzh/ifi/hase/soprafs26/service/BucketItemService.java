package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;

import ch.uzh.ifi.hase.soprafs26.repository.ActivityRepository;
import ch.uzh.ifi.hase.soprafs26.repository.BucketItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VoteRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BucketItemService {

    private final BucketItemRepository bucketItemRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final VoteRepository voteRepository;

    public BucketItemService(BucketItemRepository bucketItemRepository,
                            TripRepository tripRepository,
                            UserRepository userRepository,
                            ActivityRepository activityRepository,
                            VoteRepository voteRepository) {
        this.bucketItemRepository = bucketItemRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.voteRepository = voteRepository;
    }

    public List<BucketItemGetDTO> getBucketItems(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        List<BucketItem> items = bucketItemRepository.findByBucketTrip_TripId(tripId);
        List<BucketItemGetDTO> dtos = new ArrayList<>();
        for (BucketItem item : items) {
            BucketItemGetDTO dto = DTOMapper.INSTANCE.convertEntityToBucketItemGetDTO(item);
            dto.setScheduled(activityRepository.existsByBucketItem_BucketItemId(item.getBucketItemId()));
            voteRepository.findByBucketItem_BucketItemIdAndUser_Id(item.getBucketItemId(), user.getId())
                .ifPresent(v -> dto.setMyVote(v.getValue()));
            dtos.add(dto);
        }
        return dtos;
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

    public BucketItem updateBucketItem(Long tripId, Long itemId, BucketItemPatchDTO patch, String token) {
        User user = userRepository.findByToken(token);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");

        BucketItem item = bucketItemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));

        if (!item.getBucketTrip().getTripId().equals(tripId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this trip");

        if (!item.getAddedBy().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this item");

        // Only update fields that were actually sent in the request
        if (patch.getName() != null) {
            if (patch.getName().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            item.setName(patch.getName());
        }
        if (patch.getDescription() != null)
            item.setDescription(patch.getDescription());
        if (patch.getLocation() != null)
            item.setLocation(patch.getLocation());
        if (patch.getLatitude() != null)
            item.setLatitude(patch.getLatitude());
        if (patch.getLongitude() != null)
            item.setLongitude(patch.getLongitude());

        return bucketItemRepository.save(item);
    }

    public void deleteBucketItem(Long tripId, Long itemId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");

        BucketItem item = bucketItemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));

        if (!item.getBucketTrip().getTripId().equals(tripId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this trip");

        if (!item.getAddedBy().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this item");

        bucketItemRepository.delete(item);
    }


    public BucketItemGetDTO vote(Long tripId, Long itemId, int value, String token) {
        User user = userRepository.findByToken(token);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");

        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");

        BucketItem item = bucketItemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bucket item not found"));

        Optional<Vote> existing = voteRepository.findByBucketItem_BucketItemIdAndUser_Id(itemId, user.getId());

        if (value == 0) {
            existing.ifPresent(voteRepository::delete);
        } else {
            Vote vote = existing.orElseGet(Vote::new);
            vote.setUser(user);
            vote.setBucketItem(item);
            vote.setValue(value);
            voteRepository.save(vote);
        }

        int newScore = voteRepository.findByBucketItem_BucketItemId(itemId)
            .stream().mapToInt(Vote::getValue).sum();
        item.setVoteScore(newScore);
        bucketItemRepository.save(item);

        BucketItemGetDTO dto = DTOMapper.INSTANCE.convertEntityToBucketItemGetDTO(item);
        dto.setScheduled(activityRepository.existsByBucketItem_BucketItemId(itemId));
        dto.setMyVote(value == 0 ? 0 : value);
        return dto;
    }

}

