package ru.spark.slauncher.util.platform;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;

/**
 * The platform that indicates which the platform of operating system is, 64-bit or 32-bit.
 * Of course, 128-bit and 16-bit is not supported.
 *
 * @author spark1337
 */
@JsonAdapter(Platform.Serializer.class)
public enum Platform {
    BIT_32("32"),
    BIT_64("64"),
    UNKNOWN("unknown");

    private final String bit;

    Platform(String bit) {
        this.bit = bit;
    }

    public String getBit() {
        return bit;
    }

    /**
     * True if current Java Environment is 64-bit.
     */
    public static final boolean IS_64_BIT = OperatingSystem.SYSTEM_ARCHITECTURE.contains("64");

    /**
     * The platform of current Java Environment.
     */
    public static final Platform PLATFORM = IS_64_BIT ? BIT_64 : BIT_32;

    /**
     * The json serializer to {@link Platform}.
     */
    public static class Serializer implements JsonSerializer<Platform>, JsonDeserializer<Platform> {
        @Override
        public JsonElement serialize(Platform t, Type type, JsonSerializationContext jsc) {
            if (t == null)
                return null;
            else
                switch (t) {
                    case BIT_32:
                        return new JsonPrimitive(0);
                    case BIT_64:
                        return new JsonPrimitive(1);
                    default:
                        return new JsonPrimitive(-1);
                }
        }

        @Override
        public Platform deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            if (je == null)
                return null;
            else
                switch (je.getAsInt()) {
                    case 0:
                        return BIT_32;
                    case 1:
                        return BIT_64;
                    default:
                        return UNKNOWN;
                }
        }

    }

}
