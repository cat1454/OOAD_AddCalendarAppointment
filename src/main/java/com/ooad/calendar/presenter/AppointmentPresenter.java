package com.ooad.calendar.presenter;

import com.ooad.calendar.dto.AppointmentDTO;
import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.GroupMeeting;
import com.ooad.calendar.entity.Reminder;
import com.ooad.calendar.model.AppointmentModel;
import com.ooad.calendar.model.GroupMeetingModel;
import com.ooad.calendar.view.IAppointmentView;

import java.util.List;

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
        List<Appointment> conflicts = appointmentModel.FindConflictingAppointments(
                dto.ownerId(),
                dto.startsAt(),
                dto.endsAt(),
                dto.appointmentId()
        );
        Integer conflictIdToReplace = null;
        if (conflicts.size() > 1) {
            view.ShowMultipleConflictsWarning(conflicts);
            return;
        }
        if (conflicts.size() == 1) {
            Appointment conflict = conflicts.get(0);
            view.ShowConflictWarning(conflict);
            if (!view.RequestAppointmentReplacement(conflict)) {
                return;
            }
            conflictIdToReplace = conflict.getId();
        }

        Appointment appointment = new Appointment(
                dto.appointmentId() == null ? 0 : dto.appointmentId(),
                dto.ownerId(),
                dto.groupMeeting() ? dto.groupMeetingId() : null,
                dto.title().trim(),
                dto.location().trim(),
                dto.startsAt(),
                dto.endsAt()
        );
        dto.reminders().forEach(reminder ->
                appointment.getReminders().add(new Reminder(reminder.minutesBefore(), reminder.message()))
        );

        AppointmentModel.AppointmentTransactionStep beforeSave = null;
        boolean canPromptForGroup = dto.appointmentId() == null || dto.groupMeeting();
        if (appointment.getGroupMeetingId() == null && canPromptForGroup) {
            GroupMeeting group = groupMeetingModel.FindMatchingGroupMeeting(appointment);
            if (group != null && view.RequestJoinGroupMeeting(group.title())) {
                appointment.setGroupMeetingId(group.id());
                beforeSave = connection -> groupMeetingModel.AddParticipant(connection, group.id(), dto.ownerId());
            } else if (dto.groupMeeting()) {
                beforeSave = connection -> {
                    int groupId = groupMeetingModel.CreateGroupMeetingForAppointment(connection, appointment);
                    groupMeetingModel.AddParticipant(connection, groupId, dto.ownerId());
                    appointment.setGroupMeetingId(groupId);
                };
            }
        }

        if (dto.appointmentId() == null) {
            appointmentModel.SaveReplacingConflict(appointment, conflictIdToReplace, beforeSave);
        } else {
            appointmentModel.UpdateReplacingConflict(appointment, conflictIdToReplace, beforeSave);
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
        return !appointmentModel.FindConflictingAppointments(
                dto.ownerId(),
                dto.startsAt(),
                dto.endsAt(),
                dto.appointmentId()
        ).isEmpty();
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
