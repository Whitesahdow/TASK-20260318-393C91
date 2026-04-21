package com.busapp;

import com.busapp.model.NotificationPreference;
import com.busapp.repository.MessageQueueRepository;
import com.busapp.repository.NotificationPreferenceRepository;
import com.busapp.repository.UserRepository;
import com.busapp.service.NotificationScheduler;
import com.busapp.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class NotificationModuleTest {
    @Mock
    private MessageQueueRepository queueRepository;
    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void whenTimeIs2300_andDNDIs22to07_thenSuppress() {
        NotificationScheduler scheduler = new NotificationScheduler(queueRepository, preferenceRepository);
        LocalTime now = LocalTime.of(23, 0);
        NotificationPreference pref = new NotificationPreference();
        pref.setDndStart(LocalTime.of(22, 0));
        pref.setDndEnd(LocalTime.of(7, 0));

        assertTrue(scheduler.isInsideDND(now, pref), "Notification should be suppressed at 23:00");
    }

    @Test
    void whenTimeIs5MinAfterStart_thenTriggerMissedCheckIn() {
        NotificationService notificationService = new NotificationService(preferenceRepository, queueRepository, userRepository);
        LocalDateTime busStartTime = LocalDateTime.now().minusMinutes(6);
        boolean shouldTrigger = notificationService.checkMissedCheckIn(busStartTime);
        assertTrue(shouldTrigger);
    }
}
