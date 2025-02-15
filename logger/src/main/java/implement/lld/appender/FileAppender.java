package implement.lld.appender;

import implement.lld.LogMessage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class FileAppender implements LogAppender, LifeCycle {
    private final AtomicReference<FileWriter> fileWriterRef;
    private final String filePath;

    public FileAppender(String filePath) {
        this.fileWriterRef = new AtomicReference<>();
        this.filePath = filePath;
    }

    @Override
    public void open() {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            if (!fileWriterRef.compareAndSet(null, fileWriter)) {
                throw new IllegalStateException("File writer already opened");
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while opening file: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            FileWriter fileWriter = fileWriterRef.getAndSet(null);
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while closing file: " + e.getMessage(), e);
        }
    }

    @Override
    public void append(LogMessage logMessage) {
        FileWriter fileWriter = fileWriterRef.get();
        if (fileWriter == null) {
            throw new IllegalStateException("File writer is not opened");
        }
        try {
            fileWriter.write(logMessage.getFormattedLog());
            fileWriter.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while appending to file: " + e.getMessage(), e);
        }
    }
}
