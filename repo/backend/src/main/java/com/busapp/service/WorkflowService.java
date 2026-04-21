package com.busapp.service;

import com.busapp.model.ApprovalAction;
import com.busapp.model.TaskStatus;
import com.busapp.model.TaskType;
import com.busapp.model.WorkflowTask;
import com.busapp.repository.WorkflowTaskRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {
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
            task.setStatus(TaskStatus.RETURNED);
            task.setComment("Returned for resubmission");
            task.setProgress(Math.max(10, task.getProgress() - 20));
        } else {
            task.setStatus(TaskStatus.APPROVED);
            task.setComment("Approved");
            task.setProgress(100);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public List<WorkflowTask> batchApprove(List<Long> taskIds) {
        List<WorkflowTask> result = new ArrayList<>();
        for (Long taskId : taskIds) {
            result.add(processApproval(taskId, ApprovalAction.APPROVE));
        }
        return result;
    }
}
