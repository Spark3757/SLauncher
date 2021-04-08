package ru.spark.slauncher.ui.account;


import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.stage.Modality;
import ru.spark.slauncher.auth.microsoft.MicrosoftService;
import ru.spark.slauncher.ui.WebStage;

import static ru.spark.slauncher.Launcher.COOKIE_MANAGER;

public class MicrosoftAccountLoginStage extends WebStage implements MicrosoftService.WebViewCallback {
    public static final MicrosoftAccountLoginStage INSTANCE = new MicrosoftAccountLoginStage();

    CompletableFuture<String> future;
    Predicate<String> urlTester;

    public MicrosoftAccountLoginStage() {
        super(600, 600);
        initModality(Modality.APPLICATION_MODAL);

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (urlTester != null && urlTester.test(newValue)) {
                future.complete(newValue);
                hide();
            }
        });

        showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (future != null) {
                    future.completeExceptionally(new InterruptedException());
                }
                future = null;
                urlTester = null;
            }
        });
    }

    @Override
    public CompletableFuture<String> show(MicrosoftService service, Predicate<String> urlTester, String initialURL) {
        Platform.runLater(() -> {
            COOKIE_MANAGER.getCookieStore().removeAll();

            webEngine.load(initialURL);
            show();
        });
        this.future = new CompletableFuture<>();
        this.urlTester = urlTester;
        return future;
    }
}