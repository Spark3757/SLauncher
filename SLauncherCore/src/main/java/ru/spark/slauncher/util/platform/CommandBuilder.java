package ru.spark.slauncher.util.platform;

import ru.spark.slauncher.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CommandBuilder {
    private final OperatingSystem os;
    private List<Item> raw = new LinkedList<>();

    public CommandBuilder() {
        this(OperatingSystem.CURRENT_OS);
    }

    public CommandBuilder(OperatingSystem os) {
        this.os = os;
    }

    private String parse(String s) {
        if (OperatingSystem.WINDOWS == os) {
            return parseBatch(s);
        } else {
            return parseShell(s);
        }
    }

    /**
     * Parsing will ignore your manual escaping
     *
     * @param args commands
     * @return this
     */
    public CommandBuilder add(String... args) {
        for (String s : args)
            raw.add(new Item(s, true));
        return this;
    }

    public CommandBuilder addAll(Collection<String> args) {
        for (String s : args)
            raw.add(new Item(s, true));
        return this;
    }

    public CommandBuilder addWithoutParsing(String... args) {
        for (String s : args)
            raw.add(new Item(s, false));
        return this;
    }

    public CommandBuilder addAllWithoutParsing(Collection<String> args) {
        for (String s : args)
            raw.add(new Item(s, false));
        return this;
    }

    public boolean removeIf(Predicate<String> pred) {
        return raw.removeIf(i -> pred.test(i.arg));
    }

    @Override
    public String toString() {
        return String.join(" ", raw.stream().map(i -> i.parse ? parse(i.arg) : i.arg).collect(Collectors.toList()));
    }

    public List<String> asList() {
        return raw.stream().map(i -> i.arg).collect(Collectors.toList());
    }

    private static class Item {
        String arg;
        boolean parse;

        Item(String arg, boolean parse) {
            this.arg = arg;
            this.parse = parse;
        }

        @Override
        public String toString() {
            return parse ? (OperatingSystem.WINDOWS == OperatingSystem.CURRENT_OS ? parseBatch(arg) : parseShell(arg)) : arg;
        }
    }

    private static String parseBatch(String s) {
        String escape = " \t\"^&<>|";
        if (StringUtils.containsOne(s, escape.toCharArray()))
            // The argument has not been quoted, add quotes.
            return '"' + s
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    + '"';
        else {
            return s;
        }
    }

    private static String parseShell(String s) {
        String escaping = " \t\"!#$&'()*,;<=>?[\\]^`{|}~";
        String escaped = "\"$&`";
        if (s.indexOf(' ') >= 0 || s.indexOf('\t') >= 0 || StringUtils.containsOne(s, escaping.toCharArray())) {
            // The argument has not been quoted, add quotes.
            for (char ch : escaped.toCharArray())
                s = s.replace("" + ch, "\\" + ch);
            return '"' + s + '"';
        } else
            return s;
    }
}
