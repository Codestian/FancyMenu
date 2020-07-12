package de.keksuccino.fancymenu.menu.fancy.helper;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.core.file.FileUtils;
import de.keksuccino.core.gui.content.AdvancedButton;
import de.keksuccino.core.gui.content.PopupMenu;
import de.keksuccino.core.gui.screens.SimpleLoadingScreen;
import de.keksuccino.core.gui.screens.popup.NotificationPopup;
import de.keksuccino.core.gui.screens.popup.PopupHandler;
import de.keksuccino.core.gui.screens.popup.TextInputPopup;
import de.keksuccino.core.gui.screens.popup.YesNoPopup;
import de.keksuccino.core.input.MouseInput;
import de.keksuccino.core.properties.PropertiesSection;
import de.keksuccino.core.properties.PropertiesSerializer;
import de.keksuccino.core.properties.PropertiesSet;
import de.keksuccino.core.rendering.animation.IAnimationRenderer;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.localization.Locals;
import de.keksuccino.fancymenu.menu.animation.AdvancedAnimation;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomizationProperties;
import de.keksuccino.fancymenu.menu.fancy.gameintro.GameIntroScreen;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiBase;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiLoader;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.CreateCustomGuiPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutCreatorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.PreloadedLayoutCreatorScreen;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomizationHelper {
	
	private static CustomizationHelper instance;

	private AdvancedButton dropdownButton;
	private PopupMenu dropdown;
	private PopupMenu overridePopup;
	private PopupMenu customGuisPopup;
	private ManageCustomGuiPopupMenu manageCustomGuiPopup;
	private boolean showButtonInfo = false;
	private boolean showMenuInfo = false;
	private List<Widget> buttons = new ArrayList<Widget>();
	private AdvancedButton buttonInfoButton;
	private AdvancedButton menuInfoButton;
	private AdvancedButton reloadButton;
	private AdvancedButton overrideButton;
	private AdvancedButton customGuisButton;
	
	public Screen current;
	
	public static void init() {
		instance = new CustomizationHelper();
		MinecraftForge.EVENT_BUS.register(instance);
	}
	
	public static CustomizationHelper getInstance() {
		return instance;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onInitPost(GuiScreenEvent.InitGuiEvent.Post e) {

		if (this.dropdown != null) {
			this.dropdown.closeMenu();
		}
		MouseInput.unblockVanillaInput("customizationhelper");
		
		if (!isValidScreen(e.getGui())) {
			return;
		}
		
		this.current = e.getGui();

		this.handleWidgetsUpdate(e.getWidgetList());

		String infoLabel = Locals.localize("helper.button.buttoninfo");
		if (this.showButtonInfo) {
			infoLabel = "§a" + Locals.localize("helper.button.buttoninfo");
		}
		AdvancedButton iButton = new CustomizationButton(5, 5, 70, 20, infoLabel, true, (onPress) -> {
			this.onInfoButtonPress();
		}); 
		this.buttonInfoButton = iButton;

		String minfoLabel = Locals.localize("helper.button.menuinfo");
		if (this.showMenuInfo) {
			minfoLabel = "§a" + Locals.localize("helper.button.menuinfo");
		}
		AdvancedButton miButton = new CustomizationButton(80, 5, 70, 20, minfoLabel, true, (onPress) -> {
			this.onMoreInfoButtonPress();
		});
		this.menuInfoButton = miButton;

		this.reloadButton = new CustomizationButton(e.getGui().width - 55, 5, 50, 20, Locals.localize("helper.button.reload"), true, (onPress) -> {
			onReloadButtonPress();
		});

		AdvancedButton layoutCreatorButton = new CustomizationButton(e.getGui().width - 150, 5, 90, 20, Locals.localize("helper.button.createlayout"), true, (onPress) -> {
			Minecraft.getInstance().displayGuiScreen(new LayoutCreatorScreen(this.current));
			LayoutCreatorScreen.isActive = true;
			MenuCustomization.stopSounds();
			MenuCustomization.resetSounds();
			for (IAnimationRenderer r : AnimationHandler.getAnimations()) {
				if (r instanceof AdvancedAnimation) {
					((AdvancedAnimation)r).stopAudio();
					if (((AdvancedAnimation)r).replayIntro()) {
						((AdvancedAnimation)r).resetAnimation();
					}
				}
			}
		});

		AdvancedButton editLayoutButton = new CustomizationButton(e.getGui().width - 245, 5, 90, 20, Locals.localize("helper.creator.editlayout"), true, (onPress) -> {
			String identifier = this.current.getClass().getName();
			if (this.current instanceof CustomGuiBase) {
				identifier = ((CustomGuiBase) this.current).getIdentifier();
			}
			List<PropertiesSet> l = MenuCustomizationProperties.getPropertiesWithIdentifier(identifier);
			if (l.isEmpty()) {
				PopupHandler.displayPopup(new NotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.creator.editlayout.nolayouts.msg")));
			}
			if (l.size() == 1) {
				if (!MenuCustomization.containsCalculations(l.get(0))) {
					Minecraft.getInstance().displayGuiScreen(new PreloadedLayoutCreatorScreen(this.current, l));
					LayoutCreatorScreen.isActive = true;
					MenuCustomization.stopSounds();
					MenuCustomization.resetSounds();
					for (IAnimationRenderer r : AnimationHandler.getAnimations()) {
						if (r instanceof AdvancedAnimation) {
							((AdvancedAnimation)r).stopAudio();
							if (((AdvancedAnimation)r).replayIntro()) {
								((AdvancedAnimation)r).resetAnimation();
							}
						}
					}
				} else {
					PopupHandler.displayPopup(new NotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.creator.editlayout.unsupportedvalues")));
				}
			}
			if (l.size() > 1) {
				PopupHandler.displayPopup(new EditLayoutPopup(l));
			}
		});

		String overrLabel = Locals.localize("helper.buttons.tools.overridemenu");
		if (this.isScreenOverridden()) {
			overrLabel = Locals.localize("helper.buttons.tools.resetoverride");
		}
		this.overrideButton = new CustomizationButton(e.getGui().width - 150, 5, 90, 20, overrLabel, true, (onPress) -> {
			if (!this.isScreenOverridden()) {
				this.overridePopup = new PopupMenu(100, 20, -1);

				List<String> l = CustomGuiLoader.getCustomGuis();

				if (!l.isEmpty()) {

					this.overridePopup.addContent(new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.customguis.pickbyname"), true, (press) -> {
						PopupHandler.displayPopup(new TextInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.buttons.tools.customguis.pickbyname"), null, 240, (call) -> {
							if (call != null) {
								if (CustomGuiLoader.guiExists(call)) {
									this.onOverrideWithCustomGui(call);
								} else {
									PopupHandler.displayPopup(new NotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.buttons.tools.customguis.invalididentifier")));
								}
							}
						}));
					}));
					
					for (String s : l) {
						String label = s;
						if (Minecraft.getInstance().fontRenderer.getStringWidth(label) > 80) {
							//TODO trimStringToWidth
							label = Minecraft.getInstance().fontRenderer.func_238412_a_(label, 75) + "..";
						}

						this.overridePopup.addContent(new CustomizationButton(0, 0, 0, 0, label, true, (press) -> {
							this.onOverrideWithCustomGui(s);
						}));

					}

				} else {
					this.overridePopup.addContent(new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.creator.empty"), true, (press) -> {}));
				}

				this.overridePopup.openMenuAt(onPress.x - this.overridePopup.getWidth() - 2, onPress.y);

			} else {

				for (String s : FileUtils.getFiles(FancyMenu.getCustomizationPath().getPath())) {
					PropertiesSet props = PropertiesSerializer.getProperties(s);
					if (props == null) {
						continue;
					}
					PropertiesSet props2 = new PropertiesSet(props.getPropertiesType());
					List<PropertiesSection> l = props.getProperties();
					List<PropertiesSection> l2 = new ArrayList<PropertiesSection>();
					boolean b = false;

					List<PropertiesSection> metas = props.getPropertiesOfType("customization-meta");
					if ((metas == null) || metas.isEmpty()) {
						metas = props.getPropertiesOfType("type-meta");
					}
					if (metas != null) {
						if (metas.isEmpty()) {
							continue;
						}
						String identifier = metas.get(0).getEntryValue("identifier");
						Screen overridden = ((CustomGuiBase)this.current).getOverriddenScreen();
						if ((identifier == null) || !identifier.equalsIgnoreCase(overridden.getClass().getName())) {
							continue;
						}

					} else {
						continue;
					}

					for (PropertiesSection sec : l) {
						String action = sec.getEntryValue("action");
						if (sec.getSectionType().equalsIgnoreCase("customization-meta") || sec.getSectionType().equalsIgnoreCase("type-meta")) {
							l2.add(sec);
							continue;
						}
						if ((action != null) && !action.equalsIgnoreCase("overridemenu")) {
							l2.add(sec);
						}
						if ((action != null) && action.equalsIgnoreCase("overridemenu")) {
							b = true;
						}
					}

					if (b) {
						File f = new File(s);
						if (f.exists() && f.isFile()) {
							f.delete();
						}

						if (l2.size() > 1) {
							for (PropertiesSection sec : l2) {
								props2.addProperties(sec);
							}

							PropertiesSerializer.writeProperties(props2, s);
						}
					}
				}

				this.onReloadButtonPress();
				if (this.current instanceof CustomGuiBase) {
					Minecraft.getInstance().displayGuiScreen(((CustomGuiBase) this.current).getOverriddenScreen());
				}
			}
		});

		AdvancedButton createGuiButton = new CustomizationButton(e.getGui().width - 55, 5, 50, 20, Locals.localize("helper.buttons.tools.creategui"), true, (onPress) -> {
			PopupHandler.displayPopup(new CreateCustomGuiPopup());
		});

		this.manageCustomGuiPopup = new ManageCustomGuiPopupMenu(100, 20, -1);
		this.customGuisPopup = new PopupMenu(100, 20, -1);
		List<String> l = CustomGuiLoader.getCustomGuis();
		if (!l.isEmpty()) {
			
			this.customGuisPopup.addContent(new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.customguis.openbyname"), true, (press) -> {
				PopupHandler.displayPopup(new TextInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.buttons.tools.customguis.openbyname"), null, 240, (call) -> {
					if (call != null) {
						if (CustomGuiLoader.guiExists(call)) {
							Minecraft.getInstance().displayGuiScreen(CustomGuiLoader.getGui(call, Minecraft.getInstance().currentScreen, null));
						} else {
							PopupHandler.displayPopup(new NotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.buttons.tools.customguis.invalididentifier")));
						}
					}
				}));
			}));
			
			this.customGuisPopup.addContent(new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.customguis.deletebyname"), true, (press) -> {
				PopupHandler.displayPopup(new TextInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.buttons.tools.customguis.deletebyname"), null, 240, (call) -> {
					if (call != null) {
						if (CustomGuiLoader.guiExists(call)) {
							CustomizationHelper.getInstance().dropdown.closeMenu();
							PopupHandler.displayPopup(new YesNoPopup(300, new Color(0, 0, 0, 0), 240, (call2) -> {
								if (call2) {
									if (CustomGuiLoader.guiExists(call)) {
										List<File> delete = new ArrayList<File>();
										for (String s : FileUtils.getFiles(FancyMenu.getCustomGuiPath().getPath())) {
											File f = new File(s);
											for (String s2 : FileUtils.getFileLines(f)) {
												if (s2.replace(" ", "").toLowerCase().equals("identifier=" + call)) {
													delete.add(f);
												}
											}
										}

										for (File f : delete) {
											if (f.isFile()) {
												f.delete();
											}
										}

										CustomizationHelper.getInstance().onReloadButtonPress();
									}
								}
							}, Locals.localize("helper.buttons.tools.customguis.sure")));
						} else {
							PopupHandler.displayPopup(new NotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.buttons.tools.customguis.invalididentifier")));
						}
					}
				}));
			}));
			
			for (String s : l) {
				String label = s;
				if (Minecraft.getInstance().fontRenderer.getStringWidth(label) > 80) {
					//TODO trimStringToWidth
					label = Minecraft.getInstance().fontRenderer.func_238412_a_(label, 75) + "..";
				}

				this.customGuisPopup.addContent(new CustomizationButton(0, 0, 0, 0, label, true, (press) -> {
					this.manageCustomGuiPopup.openMenuAt(press.x - this.manageCustomGuiPopup.getWidth() - 2, press.y, s);
				}));
			}
		} else {
			this.customGuisPopup.addContent(new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.creator.empty"), true, (press) -> {}));
		}

		this.customGuisButton = new CustomizationButton(e.getGui().width - 55, 5, 50, 20, Locals.localize("helper.buttons.tools.customguis"), true, (press) -> {
			this.customGuisPopup.openMenuAt(press.x - this.customGuisPopup.getWidth() - 2, press.y);
		});
		
		AdvancedButton closeCustomGuiButton = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.closecustomgui"), (press) -> {
			e.getGui().onClose();
		});

		this.dropdown = new PopupMenu(100, 20, -1);

		this.dropdown.addContent(iButton);
		this.dropdown.addContent(miButton);
		this.dropdown.addContent(createGuiButton);
		this.dropdown.addContent(customGuisButton);
		this.dropdown.addContent(layoutCreatorButton);
		this.dropdown.addContent(editLayoutButton);
		if (this.isScreenOverridden()) {
			this.dropdown.addContent(overrideButton);
		} else if (!(e.getGui() instanceof CustomGuiBase)) {
			this.dropdown.addContent(overrideButton);
		} else {
			this.dropdown.addContent(closeCustomGuiButton);
		}

		this.dropdownButton = new CustomizationButton(e.getGui().width - 160, 5, 100, 20, Locals.localize("helper.buttons.tools.dropdownlabel"), true, (press) -> {
			if (!this.dropdown.isOpen()) {
				this.dropdown.openMenuAt(press.x, press.y + press.getHeight() - 1);
			} else {
				this.dropdown.closeMenu();
			}
		});
		//------------------
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderPost(GuiScreenEvent.DrawScreenEvent.Post e) {
		if (PopupHandler.isPopupActive()) {
			return;
		}
		if (!isValidScreen(e.getGui())) {
			return;
		}

		if (FancyMenu.config.getOrDefault("showcustomizationbuttons", true)) {
			this.dropdownButton.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
			this.reloadButton.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
			
			this.dropdown.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY());
			
			if (this.dropdown.isOpen()) {
				MouseInput.blockVanillaInput("customizationhelper");
			} else {
				MouseInput.unblockVanillaInput("customizationhelper");
			}
			
			if (this.dropdown.isOpen() && !this.customGuisPopup.isHovered() && !this.dropdownButton.isHovered() && !this.dropdown.isHovered() && (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown())) {
				this.dropdown.closeMenu();
			}
			if (this.overridePopup != null) {
				this.overridePopup.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY());
				if (this.overridePopup.isOpen() && !this.overrideButton.isHovered() && !this.overridePopup.isHovered() && (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown())) {
					this.overridePopup.closeMenu();
				}
				if (!this.dropdown.isOpen()) {
					this.overridePopup.closeMenu();
				}
			}
			if (this.customGuisPopup != null) {
				this.customGuisPopup.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY());
				if (this.customGuisPopup.isOpen() && !this.customGuisButton.isHovered() && !this.customGuisPopup.isHovered() && (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown())) {
					this.customGuisPopup.closeMenu();
				}
				if (!this.dropdown.isOpen()) {
					this.customGuisPopup.closeMenu();
				}
				if (this.manageCustomGuiPopup != null) {
					this.manageCustomGuiPopup.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY());
					if (!this.customGuisPopup.isOpen()) {
						this.manageCustomGuiPopup.closeMenu();
					}
				}
			}
		}
		
		if (this.showMenuInfo && !(e.getGui() instanceof LayoutCreatorScreen)) {
			RenderSystem.enableBlend();
			e.getGui().drawString(e.getMatrixStack(), Minecraft.getInstance().fontRenderer, "§f§l" + Locals.localize("helper.menuinfo.identifier") + ":", 5, 5, 0);
			if (e.getGui() instanceof CustomGuiBase) {
				e.getGui().drawString(e.getMatrixStack(), Minecraft.getInstance().fontRenderer, "§f" + ((CustomGuiBase)e.getGui()).getIdentifier(), 5, 15, 0);
			} else {
				e.getGui().drawString(e.getMatrixStack(), Minecraft.getInstance().fontRenderer, "§f" + e.getGui().getClass().getName(), 5, 15, 0);
			}
			RenderSystem.disableBlend();
		}

		if (this.showButtonInfo) {
			for (Widget w : this.buttons) {
				if (w.isHovered()) {
					int id = getButtonId(w);
					String idString = Locals.localize("helper.buttoninfo.idnotfound");
					if (id >= 0) {
						idString = String.valueOf(id);
					}
					String key = ButtonCache.getKeyForButton(w);
					if (key == null) {
						key = Locals.localize("helper.buttoninfo.keynotfound");
					}
					
					List<String> info = new ArrayList<String>();
					int width = Minecraft.getInstance().fontRenderer.getStringWidth(Locals.localize("helper.button.buttoninfo")) + 10;
					
					info.add("§f" + Locals.localize("helper.buttoninfo.id") + ": " + idString);
					info.add("§f" + Locals.localize("helper.buttoninfo.key") + ": " + key);
					info.add("§f" + Locals.localize("general.width") + ": " + w.getWidth());
					info.add("§f" + Locals.localize("general.height") + ": " + w.getHeight());
					info.add("§f" + Locals.localize("helper.buttoninfo.labelwidth") + ": " + Minecraft.getInstance().fontRenderer.getStringWidth(w.getMessage().getString()));
					
					//Getting the longest string from the list to render the background with the correct width
					for (String s : info) {
						int i = Minecraft.getInstance().fontRenderer.getStringWidth(s) + 10;
						if (i > width) {
							width = i;
						}
					}
					
					int x = e.getMouseX();
					if (e.getGui().width < x + width + 10) {
						x -= width + 10;
					}
					
					int y = e.getMouseY();
					if (e.getGui().height < y + 90) {
						y -= 90;
					}
					
					drawInfoBackground(e.getMatrixStack(), x, y, width + 10, 90);
					
					RenderSystem.enableBlend();
					e.getGui().drawString(e.getMatrixStack(), Minecraft.getInstance().fontRenderer, "§f§l" + Locals.localize("helper.button.buttoninfo"), x + 10, y + 10, 0);

					int i2 = 20;
					for (String s : info) {
						e.getGui().drawString(e.getMatrixStack(), Minecraft.getInstance().fontRenderer, s, x + 10, y + 10 + i2, 0);
						i2 += 10;
					}
					RenderSystem.disableBlend();
					
					break;
				}
			}
		}
	}

	private static boolean isValidScreen(Screen s) {
		//Prevents rendering in child(?)-screens like RealmsScreenProxy
		if (s != Minecraft.getInstance().currentScreen) {
			return false;
		}
		//Prevents rendering in realm screens (if it's the main screen)
		if (s instanceof RealmsScreen) {
			return false;
		}
		//Prevents rendering in FancyMenu screens
		if (s instanceof SimpleLoadingScreen) {
			return false;
		}
		if (s instanceof GameIntroScreen) {
			return false;
		}
		//Prevents rendering in layout creation screens
		if (s instanceof LayoutCreatorScreen) {
			return false;
		}
		return true;
	}
	
	private static void drawInfoBackground(MatrixStack matrix, int x, int y, int width, int height) {
		IngameGui.fill(matrix, x, y, x + width, y + height, new Color(102, 0, 102, 200).getRGB());
	}
	
	public void updateCustomizationButtons() {
		Screen current = Minecraft.getInstance().currentScreen;
		if (current != null) {
			Minecraft.getInstance().displayGuiScreen(current);
		}
	}
	
	public void onInfoButtonPress() {
		if (this.showButtonInfo) {
			this.showButtonInfo = false;
			this.buttonInfoButton.setMessage(Locals.localize("helper.button.buttoninfo"));;
			
		} else {
			this.showButtonInfo = true;
			this.buttonInfoButton.setMessage("§a" + Locals.localize("helper.button.buttoninfo"));;
		}
	}
	
	public void onMoreInfoButtonPress() {
		if (this.showMenuInfo) {
			this.showMenuInfo = false;
			this.menuInfoButton.setMessage(Locals.localize("helper.button.menuinfo"));;
			
		} else {
			this.showMenuInfo = true;
			this.menuInfoButton.setMessage("§a" + Locals.localize("helper.button.menuinfo"));;
		}
	}

	public void onReloadButtonPress() {
		FancyMenu.updateConfig();
		MenuCustomization.resetSounds();
		MenuCustomization.reload();
		MenuHandlerRegistry.setActiveHandler(null);
		CustomGuiLoader.loadCustomGuis();
		if (!FancyMenu.config.getOrDefault("showcustomizationbuttons", true)) {
			this.showButtonInfo = false;
			this.showMenuInfo = false;
		}
		try {
			Minecraft.getInstance().displayGuiScreen(Minecraft.getInstance().currentScreen);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleWidgetsUpdate(List<Widget> l) {
		this.buttons.clear();
		for (Widget w : l) {
			if (!CustomizationButton.isCustomizationButton(w)) {
				this.buttons.add(w);
			}
		}
	}
	
	/**
	 * Returns the button id or -1 if the button was not found in the button list.
	 */
	private static int getButtonId(Widget w) {
		return ButtonCache.getIdForButton(w);
	}

	private boolean isScreenOverridden() {
		if ((this.current != null) && (this.current instanceof CustomGuiBase) && (((CustomGuiBase)this.current).getOverriddenScreen() != null)) {
			return true;
		}
		return false;
	}

	private void onOverrideWithCustomGui(String customGuiIdentifier) {
		if ((customGuiIdentifier != null) && CustomGuiLoader.guiExists(customGuiIdentifier)) {
			PropertiesSection meta = new PropertiesSection("customization-meta");
			meta.addEntry("identifier", current.getClass().getName());

			PropertiesSection or = new PropertiesSection("customization");
			or.addEntry("action", "overridemenu");
			or.addEntry("identifier", customGuiIdentifier);

			PropertiesSet props = new PropertiesSet("menu");
			props.addProperties(meta);
			props.addProperties(or);

			String screenname = current.getClass().getName();
			if (screenname.contains(".")) {
				screenname = new StringBuilder(new StringBuilder(screenname).reverse().toString().split("[.]", 2)[0]).reverse().toString();
			}
			String filename = FileUtils.generateAvailableFilename(FancyMenu.getCustomizationPath().getPath(), "overridemenu_" + screenname, "txt");

			String finalpath = FancyMenu.getCustomizationPath().getPath() + "/" + filename;
			PropertiesSerializer.writeProperties(props, finalpath);

			this.onReloadButtonPress();
		}
	}

	private static class ManageCustomGuiPopupMenu extends PopupMenu {

		public ManageCustomGuiPopupMenu(int width, int buttonHeight, int space) {
			super(width, buttonHeight, space);
		}

		public void openMenuAt(int x, int y, String customGuiIdentifier) {
			this.content.clear();

			CustomizationButton openMenuButton = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.customguis.open"), (press) -> {
				if (CustomGuiLoader.guiExists(customGuiIdentifier)) {
					Minecraft.getInstance().displayGuiScreen(CustomGuiLoader.getGui(customGuiIdentifier, Minecraft.getInstance().currentScreen, null));
				}
			});
			this.addContent(openMenuButton);

			CustomizationButton deleteMenuButton = new CustomizationButton(0, 0, 0, 0, Locals.localize("helper.buttons.tools.customguis.delete"), (press) -> {
				CustomizationHelper.getInstance().dropdown.closeMenu();
				PopupHandler.displayPopup(new YesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
					if (call) {
						if (CustomGuiLoader.guiExists(customGuiIdentifier)) {
							List<File> delete = new ArrayList<File>();
							for (String s : FileUtils.getFiles(FancyMenu.getCustomGuiPath().getPath())) {
								File f = new File(s);
								for (String s2 : FileUtils.getFileLines(f)) {
									if (s2.replace(" ", "").toLowerCase().equals("identifier=" + customGuiIdentifier)) {
										delete.add(f);
									}
								}
							}

							for (File f : delete) {
								if (f.isFile()) {
									f.delete();
								}
							}

							CustomizationHelper.getInstance().onReloadButtonPress();
						}
					}
				}, Locals.localize("helper.buttons.tools.customguis.sure")));
			});
			this.addContent(deleteMenuButton);

			this.openMenuAt(x, y);
		}
	}

}
