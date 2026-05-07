package com.ooad.calendar.entity;

public record GroupMeeting(int id, String title, String location, int durationMinutes) {
    public int getId() {
        return id;
    }

    public String getName() {
        return title;
    }

    @Override
    public String toString() {
        return title + " (" + durationMinutes + " phút)";
    }
}
