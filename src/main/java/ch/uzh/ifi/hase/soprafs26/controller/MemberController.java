package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.MemberGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: MemberController
 * Package: ch.uzh.ifi.hase.soprafs26.controller
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/5/14 21:49
 * @ version 1.0
 */
@RestController
public class MemberController {

    private final MemberService memberService;

    MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/trips/{tripId}/members")
    @ResponseStatus(HttpStatus.OK)
    public List<MemberGetDTO> getMembers(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        String rawToken = authHeader.replace("Bearer ", "");
        Trip trip = memberService.getTripWithAuthCheck(tripId, rawToken);
        return trip.getMembers().stream().map(user -> {
            MemberGetDTO dto = new MemberGetDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setIsAdmin(trip.getAdmin().getId().equals(user.getId()));
            return dto;
        }).toList();
    }

    @PostMapping("/trips/{tripId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public UserGetDTO addMember(
            @PathVariable Long tripId,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String token) {

        String username = body.get("username");
        String rawToken = token.replace("Bearer ", "");
        User added = memberService.addMember(tripId, username, rawToken);

        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(added);
    }

    @DeleteMapping("/trips/{tripId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable Long tripId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        String rawToken = token.replace("Bearer ", "");
        memberService.removeMember(tripId, userId, rawToken);
    }

    @PatchMapping("/trips/{tripId}/members/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void transferAdmin(
            @PathVariable Long tripId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        String rawToken = token.replace("Bearer ", "");
        memberService.transferAdmin(tripId, userId, rawToken);
    }
}