package com.ooad.calendar.entity;

public class ReminderInfo {
    private final int id;
    private final int appointmentId;
    private final String appointmentTitle;
    private final int minutesBefore;
    private final String message;

    public ReminderInfo(int id, int appointmentId, String appointmentTitle, int minutesBefore, String message) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.appointmentTitle = appointmentTitle;
        this.minutesBefore = minutesBefore;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return appointmentTitle + " - trước " + minutesBefore + " phút: " + message;
    }
}
