# Class Diagram Explanation

The current class diagram focuses on the Phase 2 domain model only.

- `User` represents the demo calendar user and keeps the user's appointments and reminders.
- `Calendar` stores appointments, reminders, and group meetings in memory. It can find time conflicts and matching group meetings.
- `Appointment` represents a normal calendar appointment with a name, location, start time, end time, duration, and overlap behavior.
- `GroupMeeting` extends `Appointment` and adds a participant list.
- `Reminder` represents a reminder for an appointment, including the reminder time, message, and method.
- `ValidationResult` represents the result of a validation check that will be used by later phases.
- `ReminderMethod` defines the available reminder methods: none, popup, email, or SMS.
