package com.busapp.service;

import com.busapp.model.TaskStatus;
import com.busapp.model.WorkflowTask;
import com.busapp.repository.WorkflowTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EscalationService {
    private final WorkflowTaskRepository taskRepository;
    private final NotificationService notificationService;

    public EscalationService(WorkflowTaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkTimeouts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<WorkflowTask> overdueTasks = taskRepository.findAllByStatusAndCreatedAtBefore(TaskStatus.PENDING, cutoff);
        for (WorkflowTask task : overdueTasks) {
            task.setStatus(TaskStatus.ESCALATED);
            task.setComment("Escalated after 24h timeout");
            taskRepository.save(task);
            notificationService.sendAlertToAdmin("ESCALATION: Task " + task.getId() + " is overdue.");
        }
    }
}
