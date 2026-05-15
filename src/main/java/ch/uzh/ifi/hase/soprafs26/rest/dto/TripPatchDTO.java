package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

public class TripPatchDTO {
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
