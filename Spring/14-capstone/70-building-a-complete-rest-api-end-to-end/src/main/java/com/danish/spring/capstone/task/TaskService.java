package com.danish.spring.capstone.task;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = "taskStats", key = "#ownerUsername")
    public TaskResponse createTask(String ownerUsername, TaskRequest request) {
        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        Task saved = taskRepository.save(new Task(request.title(), request.description(), status, ownerUsername));
        return TaskResponse.from(saved);
    }

    public Page<TaskResponse> listTasks(String ownerUsername, TaskStatus statusFilter, Pageable pageable) {
        Page<Task> page = statusFilter != null
                ? taskRepository.findByOwnerUsernameAndStatus(ownerUsername, statusFilter, pageable)
                : taskRepository.findByOwnerUsername(ownerUsername, pageable);
        return page.map(TaskResponse::from);
    }

    public TaskResponse getTask(String ownerUsername, Long taskId) {
        return TaskResponse.from(findOwnedTaskOrThrow(ownerUsername, taskId));
    }

    // @Transactional means the dirty-checking pattern from lesson 29 applies here -
    // mutating the managed entity is enough; no explicit save() call needed.
    @Transactional
    @CacheEvict(cacheNames = "taskStats", key = "#ownerUsername")
    public TaskResponse updateTask(String ownerUsername, Long taskId, TaskRequest request) {
        Task task = findOwnedTaskOrThrow(ownerUsername, taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return TaskResponse.from(task);
    }

    @Transactional
    @CacheEvict(cacheNames = "taskStats", key = "#ownerUsername")
    public void deleteTask(String ownerUsername, Long taskId) {
        Task task = findOwnedTaskOrThrow(ownerUsername, taskId);
        taskRepository.delete(task);
    }

    // Cached per-user - a genuinely expensive-ish aggregate (three COUNT queries) that
    // doesn't need to be recomputed on every request, only when this user's tasks
    // actually change (see the @CacheEvict calls above).
    @Cacheable(cacheNames = "taskStats", key = "#ownerUsername")
    public TaskStatsResponse getStats(String ownerUsername) {
        long todo = taskRepository.countByOwnerUsernameAndStatus(ownerUsername, TaskStatus.TODO);
        long inProgress = taskRepository.countByOwnerUsernameAndStatus(ownerUsername, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByOwnerUsernameAndStatus(ownerUsername, TaskStatus.DONE);
        return new TaskStatsResponse(todo, inProgress, done);
    }

    // 404, not 403, for a task owned by someone else - doesn't confirm to a caller
    // that a task with this id even exists at all if it isn't theirs.
    private Task findOwnedTaskOrThrow(String ownerUsername, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found"));
        if (!task.getOwnerUsername().equals(ownerUsername)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found");
        }
        return task;
    }
}
