package com.taskmanagement.dto;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.Priority;
import com.taskmanagement.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Flattened view of a Task — never expose the entity directly.
 * FK relationships are reduced to plain IDs (+ names where useful)
 * so we don't leak lazy-loading proxies or internal entity structure.
 */
@Getter
@Setter
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;

    private Long projectId;
    private String projectName;

    private Long createdById;
    private String createdByName;

    private Long assignedToId;
    private String assignedToName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());

        response.setProjectId(task.getProject().getId());
        response.setProjectName(task.getProject().getName());

        response.setCreatedById(task.getCreatedBy().getId());
        response.setCreatedByName(task.getCreatedBy().getName());

        if (task.getAssignedTo() != null) {
            response.setAssignedToId(task.getAssignedTo().getId());
            response.setAssignedToName(task.getAssignedTo().getName());
        }

        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }
}
