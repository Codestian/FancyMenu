package de.keksuccino.fancymenu.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(ImageButton.class)
public abstract class MixinImageButton extends GuiComponent {

<<<<<<< HEAD
	@WrapWithCondition(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ImageButton;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIFFIIII)V"))
	private boolean wrapRenderTextureFancyMenu(PoseStack pose, int i1, int i2, float v3, float v4, int i5, int i6, int i7, int i8) {
=======
	//TODO übernehmen
	@Unique private float[] cachedShaderColor_FancyMenu;

	@WrapWithCondition(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ImageButton;renderTexture(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIIIII)V"))
	private boolean wrapRenderTextureFancyMenu(ImageButton instance, GuiGraphics graphics, ResourceLocation location, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i0) {
>>>>>>> 71aa26f... v3.3.0 MC 1.20.1

		ImageButton button = (ImageButton)((Object)this);
		CustomizableWidget customizable = ((CustomizableWidget)this);

		//Render custom background if present
		boolean renderVanilla = ((CustomizableWidget)this).renderCustomBackgroundFancyMenu(button, pose, button.x, button.y, button.getWidth(), button.getHeight());

		//Render custom labels if present
		if (!renderVanilla && (((customizable.getCustomLabelFancyMenu() != null) && !button.isHoveredOrFocused()) || ((customizable.getHoverLabelFancyMenu() != null) && button.isHoveredOrFocused()))) {
			int labelColor = button.active ? 16777215 : 10526880;
			drawCenteredString(pose, Minecraft.getInstance().font, button.getMessage(), button.x + button.getWidth() / 2, button.y + (button.getHeight() - 8) / 2, labelColor | Mth.ceil(((IMixinAbstractWidget)button).getAlphaFancyMenu() * 255.0F) << 24);
		}

		//TODO übernehmen
		cachedShaderColor_FancyMenu = RenderSystem.getShaderColor();
		if (cachedShaderColor_FancyMenu.length < 4) cachedShaderColor_FancyMenu = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
		//------------------

		RenderSystem.enableBlend();
		//Fix missing alpha handling for ImageButtons (Vanilla bug)
<<<<<<< HEAD
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, ((IMixinAbstractWidget)button).getAlphaFancyMenu());
=======
		//TODO übernehmen
		graphics.setColor(cachedShaderColor_FancyMenu[0], cachedShaderColor_FancyMenu[1], cachedShaderColor_FancyMenu[2], ((IMixinAbstractWidget)button).getAlphaFancyMenu());
>>>>>>> 71aa26f... v3.3.0 MC 1.20.1

		//If it should render the Vanilla background
		return renderVanilla;

	}

	@Inject(method = "renderButton", at = @At("RETURN"))
	private void afterRenderWidgetFancyMenu(PoseStack $$0, int $$1, int $$2, float $$3, CallbackInfo info) {
		//Reset shader color after alpha handling
<<<<<<< HEAD
		RenderingUtils.resetShaderColor();
=======
		//TODO übernehmen
		if (cachedShaderColor_FancyMenu == null) cachedShaderColor_FancyMenu = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
		graphics.setColor(cachedShaderColor_FancyMenu[0], cachedShaderColor_FancyMenu[1], cachedShaderColor_FancyMenu[2], cachedShaderColor_FancyMenu[3]);
		cachedShaderColor_FancyMenu = null;
		//------------------
>>>>>>> 71aa26f... v3.3.0 MC 1.20.1
	}
	
}
