package com.busapp.controller;

import com.busapp.model.MessageTask;
import com.busapp.model.NotificationPreference;
import com.busapp.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passenger")
public class PassengerNotificationController {
    private final NotificationService notificationService;

    public PassengerNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreference> getPreferences(@RequestParam String username) {
        return ResponseEntity.ok(notificationService.getPreferenceByUsername(username));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> savePreferences(
            @RequestParam String username,
            @RequestBody NotificationPreference preference
    ) {
        return ResponseEntity.ok(notificationService.savePreference(username, preference));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageTask>> messages(@RequestParam String username) {
        return ResponseEntity.ok(notificationService.listMessages(username));
    }

    @GetMapping("/messages/latest")
    public ResponseEntity<MessageTask> latestMessage(@RequestParam String username) {
        return ResponseEntity.ok(notificationService.latestMessage(username));
    }

    @PostMapping("/messages/reminder")
    public ResponseEntity<MessageTask> enqueueReminder(
            @RequestParam String username,
            @RequestBody Map<String, String> body
    ) {
        String stopName = body.getOrDefault("stopName", "Unknown Stop");
        return ResponseEntity.ok(notificationService.enqueueReminder(username, stopName));
    }

    @PostMapping("/messages/missed-checkin")
    public ResponseEntity<MessageTask> enqueueMissedCheckIn(
            @RequestParam String username,
            @RequestBody Map<String, String> body
    ) {
        LocalDateTime startTime = LocalDateTime.parse(body.get("busStartTime"));
        return ResponseEntity.ok(notificationService.enqueueMissedCheckIn(username, startTime));
    }
}
