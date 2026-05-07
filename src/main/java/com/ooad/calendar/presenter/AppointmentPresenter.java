package com.ooad.calendar.presenter;

import com.ooad.calendar.dto.AppointmentDTO;
import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.GroupMeeting;
import com.ooad.calendar.entity.Reminder;
import com.ooad.calendar.model.AppointmentModel;
import com.ooad.calendar.model.GroupMeetingModel;
import com.ooad.calendar.view.IAppointmentView;

public class AppointmentPresenter {
    private final AppointmentModel appointmentModel;
    private final GroupMeetingModel groupMeetingModel;
    private final Runnable calendarRefresh;

    public AppointmentPresenter(AppointmentModel appointmentModel, GroupMeetingModel groupMeetingModel, Runnable calendarRefresh) {
        this.appointmentModel = appointmentModel;
        this.groupMeetingModel = groupMeetingModel;
        this.calendarRefresh = calendarRefresh;
    }

    public void SaveAppointment(IAppointmentView view) {
        AppointmentDTO dto = view.GetInputData();
        Appointment conflict = appointmentModel.FindConflictingAppointment(dto.ownerId(), dto.startsAt(), dto.endsAt(), dto.appointmentId());
        if (conflict != null) {
            view.ShowConflictWarning(conflict);
            if (!view.RequestAppointmentReplacement(conflict)) {
                return;
            }
            appointmentModel.DeleteAppointment(conflict.getId());
        }

        Appointment appointment = new Appointment(
                dto.appointmentId() == null ? 0 : dto.appointmentId(),
                dto.ownerId(),
                null,
                dto.title().trim(),
                dto.location().trim(),
                dto.startsAt(),
                dto.endsAt()
        );
        dto.reminders().forEach(reminder ->
                appointment.getReminders().add(new Reminder(reminder.minutesBefore(), reminder.message()))
        );

        GroupMeeting group = groupMeetingModel.FindMatchingGroupMeeting(appointment);
        if (group != null && view.RequestJoinGroupMeeting(group.title())) {
            groupMeetingModel.AddParticipant(group.id(), dto.ownerId());
            appointment.setGroupMeetingId(group.id());
        } else if (dto.groupMeeting()) {
            int groupId = groupMeetingModel.CreateGroupMeetingForAppointment(appointment);
            groupMeetingModel.AddParticipant(groupId, dto.ownerId());
            appointment.setGroupMeetingId(groupId);
        }

        if (dto.appointmentId() == null) {
            appointmentModel.Save(appointment);
        } else {
            appointmentModel.Update(appointment);
        }
        calendarRefresh.run();
        view.CloseWindow();
    }

    public void DeleteAppointment(IAppointmentView view) {
        AppointmentDTO dto = view.GetInputData();
        if (dto.appointmentId() == null) {
            return;
        }
        appointmentModel.DeleteAppointment(dto.appointmentId());
        calendarRefresh.run();
        view.CloseWindow();
    }

    public boolean CheckAppointmentConflict(IAppointmentView view) {
        AppointmentDTO dto = view.GetInputData();
        return appointmentModel.CheckConflict(dto.ownerId(), dto.startsAt(), dto.endsAt());
    }

    public void HandleAppointmentReplacement(IAppointmentView view, Appointment conflict) {
        if (view.RequestAppointmentReplacement(conflict)) {
            appointmentModel.DeleteAppointment(conflict.getId());
        }
    }

    public void CheckAndJoinGroupMeeting(IAppointmentView view, Appointment appointment) {
        GroupMeeting group = groupMeetingModel.FindMatchingGroupMeeting(appointment);
        if (group != null && view.RequestJoinGroupMeeting(group.title())) {
            groupMeetingModel.AddParticipant(group.id(), appointment.getOwnerId());
            appointment.setGroupMeetingId(group.id());
        }
    }
}
