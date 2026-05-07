package com.ooad.calendar.view;

import com.ooad.calendar.dto.AppointmentDTO;
import com.ooad.calendar.entity.Appointment;

import java.util.List;

public interface IAppointmentView {
    default void ShowAddWindow(java.time.LocalDateTime defaultDate) {
    }

    AppointmentDTO GetInputData();

    void ShowErrorMessage(String message);

    void ShowConflictWarning(Appointment conflict);

    default void ShowMultipleConflictsWarning(List<Appointment> conflicts) {
        ShowErrorMessage("Khoảng thời gian này trùng với nhiều lịch hẹn. Vui lòng chọn khoảng thời gian khác.");
    }

    boolean RequestAppointmentReplacement(Appointment conflictAppointment);

    boolean RequestJoinGroupMeeting(String groupName);

    void UpdateValidationState(boolean valid);

    void RefreshCalendarView();

    void CloseWindow();
}
