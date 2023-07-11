package de.keksuccino.fancymenu.customization.background;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MenuBackground extends GuiComponent implements Renderable {

    public final MenuBackgroundBuilder<?> builder;
    /** This gets set by the {@link ScreenCustomizationLayer} when screens fade in or out and should only get used as getter. **/
    public float opacity = 1.0F;
    /** This gets set by the {@link ScreenCustomizationLayer} and should only be used as getter. **/
    public boolean keepBackgroundAspectRatio = false;

    public MenuBackground(MenuBackgroundBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public abstract void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial);

    @Nullable
    public MenuBackground copy() {
        try {
            return this.builder.deserializeBackgroundInternal(this.builder.serializedBackgroundInternal(this));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isEditor() {
        return (Minecraft.getInstance().screen instanceof LayoutEditorScreen);
    }

    public static int getScreenWidth() {
        return AbstractElement.getScreenWidth();
    }

    public static int getScreenHeight() {
        return AbstractElement.getScreenHeight();
    }

}