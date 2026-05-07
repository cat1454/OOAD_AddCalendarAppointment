package com.ooad.calendar.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:h2:./data/calendar;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws Exception;
    }

    public static <T> T withTransaction(TransactionCallback<T> callback) throws Exception {
        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = callback.execute(connection);
                connection.commit();
                return result;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT PRIMARY KEY,
                        username VARCHAR(80) NOT NULL UNIQUE,
                        full_name VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS group_meetings (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(160) NOT NULL,
                        location VARCHAR(220),
                        duration_minutes INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        owner_id INT NOT NULL,
                        group_meeting_id INT,
                        title VARCHAR(160) NOT NULL,
                        location VARCHAR(220),
                        starts_at TIMESTAMP NOT NULL,
                        ends_at TIMESTAMP NOT NULL,
                        FOREIGN KEY (owner_id) REFERENCES users(id),
                        FOREIGN KEY (group_meeting_id) REFERENCES group_meetings(id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        appointment_id INT NOT NULL,
                        minutes_before INT NOT NULL,
                        message VARCHAR(240) NOT NULL,
                        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS group_members (
                        group_id INT NOT NULL,
                        user_id INT NOT NULL,
                        PRIMARY KEY (group_id, user_id),
                        FOREIGN KEY (group_id) REFERENCES group_meetings(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                    """);
            statement.execute("""
                    MERGE INTO users KEY(id) VALUES (1, 'nguyenvanB', 'Nguyen Van B')
                    """);
            statement.execute("""
                    INSERT INTO group_meetings(title, location, duration_minutes)
                    SELECT 'Họp nhóm OOAD', 'Phòng học A1', 60
                    WHERE NOT EXISTS (SELECT 1 FROM group_meetings WHERE title = 'Họp nhóm OOAD' AND duration_minutes = 60)
                    """);
        }
    }
}
