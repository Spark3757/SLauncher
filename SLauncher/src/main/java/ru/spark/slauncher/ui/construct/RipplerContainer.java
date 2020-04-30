package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXRippler;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import ru.spark.slauncher.util.Lang;

import java.util.List;

@DefaultProperty("container")
public class RipplerContainer extends StackPane {
    private static final String DEFAULT_STYLE_CLASS = "rippler-container";

    private final ObjectProperty<Node> container = new SimpleObjectProperty<>(this, "container", null);
    private final StyleableObjectProperty<Paint> ripplerFill = new SimpleStyleableObjectProperty<>(StyleableProperties.RIPPLER_FILL, this, "ripplerFill", null);
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected", false);

    private final StackPane buttonContainer = new StackPane();
    private final JFXRippler buttonRippler = new JFXRippler(new StackPane()) {
        @Override
        protected Node getMask() {
            StackPane mask = new StackPane();
            mask.shapeProperty().bind(buttonContainer.shapeProperty());
            mask.backgroundProperty().bind(Bindings.createObjectBinding(() -> new Background(new BackgroundFill(Color.WHITE, buttonContainer.getBackground() != null && buttonContainer.getBackground().getFills().size() > 0 ? buttonContainer.getBackground().getFills().get(0).getRadii() : defaultRadii, buttonContainer.getBackground() != null && buttonContainer.getBackground().getFills().size() > 0 ? buttonContainer.getBackground().getFills().get(0).getInsets() : Insets.EMPTY)), buttonContainer.backgroundProperty()));
            mask.resize(buttonContainer.getWidth() - buttonContainer.snappedRightInset() - buttonContainer.snappedLeftInset(), buttonContainer.getHeight() - buttonContainer.snappedBottomInset() - buttonContainer.snappedTopInset());
            return mask;
        }
    };

    private final CornerRadii defaultRadii = new CornerRadii(3);

    public RipplerContainer(@NamedArg("container") Node container) {
        setContainer(container);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
        buttonRippler.setPosition(JFXRippler.RipplerPos.BACK);
        buttonContainer.getChildren().add(buttonRippler);
        focusedProperty().addListener((a, b, newValue) -> {
            if (newValue) {
                if (!isPressed())
                    buttonRippler.showOverlay();
            } else {
                buttonRippler.hideOverlay();
            }
        });
        pressedProperty().addListener(o -> buttonRippler.hideOverlay());
        setPickOnBounds(false);

        buttonContainer.setPickOnBounds(false);
        buttonRippler.ripplerFillProperty().bind(ripplerFillProperty());

        containerProperty().addListener(o -> updateChildren());
        updateChildren();

        InvalidationListener listener = o -> {
            if (isSelected()) setBackground(new Background(new BackgroundFill(getRipplerFill(), defaultRadii, null)));
            else setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, defaultRadii, null)));
        };

        selectedProperty().addListener(listener);
        selectedProperty().addListener((a, b, newValue) ->
                pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), newValue));
        ripplerFillProperty().addListener(listener);

        setShape(Lang.apply(new Rectangle(), rectangle -> {
            rectangle.widthProperty().bind(widthProperty());
            rectangle.heightProperty().bind(heightProperty());
        }));
    }

    protected void updateChildren() {
        getChildren().addAll(buttonContainer, getContainer());

        for (int i = 1; i < getChildren().size(); ++i)
            getChildren().get(i).setPickOnBounds(false);
    }

    public Node getContainer() {
        return container.get();
    }

    public ObjectProperty<Node> containerProperty() {
        return container;
    }

    public void setContainer(Node container) {
        this.container.set(container);
    }

    public Paint getRipplerFill() {
        return ripplerFill.get();
    }

    public StyleableObjectProperty<Paint> ripplerFillProperty() {
        return ripplerFill;
    }

    public void setRipplerFill(Paint ripplerFill) {
        this.ripplerFill.set(ripplerFill);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.FACTORY.getCssMetaData();
    }

    private static class StyleableProperties {
        private static final StyleablePropertyFactory<RipplerContainer> FACTORY = new StyleablePropertyFactory<>(StackPane.getClassCssMetaData());

        private static final CssMetaData<RipplerContainer, Paint> RIPPLER_FILL = FACTORY.createPaintCssMetaData("-jfx-rippler-fill", s -> s.ripplerFill, Color.rgb(0, 200, 255));
    }
}
