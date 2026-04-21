package com.busapp.repository;

import com.busapp.model.TaskStatus;
import com.busapp.model.WorkflowTask;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findAllByStatusAndCreatedAtBefore(TaskStatus status, LocalDateTime cutoff);
}
