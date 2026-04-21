package com.busapp.repository;

import com.busapp.model.MessageStatus;
import com.busapp.model.MessageTask;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageQueueRepository extends JpaRepository<MessageTask, Long> {
    List<MessageTask> findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(MessageStatus status, LocalDateTime time);
    List<MessageTask> findTop50ByUserIdOrderByScheduledAtDesc(Long userId);
    MessageTask findTopByUserIdOrderByScheduledAtDesc(Long userId);
}
