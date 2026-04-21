package com.busapp.controller;

import com.busapp.model.ApprovalAction;
import com.busapp.model.WorkflowTask;
import com.busapp.service.WorkflowService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatcherWorkflowController {
    private final WorkflowService workflowService;

    public DispatcherWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<WorkflowTask>> tasks() {
        return ResponseEntity.ok(workflowService.listTasks());
    }

    @PostMapping("/tasks/{taskId}/approve")
    public ResponseEntity<WorkflowTask> approve(@PathVariable Long taskId) {
        return ResponseEntity.ok(workflowService.processApproval(taskId, ApprovalAction.APPROVE));
    }

    @PostMapping("/tasks/{taskId}/return")
    public ResponseEntity<WorkflowTask> returnTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(workflowService.processApproval(taskId, ApprovalAction.RETURN));
    }

    @PostMapping("/tasks/batch-approve")
    public ResponseEntity<List<WorkflowTask>> batchApprove(@RequestBody Map<String, List<Long>> body) {
        return ResponseEntity.ok(workflowService.batchApprove(body.getOrDefault("taskIds", List.of())));
    }
}
