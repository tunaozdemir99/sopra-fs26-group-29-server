package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class TaskPostDTO {

    private String title;
    private String description;
    private Long assigneeId;

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

    public Long getAssigneeId() { 
        return assigneeId; 
    }

    public void setAssigneeId(Long assigneeId) { 
        this.assigneeId = assigneeId; 
    }
}