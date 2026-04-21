package com.busapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preference")
public class NotificationPreference {
    @Id
    private Long userId;

    @Column(nullable = false)
    private boolean arrivalRemindersEnabled = true;

    @Column(nullable = false)
    private boolean quietHoursEnabled = false;

    @Column(nullable = false)
    private LocalTime dndStart = LocalTime.of(22, 0);

    @Column(nullable = false)
    private LocalTime dndEnd = LocalTime.of(7, 0);

    @Column(nullable = false)
    private int leadTimeMinutes = 10;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isArrivalRemindersEnabled() {
        return arrivalRemindersEnabled;
    }

    public void setArrivalRemindersEnabled(boolean arrivalRemindersEnabled) {
        this.arrivalRemindersEnabled = arrivalRemindersEnabled;
    }

    public boolean isQuietHoursEnabled() {
        return quietHoursEnabled;
    }

    public void setQuietHoursEnabled(boolean quietHoursEnabled) {
        this.quietHoursEnabled = quietHoursEnabled;
    }

    public LocalTime getDndStart() {
        return dndStart;
    }

    public void setDndStart(LocalTime dndStart) {
        this.dndStart = dndStart;
    }

    public LocalTime getDndEnd() {
        return dndEnd;
    }

    public void setDndEnd(LocalTime dndEnd) {
        this.dndEnd = dndEnd;
    }

    public int getLeadTimeMinutes() {
        return leadTimeMinutes;
    }

    public void setLeadTimeMinutes(int leadTimeMinutes) {
        this.leadTimeMinutes = leadTimeMinutes;
    }
}
