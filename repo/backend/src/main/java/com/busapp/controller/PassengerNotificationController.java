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
    public ResponseEntity<List<com.busapp.model.MessageResponse>> messages() {
        List<com.busapp.model.MessageResponse> dtos = notificationService.listMessages(getUsername()).stream()
            .map(com.busapp.model.MessageResponse::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/latest")
    public ResponseEntity<com.busapp.model.MessageResponse> latestMessage() {
        MessageTask task = notificationService.latestMessage(getUsername());
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new com.busapp.model.MessageResponse(task));
    }

    @PostMapping("/reservations")
    public ResponseEntity<com.busapp.model.MessageResponse> enqueueReservation(
            @RequestBody Map<String, String> body
    ) {
        String stopName = body.getOrDefault("stopName", "Unknown Stop");
        String arrivalEtaStr = body.get("arrivalEta");
        LocalDateTime arrivalEta = null;
        if (arrivalEtaStr != null) {
            arrivalEta = LocalDateTime.parse(arrivalEtaStr);
        }
        return ResponseEntity.ok(new com.busapp.model.MessageResponse(notificationService.enqueueReservation(getUsername(), stopName, arrivalEta)));
    }

    @PostMapping("/missed-checkin")
    public ResponseEntity<com.busapp.model.MessageResponse> enqueueMissedCheckIn(
            @RequestBody java.util.Map<String, String> body
    ) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(body.get("busStartTime"));
        String stopName = body.getOrDefault("stopName", "Unknown Stop");
        return ResponseEntity.ok(new com.busapp.model.MessageResponse(notificationService.enqueueMissedCheckIn(getUsername(), startTime, stopName)));
    }
}
