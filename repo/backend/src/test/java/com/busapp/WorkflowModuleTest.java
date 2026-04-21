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
        assertEquals(TaskStatus.PENDING, processed.getStatus());
    }

    @Test
    void testBatchApproveRoutineTasks() {
        WorkflowTask task1 = new WorkflowTask();
        task1.setId(1L);
        task1.setType(TaskType.REMINDER_RULE);
        task1.setHighImpact(false);
        task1.setStatus(TaskStatus.PENDING);
        
        WorkflowTask task2 = new WorkflowTask();
        task2.setId(2L);
        task2.setType(TaskType.ABNORMAL_DATA_REVIEW);
        task2.setHighImpact(false);
        task2.setStatus(TaskStatus.PENDING);
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> i.getArguments()[0]);
        
        workflowService.batchApprove(List.of(1L, 2L));
        
        assertEquals(TaskStatus.APPROVED, task1.getStatus());
        assertEquals("ROUTINE", task1.getBranch());
        assertEquals(TaskStatus.APPROVED, task2.getStatus());
        assertEquals("ROUTINE", task2.getBranch());
    }
}
