package com.busapp;

import com.busapp.model.ApprovalAction;
import com.busapp.model.TaskStatus;
import com.busapp.model.TaskType;
import com.busapp.model.WorkflowTask;
import com.busapp.repository.WorkflowTaskRepository;
import com.busapp.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WorkflowModuleTest {
    @Mock
    private WorkflowTaskRepository taskRepository;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void highImpactRouteChangeShouldBecomeRiskyBranch() {
        WorkflowTask task = new WorkflowTask();
        task.setId(1L);
        task.setType(TaskType.ROUTE_CHANGE);
        task.setHighImpact(true);
        task.setStatus(TaskStatus.PENDING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        WorkflowTask processed = workflowService.processApproval(1L, ApprovalAction.RETURN);
        assertEquals("RISKY", processed.getBranch());
        assertEquals(TaskStatus.RETURNED, processed.getStatus());
    }
}
