package calendarappointment.service;

import calendarappointment.model.Appointment;
import calendarappointment.model.Calendar;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.Reminder;
import calendarappointment.model.User;

import java.util.Optional;

public class CalendarService {
    private final Calendar calendar;

    public CalendarService(Calendar calendar) {
        this.calendar = calendar;
    }

    public Optional<Appointment> findConflict(Appointment appointment) {
        return calendar.findConflict(appointment);
    }

    public Optional<GroupMeeting> findMatchingGroupMeeting(Appointment appointment) {
        return calendar.findMatchingGroupMeeting(appointment);
    }

    public void addAppointment(User user, Appointment appointment) {
        if (appointment instanceof GroupMeeting groupMeeting) {
            calendar.addGroupMeeting(groupMeeting);
        } else {
            calendar.addAppointment(appointment);
        }
        user.addAppointment(appointment);
    }

    public void replaceAppointment(User user, Appointment oldAppointment, Appointment newAppointment) {
        if (oldAppointment instanceof GroupMeeting groupMeeting) {
            calendar.removeGroupMeeting(groupMeeting);
        } else {
            calendar.removeAppointment(oldAppointment);
        }
        user.removeAppointment(oldAppointment);
        addAppointment(user, newAppointment);
    }

    public void addReminder(User user, Reminder reminder) {
        calendar.addReminder(reminder);
        user.addReminder(reminder);
    }

    public void joinGroupMeeting(User user, GroupMeeting groupMeeting) {
        user.joinGroupMeeting(groupMeeting);
    }
}
