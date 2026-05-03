package calendarappointment.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Appointment {
    private final String appointmentId;
    private final String name;
    private final String location;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public Appointment(String appointmentId, String name, String location,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.appointmentId = appointmentId;
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public boolean overlapsWith(Appointment other) {
        if (other == null) {
            return false;
        }
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }

    public boolean hasSameNameAndDuration(Appointment other) {
        if (other == null) {
            return false;
        }
        return name.equalsIgnoreCase(other.name) && getDuration().equals(other.getDuration());
    }

    public String toDisplayRow() {
        return name + " | " + location + " | " + startTime + " - " + endTime;
    }

    @Override
    public String toString() {
        return "Appointment{"
                + "appointmentId='" + appointmentId + '\''
                + ", name='" + name + '\''
                + ", location='" + location + '\''
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + '}';
    }
}
