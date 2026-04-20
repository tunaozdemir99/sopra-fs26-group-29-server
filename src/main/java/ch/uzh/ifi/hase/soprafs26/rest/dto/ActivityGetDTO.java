package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ActivityGetDTO {

    private Long activityId;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean fromBucketItem;
    private Long bucketItemId;

    private String locationName;
    private Double latitude;
    private Double longitude;
    private Integer travelTimeToNextActivity;

    // TODO: location (name, lat, lng, placeId) — add when Google Maps is integrated
    // TODO: travelTimeToNextMinutes — add when timeline conflict logic is implemented
    // TODO: hasOverlapConflict — add when timeline conflict logic is implemented
    // TODO: hasTravelTimeConflict — add when timeline conflict logic is implemented

    public Long getActivityId() { 
        return activityId; 
    }

    public void setActivityId(Long activityId) { 
        this.activityId = activityId; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
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

    public boolean isFromBucketItem() { 
        return fromBucketItem; 
    }

    public void setFromBucketItem(boolean fromBucketItem) {
        this.fromBucketItem = fromBucketItem;
    }

    public Long getBucketItemId() { return bucketItemId; }
    public void setBucketItemId(Long bucketItemId) { this.bucketItemId = bucketItemId; }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getTravelTimeToNextActivity(){
        return travelTimeToNextActivity;
    }

     public void setTravelTimeToNextActivity(Integer travelTimeToNextActivity) {
      this.travelTimeToNextActivity = travelTimeToNextActivity;                                                                                                                                   
  } 

}