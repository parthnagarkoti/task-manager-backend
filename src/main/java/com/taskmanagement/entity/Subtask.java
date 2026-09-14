package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A child item under a Task.
 * Simple: just a title and a completion flag.
 * No dedicated API endpoint — exists for relational completeness.
 */
@Entity
@Table(name = "subtasks")
@Getter
@Setter
@NoArgsConstructor
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Defaults to false — not yet completed
    @Column(nullable = false)
    private boolean completed = false;

    // Each subtask belongs to exactly one parent task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
