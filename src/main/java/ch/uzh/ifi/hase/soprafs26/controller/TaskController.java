package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Task;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TaskGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TaskPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/trips/{tripId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskGetDTO> getTasks(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        List<Task> tasks = taskService.getTasksForTrip(tripId, token);
        return tasks.stream()
            .map(DTOMapper.INSTANCE::convertEntityToTaskGetDTO)
            .collect(Collectors.toList());
    }

    @PostMapping("/trips/{tripId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskGetDTO createTask(
            @PathVariable Long tripId,
            @RequestBody TaskPostDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Task task = taskService.createTask(tripId, dto.getDescription(), dto.getAssigneeId(), token);
        return DTOMapper.INSTANCE.convertEntityToTaskGetDTO(task);
    }

    @PatchMapping("/trips/{tripId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskGetDTO updateTaskStatus(
            @PathVariable Long tripId,
            @PathVariable Long taskId,
            @RequestBody TaskPatchDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Task task = taskService.updateTaskStatus(tripId, taskId, dto.getStatus(), token);
        return DTOMapper.INSTANCE.convertEntityToTaskGetDTO(task);
    }

    @DeleteMapping("/trips/{tripId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long tripId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        taskService.deleteTask(tripId, taskId, token);
    }
}
