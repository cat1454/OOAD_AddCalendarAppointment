package com.ooad.calendar.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentDTO(
        Integer appointmentId,
        Integer groupMeetingId,
        int ownerId,
        String title,
        String location,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        List<ReminderDTO> reminders,
        boolean groupMeeting
) {
}
