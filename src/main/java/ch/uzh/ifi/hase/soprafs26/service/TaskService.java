package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Task;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       TripRepository tripRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    // GET /trips/{tripId}/tasks
    public List<Task> getTasksForTrip(Long tripId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        return taskRepository.findByTaskTrip_TripId(tripId);
    }

    // POST /trips/{tripId}/tasks
    public Task createTask(Long tripId, String title, String description, Long assigneeId, String token) {
        User creator = userRepository.findByToken(token);
        if (creator == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(creator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
        if (!trip.getMembers().contains(assignee)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee is not a member of this trip");
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(TaskStatus.TO_DO);
        task.setAssignee(assignee);
        task.setTaskTrip(trip);
        return taskRepository.save(task);
    }

    // PATCH /trips/{tripId}/tasks/{taskId}
    public Task updateTaskStatus(Long tripId, Long taskId, TaskStatus newStatus, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    // DELETE /trips/{tripId}/tasks/{taskId}
    public void deleteTask(Long tripId, Long taskId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip");
        }
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }
}
