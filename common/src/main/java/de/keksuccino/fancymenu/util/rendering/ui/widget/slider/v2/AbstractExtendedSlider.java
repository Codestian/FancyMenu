package de.keksuccino.fancymenu.util.rendering.ui.widget.slider.v2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.mixin.mixins.common.client.IMixinAbstractSliderButton;
import de.keksuccino.fancymenu.util.ConsumingSupplier;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableSlider;
import de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableWidget;
import de.keksuccino.fancymenu.util.rendering.ui.widget.IExtendedWidget;
import de.keksuccino.fancymenu.util.resources.RenderableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.*;

@SuppressWarnings("unused")
public abstract class AbstractExtendedSlider extends AbstractSliderButton implements IExtendedWidget {

    protected static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");

    @Nullable
    protected DrawableColor sliderBackgroundColorNormal;
    @Nullable
    protected DrawableColor sliderBackgroundColorHighlighted;
    @Nullable
    protected DrawableColor sliderBorderColorNormal;
    @Nullable
    protected DrawableColor sliderBorderColorHighlighted;
    @Nullable
    protected DrawableColor sliderHandleColorNormal;
    @Nullable
    protected DrawableColor sliderHandleColorHover;
    @Nullable
    protected DrawableColor sliderHandleColorInactive;
    @NotNull
    protected DrawableColor labelColorNormal = DrawableColor.of(new Color(16777215));
    @NotNull
    protected DrawableColor labelColorInactive = DrawableColor.of(new Color(10526880));
    protected boolean labelShadow = true;
    @Nullable
    protected SliderValueUpdateListener sliderValueUpdateListener;
    @NotNull
    protected ConsumingSupplier<AbstractExtendedSlider, Component> labelSupplier = slider -> Component.literal(slider.getValueDisplayText());

    public AbstractExtendedSlider(int x, int y, int width, int height, Component label, double value) {
        super(x, y, width, height, label, value);
    }

    @Override
    public void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {

        this.renderBackground(pose, mouseX, mouseY, partial);
        RenderingUtils.resetShaderColor();

        this.renderHandle(pose, mouseX, mouseY, partial);
        RenderingUtils.resetShaderColor();

        this.renderLabel(pose, mouseX, mouseY, partial);
        RenderingUtils.resetShaderColor();

    }

    protected void renderBackground(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        boolean renderVanilla = this.renderColorBackground(pose, mouseX, mouseY, partial);
        if (renderVanilla) renderVanilla = this.getAsCustomizableSlider().renderSliderBackgroundFancyMenu(pose, this, this.getAccessor().getCanChangeValueFancyMenu());
        if (renderVanilla) this.renderVanillaBackground(pose, mouseX, mouseY, partial);
    }

    /**
     * Returns if the slider should render its Vanilla background (true) or not (false).
     */
    protected boolean renderColorBackground(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        RenderSystem.enableBlend();
        RenderingUtils.resetShaderColor();
        if ((this.isFocused() && !this.getAccessor().getCanChangeValueFancyMenu()) && (this.sliderBackgroundColorHighlighted != null)) {
            fill(pose, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.sliderBackgroundColorHighlighted.getColorInt());
            if (this.sliderBorderColorHighlighted != null) {
                UIBase.renderBorder(pose, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 1, this.sliderBorderColorHighlighted.getColorInt(), true, true, true, true);
            }
            return false;
        } else if (this.sliderBackgroundColorNormal != null) {
            fill(pose, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.sliderBackgroundColorNormal.getColorInt());
            if (this.sliderBorderColorNormal != null) {
                UIBase.renderBorder(pose, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 1, this.sliderBorderColorNormal.getColorInt(), true, true, true, true);
            }
            return false;
        }
        return true;
    }

    protected void renderVanillaBackground(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blitNineSliced(pose, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getBackgroundTextureY());
    }

    protected int getBackgroundTextureY() {
        int i = this.isFocused() && !this.getAccessor().getCanChangeValueFancyMenu() ? 1 : 0;
        return i * 20;
    }

    protected void renderHandle(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        boolean renderVanilla = this.renderColorHandle(pose, mouseX, mouseY, partial);
        if (renderVanilla) renderVanilla = this.getAsCustomizableWidget().renderCustomBackgroundFancyMenu(this, pose, this.getHandleX(), this.getY(), this.getHandleWidth(), this.getHeight());
        if (renderVanilla) this.renderVanillaHandle(pose, mouseX, mouseY, partial);
    }

