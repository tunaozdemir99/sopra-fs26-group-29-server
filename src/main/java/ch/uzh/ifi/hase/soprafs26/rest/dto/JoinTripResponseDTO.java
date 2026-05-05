package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

public class JoinTripResponseDTO {
    private Long tripId;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String adminUsername;
    private String inviteUrl;
    private boolean alreadyMember;
    private String message;

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public String getInviteUrl() { return inviteUrl; }
    public void setInviteUrl(String inviteUrl) { this.inviteUrl = inviteUrl; }

    public boolean isAlreadyMember() { return alreadyMember; }
    public void setAlreadyMember(boolean alreadyMember) { this.alreadyMember = alreadyMember; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
