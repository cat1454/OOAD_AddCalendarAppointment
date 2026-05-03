package calendarappointment.controller;

import calendarappointment.model.Appointment;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.Reminder;
import calendarappointment.model.ReminderMethod;
import calendarappointment.model.User;
import calendarappointment.model.ValidationResult;
import calendarappointment.service.AppointmentValidator;
import calendarappointment.service.CalendarService;

import java.util.Optional;

public class AppointmentController {
    private final CalendarService calendarService;
    private final AppointmentValidator validator;

    public AppointmentController(CalendarService calendarService, AppointmentValidator validator) {
        this.calendarService = calendarService;
        this.validator = validator;
    }

    public enum AddAppointmentDecision {
        CHOOSE_ANOTHER_TIME,
        REPLACE_EXISTING,
        JOIN_GROUP_MEETING,
        CREATE_SEPARATE_APPOINTMENT
    }

    public AddAppointmentResult addAppointment(User user, Appointment appointment, Reminder reminder,
                                               AddAppointmentDecision conflictDecision,
                                               AddAppointmentDecision groupMeetingDecision) {
        ValidationResult validationResult = validator.validate(appointment);
        if (!validationResult.isValid()) {
            return AddAppointmentResult.failure(validationResult.getMessage());
        }

        Optional<Appointment> conflict = calendarService.findConflict(appointment);
        if (conflict.isPresent()) {
            Appointment existingAppointment = conflict.get();
            if (conflictDecision == AddAppointmentDecision.CHOOSE_ANOTHER_TIME) {
                return AddAppointmentResult.failure(
                        "This time conflicts with " + existingAppointment.getName()
                                + ". Please choose another available time."
                );
            }

            if (conflictDecision == AddAppointmentDecision.REPLACE_EXISTING) {
                calendarService.replaceAppointment(user, existingAppointment, appointment);
                Reminder addedReminder = addReminderIfSelected(user, reminder);
                return AddAppointmentResult.success(
                        "Replaced " + existingAppointment.getName() + " with " + appointment.getName() + ".",
                        appointment,
                        existingAppointment,
                        null,
                        addedReminder
                );
            }

            return AddAppointmentResult.failure("A conflict was found. Please choose how to continue.");
        }

        Optional<GroupMeeting> matchingGroupMeeting = calendarService.findMatchingGroupMeeting(appointment);
        if (matchingGroupMeeting.isPresent()) {
            GroupMeeting groupMeeting = matchingGroupMeeting.get();
            if (groupMeetingDecision == AddAppointmentDecision.JOIN_GROUP_MEETING) {
                calendarService.joinGroupMeeting(user, groupMeeting);
                return AddAppointmentResult.success(
                        "Joined existing group meeting: " + groupMeeting.getName() + ".",
                        null,
                        null,
                        groupMeeting,
                        null
                );
            }

            if (groupMeetingDecision != AddAppointmentDecision.CREATE_SEPARATE_APPOINTMENT) {
                return AddAppointmentResult.failure(
                        "A matching group meeting was found. Please choose whether to join it or create a separate appointment."
                );
            }
        }

        calendarService.addAppointment(user, appointment);
        Reminder addedReminder = addReminderIfSelected(user, reminder);
        return AddAppointmentResult.success(
                "Added appointment: " + appointment.getName() + ".",
                appointment,
                null,
                null,
                addedReminder
        );
    }

    private Reminder addReminderIfSelected(User user, Reminder reminder) {
        if (reminder != null && reminder.getMethod() != ReminderMethod.NONE) {
            calendarService.addReminder(user, reminder);
            return reminder;
        }
        return null;
    }
}
