package battleaimod.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger {

    private static PrintWriter writer = null;
    private static final Object lock = new Object();
    private static volatile boolean initialized = false;

    private static volatile boolean enabled = false;
    private static volatile boolean errorEnabled = true;

    // Format for log messages
    private static final DateTimeFormatter logFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Format for filename (safe for most OS)
    private static final DateTimeFormatter fileFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static String fileName;

    // Lazy initialization
    private static void init() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    try {
                        String timestamp = LocalDateTime.now().format(fileFormatter);
                        fileName = "log_" + timestamp + ".txt";

                        writer = new PrintWriter(
                                new BufferedWriter(
                                        new FileWriter(fileName, true)
                                ),
                                true // auto-flush
                        );

                        initialized = true;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to initialize logger", e);
                    }
                }
            }
        }
    }

    public static void log(String message) {
        if(!enabled) return;
        init();
        synchronized (lock) {
            writer.println(formatMessage("INFO", message));
        }
    }

    public static void logWarning(String message) {
        if(!enabled) return;
        init();
        synchronized (lock) {
            writer.println(formatMessage("WARN", message));
        }
    }

    public static void logError(String message) {
        if(!errorEnabled) return;
        init();
        synchronized (lock) {
            writer.println(formatMessage("ERROR", message));
        }
    }

    private static String formatMessage(String level, String message) {
        String timestamp = LocalDateTime.now().format(logFormatter);
        return "[" + timestamp + "] [" + level + "] " + message;
    }

    public static void close() {
        synchronized (lock) {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
                initialized = false;
            }
        }
    }

    // Optional helper if you want to know the file name being used
    public static String getFileName() {
        return fileName;
    }

    public static void setEnabled(boolean enabled) {
        FileLogger.enabled = enabled;
    }
}