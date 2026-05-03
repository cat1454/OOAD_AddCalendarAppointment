package calendarappointment.data;

import calendarappointment.model.Appointment;
import calendarappointment.model.Calendar;
import calendarappointment.model.GroupMeeting;
import calendarappointment.model.User;

import java.time.LocalDateTime;

public class DemoDataFactory {
    public User createDemoUser() {
        return new User("U001", "Demo User");
    }

    public Calendar createDemoCalendar() {
        Calendar calendar = new Calendar();

        Appointment mathClass = new Appointment(
                "A001",
                "Math Class",
                "Room A101",
                LocalDateTime.of(2026, 5, 4, 8, 0),
                LocalDateTime.of(2026, 5, 4, 9, 0)
        );

        GroupMeeting projectMeeting = new GroupMeeting(
                "G001",
                "Project Meeting",
                "Room B202",
                LocalDateTime.of(2026, 5, 4, 10, 0),
                LocalDateTime.of(2026, 5, 4, 11, 0)
        );

        calendar.addAppointment(mathClass);
        calendar.addGroupMeeting(projectMeeting);

        return calendar;
    }
}