    /**
     * Returns if the slider should render its Vanilla handle (true) or not (false).
     */
    protected boolean renderColorHandle(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        RenderSystem.enableBlend();
        int handleX = this.getHandleX();
        int handleWidth = this.getHandleWidth();
        if (this.active) {
            if (this.isHoveredOrFocused()) {
                if (this.sliderHandleColorHover != null) {
                    fill(pose, handleX, this.getY(), handleX + handleWidth, this.getY() + this.getHeight(), this.sliderHandleColorHover.getColorInt());
                    return false;
                }
            } else {
                if (this.sliderHandleColorNormal != null) {
                    fill(pose, handleX, this.getY(), handleX + handleWidth, this.getY() + this.getHeight(), this.sliderHandleColorNormal.getColorInt());
                    return false;
                }
            }
        } else {
            if (this.sliderHandleColorInactive != null) {
                fill(pose, handleX, this.getY(), handleX + handleWidth, this.getY() + this.getHeight(), this.sliderHandleColorInactive.getColorInt());
                return false;
            }
        }
        return true;
    }

    protected void renderVanillaHandle(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blitNineSliced(pose, this.getHandleX(), this.getY(), this.getHandleWidth(), 20, 20, 4, 200, 20, 0, this.getHandleTextureY());
    }

    protected int getHandleTextureY() {
        int i = !this.isHovered && !this.getAccessor().getCanChangeValueFancyMenu() ? 2 : 3;
        return i * 20;
    }

    protected void renderLabel(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        int textColor = this.active ? this.labelColorNormal.getColorInt() : this.labelColorInactive.getColorInt();
        this.renderScrollingLabel(this, pose, Minecraft.getInstance().font, 2, this.labelShadow, RenderingUtils.replaceAlphaInColor(textColor, this.alpha));
    }

    public int getHandleX() {
        return this.getX() + (int)(this.value * (double)(this.getWidth() - this.getHandleWidth()));
    }

    public int getHandleWidth() {
        return 8;
    }

    @Override
    public void updateMessage() {
        Component label = this.labelSupplier.get(this);
        if (label == null) label = Component.empty();
        this.setMessage(label);
    }

    @Override
    protected void applyValue() {
        if (this.sliderValueUpdateListener != null) {
            this.sliderValueUpdateListener.update(this, this.getValueDisplayText(), this.value);
        }
    }

    @NotNull
    public abstract String getValueDisplayText();

    public AbstractExtendedSlider setSliderValueUpdateListener(@Nullable SliderValueUpdateListener listener) {
        this.sliderValueUpdateListener = listener;
        this.updateMessage();
        return this;
    }

