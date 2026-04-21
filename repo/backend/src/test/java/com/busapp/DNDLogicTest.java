package com.busapp;

import com.busapp.model.NotificationPreference;
import com.busapp.repository.MessageQueueRepository;
import com.busapp.repository.NotificationPreferenceRepository;
import com.busapp.service.NotificationScheduler;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DNDLogicTest {
    @Mock
    private MessageQueueRepository queueRepository;
    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Test
    void whenTimeIs2300_andDNDIs22to07_thenSuppress() {
        NotificationScheduler scheduler = new NotificationScheduler(queueRepository, preferenceRepository);
        LocalTime now = LocalTime.of(23, 0);
        NotificationPreference pref = new NotificationPreference();
        pref.setDndStart(LocalTime.of(22, 0));
        pref.setDndEnd(LocalTime.of(7, 0));

        assertTrue(scheduler.isInsideDND(now, pref), "Notification should be suppressed at 23:00");
    }
}
