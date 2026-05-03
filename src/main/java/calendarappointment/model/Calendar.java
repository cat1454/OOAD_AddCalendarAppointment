package calendarappointment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Calendar {
    private final List<Appointment> appointments;
    private final List<Reminder> reminders;
    private final List<GroupMeeting> groupMeetings;

    public Calendar() {
        this.appointments = new ArrayList<>();
        this.reminders = new ArrayList<>();
        this.groupMeetings = new ArrayList<>();
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public List<GroupMeeting> getGroupMeetings() {
        return groupMeetings;
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

    public void addGroupMeeting(GroupMeeting groupMeeting) {
        if (!groupMeetings.contains(groupMeeting)) {
            groupMeetings.add(groupMeeting);
        }
    }

    public void removeGroupMeeting(GroupMeeting groupMeeting) {
        groupMeetings.remove(groupMeeting);
    }

    public Optional<Appointment> findConflict(Appointment appointment) {
        Optional<Appointment> normalAppointmentConflict = appointments.stream()
                .filter(existingAppointment -> existingAppointment.overlapsWith(appointment))
                .findFirst();

        if (normalAppointmentConflict.isPresent()) {
            return normalAppointmentConflict;
        }

        return groupMeetings.stream()
                .filter(existingMeeting -> existingMeeting.overlapsWith(appointment))
                .map(existingMeeting -> (Appointment) existingMeeting)
                .findFirst();
    }

    public Optional<GroupMeeting> findMatchingGroupMeeting(Appointment appointment) {
        return groupMeetings.stream()
                .filter(groupMeeting -> groupMeeting.hasSameNameAndDuration(appointment))
                .findFirst();
    }
}
