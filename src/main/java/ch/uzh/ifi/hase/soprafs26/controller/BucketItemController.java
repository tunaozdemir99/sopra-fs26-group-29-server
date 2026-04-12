package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BucketItemPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.BucketItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<BucketItem> items = bucketItemService.getBucketItems(tripId, token);
        List<BucketItemGetDTO> dtos = new ArrayList<>();
        for (BucketItem item : items) {
            dtos.add(DTOMapper.INSTANCE.convertEntityToBucketItemGetDTO(item));
        }
        return dtos;
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
}
