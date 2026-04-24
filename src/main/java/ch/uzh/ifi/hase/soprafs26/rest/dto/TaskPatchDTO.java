package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;

public class TaskPatchDTO {

    private TaskStatus status;

    public TaskStatus getStatus() { 
        return status; 
    }

    public void setStatus(TaskStatus status) { 
        this.status = status; 
    }
}
