package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PinGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PinPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.PinService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: PinController
 * Package: ch.uzh.ifi.hase.soprafs26.controller
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/4/21 22:26
 * @ version 1.0
 */
@RestController
public class PinController {

    private final PinService pinService;

    PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @GetMapping("/trips/{tripId}/pins")
    @ResponseStatus(HttpStatus.OK)
    public List<PinGetDTO> getPins(@PathVariable Long tripId) {
        List<Pin> pins = pinService.getPinsByTripId(tripId);
        return pins.stream()
                .map(DTOMapper.INSTANCE::convertEntityToPinGetDTO)
                .toList();
    }

    @PostMapping("/trips/{tripId}/pins")
    @ResponseStatus(HttpStatus.CREATED)
    public PinGetDTO createPin(
            @PathVariable Long tripId,
            @RequestBody PinPostDTO pinPostDTO) {

        Pin pinInput = DTOMapper.INSTANCE.convertPinPostDTOtoEntity(pinPostDTO);
        Pin createdPin = pinService.createPin(tripId, pinInput);

        return DTOMapper.INSTANCE.convertEntityToPinGetDTO(createdPin);
    }

    @DeleteMapping("/trips/{tripId}/pins/{pinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePin(
            @PathVariable Long tripId,
            @PathVariable Long pinId) {

        pinService.deletePin(tripId, pinId);
    }
}
