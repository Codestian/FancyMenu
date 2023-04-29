package de.keksuccino.fancymenu.menu.fancy.menuhandler;

import com.mojang.blaze3d.platform.Window;
import de.keksuccino.fancymenu.events.acara.EventPriority;
import de.keksuccino.fancymenu.events.acara.SubscribeEvent;
import de.keksuccino.fancymenu.events.ticking.ClientTickEvent;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.events.screen.OpenScreenEvent;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiBase;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class MenuHandlerEvents {

	private Screen lastScreen;
	
	@SubscribeEvent
	public void onOpenGui(OpenScreenEvent.Pre e) {
		this.initHandler(e.getScreen());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onScreenInitPre(InitOrResizeScreenEvent.Pre e) {
		//Second try to register the menu handler, if onOpenGui failed because of changing the menu by another mod
		this.initHandler(e.getScreen());
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent.Post e) {

		//Resetting scale to the normal value when no GUI is active
		if ((MenuHandlerBase.scaleChangedIn != null) && (Minecraft.getInstance().screen == null)) {
			MenuHandlerBase.scaleChangedIn = null;
			int mcscale = Minecraft.getInstance().getWindow().calculateScale(Minecraft.getInstance().options.guiScale().get(), Minecraft.getInstance().isEnforceUnicode());
			Window m = Minecraft.getInstance().getWindow();
			m.setGuiScale((double)mcscale);
		}

		//Resetting last active menu handler when no GUI is displayed
		if (Minecraft.getInstance().screen == null) {
			MenuHandlerRegistry.setActiveHandler(null);
		}

		//Play menu close audio on menu close/switch
		if (this.lastScreen != Minecraft.getInstance().screen) {
			if (this.lastScreen != null) {
				MenuHandlerBase handler = MenuHandlerRegistry.getHandlerFor(this.lastScreen);
				if (handler != null) {
					String audio = handler.closeAudio;
					if (audio != null) {
						SoundHandler.resetSound(audio);
						SoundHandler.playSound(audio);
					}
				}
			}
		}

		this.lastScreen = Minecraft.getInstance().screen;
		
	}
	
	private void initHandler(Screen s) {
		if (s != null) {
			if (MenuCustomization.isBlacklistedMenu(s.getClass().getName())) {
				return;
			}
			if (s instanceof CustomGuiBase) {
				if (!MenuHandlerRegistry.isHandlerRegistered(((CustomGuiBase)s).getIdentifier())) {
					MenuHandlerRegistry.registerHandler(new CustomGuiMenuHandlerBase(((CustomGuiBase)s).getIdentifier()));
				}
			} else {
				if (!MenuHandlerRegistry.isHandlerRegistered(s.getClass().getName())) {
					MenuHandlerRegistry.registerHandler(new MenuHandlerBase(s.getClass().getName()));
				}
			}
		}
	}

}
