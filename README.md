# Add Calendar Appointment OOAD Demo

## Project Description

This project is a Java desktop application demo for the Object-Oriented Analysis and Design coursework use case **Add Calendar Appointment**.

The application will later demonstrate a user viewing appointments and choosing to add a calendar appointment. The implementation will remain simple, use in-memory data, and follow the behavior described by the project sequence diagram.

## Assignment Requirements

- Export a Class Diagram and Sequence Diagram as an image or PDF.
- Provide a demo program with a main UI that shows the user's appointments and allows the user to choose Add Calendar Appointment.
- Ensure the program follows the algorithm shown in the Sequence Diagram.
- Provide a demo report file.
- Provide a source code link.

## Current Development Phase

The project is currently in **Phase 3 - Core algorithm**.

This phase adds the controller and service logic for the Add Calendar Appointment use case. The console demo now validates appointment input, checks schedule conflicts, supports choosing another time or replacing an existing appointment, detects matching group meetings, joins a group meeting when selected, and adds reminders when a reminder method is selected. Swing UI and database storage are still intentionally left for later phases.

## Planned Phases

1. Phase 1 - Project foundation
2. Phase 2 - Domain model
3. Phase 3 - Core algorithm
4. Phase 4 - Swing UI
5. Phase 5 - Diagrams and demo report

## How to Run

Use Maven from the project root:

```bash
mvn clean compile exec:java
```

Expected output includes the startup information plus six console scenarios:

```text
=== Case 1: Invalid blank name ===
Message: Appointment name is required.

=== Case 2: Invalid duration ===
Message: End time must be after start time.

=== Case 3: Conflict with Math Class, choose another time ===
Message: This time conflicts with Math Class. Please choose another available time.

=== Case 4: Conflict with Math Class, replace existing ===
Message: Replaced Math Class with Chemistry Lab.

=== Case 5: Match Project Meeting, join group meeting ===
Message: Joined existing group meeting: Project Meeting.

=== Case 6: Normal appointment with reminder ===
Message: Added appointment: Dentist Appointment.
```

## Progress Checklist

- [x] Phase 1 - Project foundation
- [x] Phase 2 - Domain model
- [x] Phase 3 - Core algorithm
- [ ] Phase 4 - Swing UI
- [ ] Phase 5 - Diagrams and demo report
