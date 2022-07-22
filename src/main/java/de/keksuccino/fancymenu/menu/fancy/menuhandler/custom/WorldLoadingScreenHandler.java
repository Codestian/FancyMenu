package de.keksuccino.fancymenu.menu.fancy.menuhandler.custom;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.mixin.client.IMixinWorldLoadProgressScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldLoadProgressScreen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.listener.TrackingChunkStatusListener;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldLoadingScreenHandler extends MenuHandlerBase {

	private long lastNarratorUpdateTime = -1L;
	 
	public WorldLoadingScreenHandler() {
		super(WorldLoadProgressScreen.class.getName());
	}
	
	@SubscribeEvent
	public void onRender(GuiScreenEvent.DrawScreenEvent.Pre e) {
		if (this.shouldCustomize(e.getGui())) {
			if (MenuCustomization.isMenuCustomizable(e.getGui())) {
				e.setCanceled(true);
				
				e.getGui().renderBackground(e.getMatrixStack());
				
				this.renderMenu(e.getMatrixStack(), e.getGui());
			}
		}
	}
	
	private void renderMenu(MatrixStack matrix, Screen screen) {
		
		TrackingChunkStatusListener tracker = getTracker(screen);
		FontRenderer font = Minecraft.getInstance().font;
		int j = screen.width / 2;
		int k = screen.height / 2;
		String s = "";
		
		if (tracker != null) {
			s = MathHelper.clamp(getTracker(screen).getProgress(), 0, 100) + "%";
			long i = Util.getMillis();
			if (i - this.lastNarratorUpdateTime > 2000L) {
				this.lastNarratorUpdateTime = i;
				NarratorChatListener.INSTANCE.sayNow((new TranslationTextComponent("narrator.loading", s)).getString());
			}
			
			if (FancyMenu.config.getOrDefault("showloadingscreenanimation", true)) {
				WorldLoadProgressScreen.renderChunks(matrix, getTracker(screen), j, k + 30, 2, 0);
			}
		}
		
		if (FancyMenu.config.getOrDefault("showloadingscreenpercent", true)) {
			AbstractGui.drawCenteredString(matrix, font, s, j, k - 9 / 2 - 30, 16777215);
		}
		
	}
	
	private static TrackingChunkStatusListener getTracker(Screen screen) {
		return ((IMixinWorldLoadProgressScreen)screen).getProgressListenerFancyMenu();
	}

}
