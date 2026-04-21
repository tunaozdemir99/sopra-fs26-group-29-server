package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Task;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TaskPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.TaskService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TaskService taskService;

    private Task buildTask(Long id, String title, String description, TaskStatus status) {
        User assignee = new User();
        assignee.setId(1L);
        assignee.setUsername("alice");

        Task t = new Task();
        t.setTaskId(id);
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus(status);
        t.setAssignee(assignee);
        return t;
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }

    @Test
    public void getTasks_validMember_returnsTaskList() throws Exception {
        Task task = buildTask(1L, "Book hotel", "4-star hotel", TaskStatus.TO_DO);
        given(taskService.getTasksForTrip(eq(10L), any())).willReturn(List.of(task));

        MockHttpServletRequestBuilder getRequest = get("/trips/10/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Book hotel")))
                .andExpect(jsonPath("$[0].status", is("TO_DO")));
    }

    @Test
    public void getTasks_nonMember_returnsForbidden() throws Exception {
        given(taskService.getTasksForTrip(eq(10L), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        MockHttpServletRequestBuilder getRequest = get("/trips/10/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer outsider-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void createTask_validInput_returnsCreated() throws Exception {
        Task task = buildTask(1L, "Buy tickets", "Train tickets", TaskStatus.TO_DO);
        given(taskService.createTask(eq(10L), any(), any(), any(), any())).willReturn(task);

        TaskPostDTO dto = new TaskPostDTO();
        dto.setTitle("Buy tickets");
        dto.setDescription("Train tickets");
        dto.setAssigneeId(1L);

        MockHttpServletRequestBuilder postRequest = post("/trips/10/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(dto));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Buy tickets")))
                .andExpect(jsonPath("$.status", is("TO_DO")));
    }

    @Test
    public void updateTaskStatus_member_returnsUpdatedTask() throws Exception {
        Task updated = buildTask(1L, "Book hotel", "desc", TaskStatus.IN_PROGRESS);
        given(taskService.updateTaskStatus(eq(10L), eq(1L), any(), any())).willReturn(updated);

        TaskPatchDTO dto = new TaskPatchDTO();
        dto.setStatus(TaskStatus.IN_PROGRESS);

        MockHttpServletRequestBuilder patchRequest = patch("/trips/10/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token")
                .content(asJsonString(dto));

        mockMvc.perform(patchRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    public void deleteTask_member_returnsNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(eq(10L), eq(1L), any());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/10/tasks/1")
                .header("Authorization", "Bearer valid-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTask_nonMember_returnsForbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(taskService).deleteTask(eq(10L), eq(1L), any());

        MockHttpServletRequestBuilder deleteRequest = delete("/trips/10/tasks/1")
                .header("Authorization", "Bearer outsider-token");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden());
    }
}