    public void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, 0.0D, 1.0D);
        if (d0 != this.value) {
            this.applyValue();
        }
        this.updateMessage();
    }

    public double getValue() {
        return this.value;
    }

    @Nullable
    public RenderableResource getHandleTextureNormal() {
        return this.getAsCustomizableWidget().getCustomBackgroundNormalFancyMenu();
    }

    public AbstractExtendedSlider setHandleTextureNormal(@Nullable RenderableResource texture) {
        this.getAsCustomizableWidget().setCustomBackgroundNormalFancyMenu(texture);
        return this;
    }

    @Nullable
    public RenderableResource getHandleTextureHover() {
        return this.getAsCustomizableWidget().getCustomBackgroundHoverFancyMenu();
    }

    public AbstractExtendedSlider setHandleTextureHover(@Nullable RenderableResource texture) {
        this.getAsCustomizableWidget().setCustomBackgroundHoverFancyMenu(texture);
        return this;
    }

    @Nullable
    public RenderableResource getHandleTextureInactive() {
        return this.getAsCustomizableWidget().getCustomBackgroundInactiveFancyMenu();
    }

    public AbstractExtendedSlider setHandleTextureInactive(@Nullable RenderableResource texture) {
        this.getAsCustomizableWidget().setCustomBackgroundInactiveFancyMenu(texture);
        return this;
    }

    @Nullable
    public RenderableResource getBackgroundTextureNormal() {
        return this.getAsCustomizableSlider().getCustomSliderBackgroundNormalFancyMenu();
    }

    public AbstractExtendedSlider setBackgroundTextureNormal(@Nullable RenderableResource texture) {
        this.getAsCustomizableSlider().setCustomSliderBackgroundNormalFancyMenu(texture);
        return this;
    }

    @Nullable
    public RenderableResource getBackgroundTextureHighlighted() {
        return this.getAsCustomizableSlider().getCustomSliderBackgroundHighlightedFancyMenu();
    }

    public AbstractExtendedSlider setBackgroundTextureHighlighted(@Nullable RenderableResource texture) {
        this.getAsCustomizableSlider().setCustomSliderBackgroundHighlightedFancyMenu(texture);
        return this;
    }

    @Nullable
    public DrawableColor getSliderBackgroundColorNormal() {
        return sliderBackgroundColorNormal;
    }

    public AbstractExtendedSlider setSliderBackgroundColorNormal(@Nullable DrawableColor sliderBackgroundColorNormal) {
        this.sliderBackgroundColorNormal = sliderBackgroundColorNormal;
        return this;
    }

    @Nullable
    public DrawableColor getSliderBackgroundColorHighlighted() {
        return sliderBackgroundColorHighlighted;
    }

    public AbstractExtendedSlider setSliderBackgroundColorHighlighted(@Nullable DrawableColor sliderBackgroundColorHighlighted) {
        this.sliderBackgroundColorHighlighted = sliderBackgroundColorHighlighted;
        return this;
    }

    @Nullable
    public DrawableColor getSliderBorderColorNormal() {
        return sliderBorderColorNormal;
    }

    public AbstractExtendedSlider setSliderBorderColorNormal(@Nullable DrawableColor sliderBorderColorNormal) {
        this.sliderBorderColorNormal = sliderBorderColorNormal;
        return this;
    }

    @Nullable
    public DrawableColor getSliderBorderColorHighlighted() {
        return sliderBorderColorHighlighted;
    }

    public AbstractExtendedSlider setSliderBorderColorHighlighted(@Nullable DrawableColor sliderBorderColorHighlighted) {
        this.sliderBorderColorHighlighted = sliderBorderColorHighlighted;
        return this;
    }

    @Nullable
    public DrawableColor getSliderHandleColorNormal() {
        return sliderHandleColorNormal;
    }

    public AbstractExtendedSlider setSliderHandleColorNormal(@Nullable DrawableColor sliderHandleColorNormal) {
        this.sliderHandleColorNormal = sliderHandleColorNormal;
        return this;
    }

    @Nullable
    public DrawableColor getSliderHandleColorHover() {
        return sliderHandleColorHover;
    }

    public AbstractExtendedSlider setSliderHandleColorHover(@Nullable DrawableColor sliderHandleColorHover) {
        this.sliderHandleColorHover = sliderHandleColorHover;
        return this;
    }

    @Nullable
    public DrawableColor getSliderHandleColorInactive() {
        return sliderHandleColorInactive;
    }

    public AbstractExtendedSlider setSliderHandleColorInactive(@Nullable DrawableColor sliderHandleColorInactive) {
        this.sliderHandleColorInactive = sliderHandleColorInactive;
        return this;
    }

    @NotNull
    public DrawableColor getLabelColorNormal() {
        return this.labelColorNormal;
    }

    public AbstractExtendedSlider setLabelColorNormal(@NotNull DrawableColor labelColorNormal) {
        this.labelColorNormal = labelColorNormal;
        return this;
    }

    @NotNull
    public DrawableColor getLabelColorInactive() {
        return this.labelColorInactive;
    }

    public AbstractExtendedSlider setLabelColorInactive(@NotNull DrawableColor labelColorInactive) {
        this.labelColorInactive = labelColorInactive;
        return this;
    }

    public boolean isLabelShadow() {
        return labelShadow;
    }

    public AbstractExtendedSlider setLabelShadow(boolean labelShadow) {
        this.labelShadow = labelShadow;
        return this;
    }

    @NotNull
    public ConsumingSupplier<AbstractExtendedSlider, Component> getLabelSupplier() {
        return this.labelSupplier;
    }

    public AbstractExtendedSlider setLabelSupplier(@NotNull ConsumingSupplier<AbstractExtendedSlider, Component> labelSupplier) {
        this.labelSupplier = labelSupplier;
        return this;
    }

    public IMixinAbstractSliderButton getAccessor() {
        return (IMixinAbstractSliderButton) this;
    }

    public CustomizableSlider getAsCustomizableSlider() {
        return (CustomizableSlider) this;
    }

    public CustomizableWidget getAsCustomizableWidget() {
        return (CustomizableWidget) this;
    }

    @FunctionalInterface
    public interface SliderValueUpdateListener {
        void update(@NotNull AbstractExtendedSlider slider, @NotNull String valueDisplayText, double value);
    }

}