package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Task;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User member;
    private Trip trip;
    private Task task;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        member = new User();
        member.setId(1L);
        member.setToken("valid-token");

        trip = new Trip();
        trip.setTripId(10L);
        trip.setMembers(new HashSet<>(Set.of(member)));

        task = new Task();
        task.setTaskId(100L);
        task.setTitle("Book hotel");
        task.setDescription("Book a 4-star hotel in Berlin");
        task.setStatus(TaskStatus.TO_DO);
        task.setAssignee(member);
        task.setTaskTrip(trip);
    }

    @Test
    public void createTask_validInputs_returnsTask() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);

        Task created = taskService.createTask(10L, "Book hotel", "Some description", 1L, "valid-token");

        Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals("Book hotel", created.getTitle());
        assertEquals(member, created.getAssignee());
        assertEquals(TaskStatus.TO_DO, created.getStatus());
    }

    @Test
    public void createTask_blankTitle_throwsBadRequest() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThrows(ResponseStatusException.class,
            () -> taskService.createTask(10L, "  ", "desc", 1L, "valid-token"));
    }

    @Test
    public void createTask_assigneeNotMember_throwsBadRequest() {
        User outsider = new User();
        outsider.setId(99L);

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.of(outsider));

        assertThrows(ResponseStatusException.class,
            () -> taskService.createTask(10L, "Task", "desc", 99L, "valid-token"));
    }

    @Test
    public void createTask_invalidToken_throwsUnauthorized() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
            () -> taskService.createTask(10L, "Task", "desc", 1L, "bad-token"));
    }

    @Test
    public void getTasksForTrip_memberToken_returnsList() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(taskRepository.findByTaskTrip_TripId(10L)).thenReturn(List.of(task));

        List<Task> tasks = taskService.getTasksForTrip(10L, "valid-token");

        assertEquals(1, tasks.size());
        assertEquals("Book hotel", tasks.get(0).getTitle());
    }

    @Test
    public void getTasksForTrip_nonMemberToken_throwsForbidden() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setToken("outsider-token");

        Mockito.when(userRepository.findByToken("outsider-token")).thenReturn(outsider);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThrows(ResponseStatusException.class,
            () -> taskService.getTasksForTrip(10L, "outsider-token"));
    }

    @Test
    public void updateTaskStatus_member_updatesStatus() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        Mockito.when(taskRepository.save(Mockito.any())).thenReturn(task);

        Task updated = taskService.updateTaskStatus(10L, 100L, TaskStatus.IN_PROGRESS, "valid-token");

        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    public void updateTaskStatus_nonMember_throwsForbidden() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setToken("outsider-token");

        Mockito.when(userRepository.findByToken("outsider-token")).thenReturn(outsider);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThrows(ResponseStatusException.class,
            () -> taskService.updateTaskStatus(10L, 100L, TaskStatus.DONE, "outsider-token"));
    }

    @Test
    public void deleteTask_member_deletesTask() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L, 100L, "valid-token");

        Mockito.verify(taskRepository, Mockito.times(1)).delete(task);
    }

    @Test
    public void deleteTask_nonMember_throwsForbidden() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setToken("outsider-token");

        Mockito.when(userRepository.findByToken("outsider-token")).thenReturn(outsider);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThrows(ResponseStatusException.class,
            () -> taskService.deleteTask(10L, 100L, "outsider-token"));
    }

    @Test
    public void deleteTask_taskNotFound_throwsNotFound() {
        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(member);
        Mockito.when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Mockito.when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
            () -> taskService.deleteTask(10L, 999L, "valid-token"));
    }
}
