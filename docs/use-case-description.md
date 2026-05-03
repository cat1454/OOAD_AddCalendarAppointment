# Use Case Description: Add Calendar Appointment

## Use Case Name

Add Calendar Appointment

## Primary Actor

User

## Goal

The user adds a new appointment to the calendar for a selected date and time.

## Preconditions

- The user has opened the calendar application.
- The calendar UI is showing the user's appointments.
- The user has selected or activated a date and time area in the calendar.

## Main Success Scenario

1. The user chooses to add a new appointment in the UI.
2. The UI identifies which part of the calendar is active.
3. The UI opens an Add Appointment window for the selected date and time.
4. The user enters the appointment name, location, start time, and end time.
5. The UI checks that the appointment information is valid.
6. The calendar records the new appointment in the user's list of appointments.
7. If the user selected a reminder, the reminder is added to the list of reminders.
8. The UI updates the displayed appointment list.

## Alternative Flow A1: Invalid Appointment Information

1. The user enters invalid appointment information, such as an empty name or a negative duration.
2. The UI prevents the appointment from being saved.
3. The UI shows an error message explaining the invalid information.
4. The user corrects the information and submits the appointment again.
5. The use case resumes at step 5 of the main success scenario.

## Alternative Flow A2: Appointment Time Conflict

1. The calendar detects that the user already has an appointment at the selected time.
2. The UI shows a warning message.
3. The user chooses an available time or chooses to replace the previous appointment.
4. If the user chooses an available time, the use case resumes at step 4 of the main success scenario.
5. If the user chooses to replace the previous appointment, the calendar replaces the previous appointment with the new appointment.
6. The UI updates the displayed appointment list.

## Alternative Flow A3: Same Name and Duration as an Existing Group Meeting

1. The calendar detects that the new appointment has the same name and duration as an existing group meeting.
2. The UI asks whether the user intended to join that group meeting.
3. If the user confirms, the user is added to the group meeting's list of participants.
4. The UI updates the displayed appointment list.
5. If the user does not confirm, the use case continues as a normal appointment.

## Postconditions

- The appointment is saved in the user's appointment list, or the user is added to an existing group meeting.
- Any selected reminder is saved in the reminder list.
- The UI displays the updated appointment information.
