package com.busapp;

import com.busapp.repository.MessageQueueRepository;
import com.busapp.repository.NotificationPreferenceRepository;
import com.busapp.repository.UserRepository;
import com.busapp.service.NotificationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CheckInTriggerTest {
    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private MessageQueueRepository queueRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void whenTimeIs5MinAfterStart_thenTriggerMissedCheckIn() {
        NotificationService notificationService = new NotificationService(preferenceRepository, queueRepository, userRepository);
        LocalDateTime busStartTime = LocalDateTime.now().minusMinutes(6);
        boolean shouldTrigger = notificationService.checkMissedCheckIn(busStartTime);
        assertTrue(shouldTrigger);
    }
}
