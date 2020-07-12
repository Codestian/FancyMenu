package de.keksuccino.fancymenu.menu.fancy;

import java.util.ArrayList;
import java.util.List;

import de.keksuccino.core.math.MathUtils;
import de.keksuccino.core.properties.PropertiesSection;
import de.keksuccino.core.properties.PropertiesSet;
import de.keksuccino.core.sound.SoundHandler;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelper;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerEvents;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.DummyCoreMainHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.LanguageMenuHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.MainMenuHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.MoreRefinedStorageMainHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.controls.ControlsMenuHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.serverselection.ServerSelectionMenuHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.videosettings.VideoSettingsMenuHandler;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.worldselection.WorldSelectionMenuHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.common.MinecraftForge;

public class MenuCustomization {
	
	private static boolean initDone = false;
	private static List<String> sounds = new ArrayList<String>();
	
	public static void init() {
		if (!initDone) {
			//Registering (base) events for the MenuCustomization system
			MinecraftForge.EVENT_BUS.register(new MenuCustomizationEvents());
			
			//Registering all custom menu handlers
			MenuHandlerRegistry.registerHandler(new MainMenuHandler());
			MenuHandlerRegistry.registerHandler(new MoreRefinedStorageMainHandler());
			MenuHandlerRegistry.registerHandler(new DummyCoreMainHandler());
			
			if (!FancyMenu.config.getOrDefault("softmode", false)) {
				MenuHandlerRegistry.registerHandler(new WorldSelectionMenuHandler());
				MenuHandlerRegistry.registerHandler(new ServerSelectionMenuHandler());
				MenuHandlerRegistry.registerHandler(new ControlsMenuHandler());
				MenuHandlerRegistry.registerHandler(new LanguageMenuHandler());
				MenuHandlerRegistry.registerHandler(new VideoSettingsMenuHandler());
			}
			
			//Registering event to automatically register handlers for all menus (its necessary to do this AFTER registering custom handlers!)
			MinecraftForge.EVENT_BUS.register(new MenuHandlerEvents());
			
			CustomizationHelper.init();
			
			//Registering the update event for the button cache
			MinecraftForge.EVENT_BUS.register(new ButtonCache());
			
			//Caching menu customization properties from config/fancymain/customization
			MenuCustomizationProperties.loadProperties();
			initDone = true;
		}
	}
	
	public static void reload() {
		if (initDone) {
			//Resets itself automatically and can be used for both loading and reloading
			MenuCustomizationProperties.loadProperties();
		}
	}
	
	public static boolean isValidScreen(GuiScreen screen) {
		if (screen == null) {
			return false;
		}
		if (screen instanceof NotificationModUpdateScreen) {
			return false;
		}
		if (screen instanceof GuiScreenRealmsProxy) {
			return false;
		}
		return true;
	}
	
	public static void registerSound(String key, String path) {
		if (!sounds.contains(key)) {
			sounds.add(key);
		}
		SoundHandler.registerSound(key, path);
	}
	
	public static void unregisterSound(String key) {
		if (sounds.contains(key)) {
			sounds.remove(key);
		}
		SoundHandler.unregisterSound(key);
	}
	
	public static void stopSounds() {
		for (String s : sounds) {
			SoundHandler.stopSound(s);
		}
	}
	
	public static void resetSounds() {
		for (String s : sounds) {
			SoundHandler.resetSound(s);
		}
	}
	
	public static boolean containsCalculations(PropertiesSet properties) {
		for (PropertiesSection s : properties.getPropertiesOfType("customization")) {
			String width = s.getEntryValue("width");
			String height = s.getEntryValue("height");
			String x = s.getEntryValue("x");
			String y = s.getEntryValue("y");
			String posX = s.getEntryValue("posX");
			String posY = s.getEntryValue("posY");
			if ((width != null) && !MathUtils.isInteger(width)) {
				return true;
			}
			if ((height != null) && !MathUtils.isInteger(height)) {
				return true;
			}
			if ((x != null) && !MathUtils.isInteger(x)) {
				return true;
			}
			if ((y != null) && !MathUtils.isInteger(y)) {
				return true;
			}
			if ((posX != null) && !MathUtils.isInteger(posX)) {
				return true;
			}
			if ((posY != null) && !MathUtils.isInteger(posY)) {
				return true;
			}
		}
		return false;
	}
}
