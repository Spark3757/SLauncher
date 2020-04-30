package ru.spark.slauncher.util;

import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author spark1337
 */
public enum Log4jLevel {
    FATAL(1, Color.web("#F7A699")),
    ERROR(2, Color.web("#FFCCBB")),
    WARN(3, Color.web("#FFEECC")),
    INFO(4, Color.web("#FBFBFB")),
    DEBUG(5, Color.web("#EEE9E0")),
    TRACE(6, Color.BLUE),
    ALL(2147483647, Color.BLACK);

    private final int level;
    private final Color color;

    Log4jLevel(int level, Color color) {
        this.level = level;
        this.color = color;
    }

    public int getLevel() {
        return level;
    }

    public Color getColor() {
        return color;
    }

    public boolean lessOrEqual(Log4jLevel level) {
        return this.level <= level.level;
    }

    public static final Pattern MINECRAFT_LOGGER = Pattern.compile("\\[(?<timestamp>[0-9:]+)] \\[[^/]+/(?<level>[^]]+)]");
    public static final Pattern MINECRAFT_LOGGER_CATEGORY = Pattern.compile("\\[(?<timestamp>[0-9:]+)] \\[[^/]+/(?<level>[^]]+)] \\[(?<category>[^]]+)]");
    public static final String JAVA_SYMBOL = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+[a-zA-Z_$][a-zA-Z\\d_$]*";

    public static Log4jLevel guessLevel(String line) {
        Log4jLevel level = null;
        Matcher m = MINECRAFT_LOGGER.matcher(line);
        if (m.find()) {
            // New style logs from log4j
            String levelStr = m.group("level");
            if (null != levelStr)
                switch (levelStr) {
                    case "INFO":
                        level = INFO;
                        break;
                    case "WARN":
                        level = WARN;
                        break;
                    case "ERROR":
                        level = ERROR;
                        break;
                    case "FATAL":
                        level = FATAL;
                        break;
                    case "TRACE":
                        level = TRACE;
                        break;
                    case "DEBUG":
                        level = DEBUG;
                        break;
                    default:
                        break;
                }
            Matcher m2 = MINECRAFT_LOGGER_CATEGORY.matcher(line);
            if (m2.find()) {
                String level2Str = m2.group("category");
                if (null != level2Str)
                    switch (level2Str) {
                        case "STDOUT":
                            level = INFO;
                            break;
                        case "STDERR":
                            level = ERROR;
                            break;
                    }
            }
        } else {
            if (line.contains("[INFO]") || line.contains("[CONFIG]") || line.contains("[FINE]")
                    || line.contains("[FINER]") || line.contains("[FINEST]"))
                level = INFO;
            if (line.contains("[SEVERE]") || line.contains("[STDERR]"))
                level = ERROR;
            if (line.contains("[WARNING]"))
                level = WARN;
            if (line.contains("[DEBUG]"))
                level = DEBUG;
        }
        if (line.contains("overwriting existing"))
            level = FATAL;

        /*if (line.contains("Exception in thread")
                || line.matches("\\s+at " + JAVA_SYMBOL)
                || line.matches("Caused by: " + JAVA_SYMBOL)
                || line.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)+[a-zA-Z_$]?[a-zA-Z\\d_$]*(Exception|Error|Throwable)")
                || line.matches("... \\d+ more$"))
            return ERROR;*/
        return level;
    }

    public static boolean isError(Log4jLevel a) {
        return a != null && a.lessOrEqual(Log4jLevel.ERROR);
    }

    public static Log4jLevel mergeLevel(Log4jLevel a, Log4jLevel b) {
        if (a == null)
            return b;
        else if (b == null)
            return a;
        else
            return a.level < b.level ? a : b;
    }

    public static boolean guessLogLineError(String log) {
        return isError(guessLevel(log));
    }
}
