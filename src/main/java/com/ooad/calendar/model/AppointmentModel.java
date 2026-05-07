package com.ooad.calendar.model;

import com.ooad.calendar.config.Database;
import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.Reminder;
import com.ooad.calendar.entity.ReminderInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentModel {
    @FunctionalInterface
    public interface AppointmentTransactionStep {
        void execute(Connection connection) throws Exception;
    }

    private boolean hasConflict(int userId, LocalDateTime start, LocalDateTime end) {
        return !findConflicts(userId, start, end, null).isEmpty();
    }

    public boolean CheckConflict(int userId, LocalDateTime start, LocalDateTime end) {
        return !findConflicts(userId, start, end, null).isEmpty();
    }

    private Appointment findConflict(int userId, LocalDateTime start, LocalDateTime end) {
        return findConflict(userId, start, end, null);
    }

    private Appointment findConflict(int userId, LocalDateTime start, LocalDateTime end, Integer ignoredAppointmentId) {
        List<Appointment> conflicts = findConflicts(userId, start, end, ignoredAppointmentId);
        return conflicts.isEmpty() ? null : conflicts.get(0);
    }

    private List<Appointment> findConflicts(int userId, LocalDateTime start, LocalDateTime end, Integer ignoredAppointmentId) {
        String sql = """
                SELECT * FROM appointments
                WHERE owner_id = ? AND starts_at < ? AND ends_at > ?
                  AND (? IS NULL OR id <> ?)
                ORDER BY starts_at
                """;
        List<Appointment> conflicts = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setTimestamp(2, Timestamp.valueOf(end));
            statement.setTimestamp(3, Timestamp.valueOf(start));
            if (ignoredAppointmentId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, ignoredAppointmentId);
                statement.setInt(5, ignoredAppointmentId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    conflicts.add(mapAppointment(rs));
                }
            }
            return conflicts;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot check calendar conflict", ex);
        }
    }

    public Appointment FindConflictingAppointment(int userId, LocalDateTime start, LocalDateTime end) {
        return findConflict(userId, start, end);
    }

    public Appointment FindConflictingAppointment(int userId, LocalDateTime start, LocalDateTime end, Integer ignoredAppointmentId) {
        return findConflict(userId, start, end, ignoredAppointmentId);
    }

    public List<Appointment> FindConflictingAppointments(int userId, LocalDateTime start, LocalDateTime end) {
        return findConflicts(userId, start, end, null);
    }

    public List<Appointment> FindConflictingAppointments(int userId, LocalDateTime start, LocalDateTime end, Integer ignoredAppointmentId) {
        return findConflicts(userId, start, end, ignoredAppointmentId);
    }

    private void save(Connection connection, Appointment appointment) throws Exception {
        String sql = """
                INSERT INTO appointments(owner_id, group_meeting_id, title, location, starts_at, ends_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, appointment.getOwnerId());
            if (appointment.getGroupMeetingId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, appointment.getGroupMeetingId());
            }
            statement.setString(3, appointment.getTitle());
            statement.setString(4, appointment.getLocation());
            statement.setTimestamp(5, Timestamp.valueOf(appointment.getStartsAt()));
            statement.setTimestamp(6, Timestamp.valueOf(appointment.getEndsAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setId(keys.getInt(1));
                }
            }
            saveReminders(connection, appointment);
        }
    }

    public void Save(Appointment appointment) {
        SaveReplacingConflict(appointment, null, null);
    }

    private void update(Connection connection, Appointment appointment) throws Exception {
        String sql = """
                UPDATE appointments
                SET group_meeting_id = ?, title = ?, location = ?, starts_at = ?, ends_at = ?
                WHERE id = ? AND owner_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (appointment.getGroupMeetingId() == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, appointment.getGroupMeetingId());
            }
            statement.setString(2, appointment.getTitle());
            statement.setString(3, appointment.getLocation());
            statement.setTimestamp(4, Timestamp.valueOf(appointment.getStartsAt()));
            statement.setTimestamp(5, Timestamp.valueOf(appointment.getEndsAt()));
            statement.setInt(6, appointment.getId());
            statement.setInt(7, appointment.getOwnerId());
            statement.executeUpdate();
            replaceReminders(connection, appointment);
        }
    }

    public void Update(Appointment appointment) {
        UpdateReplacingConflict(appointment, null, null);
    }

    public void SaveReplacingConflict(Appointment appointment, Integer conflictIdToReplace,
                                      AppointmentTransactionStep beforeSave) {
        persistAppointment(appointment, true, conflictIdToReplace, beforeSave, "Cannot save appointment");
    }

    public void UpdateReplacingConflict(Appointment appointment, Integer conflictIdToReplace,
                                        AppointmentTransactionStep beforeSave) {
        persistAppointment(appointment, false, conflictIdToReplace, beforeSave, "Cannot update appointment");
    }

    private void persistAppointment(Appointment appointment, boolean create, Integer conflictIdToReplace,
                                    AppointmentTransactionStep beforeSave, String errorMessage) {
        try {
            Database.withTransaction(connection -> {
                if (beforeSave != null) {
                    beforeSave.execute(connection);
                }
                if (create) {
                    save(connection, appointment);
                } else {
                    update(connection, appointment);
                }
                if (conflictIdToReplace != null) {
                    deleteAppointment(connection, conflictIdToReplace);
                }
                return null;
            });
        } catch (Exception ex) {
            throw new IllegalStateException(errorMessage, ex);
        }
    }

    private void deleteAppointment(Connection connection, int id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void deleteAppointment(int id) {
        try {
            Database.withTransaction(connection -> {
                deleteAppointment(connection, id);
                return null;
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot delete appointment", ex);
        }
    }

    public void DeleteAppointment(int id) {
        deleteAppointment(id);
    }

    public List<Appointment> findByWeek(int userId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atStartOfDay();
        String sql = """
                SELECT * FROM appointments
                WHERE owner_id = ? AND starts_at < ? AND ends_at > ?
                ORDER BY starts_at
                """;
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setTimestamp(2, Timestamp.valueOf(end));
            statement.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapAppointment(rs);
                    appointment.getReminders().addAll(findReminders(connection, appointment.getId()));
                    appointments.add(appointment);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load appointments", ex);
        }
        return appointments;
    }

    public List<Appointment> findAllByUser(int userId) {
        String sql = """
                SELECT * FROM appointments
                WHERE owner_id = ?
                ORDER BY starts_at
                """;
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapAppointment(rs);
                    appointment.getReminders().addAll(findReminders(connection, appointment.getId()));
                    appointments.add(appointment);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load user appointments", ex);
        }
        return appointments;
    }

    public List<ReminderInfo> findReminderInfos(int userId) {
        String sql = """
                SELECT r.id, r.appointment_id, a.title, a.starts_at, r.minutes_before, r.message
                FROM reminders r
                JOIN appointments a ON a.id = r.appointment_id
                WHERE a.owner_id = ?
                ORDER BY a.starts_at, r.minutes_before
                """;
        List<ReminderInfo> reminders = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reminders.add(new ReminderInfo(
                            rs.getInt("id"),
                            rs.getInt("appointment_id"),
                            rs.getString("title"),
                            rs.getInt("minutes_before"),
                            rs.getString("message"),
                            rs.getTimestamp("starts_at").toLocalDateTime()
                    ));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load reminders", ex);
        }
        return reminders;
    }

    public List<ReminderInfo> findDueReminderInfos(int userId, LocalDateTime now) {
        List<ReminderInfo> dueReminders = new ArrayList<>();
        for (ReminderInfo reminder : findReminderInfos(userId)) {
            if (reminder.getRemindAt() != null
                    && !reminder.getRemindAt().isAfter(now)
                    && reminder.getStartsAt() != null
                    && !reminder.getStartsAt().plusMinutes(1).isBefore(now)) {
                dueReminders.add(reminder);
            }
        }
        return dueReminders;
    }

    public void deleteReminder(int reminderId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM reminders WHERE id = ?")) {
            statement.setInt(1, reminderId);
            statement.executeUpdate();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot delete reminder", ex);
        }
    }

    private void saveReminders(Connection connection, Appointment appointment) throws Exception {
        String sql = "INSERT INTO reminders(appointment_id, minutes_before, message) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Reminder reminder : appointment.getReminders()) {
                statement.setInt(1, appointment.getId());
                statement.setInt(2, reminder.minutesBefore());
                statement.setString(3, reminder.message());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceReminders(Connection connection, Appointment appointment) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM reminders WHERE appointment_id = ?")) {
            statement.setInt(1, appointment.getId());
            statement.executeUpdate();
        }
        saveReminders(connection, appointment);
    }

    private List<Reminder> findReminders(Connection connection, int appointmentId) throws Exception {
        List<Reminder> reminders = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT minutes_before, message FROM reminders WHERE appointment_id = ? ORDER BY minutes_before")) {
            statement.setInt(1, appointmentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reminders.add(new Reminder(rs.getInt("minutes_before"), rs.getString("message")));
                }
            }
        }
        return reminders;
    }

    private Appointment mapAppointment(ResultSet rs) throws Exception {
        int groupId = rs.getInt("group_meeting_id");
        Integer nullableGroupId = rs.wasNull() ? null : groupId;
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("owner_id"),
                nullableGroupId,
                rs.getString("title"),
                rs.getString("location"),
                rs.getTimestamp("starts_at").toLocalDateTime(),
                rs.getTimestamp("ends_at").toLocalDateTime()
        );
    }
}
