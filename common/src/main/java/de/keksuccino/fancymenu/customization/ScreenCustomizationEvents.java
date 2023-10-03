package de.keksuccino.fancymenu.customization;

import java.io.File;
import java.io.IOException;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.customization.customgui.CustomGuiBaseScreen;
import de.keksuccino.fancymenu.customization.widget.WidgetMeta;
import de.keksuccino.fancymenu.events.screen.CloseScreenEvent;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenStartingEvent;
import de.keksuccino.fancymenu.util.audio.SoundRegistry;
import de.keksuccino.fancymenu.customization.widget.WidgetLocatorHandler;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.event.acara.EventPriority;
import de.keksuccino.fancymenu.util.event.acara.EventListener;
import de.keksuccino.fancymenu.events.ticking.ClientTickEvent;
import de.keksuccino.fancymenu.events.widget.RenderGuiListBackgroundEvent;
import de.keksuccino.fancymenu.events.ScreenReloadEvent;
import de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableWidget;
import de.keksuccino.fancymenu.util.window.WindowHandler;
import de.keksuccino.fancymenu.events.ModReloadEvent;
import de.keksuccino.konkrete.file.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScreenCustomizationEvents {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private boolean idle = false;
	private boolean iconSetAfterFullscreen = false;
	private boolean scaleChecked = false;
	private boolean resumeWorldMusic = false;
	protected Screen lastScreen = null;

	@EventListener(priority = EventPriority.HIGH)
	public void onModReloaded(ModReloadEvent e) {
		WidgetLocatorHandler.clearCache();
		ScreenCustomization.isNewMenu = true;
		this.lastScreen = null;
	}

	@EventListener(priority =  EventPriority.HIGH)
	public void onSoftReload(ScreenReloadEvent e) {
		WidgetLocatorHandler.clearCache();
		ScreenCustomization.isNewMenu = true;
		this.lastScreen = null;
	}

	@EventListener
	public void onCloseScreen(CloseScreenEvent e) {
		if (e.getScreen() != null) {
			//Reset customizable widgets before close, so button backgrounds and other stuff gets correctly stopped
			for (GuiEventListener l : e.getScreen().children()) {
				if (l instanceof CustomizableWidget w) {
					w.resetWidgetCustomizationsFancyMenu();
				}
			}
			if (e.getScreen() instanceof LayoutEditorScreen editor) {
				for (WidgetMeta m : editor.cachedVanillaWidgetMetas) {
					if (m.getWidget() instanceof CustomizableWidget w) {
						w.resetWidgetCustomizationsFancyMenu();
					}
				}
			}
		}
	}

	@EventListener
	public void onInitStarting(InitOrResizeScreenStartingEvent e) {

		if (e.getScreen() != null) {

			//Reset customizable widgets before init/resize (in case the screen doesn't rebuild its widgets on init/resize)
			for (GuiEventListener l : e.getScreen().children()) {
				if (l instanceof CustomizableWidget w) {
					w.resetWidgetCustomizationsFancyMenu();
				}
			}

			if (this.lastScreen != null) {
				ScreenCustomization.isNewMenu = !this.lastScreen.getClass().getName().equals(e.getScreen().getClass().getName());
				if ((this.lastScreen instanceof CustomGuiBaseScreen cLast) && (e.getScreen() instanceof CustomGuiBaseScreen cNow)) {
					ScreenCustomization.isNewMenu = !cLast.getIdentifier().equals(cNow.getIdentifier());
				}
			} else {
				ScreenCustomization.isNewMenu = true;
			}

		}

		this.lastScreen = e.getScreen();
		if (ScreenCustomization.isNewMenu) {
			WidgetLocatorHandler.clearCache();
		}

		ScreenCustomization.isCurrentScrollable = false;

		if (!(e.getScreen() instanceof LayoutEditorScreen)) {
			this.idle = false;
		}
		if (!ScreenCustomization.isCustomizationEnabledForScreen(e.getScreen()) && !(e.getScreen() instanceof LayoutEditorScreen)) {
			SoundRegistry.stopSounds();
			SoundRegistry.resetSounds();
		}

		//Stopping menu music when deactivated in config
		if ((Minecraft.getInstance().level == null)) {
			if (!FancyMenu.getOptions().playVanillaMenuMusic.getValue()) {
				Minecraft.getInstance().getMusicManager().stopPlaying();
			}
		}

	}

	@EventListener
	public void onTick(ClientTickEvent.Pre e) {

		if (Minecraft.getInstance().screen == null) {
			this.lastScreen = null;
		}

		//Stopping audio for all menu handlers if no screen is being displayed
		if ((Minecraft.getInstance().screen == null) && !this.idle) {
			SoundRegistry.stopSounds();
			SoundRegistry.resetSounds();
			this.idle = true;
		}

		if ((Minecraft.getInstance().level != null) && (Minecraft.getInstance().screen == null) && this.resumeWorldMusic) {
			Minecraft.getInstance().getSoundManager().resume();
			this.resumeWorldMusic = false;
		}

		if (Minecraft.getInstance().getWindow().isFullscreen()) {
			this.iconSetAfterFullscreen = false;
		} else {
			if (!this.iconSetAfterFullscreen) {
				WindowHandler.updateCustomWindowIcon();
				this.iconSetAfterFullscreen = true;
			}
		}

		//Handle default GUI scale
		if (!scaleChecked) {
			scaleChecked = true;
			int scale = FancyMenu.getOptions().defaultGuiScale.getValue();
			if ((scale != -1) && (scale != 0)) {
				File f = FancyMenu.INSTANCE_DATA_DIR;
				if (!f.exists()) {
					f.mkdirs();
				}
				File f2 = new File(f.getPath() + "/default_scale_set.fm");
				if (!f2.exists()) {
					try {
						f2.createNewFile();
						FileUtils.writeTextToFile(f2, false, "You're not supposed to be here! Shoo!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					LOGGER.info("[FANCYMENU] Setting default GUI scale..");
					Minecraft.getInstance().options.guiScale().set(scale);
					Minecraft.getInstance().options.save();
					Minecraft.getInstance().resizeDisplay();
				}
			}
		}

		if (Minecraft.getInstance().screen == null) {
			ScreenCustomization.isCurrentScrollable = false;
		}
		
	}

	@EventListener
	public void onRenderListBackground(RenderGuiListBackgroundEvent.Pre e) {
		ScreenCustomization.isCurrentScrollable = true;
	}
	
}
