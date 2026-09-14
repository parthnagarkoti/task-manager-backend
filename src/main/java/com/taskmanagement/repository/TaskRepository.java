package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * TaskRepository extends both:
 *
 * JpaRepository<Task, Long>
 *   → Standard CRUD: save(), findById(), findAll(), delete()
 *
 * JpaSpecificationExecutor<Task>
 *   → Enables dynamic, type-safe queries via Specification objects.
 *     Used by API #4 (GET /api/tasks) to filter by status, priority,
 *     projectId — all optional, any combination.
 *     Without this, we'd need a separate query method for every
 *     combination of filters (explosion of methods).
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>,
                                        JpaSpecificationExecutor<Task> {
}
