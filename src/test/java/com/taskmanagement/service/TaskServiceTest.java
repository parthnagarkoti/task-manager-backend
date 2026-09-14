package com.taskmanagement.service;

import com.taskmanagement.dto.AssignTaskRequest;
import com.taskmanagement.dto.CreateTaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.dto.UpdateStatusRequest;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.Priority;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.TaskStatusHistoryRepository;
import com.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        task = new Task();
        task.setId(1L);
        task.setTitle("Existing Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setProject(project);
        task.setCreatedBy(user);
    }

    // ── createTask ───────────────────────────────────────────────

    @Test
    void createTask_savesAndReturnsTask() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setProjectId(1L);
        request.setPriority(Priority.HIGH);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TaskResponse response = taskService.createTask(request, "test@example.com");

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitle()).isEqualTo("New Task");
        assertThat(response.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getCreatedById()).isEqualTo(1L);
    }

    @Test
    void createTask_projectNotFound_throws404() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setProjectId(99L);

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request, "test@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any());
    }

    // ── assignTask ───────────────────────────────────────────────

    @Test
    void assignTask_setsAssignedUser() {
        User assignee = new User();
        assignee.setId(2L);
        assignee.setName("Assignee");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(2L);

        TaskResponse response = taskService.assignTask(1L, request);

        assertThat(response.getAssignedToId()).isEqualTo(2L);
        assertThat(response.getAssignedToName()).isEqualTo("Assignee");
    }

    @Test
    void assignTask_taskNotFound_throwsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(2L);

        assertThatThrownBy(() -> taskService.assignTask(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void assignTask_userNotFound_throws404() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(99L);

        assertThatThrownBy(() -> taskService.assignTask(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateStatus ─────────────────────────────────────────────

    @Test
    void updateStatus_updatesTaskAndWritesHistory() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse response = taskService.updateStatus(1L, request, "test@example.com");

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        ArgumentCaptor<com.taskmanagement.entity.TaskStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(com.taskmanagement.entity.TaskStatusHistory.class);
        verify(taskStatusHistoryRepository).save(historyCaptor.capture());

        com.taskmanagement.entity.TaskStatusHistory history = historyCaptor.getValue();
        assertThat(history.getOldStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(history.getNewStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(history.getChangedBy()).isEqualTo(user);
    }

    @Test
    void updateStatus_taskNotFound_throwsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TaskStatus.DONE);

        assertThatThrownBy(() -> taskService.updateStatus(99L, request, "test@example.com"))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskStatusHistoryRepository, never()).save(any());
    }

    // ── getTaskById ──────────────────────────────────────────────

    @Test
    void getTaskById_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Existing Task");
    }

    @Test
    void getTaskById_notFound_throwsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getTasks ─────────────────────────────────────────────────

    @Test
    void getTasks_delegatesToSpecificationAndMapsResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);

        when(taskRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(taskPage);

        Page<TaskResponse> result = taskService.getTasks(TaskStatus.TODO, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }
}
