package com.danish.spring.capstone.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByOwnerUsername(String ownerUsername, Pageable pageable);
    Page<Task> findByOwnerUsernameAndStatus(String ownerUsername, TaskStatus status, Pageable pageable);
    long countByOwnerUsernameAndStatus(String ownerUsername, TaskStatus status);
}
