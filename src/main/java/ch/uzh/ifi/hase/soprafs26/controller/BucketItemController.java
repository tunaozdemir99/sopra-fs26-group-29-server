package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPatchDTO; 
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.BucketItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class BucketItemController {

    private final BucketItemService bucketItemService;

    public BucketItemController(BucketItemService bucketItemService) {
        this.bucketItemService = bucketItemService;
    }

    @GetMapping("/trips/{tripId}/bucketItems")
    @ResponseStatus(HttpStatus.OK)
    public List<BucketItemGetDTO> getBucketItems(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        List<BucketItemGetDTO> items = bucketItemService.getBucketItems(tripId, token);
        return items;
    }

    @PostMapping("/trips/{tripId}/bucketItems")
    @ResponseStatus(HttpStatus.CREATED)
    public BucketItemGetDTO addBucketItem(
            @PathVariable Long tripId,
            @RequestBody BucketItemPostDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        BucketItem item = DTOMapper.INSTANCE.convertBucketItemPostDTOtoEntity(dto);
        BucketItem saved = bucketItemService.addBucketItem(tripId, item, token);
        return DTOMapper.INSTANCE.convertEntityToBucketItemGetDTO(saved);
    }

    @PatchMapping("/trips/{tripId}/bucketItems/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public BucketItemGetDTO updateBucketItem(
            @PathVariable Long tripId,
            @PathVariable Long itemId,
            @RequestBody BucketItemPatchDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        BucketItem updated = bucketItemService.updateBucketItem(tripId, itemId, dto, token);
        return DTOMapper.INSTANCE.convertEntityToBucketItemGetDTO(updated);
    }

    @DeleteMapping("/trips/{tripId}/bucketItems/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBucketItem(
            @PathVariable Long tripId,
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        bucketItemService.deleteBucketItem(tripId, itemId, token);
    }

    @PostMapping("/trips/{tripId}/bucketItems/{itemId}/vote")
    @ResponseStatus(HttpStatus.OK)
    public BucketItemGetDTO vote(
            @PathVariable Long tripId,
            @PathVariable Long itemId,
            @RequestBody VotePostDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return bucketItemService.vote(tripId, itemId, dto.getValue(), token);
    }

}
