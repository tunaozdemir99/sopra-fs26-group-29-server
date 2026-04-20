package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tasks")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long taskId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)   // stores "TO_DO" in DB, not a number
    private TaskStatus status = TaskStatus.TO_DO;

    // the user the task is assigned to
    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    // which trip this task belongs to
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip taskTrip;

    public Long getTaskId() { 
        return taskId; 
    }

    public void setTaskId(Long taskId) { 
        this.taskId = taskId; 
    }

    public String getTitle() { 
        return title; 
    }

    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }

    public TaskStatus getStatus() { 
        return status; 
    }

    public void setStatus(TaskStatus status) { 
        this.status = status; 
    }

    public User getAssignee() { 
        return assignee; 
    }
    public void setAssignee(User assignee) { 
        this.assignee = assignee; 
    }

    public Trip getTaskTrip() { 
        return taskTrip; 
    }

    public void setTaskTrip(Trip taskTrip) {
        this.taskTrip = taskTrip; 
    }
}