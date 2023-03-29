package de.keksuccino.fancymenu.mixin.client;

import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.keksuccino.fancymenu.events.GuiInitCompletedEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

@Mixin(value = Screen.class)
public class MixinScreen {

	@Inject(at = @At("TAIL"), method = "<init>")
	protected void onConstructInstance(Component title, CallbackInfo info) {
		MenuHandlerBase.cachedOriginalMenuTitles.put(this.getClass(), title);
	}

	@Inject(at = @At(value = "TAIL"), method = "init(Lnet/minecraft/client/Minecraft;II)V")
	private void onInitCompleted(Minecraft minecraft, int width, int height, CallbackInfo info) {
		GuiInitCompletedEvent e = new GuiInitCompletedEvent((Screen) ((Object)this));
		MinecraftForge.EVENT_BUS.post(e);
	}
	
}
