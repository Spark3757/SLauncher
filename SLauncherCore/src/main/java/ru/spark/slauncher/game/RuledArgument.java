package ru.spark.slauncher.game;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.util.Immutable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author spark1337
 */
@JsonAdapter(RuledArgument.Serializer.class)
@Immutable
public class RuledArgument implements Argument {

    private final List<CompatibilityRule> rules;
    private final List<String> value;

    public RuledArgument() {
        this(null, null);
    }

    public RuledArgument(List<CompatibilityRule> rules, List<String> args) {
        this.rules = rules;
        this.value = args;
    }

    public List<CompatibilityRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public List<String> getValue() {
        return Collections.unmodifiableList(value);
    }

    @Override
    public Object clone() {
        return new RuledArgument(
                rules == null ? null : new ArrayList<>(rules),
                value == null ? null : new ArrayList<>(value)
        );
    }

    @Override
    public List<String> toString(Map<String, String> keys, Map<String, Boolean> features) {
        if (CompatibilityRule.appliesToCurrentEnvironment(rules, features) && value != null)
            return value.stream()
                    .filter(Objects::nonNull)
                    .map(StringArgument::new)
                    .map(str -> str.toString(keys, features).get(0))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    public static class Serializer implements JsonSerializer<RuledArgument>, JsonDeserializer<RuledArgument> {
        @Override
        public JsonElement serialize(RuledArgument src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("rules", context.serialize(src.rules));
            obj.add("value", context.serialize(src.value));
            return obj;
        }

        @Override
        public RuledArgument deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new RuledArgument(
                    context.deserialize(obj.get("rules"), new TypeToken<List<CompatibilityRule>>() {
                    }.getType()),
                    obj.get("value").isJsonPrimitive()
                            ? Collections.singletonList(obj.get("value").getAsString())
                            : context.deserialize(obj.get("value"), new TypeToken<List<String>>() {
                    }.getType()));
        }

    }
}
