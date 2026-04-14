package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;


/**
 * ClassName: BucketItem
 * Package: ch.uzh.ifi.hase.soprafs26.entity
 * Description:
 *
 * @ author tunaozdemir99
 * @ create 2026/3/31
 * @ version 1.0
 */
@Entity
@Table(name = "bucketItems")
public class BucketItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long bucketItemId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String location;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // the user who created the item
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User addedBy;

    // which trip the bucket belongs to
    @ManyToOne
    @JoinColumn(name = "tripId", nullable = false)
    private Trip bucketTrip;

    @Column(nullable = false)
    private int voteScore = 0;  

    // getters & setters
    public Long getbucketItemId() {
        return bucketItemId;
    }

    public void setbucketItemId(Long bucketItemId) {
        this.bucketItemId = bucketItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }

    public Trip getBucketTrip() {
        return bucketTrip;
    }

    public void setBucketTrip(Trip bucketTrip) {
        this.bucketTrip = bucketTrip;
    }

    public int getVoteScore() {
        return voteScore;
    }

    public void setVoteScore(int voteScore) {
        this.voteScore = voteScore;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
