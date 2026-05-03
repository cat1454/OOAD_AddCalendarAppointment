package calendarappointment;

import calendarappointment.controller.AddAppointmentResult;
import calendarappointment.controller.AppointmentController;
import calendarappointment.data.DemoDataFactory;
import calendarappointment.model.Appointment;
import calendarappointment.model.Calendar;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.Reminder;
import calendarappointment.model.ReminderMethod;
import calendarappointment.model.User;
import calendarappointment.service.AppointmentValidator;
import calendarappointment.service.CalendarService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        DemoDataFactory demoDataFactory = new DemoDataFactory();
        User demoUser = demoDataFactory.createDemoUser();
        Calendar demoCalendar = demoDataFactory.createDemoCalendar();
        CalendarService calendarService = new CalendarService(demoCalendar);
        AppointmentValidator validator = new AppointmentValidator();
        AppointmentController appointmentController = new AppointmentController(calendarService, validator);

        System.out.println("Add Calendar Appointment OOAD Demo - Phase 3 Core Algorithm");
        System.out.println();
        System.out.println("Demo user:");
        System.out.println(demoUser);
        printCalendarState(demoCalendar);

        printCaseHeader("Case 1: Invalid blank name");
        Appointment blankNameAppointment = new Appointment(
                "A002",
                " ",
                "Room C303",
                LocalDateTime.of(2026, 5, 4, 13, 0),
                LocalDateTime.of(2026, 5, 4, 14, 0)
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                blankNameAppointment,
                null,
                null,
                null
        ));

        printCaseHeader("Case 2: Invalid duration");
        Appointment invalidDurationAppointment = new Appointment(
                "A003",
                "Study Session",
                "Library",
                LocalDateTime.of(2026, 5, 4, 15, 0),
                LocalDateTime.of(2026, 5, 4, 14, 0)
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                invalidDurationAppointment,
                null,
                null,
                null
        ));

        printCaseHeader("Case 3: Conflict with Math Class, choose another time");
        Appointment conflictingAppointment = new Appointment(
                "A004",
                "English Class",
                "Room D404",
                LocalDateTime.of(2026, 5, 4, 8, 30),
                LocalDateTime.of(2026, 5, 4, 9, 30)
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                conflictingAppointment,
                null,
                AppointmentController.AddAppointmentDecision.CHOOSE_ANOTHER_TIME,
                null
        ));

        printCaseHeader("Case 4: Conflict with Math Class, replace existing");
        Appointment replacementAppointment = new Appointment(
                "A005",
                "Chemistry Lab",
                "Lab 2",
                LocalDateTime.of(2026, 5, 4, 8, 0),
                LocalDateTime.of(2026, 5, 4, 9, 0)
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                replacementAppointment,
                null,
                AppointmentController.AddAppointmentDecision.REPLACE_EXISTING,
                null
        ));
        printCalendarState(demoCalendar);

        printCaseHeader("Case 5: Match Project Meeting, join group meeting");
        Appointment matchingGroupMeetingAppointment = new Appointment(
                "A006",
                "Project Meeting",
                "Room B202",
                LocalDateTime.of(2026, 5, 4, 12, 0),
                LocalDateTime.of(2026, 5, 4, 13, 0)
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                matchingGroupMeetingAppointment,
                null,
                null,
                AppointmentController.AddAppointmentDecision.JOIN_GROUP_MEETING
        ));
        printGroupMeetingParticipants(demoCalendar);

        printCaseHeader("Case 6: Normal appointment with reminder");
        Appointment dentistAppointment = new Appointment(
                "A007",
                "Dentist Appointment",
                "City Dental Clinic",
                LocalDateTime.of(2026, 5, 4, 16, 0),
                LocalDateTime.of(2026, 5, 4, 16, 30)
        );
        Reminder dentistReminder = new Reminder(
                "R001",
                dentistAppointment.getAppointmentId(),
                LocalDateTime.of(2026, 5, 4, 15, 30),
                "Dentist appointment starts in 30 minutes.",
                ReminderMethod.POPUP
        );
        printResult(appointmentController.addAppointment(
                demoUser,
                dentistAppointment,
                dentistReminder,
                null,
                null
        ));
        printCalendarState(demoCalendar);
        System.out.println("Demo user after Phase 3 scenarios:");
        System.out.println(demoUser);
    }

    private static void printCaseHeader(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printResult(AddAppointmentResult result) {
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Message: " + result.getMessage());
        if (result.getAppointment() != null) {
            System.out.println("Added appointment: " + result.getAppointment().toDisplayRow());
        }
        if (result.getReplacedAppointment() != null) {
            System.out.println("Replaced appointment: " + result.getReplacedAppointment().toDisplayRow());
        }
        if (result.getGroupMeeting() != null) {
            System.out.println("Joined group meeting: " + result.getGroupMeeting().toDisplayRow());
        }
        if (result.getReminder() != null) {
            System.out.println("Added reminder: " + result.getReminder());
        }
    }

    private static void printCalendarState(Calendar calendar) {
        System.out.println();
        System.out.println("Current appointments:");
        for (Appointment appointment : calendar.getAppointments()) {
            System.out.println("- " + appointment.toDisplayRow());
        }
        System.out.println();
        System.out.println("Current group meetings:");
        for (GroupMeeting groupMeeting : calendar.getGroupMeetings()) {
            System.out.println("- " + groupMeeting.toDisplayRow());
        }
        System.out.println();
        System.out.println("Reminders count: " + calendar.getReminders().size());
    }

    private static void printGroupMeetingParticipants(Calendar calendar) {
        for (GroupMeeting groupMeeting : calendar.getGroupMeetings()) {
            System.out.println(groupMeeting.getName() + " participants: "
                    + groupMeeting.getParticipants().size());
            for (User participant : groupMeeting.getParticipants()) {
                System.out.println("- " + participant.getFullName());
            }
        }
    }
}
