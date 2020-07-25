package ru.spark.slauncher.setting;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.paint.Color;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.ResourceNotFoundError;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.IOUtils;
import ru.spark.slauncher.util.javafx.BindingMapping;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import static ru.spark.slauncher.setting.ConfigHolder.config;

@JsonAdapter(Theme.TypeAdapter.class)
public class Theme {
    public static final Theme GREEN = new Theme("green", "#2E7D32");
    public static final Color BLACK = Color.web("#292929");
    public static final Color[] SUGGESTED_COLORS = new Color[]{
            Color.web("#5C6BC0"), // blue
            Color.web("#283593"), // dark blue
            Color.web("#2E7D32"), // green
            Color.web("#E67E22"), // orange
            Color.web("#9C27B0"), // purple
            Color.web("#B71C1C")  // red
    };

    private final Color paint;
    private final String color;
    private final String name;

    Theme(String name, String color) {
        this.name = name;
        this.color = color;
        this.paint = Color.web(color);
    }

    public static Theme custom(String color) {
        if (!color.startsWith("#"))
            throw new IllegalArgumentException();
        return new Theme(color, color);
    }

    public static Optional<Theme> getTheme(String name) {
        if (name == null)
            return Optional.empty();
        else if (name.equalsIgnoreCase("blue"))
            return Optional.of(custom("#5C6BC0"));
        else if (name.equalsIgnoreCase("darker_blue"))
            return Optional.of(custom("#283593"));
        else if (name.equalsIgnoreCase("green"))
            return Optional.of(custom("#2E7D32"));
        else if (name.equalsIgnoreCase("orange"))
            return Optional.of(custom("#E67E22"));
        else if (name.equalsIgnoreCase("purple"))
            return Optional.of(custom("#9C27B0"));
        else if (name.equalsIgnoreCase("red"))
            return Optional.of(custom("#F44336"));

        if (name.startsWith("#"))
            try {
                Color.web(name);
                return Optional.of(custom(name));
            } catch (IllegalArgumentException ignore) {
            }

        return Optional.empty();
    }

    public static String getColorDisplayName(Color c) {
        return c != null ? String.format("#%02x%02x%02x", Math.round(c.getRed() * 255.0D), Math.round(c.getGreen() * 255.0D), Math.round(c.getBlue() * 255.0D)).toUpperCase() : null;
    }

    public static ObjectBinding<Color> foregroundFillBinding() {
        return BindingMapping.of(config().themeProperty())
                .map(Theme::getForegroundColor);
    }

    public static ObjectBinding<Color> blackFillBinding() {
        return Bindings.createObjectBinding(() -> BLACK);
    }

    public static ObjectBinding<Color> whiteFillBinding() {
        return Bindings.createObjectBinding(() -> Color.WHITE);
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean isCustom() {
        return name.startsWith("#");
    }

    public boolean isLight() {
        return Color.web(color).grayscale().getRed() >= 0.5;
    }

    public Color getForegroundColor() {
        return isLight() ? Color.BLACK : Color.WHITE;
    }

    public String[] getStylesheets() {
        Color textFill = getForegroundColor();

        String css;
        try {
            File temp = File.createTempFile("slauncher", ".css");

            FileUtils.writeText(temp, IOUtils.readFullyAsString(ResourceNotFoundError.getResourceAsStream("/assets/css/custom.css"))
                    .replace("${base-color}", color)
                    .replace("${base-rippler-color}", String.format("rgba(%d, %d, %d, 0.3)", (int) Math.ceil(paint.getRed() * 256), (int) Math.ceil(paint.getGreen() * 256), (int) Math.ceil(paint.getBlue() * 256)))
                    .replace("${disabled-font-color}", String.format("rgba(%d, %d, %d, 0.7)", (int) Math.ceil(textFill.getRed() * 256), (int) Math.ceil(textFill.getGreen() * 256), (int) Math.ceil(textFill.getBlue() * 256)))
                    .replace("${font-color}", getColorDisplayName(getForegroundColor()))
                    .replace("${font}", Optional.ofNullable(System.getProperty("slauncher.font.override")).map(fontFamily -> "-fx-font-family: " + fontFamily + ";").orElse("")));
            css = temp.toURI().toString();
        } catch (IOException | NullPointerException e) {
            Logging.LOG.log(Level.SEVERE, "Unable to create theme stylesheet. Fallback to green theme.", e);
            css = "/assets/css/green.css";
        }

        return new String[]{
                css,
                "/assets/css/root.css"
        };
    }

    public static class TypeAdapter extends com.google.gson.TypeAdapter<Theme> {
        @Override
        public void write(JsonWriter out, Theme value) throws IOException {
            out.value(value.getName().toLowerCase());
        }

        @Override
        public Theme read(JsonReader in) throws IOException {
            return getTheme(in.nextString()).orElse(Theme.GREEN);
        }
    }
}
