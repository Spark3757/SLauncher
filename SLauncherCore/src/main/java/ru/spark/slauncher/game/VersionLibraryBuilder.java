package ru.spark.slauncher.game;

import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.platform.CommandBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Spark1337
 */
public final class VersionLibraryBuilder {
    private final Version version;
    private final List<String> mcArgs;
    private final List<Argument> game;
    private final boolean useMcArgs;

    public VersionLibraryBuilder(Version version) {
        this.version = version;
        this.mcArgs = version.getMinecraftArguments().map(StringUtils::tokenize).map(ArrayList::new).orElse(null);
        this.game = version.getArguments().map(Arguments::getGame).map(ArrayList::new).orElseGet(ArrayList::new);
        this.useMcArgs = mcArgs != null;
    }

    public Version build() {
        if (useMcArgs) {
            // Since $ will be escaped in linux, and our maintain of minecraftArgument will not cause escaping,
            // so we regenerate the minecraftArgument without escaping.
            return version.setMinecraftArguments(new CommandBuilder().addAllWithoutParsing(mcArgs).toString());
        } else {
            return version.setArguments(version.getArguments().map(args -> args.withGame(game)).orElse(new Arguments(game, Collections.emptyList())));
        }
    }

    public void removeTweakClass(String target) {
        if (useMcArgs) {
            for (int i = 0; i + 1 < mcArgs.size(); ++i) {
                String arg0Str = mcArgs.get(i);
                String arg1Str = mcArgs.get(i + 1);
                if (arg0Str.equals("--tweakClass") && arg1Str.toLowerCase().contains(target)) {
                    mcArgs.remove(i);
                    mcArgs.remove(i);
                    --i;
                }
            }
        } else {
            for (int i = 0; i + 1 < game.size(); ++i) {
                Argument arg0 = game.get(i);
                Argument arg1 = game.get(i + 1);
                if (arg0 instanceof StringArgument && arg1 instanceof StringArgument) {
                    // We need to preserve the tokens
                    String arg0Str = arg0.toString();
                    String arg1Str = arg1.toString();
                    if (arg0Str.equals("--tweakClass") && arg1Str.toLowerCase().contains(target)) {
                        game.remove(i);
                        game.remove(i);
                        --i;
                    }
                }
            }
        }
    }

    public void addArgument(String... args) {
        if (useMcArgs) {
            mcArgs.addAll(Arrays.asList(args));
        } else {
            for (String arg : args)
                game.add(new StringArgument(arg));
        }
    }
}
