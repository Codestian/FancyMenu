package de.keksuccino.fancymenu.customization.deep.layers.titlescreen.splash;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.keksuccino.fancymenu.customization.deep.DeepElementBuilder;
import de.keksuccino.fancymenu.customization.deep.AbstractDeepElement;
import de.keksuccino.fancymenu.rendering.DrawableColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class TitleScreenSplashDeepElement extends AbstractDeepElement {

    private static final DrawableColor DEFAULT_COLOR = DrawableColor.create(255, 255, 0);

    public static String cachedSplashText;

    public TitleScreenSplashDeepElement(DeepElementBuilder<?, ?, ?> builder) {
        super(builder);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {

        if (!this.shouldRender()) return;

        this.width = 60;
        this.height = 30;

        RenderSystem.enableBlend();
        this.renderSplash(pose, Minecraft.getInstance().font);

    }

    protected void renderSplash(PoseStack pose, Font font) {

        if (cachedSplashText == null) {
            cachedSplashText = Minecraft.getInstance().getSplashManager().getSplash();
        }
        if (cachedSplashText == null) {
            cachedSplashText = "§c< ERROR! UNABLE TO GET SPLASH TEXT! >";
        }

        pose.pushPose();
        pose.translate(this.getX(), this.getY(), 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(-20));
        float f = 1.8F - Mth.abs(Mth.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
        f = f * 100.0F / (float) (font.width(cachedSplashText) + 32);
        pose.scale(f, f, f);

        drawCenteredString(pose, font, Component.literal(cachedSplashText), 0, -8, DEFAULT_COLOR.getColorInt());

        pose.popPose();

    }

    @Override
    public int getX() {
        return (getScreenWidth() / 2) + 90;
    }

    @Override
    public int getY() {
        return 70;
    }

    @Override
    public int getWidth() {
        return 60;
    }

    @Override
    public int getHeight() {
        return 30;
    }

}