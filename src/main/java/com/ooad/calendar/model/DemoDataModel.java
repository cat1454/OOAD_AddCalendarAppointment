package com.ooad.calendar.model;

import com.ooad.calendar.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemoDataModel {
    public void clearData() {
        try (Connection connection = Database.getConnection()) {
            clearData(connection);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot clear demo data", ex);
        }
    }

    public void seedDemoData(LocalDate date) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                clearData(connection);
                seedUsers(connection);

                int joinedGroupId = insertGroupMeeting(connection, "Demo nhóm đang tham gia", "Phòng OOAD", 60);
                insertGroupMember(connection, joinedGroupId, 1);
                insertGroupMember(connection, joinedGroupId, 2);

                int joinableGroupId = insertGroupMeeting(connection, "Demo tham gia nhóm", "Google Meet", 60);
                insertGroupMember(connection, joinableGroupId, 2);
                insertGroupMember(connection, joinableGroupId, 3);

                insertAppointment(connection, 1, null, "Bình thường: đọc yêu cầu", "Thư viện",
                        date.atTime(7, 45), date.atTime(8, 15));
                int conflictAppointmentId = insertAppointment(connection, 1, null, "Đã có lịch: test trùng lịch",
                        "Phòng B2", date.atTime(8, 30), date.atTime(9, 30));
                insertReminder(connection, conflictAppointmentId, 30, "Nhắc lịch bị trùng");
                insertAppointment(connection, 1, joinedGroupId, "Demo nhóm đang tham gia", "Phòng OOAD",
                        date.atTime(11, 30), date.atTime(12, 30));
                insertAppointment(connection, 1, null, "Bình thường: nộp báo cáo", "Lab 3",
                        date.atTime(13, 30), date.atTime(14, 15));

                insertAppointment(connection, 2, joinableGroupId, "Demo tham gia nhóm", "Google Meet",
                        date.atTime(9, 45), date.atTime(10, 45));
                insertAppointment(connection, 2, joinedGroupId, "Demo nhóm đang tham gia", "Phòng OOAD",
                        date.atTime(11, 30), date.atTime(12, 30));
                insertAppointment(connection, 3, joinableGroupId, "Demo tham gia nhóm", "Google Meet",
                        date.atTime(9, 45), date.atTime(10, 45));

                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot seed demo data", ex);
        }
    }

    private void clearData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM reminders");
            statement.executeUpdate("DELETE FROM appointments");
            statement.executeUpdate("DELETE FROM group_members");
            statement.executeUpdate("DELETE FROM group_meetings");
            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("ALTER TABLE appointments ALTER COLUMN id RESTART WITH 1");
            statement.executeUpdate("ALTER TABLE group_meetings ALTER COLUMN id RESTART WITH 1");
            statement.executeUpdate("ALTER TABLE reminders ALTER COLUMN id RESTART WITH 1");
        }
    }

    private void seedUsers(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users(id, username, full_name) VALUES (?, ?, ?)")) {
            insertUser(statement, 1, "nguyenvanB", "Nguyen Van B");
            insertUser(statement, 2, "tranthiA", "Tran Thi A");
            insertUser(statement, 3, "leminhC", "Le Minh C");
        }
    }

    private void insertUser(PreparedStatement statement, int id, String username, String fullName) throws Exception {
        statement.setInt(1, id);
        statement.setString(2, username);
        statement.setString(3, fullName);
        statement.executeUpdate();
    }

    private int insertGroupMeeting(Connection connection, String title, String location, int durationMinutes) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO group_meetings(title, location, duration_minutes) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setString(2, location);
            statement.setInt(3, durationMinutes);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new IllegalStateException("Group meeting id was not generated");
        }
    }

    private void insertGroupMember(Connection connection, int groupId, int userId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO group_members(group_id, user_id) VALUES (?, ?)")) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private int insertAppointment(Connection connection, int ownerId, Integer groupMeetingId, String title,
                                  String location, LocalDateTime startsAt, LocalDateTime endsAt) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                        INSERT INTO appointments(owner_id, group_meeting_id, title, location, starts_at, ends_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, ownerId);
            if (groupMeetingId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, groupMeetingId);
            }
            statement.setString(3, title);
            statement.setString(4, location);
            statement.setTimestamp(5, Timestamp.valueOf(startsAt));
            statement.setTimestamp(6, Timestamp.valueOf(endsAt));
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new IllegalStateException("Appointment id was not generated");
        }
    }

    private void insertReminder(Connection connection, int appointmentId, int minutesBefore, String message) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO reminders(appointment_id, minutes_before, message) VALUES (?, ?, ?)")) {
            statement.setInt(1, appointmentId);
            statement.setInt(2, minutesBefore);
            statement.setString(3, message);
            statement.executeUpdate();
        }
    }
}
