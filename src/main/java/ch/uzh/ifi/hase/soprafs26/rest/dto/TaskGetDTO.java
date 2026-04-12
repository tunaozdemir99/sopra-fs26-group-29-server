package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;

public class TaskGetDTO {

    private Long taskId;
    private String description;
    private TaskStatus status;
    private UserGetDTO assignee;

    public Long getTaskId() { 
        return taskId; 
    }

    public void setTaskId(Long taskId) { 
        this.taskId = taskId; 
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

    public UserGetDTO getAssignee() { 
        return assignee; 
    }

    public void setAssignee(UserGetDTO assignee) { 
        this.assignee = assignee; 
    }
}