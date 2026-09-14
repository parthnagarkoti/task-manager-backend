package com.taskmanagement.repository;

import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.Priority;
import com.taskmanagement.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest spins up an in-memory H2 database (see src/test/resources/application.properties),
 * auto-configures Hibernate against our entities, and rolls back each test's transaction —
 * so tests don't interfere with each other. The main data.sql (seeding a user/team/project)
 * is on the test classpath too and runs automatically for this embedded database.
 */
@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findByEmail("test@example.com").orElseThrow();
        project = projectRepository.findAll().get(0);

        saveTask("Low priority todo", TaskStatus.TODO, Priority.LOW);
        saveTask("High priority in progress", TaskStatus.IN_PROGRESS, Priority.HIGH);
        saveTask("High priority done", TaskStatus.DONE, Priority.HIGH);
    }

    private void saveTask(String title, TaskStatus status, Priority priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setProject(project);
        task.setCreatedBy(user);
        taskRepository.save(task);
    }

    @Test
    void findAll_withStatusFilter_returnsOnlyMatchingTasks() {
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("status"), TaskStatus.DONE);

        Page<Task> result = taskRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("High priority done");
    }

    @Test
    void findAll_withPriorityFilter_returnsAllMatchingTasks() {
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("priority"), Priority.HIGH);

        Page<Task> result = taskRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAll_withCombinedFilters_returnsIntersection() {
        Specification<Task> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("priority"), Priority.HIGH),
                cb.equal(root.get("status"), TaskStatus.IN_PROGRESS)
        );

        Page<Task> result = taskRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("High priority in progress");
    }

    @Test
    void findAll_pagination_respectsPageSize() {
        Page<Task> firstPage = taskRepository.findAll(PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
