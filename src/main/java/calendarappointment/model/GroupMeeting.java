package calendarappointment.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupMeeting extends Appointment {
    private final List<User> participants;

    public GroupMeeting(String appointmentId, String name, String location,
                        LocalDateTime startTime, LocalDateTime endTime) {
        super(appointmentId, name, location, startTime, endTime);
        this.participants = new ArrayList<>();
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public boolean hasParticipant(User user) {
        return participants.contains(user);
    }

    @Override
    public String toString() {
        return "GroupMeeting{"
                + "appointmentId='" + getAppointmentId() + '\''
                + ", name='" + getName() + '\''
                + ", location='" + getLocation() + '\''
                + ", startTime=" + getStartTime()
                + ", endTime=" + getEndTime()
                + ", participants=" + participants.size()
                + '}';
    }
}
