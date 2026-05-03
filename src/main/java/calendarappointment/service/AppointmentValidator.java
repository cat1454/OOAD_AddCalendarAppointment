package calendarappointment.service;

import calendarappointment.model.Appointment;
import calendarappointment.model.ValidationResult;

public class AppointmentValidator {
    public ValidationResult validate(Appointment appointment) {
        if (appointment == null) {
            return ValidationResult.failure("Appointment is required.");
        }

        if (appointment.getName() == null || appointment.getName().isBlank()) {
            return ValidationResult.failure("Appointment name is required.");
        }

        if (appointment.getStartTime() == null) {
            return ValidationResult.failure("Start time is required.");
        }

        if (appointment.getEndTime() == null) {
            return ValidationResult.failure("End time is required.");
        }

        if (!appointment.getEndTime().isAfter(appointment.getStartTime())) {
            return ValidationResult.failure("End time must be after start time.");
        }

        if (appointment.getDuration().isZero() || appointment.getDuration().isNegative()) {
            return ValidationResult.failure("Duration must be positive.");
        }

        return ValidationResult.success();
    }
}
