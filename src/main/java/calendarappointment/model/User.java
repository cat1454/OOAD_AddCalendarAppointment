package calendarappointment.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String userId;
    private final String fullName;
    private final List<Appointment> appointments;
    private final List<Reminder> reminders;

    public User(String userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
        this.appointments = new ArrayList<>();
        this.reminders = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
    }

    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
    }

    public void joinGroupMeeting(GroupMeeting groupMeeting) {
        groupMeeting.addParticipant(this);
        if (!appointments.contains(groupMeeting)) {
            appointments.add(groupMeeting);
        }
    }

    @Override
    public String toString() {
        return "User{"
                + "userId='" + userId + '\''
                + ", fullName='" + fullName + '\''
                + ", appointments=" + appointments.size()
                + ", reminders=" + reminders.size()
                + '}';
    }
}
