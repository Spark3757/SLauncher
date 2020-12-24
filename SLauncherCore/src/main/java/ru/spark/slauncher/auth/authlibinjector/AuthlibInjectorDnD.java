package ru.spark.slauncher.auth.authlibinjector;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author spark1337
 */
public final class AuthlibInjectorDnD {

    private static final String SCHEME = "authlib-injector";
    private static final String PATH_YGGDRASIL_SERVER = "yggdrasil-server";

    private AuthlibInjectorDnD() {
    }

    public static Optional<String> parseUrlFromDragboard(Dragboard dragboard) {
        String uri = dragboard.getString();
        if (uri == null) return Optional.empty();

        String[] uriElements = uri.split(":");
        if (uriElements.length == 3 && SCHEME.equals(uriElements[0]) && PATH_YGGDRASIL_SERVER.equals(uriElements[1])) {
            return Optional.of(NetworkUtils.decodeURL(uriElements[2]));
        }
        return Optional.empty();
    }

    public static EventHandler<DragEvent> dragOverHandler() {
        return event -> parseUrlFromDragboard(event.getDragboard()).ifPresent(url -> {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });
    }

    public static EventHandler<DragEvent> dragDroppedHandler(Consumer<String> onUrlTransfered) {
        return event -> parseUrlFromDragboard(event.getDragboard()).ifPresent(url -> {
            event.setDropCompleted(true);
            event.consume();
            onUrlTransfered.accept(url);
        });
    }

}
