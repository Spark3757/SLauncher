package ru.spark.slauncher.ui.decorator;

import com.jfoenix.controls.JFXDialog;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorDnD;
import ru.spark.slauncher.setting.Config;
import ru.spark.slauncher.setting.EnumBackgroundImage;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.account.AddAuthlibInjectorServerPane;
import ru.spark.slauncher.ui.construct.DialogAware;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.ui.construct.Navigator;
import ru.spark.slauncher.ui.construct.StackContainerPane;
import ru.spark.slauncher.ui.wizard.Refreshable;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.Logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;

import static java.util.stream.Collectors.toList;
import static ru.spark.slauncher.setting.ConfigHolder.config;

public class DecoratorController {
    private static final String PROPERTY_DIALOG_CLOSE_HANDLER = DecoratorController.class.getName() + ".dialog.closeListener";

    private final Decorator decorator;
    private final ImageView welcomeView;
    private final Navigator navigator;
    private final Node mainPage;

    private JFXDialog dialog;
    private StackContainerPane dialogPane;
    private Image defaultBackground = new Image("/assets/img/background.jpg");

    public DecoratorController(Stage stage, Node mainPage) {
        this.mainPage = mainPage;

        decorator = new Decorator(stage);
        decorator.titleProperty().set(Metadata.TITLE);
        decorator.setOnCloseButtonAction(Launcher::stopApplication);

        navigator = new Navigator();
        navigator.setOnNavigating(this::onNavigating);
        navigator.setOnNavigated(this::onNavigated);
        navigator.init(mainPage);

        decorator.getContent().setAll(navigator);
        decorator.onCloseNavButtonActionProperty().set(e -> close());
        decorator.onBackNavButtonActionProperty().set(e -> back());
        decorator.onRefreshNavButtonActionProperty().set(e -> refresh());

        welcomeView = new ImageView();
        welcomeView.setImage(new Image("/assets/img/background.jpg"));
        welcomeView.setCursor(Cursor.HAND);
        FXUtils.limitSize(welcomeView, 796, 517);
        welcomeView.setOnMouseClicked(e -> {
            Timeline nowAnimation = new Timeline();
            nowAnimation.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(welcomeView.opacityProperty(), 1.0D, Interpolator.EASE_BOTH)),
                    new KeyFrame(new Duration(300), new KeyValue(welcomeView.opacityProperty(), 0.0D, Interpolator.EASE_BOTH)),
                    new KeyFrame(new Duration(300), e2 -> decorator.getContainer().remove(welcomeView))
            );
            nowAnimation.play();
        });

        if (switchedToNewUI()) {
            if (config().getLocalization().getLocale() == Locale.CHINA) {
                // currently, user guide is only available in Chinese
                decorator.getContainer().setAll(welcomeView);
            }
        }

        setupBackground();

        setupAuthlibInjectorDnD();
    }

    public Decorator getDecorator() {
        return decorator;
    }

    // ==== Background ====

    /**
     * @return true if the user is seeing the current version of UI for the first time.
     */
    private boolean switchedToNewUI() {
        if (config().getUiVersion() < Config.CURRENT_UI_VERSION) {
            config().setUiVersion(Config.CURRENT_UI_VERSION);
            return true;
        }
        return false;
    }

    private void setupBackground() {
        decorator.backgroundProperty().bind(
                Bindings.createObjectBinding(
                        () -> {
                            Image image = null;
                            if (config().getBackgroundImageType() == EnumBackgroundImage.CUSTOM && config().getBackgroundImage() != null) {
                                image = tryLoadImage(Paths.get(config().getBackgroundImage()))
                                        .orElse(null);
                            }
                            if (image == null) {
                                image = loadDefaultBackgroundImage();
                            }
                            return new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(800, 480, false, false, true, true)));
                        },
                        config().backgroundImageTypeProperty(),
                        config().backgroundImageProperty()));
    }

    /**
     * Load background image from bg/, background.png, background.jpg
     */
    private Image loadDefaultBackgroundImage() {
        Optional<Image> image = randomImageIn(Paths.get("bg"));
        if (!image.isPresent()) {
            image = tryLoadImage(Paths.get("background.png"));
        }
        if (!image.isPresent()) {
            image = tryLoadImage(Paths.get("background.jpg"));
        }
        return image.orElse(defaultBackground);
    }

    private Optional<Image> randomImageIn(Path imageDir) {
        if (!Files.isDirectory(imageDir)) {
            return Optional.empty();
        }

        List<Path> candidates;
        try {
            candidates = Files.list(imageDir)
                    .filter(Files::isRegularFile)
                    .filter(it -> {
                        String filename = it.getFileName().toString();
                        return filename.endsWith(".png") || filename.endsWith(".jpg");
                    })
                    .collect(toList());
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Failed to list files in ./bg", e);
            return Optional.empty();
        }

        Random rnd = new Random();
        while (candidates.size() > 0) {
            int selected = rnd.nextInt(candidates.size());
            Optional<Image> loaded = tryLoadImage(candidates.get(selected));
            if (loaded.isPresent()) {
                return loaded;
            } else {
                candidates.remove(selected);
            }
        }
        return Optional.empty();
    }

    private Optional<Image> tryLoadImage(Path path) {
        if (Files.isRegularFile(path)) {
            try {
                return Optional.of(new Image(path.toAbsolutePath().toUri().toString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Optional.empty();
    }

    // ==== Navigation ====

    public Navigator getNavigator() {
        return navigator;
    }

    private void close() {
        if (navigator.getCurrentPage() instanceof DecoratorPage) {
            DecoratorPage page = (DecoratorPage) navigator.getCurrentPage();

            if (page.canForceToClose()) {
                page.onForceToClose();
                return;
            }
        }
        navigator.clear();
    }

    private void back() {
        if (navigator.getCurrentPage() instanceof DecoratorPage) {
            DecoratorPage page = (DecoratorPage) navigator.getCurrentPage();

            if (page.onClose())
                navigator.close();
        } else {
            navigator.close();
        }
    }

    private void refresh() {
        if (navigator.getCurrentPage() instanceof Refreshable) {
            Refreshable refreshable = (Refreshable) navigator.getCurrentPage();

            if (refreshable.canRefreshProperty().get())
                refreshable.refresh();
        }
    }

    private void onNavigating(Navigator.NavigationEvent event) {
        Node from = event.getNode();

        if (from instanceof DecoratorPage)
            ((DecoratorPage) from).onClose();
    }

    private void onNavigated(Navigator.NavigationEvent event) {
        Node to = event.getNode();

        if (to instanceof Refreshable) {
            decorator.canRefreshProperty().bind(((Refreshable) to).canRefreshProperty());
        } else {
            decorator.canRefreshProperty().unbind();
            decorator.canRefreshProperty().set(false);
        }

        if (to instanceof DecoratorPage) {
            decorator.drawerTitleProperty().bind(((DecoratorPage) to).titleProperty());
            decorator.showCloseAsHomeProperty().set(!((DecoratorPage) to).canForceToClose());
        } else {
            decorator.drawerTitleProperty().unbind();
            decorator.drawerTitleProperty().set("");
            decorator.showCloseAsHomeProperty().set(true);
        }

        decorator.canBackProperty().set(navigator.canGoBack());
        decorator.canCloseProperty().set(navigator.canGoBack());

        if (navigator.canGoBack()) {
            decorator.setContentBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        } else {
            decorator.setContentBackground(null);
        }

        if (to instanceof Region) {
            Region region = (Region) to;
            // Let root pane fix window size.
            StackPane parent = (StackPane) region.getParent();
            region.prefWidthProperty().bind(parent.widthProperty());
            region.prefHeightProperty().bind(parent.heightProperty());
        }
    }

    // ==== Dialog ====

    public void showDialog(Node node) {
        FXUtils.checkFxUserThread();

        if (dialog == null) {
            if (decorator.getDrawerWrapper() == null) {
                // Sometimes showDialog will be invoked before decorator was initialized.
                // Keep trying again.
                Platform.runLater(() -> showDialog(node));
                return;
            }

            dialog = new JFXDialog();
            dialogPane = new StackContainerPane();

            dialog.setContent(dialogPane);
            dialog.setDialogContainer(decorator.getDrawerWrapper());
            dialog.setOverlayClose(false);
            dialog.show();
        }

        dialogPane.push(node);

        EventHandler<DialogCloseEvent> handler = event -> closeDialog(node);
        node.getProperties().put(PROPERTY_DIALOG_CLOSE_HANDLER, handler);
        node.addEventHandler(DialogCloseEvent.CLOSE, handler);

        if (node instanceof DialogAware) {
            DialogAware dialogAware = (DialogAware) node;
            if (dialog.isVisible()) {
                dialogAware.onDialogShown();
            } else {
                dialog.visibleProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            dialogAware.onDialogShown();
                            observable.removeListener(this);
                        }
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void closeDialog(Node node) {
        FXUtils.checkFxUserThread();

        Optional.ofNullable(node.getProperties().get(PROPERTY_DIALOG_CLOSE_HANDLER))
                .ifPresent(handler -> node.removeEventHandler(DialogCloseEvent.CLOSE, (EventHandler<DialogCloseEvent>) handler));

        if (dialog != null) {
            dialogPane.pop(node);

            if (dialogPane.getChildren().isEmpty()) {
                dialog.close();
                dialog = null;
                dialogPane = null;
            }
        }
    }

    // ==== Wizard ====

    public void startWizard(WizardProvider wizardProvider) {
        startWizard(wizardProvider, null);
    }

    public void startWizard(WizardProvider wizardProvider, String category) {
        FXUtils.checkFxUserThread();

        getNavigator().navigate(new DecoratorWizardDisplayer(wizardProvider, category));
    }

    // ==== Authlib Injector DnD ====

    private void setupAuthlibInjectorDnD() {
        decorator.addEventFilter(DragEvent.DRAG_OVER, AuthlibInjectorDnD.dragOverHandler());
        decorator.addEventFilter(DragEvent.DRAG_DROPPED, AuthlibInjectorDnD.dragDroppedHandler(
                url -> Controllers.dialog(new AddAuthlibInjectorServerPane(url))));
    }
}
