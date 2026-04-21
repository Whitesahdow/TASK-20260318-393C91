package com.busapp.service;

import com.busapp.model.MessageStatus;
import com.busapp.model.MessageTask;
import com.busapp.model.NotificationPreference;
import com.busapp.repository.MessageQueueRepository;
import com.busapp.repository.NotificationPreferenceRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private final MessageQueueRepository queueRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationScheduler(
            MessageQueueRepository queueRepository,
            NotificationPreferenceRepository preferenceRepository
    ) {
        this.queueRepository = queueRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void processQueue() {
        LocalDateTime now = LocalDateTime.now();
        List<MessageTask> tasks = queueRepository.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(MessageStatus.PENDING, now);
        
        if (tasks.size() > 100) {
            log.warn("Diagnostic Alert: Message queue backlog exceeded threshold. Current depth: {}", tasks.size());
        }
        
        log.info("Processing Queue with traceId=batch size={}", tasks.size());

        for (MessageTask task : tasks) {
            NotificationPreference pref = preferenceRepository.findById(task.getUserId()).orElseGet(() -> {
                NotificationPreference p = new NotificationPreference();
                p.setUserId(task.getUserId());
                return preferenceRepository.save(p);
            });

            if (isInsideDND(LocalDateTime.now().toLocalTime(), pref)) {
                log.info("[Trace: {}] DND Active. Suppressing notification for User {}", task.getTraceId(), task.getUserId());
                task.setFinalContent("Suppressed during Do Not Disturb window.");
                task.setStatus(MessageStatus.SUPPRESSED_DND);
            } else {
                String maskedContent = MaskingUtils.mask(task.getRawContent(), task.getSensitivity());
                task.setFinalContent(maskedContent);
                task.setStatus(MessageStatus.DELIVERED);
            }
            queueRepository.save(task);
        }
    }

    public boolean isInsideDND(LocalTime now, NotificationPreference pref) {
        if (!pref.isQuietHoursEnabled()) {
            return false;
        }
        if (pref.getDndStart().equals(pref.getDndEnd())) {
            return false;
        }
        if (pref.getDndStart().isAfter(pref.getDndEnd())) {
            return now.equals(pref.getDndStart()) || now.isAfter(pref.getDndStart()) || now.isBefore(pref.getDndEnd());
        }
        return now.equals(pref.getDndStart()) || (now.isAfter(pref.getDndStart()) && now.isBefore(pref.getDndEnd()));
    }
}

