package com.ooad.calendar.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private int id;
    private int ownerId;
    private Integer groupMeetingId;
    private String title;
    private String location;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private final List<Reminder> reminders = new ArrayList<>();

    public Appointment(int id, int ownerId, Integer groupMeetingId, String title, String location,
                       LocalDateTime startsAt, LocalDateTime endsAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.groupMeetingId = groupMeetingId;
        this.title = title;
        this.location = location;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Integer getGroupMeetingId() {
        return groupMeetingId;
    }

    public void setGroupMeetingId(Integer groupMeetingId) {
        this.groupMeetingId = groupMeetingId;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return title + " - " + startsAt.format(formatter);
    }
}
