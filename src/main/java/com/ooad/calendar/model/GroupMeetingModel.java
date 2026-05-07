package com.ooad.calendar.model;

import com.ooad.calendar.config.Database;
import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.GroupMeeting;
import com.ooad.calendar.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GroupMeetingModel {
    private GroupMeeting findMatchingGroup(Appointment appointment) {
        long duration = Duration.between(appointment.getStartsAt(), appointment.getEndsAt()).toMinutes();
        String sql = """
                SELECT gm.*
                FROM group_meetings gm
                JOIN appointments a ON a.group_meeting_id = gm.id
                WHERE LOWER(gm.title) = LOWER(?)
                  AND gm.duration_minutes = ?
                  AND LOWER(COALESCE(gm.location, '')) = LOWER(COALESCE(?, ''))
                  AND a.starts_at = ?
                  AND a.ends_at = ?
                  AND a.owner_id <> ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM group_members members
                      WHERE members.group_id = gm.id AND members.user_id = ?
                  )
                LIMIT 1
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, appointment.getTitle());
            statement.setInt(2, Math.toIntExact(duration));
            statement.setString(3, appointment.getLocation());
            statement.setTimestamp(4, Timestamp.valueOf(appointment.getStartsAt()));
            statement.setTimestamp(5, Timestamp.valueOf(appointment.getEndsAt()));
            statement.setInt(6, appointment.getOwnerId());
            statement.setInt(7, appointment.getOwnerId());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new GroupMeeting(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("location"),
                            rs.getInt("duration_minutes")
                    );
                }
            }
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot find matching group meeting", ex);
        }
    }

    public GroupMeeting FindMatchingGroupMeeting(Appointment appointment) {
        return findMatchingGroup(appointment);
    }

    private void addMember(Connection connection, int groupId, int userId) throws Exception {
        String sql = "MERGE INTO group_members KEY(group_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private void addMember(int groupId, int userId) {
        try (Connection connection = Database.getConnection()) {
            addMember(connection, groupId, userId);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot add member to group meeting", ex);
        }
    }

    public void AddParticipant(int groupId, int userId) {
        addMember(groupId, userId);
    }

    public void AddParticipant(Connection connection, int groupId, int userId) throws Exception {
        addMember(connection, groupId, userId);
    }

    private int createGroupMeetingForAppointment(Connection connection, Appointment appointment) throws Exception {
        String sql = "INSERT INTO group_meetings(title, location, duration_minutes) VALUES (?, ?, ?)";
        long duration = Duration.between(appointment.getStartsAt(), appointment.getEndsAt()).toMinutes();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, appointment.getTitle());
            statement.setString(2, appointment.getLocation());
            statement.setInt(3, Math.toIntExact(duration));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new IllegalStateException("Group meeting id was not generated");
        }
    }

    public int CreateGroupMeetingForAppointment(Appointment appointment) {
        try (Connection connection = Database.getConnection()) {
            return createGroupMeetingForAppointment(connection, appointment);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot create group meeting", ex);
        }
    }

    public int CreateGroupMeetingForAppointment(Connection connection, Appointment appointment) throws Exception {
        return createGroupMeetingForAppointment(connection, appointment);
    }

    public List<GroupMeeting> findByUser(int userId) {
        String sql = """
                SELECT gm.*
                FROM group_meetings gm
                JOIN group_members members ON members.group_id = gm.id
                WHERE members.user_id = ?
                ORDER BY gm.title
                """;
        List<GroupMeeting> meetings = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    meetings.add(new GroupMeeting(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("location"),
                            rs.getInt("duration_minutes")
                    ));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load group meetings", ex);
        }
        return meetings;
    }

    public List<User> findParticipants(int groupId) {
        String sql = """
                SELECT u.id, u.username, u.full_name
                FROM users u
                JOIN group_members members ON members.user_id = u.id
                WHERE members.group_id = ?
                ORDER BY u.full_name
                """;
        List<User> participants = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    participants.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name")
                    ));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load group participants", ex);
        }
        return participants;
    }

    public void leaveGroup(int groupId, int userId) {
        try (Connection connection = Database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM group_members WHERE group_id = ? AND user_id = ?")) {
                statement.setInt(1, groupId);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE appointments SET group_meeting_id = NULL WHERE owner_id = ? AND group_meeting_id = ?")) {
                statement.setInt(1, userId);
                statement.setInt(2, groupId);
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot leave group meeting", ex);
        }
    }
}
