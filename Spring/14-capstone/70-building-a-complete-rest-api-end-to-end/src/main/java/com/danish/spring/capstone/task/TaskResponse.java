package com.danish.spring.capstone.task;

import java.time.Instant;

// A separate response DTO, not the entity itself - ownerUsername is deliberately
// excluded from what callers see; internally it's how ownership is enforced, not
// something the API needs to expose.
public record TaskResponse(Long id, String title, String description, TaskStatus status, Instant createdAt) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getCreatedAt());
    }
}
