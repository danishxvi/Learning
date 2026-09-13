package com.danish.spring.capstone.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(Authentication auth, @Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.createTask(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<TaskResponse> listTasks(Authentication auth, @RequestParam(required = false) TaskStatus status, Pageable pageable) {
        return taskService.listTasks(auth.getName(), status, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(Authentication auth, @PathVariable Long id) {
        return taskService.getTask(auth.getName(), id);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(Authentication auth, @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(Authentication auth, @PathVariable Long id) {
        taskService.deleteTask(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public TaskStatsResponse stats(Authentication auth) {
        return taskService.getStats(auth.getName());
    }
}
