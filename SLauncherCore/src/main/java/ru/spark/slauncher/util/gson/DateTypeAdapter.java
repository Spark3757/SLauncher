package ru.spark.slauncher.util.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Spark1337
 */
public final class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    public static final DateTypeAdapter INSTANCE = new DateTypeAdapter();
    public static final DateFormat EN_US_FORMAT = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    public static final DateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private DateTypeAdapter() {
    }

    public static Date deserializeToDate(String string) {
        synchronized (EN_US_FORMAT) {
            try {
                return EN_US_FORMAT.parse(string);
            } catch (ParseException ex1) {
                try {
                    return ISO_8601_FORMAT.parse(string);
                } catch (ParseException ex2) {
                    try {
                        String cleaned = string.replace("Z", "+00:00");
                        cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
                        return ISO_8601_FORMAT.parse(cleaned);
                    } catch (Exception e) {
                        throw new JsonParseException("Invalid date: " + string, e);
                    }
                }
            }
        }
    }

    public static String serializeToString(Date date) {
        synchronized (EN_US_FORMAT) {
            String result = ISO_8601_FORMAT.format(date);
            return result.substring(0, 22) + ":" + result.substring(22);
        }
    }

    @Override
    public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc) {
        synchronized (EN_US_FORMAT) {
            return new JsonPrimitive(serializeToString(t));
        }
    }

    @Override
    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive))
            throw new JsonParseException("The date should be a string value");
        else {
            Date date = deserializeToDate(json.getAsString());
            if (type == Date.class)
                return date;
            else
                throw new IllegalArgumentException(this.getClass().toString() + " cannot be deserialized to " + type);
        }
    }

}
