package de.keksuccino.fancymenu.util.rendering.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.mixin.mixins.common.client.IMixinAbstractWidget;
import de.keksuccino.fancymenu.util.ClassExtender;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.resource.PlayableResource;
import de.keksuccino.fancymenu.util.resource.RenderableResource;
import de.keksuccino.fancymenu.util.resource.resources.audio.IAudio;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * This interface gets applied to the {@link AbstractWidget} class to add a bunch of helper methods for easier customization.
 */
@SuppressWarnings("unused")
@ClassExtender(AbstractWidget.class)
public interface CustomizableWidget {

    @Nullable
    default Component getOriginalMessageFancyMenu() {
        Component custom = this.getCustomLabelFancyMenu();
        Component hover = this.getHoverLabelFancyMenu();
        this.setCustomLabelFancyMenu(null);
        this.setHoverLabelFancyMenu(null);
        Component original = null;
        if (this instanceof AbstractWidget w) original = w.getMessage();
        this.setCustomLabelFancyMenu(custom);
        this.setHoverLabelFancyMenu(hover);
        return original;
    }

    /**
     * Returns if the widget should render its Vanilla background (true) or not (false).
     */
    default boolean renderCustomBackgroundFancyMenu(@NotNull AbstractWidget widget, @NotNull PoseStack pose, int x, int y, int width, int height) {
        RenderableResource customBackground;
        RenderableResource customBackgroundNormal = this.getCustomBackgroundNormalFancyMenu();
        RenderableResource customBackgroundHover = this.getCustomBackgroundHoverFancyMenu();
        RenderableResource customBackgroundInactive = this.getCustomBackgroundInactiveFancyMenu();
        if (widget.active) {
            if (widget.isHoveredOrFocused()) {
                customBackground = customBackgroundHover;
                if (customBackgroundNormal instanceof PlayableResource p) p.pause();
            } else {
                customBackground = customBackgroundNormal;
                if (customBackgroundHover instanceof PlayableResource p) p.pause();
            }
            if (customBackgroundInactive instanceof PlayableResource p) p.pause();
        } else {
            customBackground = customBackgroundInactive;
            if (customBackgroundNormal instanceof PlayableResource p) p.pause();
            if (customBackgroundHover instanceof PlayableResource p) p.pause();
        }
        boolean renderVanilla = true;
        if (customBackground != null) {
            if (customBackground instanceof PlayableResource p) p.play();
            ResourceLocation location = customBackground.getResourceLocation();
            if (location != null) {
                renderVanilla = false;
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, ((IMixinAbstractWidget)widget).getAlphaFancyMenu());
                RenderSystem.enableBlend();
                RenderingUtils.bindTexture(location);
                GuiComponent.blit(pose, x, y, 0.0F, 0.0F, width, height, width, height);
                RenderingUtils.resetShaderColor();
            }
        }
        return renderVanilla;
    }

    void resetWidgetCustomizationsFancyMenu();

    void resetWidgetSizeAndPositionFancyMenu();

    void addResetCustomizationsListenerFancyMenu(@NotNull Runnable listener);

    @NotNull
    List<Runnable> getResetCustomizationsListenersFancyMenu();

    void addHoverStateListenerFancyMenu(@NotNull Consumer<Boolean> listener);

    void addFocusStateListenerFancyMenu(@NotNull Consumer<Boolean> listener);

    void addHoverOrFocusStateListenerFancyMenu(@NotNull Consumer<Boolean> listener);

    @NotNull
    List<Consumer<Boolean>> getHoverStateListenersFancyMenu();

    @NotNull
    List<Consumer<Boolean>> getFocusStateListenersFancyMenu();

    @NotNull
    List<Consumer<Boolean>> getHoverOrFocusStateListenersFancyMenu();

    boolean getLastHoverStateFancyMenu();

    void setLastHoverStateFancyMenu(boolean hovered);

    boolean getLastFocusStateFancyMenu();

    void setLastFocusStateFancyMenu(boolean focused);

    boolean getLastHoverOrFocusStateFancyMenu();

    void setLastHoverOrFocusStateFancyMenu(boolean hoveredOrFocused);

    default void tickHoverStateListenersFancyMenu(boolean hovered) {
        if (this.getLastHoverStateFancyMenu() != hovered) {
            for (Consumer<Boolean> listener : this.getHoverStateListenersFancyMenu()) {
                listener.accept(hovered);
            }
        }
        this.setLastHoverStateFancyMenu(hovered);
    }

    default void tickFocusStateListenersFancyMenu(boolean focused) {
        if (this.getLastFocusStateFancyMenu() != focused) {
            for (Consumer<Boolean> listener : this.getFocusStateListenersFancyMenu()) {
                listener.accept(focused);
            }
        }
        this.setLastFocusStateFancyMenu(focused);
    }

    default void tickHoverOrFocusStateListenersFancyMenu(boolean hoveredOrFocused) {
        if (this.getLastHoverOrFocusStateFancyMenu() != hoveredOrFocused) {
            for (Consumer<Boolean> listener : this.getHoverOrFocusStateListenersFancyMenu()) {
                listener.accept(hoveredOrFocused);
            }
        }
        this.setLastHoverOrFocusStateFancyMenu(hoveredOrFocused);
    }

    void setCustomLabelFancyMenu(@Nullable Component label);

    @Nullable
    Component getCustomLabelFancyMenu();

    void setHoverLabelFancyMenu(@Nullable Component hoverLabel);

    @Nullable
    Component getHoverLabelFancyMenu();

    void setCustomClickSoundFancyMenu(@Nullable IAudio sound);

    @Nullable
    IAudio getCustomClickSoundFancyMenu();

    void setHoverSoundFancyMenu(@Nullable IAudio sound);

    @Nullable
    IAudio getHoverSoundFancyMenu();

    void setHiddenFancyMenu(boolean hidden);

    boolean isHiddenFancyMenu();

    void setCustomBackgroundNormalFancyMenu(@Nullable RenderableResource background);

    @Nullable
    RenderableResource getCustomBackgroundNormalFancyMenu();

    void setCustomBackgroundHoverFancyMenu(@Nullable RenderableResource background);

    @Nullable
    RenderableResource getCustomBackgroundHoverFancyMenu();

    void setCustomBackgroundInactiveFancyMenu(@Nullable RenderableResource background);

    @Nullable
    RenderableResource getCustomBackgroundInactiveFancyMenu();

    void setCustomBackgroundResetBehaviorFancyMenu(@NotNull CustomBackgroundResetBehavior resetBehavior);

    @NotNull
    CustomBackgroundResetBehavior getCustomBackgroundResetBehaviorFancyMenu();

    @Nullable
    Integer getCustomWidthFancyMenu();

    void setCustomWidthFancyMenu(@Nullable Integer width);

    @Nullable
    Integer getCustomHeightFancyMenu();

    void setCustomHeightFancyMenu(@Nullable Integer height);

    @Nullable
    Integer getCustomXFancyMenu();

    void setCustomXFancyMenu(@Nullable Integer x);

    @Nullable
    Integer getCustomYFancyMenu();

    void setCustomYFancyMenu(@Nullable Integer y);

    enum CustomBackgroundResetBehavior {
        RESET_NEVER,
        RESET_ON_HOVER,
        RESET_ON_UNHOVER,
        RESET_ON_HOVER_AND_UNHOVER
    }

}