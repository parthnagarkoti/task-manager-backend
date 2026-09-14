package com.taskmanagement.entity;

import com.taskmanagement.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit trail — records every status change made to a Task.
 *
 * Written automatically by TaskService.updateStatus() (API #3).
 * No dedicated endpoint needed — it's purely an audit log.
 *
 * Interview talking point: "When a task's status changes, my service
 * layer writes a history record in the same transaction, giving a
 * full, queryable audit trail of every state transition."
 */
@Entity
@Table(name = "task_status_history")
@Getter
@Setter
@NoArgsConstructor
public class TaskStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Who triggered the status change
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private TaskStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private TaskStatus newStatus;

    // Set manually in the service — not using @CreationTimestamp
    // so we have explicit control over the timestamp
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
