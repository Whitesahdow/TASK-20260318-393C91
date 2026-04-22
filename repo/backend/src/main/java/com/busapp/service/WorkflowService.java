package com.busapp.service;

import com.busapp.model.ApprovalAction;
import com.busapp.model.TaskStatus;
import com.busapp.model.TaskType;
import com.busapp.model.WorkflowTask;
import com.busapp.repository.WorkflowTaskRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);
    private final WorkflowTaskRepository taskRepository;

    public WorkflowService(WorkflowTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<WorkflowTask> listTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public WorkflowTask processApproval(Long taskId, ApprovalAction action) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ValidationException("Task not found: " + taskId));

        if (task.getType() == TaskType.ROUTE_CHANGE && task.isHighImpact()) {
            task.setBranch("RISKY");
        } else {
            task.setBranch("ROUTINE");
        }

        if (action == ApprovalAction.RETURN) {
            log.info("[Trace: {}] Task {} returned for resubmission by {}", MDC.get("traceId"), taskId, org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
            task.setStatus(TaskStatus.RETURNED);
            task.setComment("Returned for resubmission");
            task.setProgress(Math.max(10, task.getProgress() - 20));
            task.setApprovedByAdmin(false);
            task.setAdminUsername(null);
            task.setApprovedByDispatcher(false);
            task.setDispatcherUsername(null);
            return taskRepository.save(task);
        }

        String role = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        if ("RISKY".equals(task.getBranch())) {
            if (role.contains("ROLE_ADMIN")) {
                task.setApprovedByAdmin(true);
                task.setAdminUsername(username);
            } else if (role.contains("ROLE_DISPATCHER")) {
                task.setApprovedByDispatcher(true);
                task.setDispatcherUsername(username);
            }

            if (task.isApprovedByAdmin() && task.isApprovedByDispatcher()) {
                task.setStatus(TaskStatus.APPROVED);
                task.setComment(String.format("Jointly Approved by Disp: %s and Admin: %s", task.getDispatcherUsername(), task.getAdminUsername()));
                task.setProgress(100);
            } else {
                task.setComment("Pending parallel approval");
                task.setProgress(70);
            }
        } else {
            task.setStatus(TaskStatus.APPROVED);
            task.setComment("Approved by " + username);
            task.setProgress(100);
        }

        log.info("[Trace: {}] Task {} approval processed. New status: {}", MDC.get("traceId"), taskId, task.getStatus());
        return taskRepository.save(task);
    }

    @Transactional
    public List<WorkflowTask> batchApprove(List<Long> taskIds) {
        List<WorkflowTask> result = new ArrayList<>();
        for (Long taskId : taskIds) {
            WorkflowTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ValidationException("Task not found: " + taskId));
            if (task.getType() == TaskType.ROUTE_CHANGE && task.isHighImpact()) {
                throw new ValidationException("Cannot batch approve RISKY tasks.");
            }
            result.add(processApproval(taskId, ApprovalAction.APPROVE));
        }
        return result;
    }
}
