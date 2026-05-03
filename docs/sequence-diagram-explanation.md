# Sequence Diagram Explanation

The Add Calendar Appointment algorithm starts when the user enters appointment details in the future add-appointment dialog. The UI creates an `Appointment` object and, when selected, a `Reminder` object. The UI then calls `AppointmentController.addAppointment(...)` with the current user, appointment, reminder, and the decisions chosen by the user.

The controller first validates the appointment through `AppointmentValidator`. The validator checks that the appointment exists, the name is not blank, the start and end times are present, and the end time is after the start time. If validation fails, the controller returns a failure result with a clear message for the UI to display.

After validation succeeds, the controller asks `CalendarService` to check for a conflict. The service delegates to `Calendar.findConflict(...)`. If a conflict exists and the user chooses another time, the controller returns a message asking the user to choose an available time. If the user chooses to replace the existing appointment, the service removes the old appointment from the calendar and user, then adds the new appointment to both.

If there is no conflict, the controller checks for a matching group meeting. A matching group meeting uses the same appointment name and duration. If the user chooses to join it, the service adds the user as a group meeting participant. If the user chooses to create a separate appointment, the controller continues with the normal add flow.

For a normal appointment, the service adds the appointment to both the calendar and the user. If a reminder was selected and its method is not `NONE`, the controller asks the service to add the reminder to both the calendar and the user.

The controller returns an `AddAppointmentResult` for every path. The result tells the future UI whether the operation succeeded and includes the message, added appointment, replaced appointment, joined group meeting, or added reminder when relevant.
