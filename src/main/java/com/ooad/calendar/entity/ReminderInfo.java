package com.ooad.calendar.entity;

import java.time.LocalDateTime;

public class ReminderInfo {
    private final int id;
    private final int appointmentId;
    private final String appointmentTitle;
    private final int minutesBefore;
    private final String message;
    private final LocalDateTime startsAt;
    private final LocalDateTime remindAt;

    public ReminderInfo(int id, int appointmentId, String appointmentTitle, int minutesBefore, String message) {
        this(id, appointmentId, appointmentTitle, minutesBefore, message, null);
    }

    public ReminderInfo(int id, int appointmentId, String appointmentTitle, int minutesBefore, String message,
                        LocalDateTime startsAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.appointmentTitle = appointmentTitle;
        this.minutesBefore = minutesBefore;
        this.message = message;
        this.startsAt = startsAt;
        this.remindAt = startsAt == null ? null : startsAt.minusMinutes(minutesBefore);
    }

    public int getId() {
        return id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    @Override
    public String toString() {
        return appointmentTitle + " - truoc " + minutesBefore + " phut: " + message;
    }
}
