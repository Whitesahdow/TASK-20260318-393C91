package com.busapp.model;

import java.time.LocalDateTime;

public class MessageResponse {
    private String typeLabel;
    private String status;
    private String finalContent;
    private LocalDateTime scheduledAt;

    public MessageResponse(MessageTask task) {
        this.typeLabel = task.getTypeLabel();
        this.status = task.getStatus().name();
        this.finalContent = task.getFinalContent();
        this.scheduledAt = task.getScheduledAt();
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinalContent() {
        return finalContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}
