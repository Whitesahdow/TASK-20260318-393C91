package com.busapp.controller;

import com.busapp.model.MessageTask;
import com.busapp.model.NotificationPreference;
import com.busapp.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class PassengerNotificationController {
    private final NotificationService notificationService;

    public PassengerNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreference> getPreferences() {
        return ResponseEntity.ok(notificationService.getPreferenceByUsername(getUsername()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> savePreferences(
            @RequestBody NotificationPreference preference
    ) {
        return ResponseEntity.ok(notificationService.savePreference(getUsername(), preference));
    }

    @GetMapping("")
    public ResponseEntity<List<MessageTask>> messages() {
        return ResponseEntity.ok(notificationService.listMessages(getUsername()));
    }

    @GetMapping("/latest")
    public ResponseEntity<MessageTask> latestMessage() {
        return ResponseEntity.ok(notificationService.latestMessage(getUsername()));
    }

    @PostMapping("/reservations")
    public ResponseEntity<MessageTask> enqueueReservation(
            @RequestBody Map<String, String> body
    ) {
        String stopName = body.getOrDefault("stopName", "Unknown Stop");
        return ResponseEntity.ok(notificationService.enqueueReservation(getUsername(), stopName));
    }

    @PostMapping("/missed-checkin")
    public ResponseEntity<MessageTask> enqueueMissedCheckIn(
            @RequestBody Map<String, String> body
    ) {
        LocalDateTime startTime = LocalDateTime.parse(body.get("busStartTime"));
        return ResponseEntity.ok(notificationService.enqueueMissedCheckIn(getUsername(), startTime));
    }
}

