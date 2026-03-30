package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

/**
 * ClassName: TripPostDTO
 * Package: ch.uzh.ifi.hase.soprafs26.rest.dto
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 15:43
 * @ version 1.0
 */
public class TripPostDTO {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
