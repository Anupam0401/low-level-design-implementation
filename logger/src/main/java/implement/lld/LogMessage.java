package implement.lld;

import java.sql.Timestamp;

public class LogMessage {
    private final String content;
    private final LogLevel logLevel;
    private final Timestamp timestamp;

    public LogMessage(String content, LogLevel logLevel, Timestamp timestamp) {
        this.content = content;
        this.logLevel = logLevel;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedLog() {
        return timestamp + " " + logLevel + " " + content;
    }
}
