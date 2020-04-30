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
 * @see https://github.com/spark1337/authlib-injector/wiki/%E5%90%AF%E5%8A%A8%E5%99%A8%E6%8A%80%E6%9C%AF%E8%A7%84%E8%8C%83#dnd-%E6%96%B9%E5%BC%8F%E6%B7%BB%E5%8A%A0-yggdrasil-%E6%9C%8D%E5%8A%A1%E7%AB%AF
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
