package com.ooad.calendar.entity;

public record Reminder(int minutesBefore, String message) {
    public int getMinutesBefore() {
        return minutesBefore;
    }

    public String getMessage() {
        return message;
    }
}
