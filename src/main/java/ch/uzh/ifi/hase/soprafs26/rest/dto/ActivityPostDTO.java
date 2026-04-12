package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ActivityPostDTO {

    private Long bucketItemId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public Long getBucketItemId() { 
        return bucketItemId; 
    }

    public void setBucketItemId(Long bucketItemId) { 
        this.bucketItemId = bucketItemId; 
    }

    public LocalDate getDate() { 
        return date; 
    }

    public void setDate(LocalDate date) { 
        this.date = date; 
    }

    public LocalTime getStartTime() { 
        return startTime; 
    }

    public void setStartTime(LocalTime startTime) { 
        this.startTime = startTime; 
    }

    public LocalTime getEndTime() { 
        return endTime; 
    }

    public void setEndTime(LocalTime endTime) { 
        this.endTime = endTime; 
    }
}