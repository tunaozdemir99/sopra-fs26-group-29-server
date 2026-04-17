package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activities")
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long activityId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean fromBucketItem = false;

    @Column
    private String locationName;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // which trip's timeline this activity belongs to
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip activityTrip;

    // source bucket item — nullable because future activities may not come from the bucket
    @ManyToOne
    @JoinColumn(name = "bucket_item_id", nullable = true)
    private BucketItem bucketItem;

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

    public Trip getActivityTrip() { 
        return activityTrip; 
    }

    public void setActivityTrip(Trip activityTrip) { 
        this.activityTrip = activityTrip; 
    }

    public BucketItem getBucketItem() { 
        return bucketItem; 
    }

    public void setBucketItem(BucketItem bucketItem) { 
        this.bucketItem = bucketItem; 
    }

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
}