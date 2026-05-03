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

The project is currently in **Phase 2 - Domain model**.

This phase adds the core in-memory domain classes for users, appointments, group meetings, reminders, calendar storage, and validation results. It also seeds simple demo data that can be printed from the console entry point. The validation logic, appointment algorithm, and Swing UI are still intentionally left for later phases.

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

Expected output:

```text
Add Calendar Appointment OOAD Demo - Project foundation is ready.
Demo user:
User{userId='U001', fullName='Demo User', appointments=0, reminders=0}

Existing appointments:
- Math Class | Room A101 | 2026-05-04T08:00 - 2026-05-04T09:00

Existing group meetings:
- Project Meeting | Room B202 | 2026-05-04T10:00 - 2026-05-04T11:00

Reminders count: 0
```

## Progress Checklist

- [x] Phase 1 - Project foundation
- [x] Phase 2 - Domain model
- [ ] Phase 3 - Core algorithm
- [ ] Phase 4 - Swing UI
- [ ] Phase 5 - Diagrams and demo report
