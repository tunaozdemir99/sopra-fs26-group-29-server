package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ClassName: Trip
 * Package: ch.uzh.ifi.hase.soprafs26.entity
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 15:17
 * @ version 1.0
 */
@Entity
@Table(name = "trips")
public class Trip implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long tripId;

    @Column(nullable = false)
    private String title;

    @Column
    private String location;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true)
    private String inviteUrl;

    // the user who created (and currently admins) this trip
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ManyToMany
    @JoinTable(
        name = "trip_members",
        joinColumns = @JoinColumn(name = "trip_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // getters & setters
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getInviteUrl() {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public void addMember(User user) {
        this.members.add(user);
    }
}
