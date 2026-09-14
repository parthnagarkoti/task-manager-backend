package com.taskmanagement.entity;

import com.taskmanagement.enums.Priority;
import com.taskmanagement.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The core entity — all 5 REST APIs operate on this table.
 *
 * Relationships:
 *   - project    : which project this task belongs to (mandatory)
 *   - createdBy  : the authenticated user who created it (set from JWT)
 *   - assignedTo : the user assigned to this task (nullable — task may be unassigned)
 *
 * Enums are stored as strings (EnumType.STRING) so MySQL shows
 * "IN_PROGRESS" not "1" — much easier to debug.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;   // default status on creation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;   // default priority on creation

    @Column(name = "due_date")
    private LocalDate dueDate;

    // Task belongs to a Project — required for creation (API #1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Set automatically from the JWT token in the service layer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Nullable — a task can exist without being assigned yet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp automatically sets this to NOW() on every UPDATE
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
