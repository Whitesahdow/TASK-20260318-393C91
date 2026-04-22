package com.busapp.service;

import com.busapp.model.MessageStatus;
import com.busapp.model.MessageTask;
import com.busapp.model.NotificationPreference;
import com.busapp.model.SensitivityLevel;
import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.repository.MessageQueueRepository;
import com.busapp.repository.NotificationPreferenceRepository;
import com.busapp.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationPreferenceRepository preferenceRepository;
    private final MessageQueueRepository queueRepository;
    private final UserRepository userRepository;
    private final com.busapp.repository.NotificationTemplateRepository templateRepository;

    public NotificationService(
            NotificationPreferenceRepository preferenceRepository,
            MessageQueueRepository queueRepository,
            UserRepository userRepository,
            com.busapp.repository.NotificationTemplateRepository templateRepository
    ) {
        this.preferenceRepository = preferenceRepository;
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
    }

    public NotificationPreference getPreferenceByUsername(String username) {
        UserEntity user = findUser(username);
        return preferenceRepository.findById(user.getId()).orElseGet(() -> defaultPreference(user.getId()));
    }

    @Transactional
    public NotificationPreference savePreference(String username, NotificationPreference incoming) {
        UserEntity user = findUser(username);
        NotificationPreference pref = preferenceRepository.findById(user.getId()).orElseGet(() -> defaultPreference(user.getId()));
        pref.setArrivalRemindersEnabled(incoming.isArrivalRemindersEnabled());
        pref.setQuietHoursEnabled(incoming.isQuietHoursEnabled());
        pref.setDndStart(incoming.getDndStart() == null ? LocalTime.of(22, 0) : incoming.getDndStart());
        pref.setDndEnd(incoming.getDndEnd() == null ? LocalTime.of(7, 0) : incoming.getDndEnd());
        pref.setLeadTimeMinutes(incoming.getLeadTimeMinutes() <= 0 ? 10 : incoming.getLeadTimeMinutes());
        return preferenceRepository.save(pref);
    }

    @Transactional
    public MessageTask enqueueReservation(String username, String stopName, LocalDateTime arrivalEta) {
        UserEntity user = findUser(username);
        NotificationPreference pref = preferenceRepository.findById(user.getId()).orElseGet(() -> defaultPreference(user.getId()));

        MessageTask reservation = new MessageTask();
        reservation.setUserId(user.getId());
        reservation.setTypeLabel("Reservation Success");
        reservation.setRawContent("Successfully reserved bus at " + stopName + ". Reminder enabled.");
        reservation.setScheduledAt(LocalDateTime.now().minusSeconds(1));
        reservation.setStatus(MessageStatus.PENDING);
        reservation.setSensitivity(sensitivityForRole(user.getRole()));
        reservation.setTraceId(org.slf4j.MDC.get("traceId") != null ? org.slf4j.MDC.get("traceId") : UUID.randomUUID().toString().substring(0, 8));
        MessageTask savedReservation = queueRepository.save(reservation);

        if (pref.isArrivalRemindersEnabled() && arrivalEta != null) {
            MessageTask reminder = new MessageTask();
            reminder.setUserId(user.getId());
            reminder.setTypeLabel("Arrival Reminder");
            
            String templateBody = templateRepository.findByTemplateKey("ARRIVAL_REMINDER")
                    .map(t -> t.getTemplateBody())
                    .orElse("Reminder: Your bus to {stopName} arrives in {leadTimeMinutes} minutes.");
            
            String content = templateBody
                    .replace("{stopName}", stopName != null ? stopName : "Unknown")
                    .replace("{leadTimeMinutes}", String.valueOf(pref.getLeadTimeMinutes()));
                    
            reminder.setRawContent(content);
            reminder.setScheduledAt(arrivalEta.minusMinutes(pref.getLeadTimeMinutes()));
            reminder.setStatus(MessageStatus.PENDING);
            reminder.setSensitivity(sensitivityForRole(user.getRole()));
            reminder.setTraceId(org.slf4j.MDC.get("traceId") != null ? org.slf4j.MDC.get("traceId") : UUID.randomUUID().toString().substring(0, 8));
            queueRepository.save(reminder);
        }

        return savedReservation;
    }

    @Transactional
    public MessageTask enqueueMissedCheckIn(String username, LocalDateTime busStartTime, String stopName) {
        if (!checkMissedCheckIn(busStartTime)) {
            throw new ValidationException("Missed check-in threshold has not been reached yet.");
        }
        UserEntity user = findUser(username);
        MessageTask task = new MessageTask();
        task.setUserId(user.getId());
        task.setTypeLabel("Missed Check-in");
        
        String templateBody = templateRepository.findByTemplateKey("MISSED_CHECKIN")
                .map(t -> t.getTemplateBody())
                .orElse("Check-in missed for your reservation at stop {stopName}");
                
        String content = templateBody.replace("{stopName}", stopName != null ? stopName : "Unknown");
                
        task.setRawContent(content);
        task.setScheduledAt(LocalDateTime.now().minusSeconds(1));
        task.setStatus(MessageStatus.PENDING);
        task.setSensitivity(sensitivityForRole(user.getRole()));
        task.setTraceId(org.slf4j.MDC.get("traceId") != null ? org.slf4j.MDC.get("traceId") : UUID.randomUUID().toString().substring(0, 8));
        return queueRepository.save(task);
    }

    public boolean checkMissedCheckIn(LocalDateTime busStartTime) {
        if (busStartTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(busStartTime.plusMinutes(5));
    }

    public List<MessageTask> listMessages(String username) {
        UserEntity user = findUser(username);
        return queueRepository.findTop50ByUserIdOrderByScheduledAtDesc(user.getId());
    }

    public MessageTask latestMessage(String username) {
        UserEntity user = findUser(username);
        return queueRepository.findTopByUserIdOrderByScheduledAtDesc(user.getId());
    }

    @Transactional
    public void sendAlertToAdmin(String content) {
        userRepository.findByUsername("admin").ifPresent(admin -> {
            MessageTask task = new MessageTask();
            task.setUserId(admin.getId());
            task.setTypeLabel("Escalation Alert");
            task.setRawContent(content);
            task.setScheduledAt(LocalDateTime.now().minusSeconds(1));
            task.setStatus(MessageStatus.PENDING);
            task.setSensitivity(SensitivityLevel.LEVEL1);
            task.setTraceId(UUID.randomUUID().toString().substring(0, 8));
            queueRepository.save(task);
        });
    }

    private NotificationPreference defaultPreference(Long userId) {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        return preferenceRepository.save(pref);
    }

    private UserEntity findUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username query parameter is required.");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found: " + username));
    }

    private SensitivityLevel sensitivityForRole(UserRole role) {
        if (role == UserRole.ADMIN) return SensitivityLevel.LEVEL1;
        if (role == UserRole.DISPATCHER) return SensitivityLevel.LEVEL2;
        return SensitivityLevel.LEVEL3;
    }
}
