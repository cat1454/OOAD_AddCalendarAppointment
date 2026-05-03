package calendarappointment;

import calendarappointment.data.DemoDataFactory;
import calendarappointment.model.Appointment;
import calendarappointment.model.Calendar;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.User;

public class Main {
    public static void main(String[] args) {
        DemoDataFactory demoDataFactory = new DemoDataFactory();
        User demoUser = demoDataFactory.createDemoUser();
        Calendar demoCalendar = demoDataFactory.createDemoCalendar();

        System.out.println("Add Calendar Appointment OOAD Demo - Project foundation is ready.");
        System.out.println();
        System.out.println("Demo user:");
        System.out.println(demoUser);
        System.out.println();
        System.out.println("Existing appointments:");
        for (Appointment appointment : demoCalendar.getAppointments()) {
            System.out.println("- " + appointment.toDisplayRow());
        }
        System.out.println();
        System.out.println("Existing group meetings:");
        for (GroupMeeting groupMeeting : demoCalendar.getGroupMeetings()) {
            System.out.println("- " + groupMeeting.toDisplayRow());
        }
        System.out.println();
        System.out.println("Reminders count: " + demoCalendar.getReminders().size());
    }
}
