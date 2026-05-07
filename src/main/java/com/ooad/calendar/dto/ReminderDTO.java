package com.ooad.calendar.dto;

public record ReminderDTO(int minutesBefore, String message) {
    @Override
    public String toString() {
        if (minutesBefore >= 1440 && minutesBefore % 1440 == 0) {
            return "Trước " + (minutesBefore / 1440) + " ngày: " + message;
        }
        if (minutesBefore >= 60 && minutesBefore % 60 == 0) {
            return "Trước " + (minutesBefore / 60) + " giờ: " + message;
        }
        return "Trước " + minutesBefore + " phút: " + message;
    }
}
