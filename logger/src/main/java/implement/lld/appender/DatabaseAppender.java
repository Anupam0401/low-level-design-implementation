package implement.lld.appender;

import implement.lld.LogMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseAppender implements LogAppender, LifeCycle {
    private final String dbUrl;
    private final String username;
    private final String password;
    private final AtomicReference<Connection> databaseConnectionRef;

    private static final String INSERT_LOG_QUERY = "INSERT INTO logs (timestamp, log_level, content) VALUES (?, ?, ?)";

    public DatabaseAppender(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.databaseConnectionRef = new AtomicReference<>();
    }

    @Override
    public void open() {
        try {
            Connection connection = DriverManager.getConnection(dbUrl, username, password);
            if (!databaseConnectionRef.compareAndSet(null, connection)) {
                throw new IllegalStateException("Database connection already opened");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception occurred while opening database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        Connection connection = databaseConnectionRef.getAndSet(null);
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Exception occurred while closing database connection: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void append(LogMessage logMessage) {
        Connection connection = databaseConnectionRef.get();
        if (connection == null) {
            throw new IllegalStateException("Database connection is not opened");
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LOG_QUERY)) {
            preparedStatement.setTimestamp(1, logMessage.getTimestamp());
            preparedStatement.setString(2, logMessage.getLogLevel().name());
            preparedStatement.setString(3, logMessage.getContent());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Exception occurred while appending to database: " + e.getMessage(), e);
        }
    }
}
