package calendarappointment.controller;

import calendarappointment.model.Appointment;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.Reminder;

public class AddAppointmentResult {
    private final boolean success;
    private final String message;
    private final Appointment appointment;
    private final Appointment replacedAppointment;
    private final GroupMeeting groupMeeting;
    private final Reminder reminder;

    private AddAppointmentResult(boolean success, String message, Appointment appointment,
                                 Appointment replacedAppointment, GroupMeeting groupMeeting,
                                 Reminder reminder) {
        this.success = success;
        this.message = message;
        this.appointment = appointment;
        this.replacedAppointment = replacedAppointment;
        this.groupMeeting = groupMeeting;
        this.reminder = reminder;
    }

    public static AddAppointmentResult success(String message) {
        return new AddAppointmentResult(true, message, null, null, null, null);
    }

    public static AddAppointmentResult success(String message, Appointment appointment,
                                               Appointment replacedAppointment,
                                               GroupMeeting groupMeeting, Reminder reminder) {
        return new AddAppointmentResult(true, message, appointment, replacedAppointment,
                groupMeeting, reminder);
    }

    public static AddAppointmentResult failure(String message) {
        return new AddAppointmentResult(false, message, null, null, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Appointment getReplacedAppointment() {
        return replacedAppointment;
    }

    public GroupMeeting getGroupMeeting() {
        return groupMeeting;
    }

    public Reminder getReminder() {
        return reminder;
    }
}
