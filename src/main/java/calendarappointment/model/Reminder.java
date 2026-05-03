package calendarappointment.model;

import java.time.LocalDateTime;

public class Reminder {
    private final String reminderId;
    private final String appointmentId;
    private final LocalDateTime remindAt;
    private final String message;
    private final ReminderMethod method;

    public Reminder(String reminderId, String appointmentId, LocalDateTime remindAt,
                    String message, ReminderMethod method) {
        this.reminderId = reminderId;
        this.appointmentId = appointmentId;
        this.remindAt = remindAt;
        this.message = message;
        this.method = method;
    }

    public String getReminderId() {
        return reminderId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public String getMessage() {
        return message;
    }

    public ReminderMethod getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "Reminder{"
                + "reminderId='" + reminderId + '\''
                + ", appointmentId='" + appointmentId + '\''
                + ", remindAt=" + remindAt
                + ", message='" + message + '\''
                + ", method=" + method
                + '}';
    }
}
