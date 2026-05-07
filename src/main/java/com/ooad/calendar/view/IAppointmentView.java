package com.ooad.calendar.view;

import com.ooad.calendar.dto.AppointmentDTO;
import com.ooad.calendar.entity.Appointment;

public interface IAppointmentView {
    default void ShowAddWindow(java.time.LocalDateTime defaultDate) {
    }

    AppointmentDTO GetInputData();

    void ShowErrorMessage(String message);

    void ShowConflictWarning(Appointment conflict);

    boolean RequestAppointmentReplacement(Appointment conflictAppointment);

    boolean RequestJoinGroupMeeting(String groupName);

    void UpdateValidationState(boolean valid);

    void RefreshCalendarView();

    void CloseWindow();
}
