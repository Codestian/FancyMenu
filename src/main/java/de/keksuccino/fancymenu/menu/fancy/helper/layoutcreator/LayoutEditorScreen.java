package de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.animation.AdvancedAnimation;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.button.ButtonData;
import de.keksuccino.fancymenu.menu.fancy.DynamicValueHelper;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiBase;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelper;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.EditHistory.Snapshot;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorUI.LayoutPropertiesContextMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.ChooseFilePopup;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutAnimation;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutElement;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutPlayerEntity;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutShape;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutSlideshow;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutSplashText;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutString;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutTexture;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutWebString;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutWebTexture;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutButton;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutButtonDummyCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.button.LayoutVanillaButton;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.FMContextMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMTextInputPopup;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMYesNoPopup;
import de.keksuccino.fancymenu.menu.fancy.item.AnimationCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.ShapeCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.ShapeCustomizationItem.Shape;
import de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.SlideshowCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.SplashTextCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.StringCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.TextureCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.WebStringCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.WebTextureCustomizationItem;
import de.keksuccino.fancymenu.menu.panorama.ExternalTexturePanoramaRenderer;
import de.keksuccino.fancymenu.menu.slideshow.ExternalTextureSlideshowRenderer;
import de.keksuccino.fancymenu.menu.slideshow.SlideshowHandler;
import de.keksuccino.konkrete.gui.content.ContextMenu;
import de.keksuccino.konkrete.gui.content.IMenu;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.sound.SoundHandler;
import de.keksuccino.konkrete.web.WebUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class LayoutEditorScreen extends Screen {
	
	public static boolean isActive = false;

	protected static final List<PropertiesSection> COPIED_ELEMENT_CACHE = new ArrayList<PropertiesSection>();
	protected static boolean initDone = false;
	
	public EditHistory history = new EditHistory(this);
	
	public List<Runnable> postRenderTasks = new ArrayList<Runnable>();
	
	public final Screen screen;
	protected List<LayoutElement> content = new ArrayList<LayoutElement>();
	protected List<LayoutElement> newContentMove;
	protected List<LayoutElement> newContentPaste = new ArrayList<LayoutElement>();
	public List<LayoutElement> deleteContentQueue = new ArrayList<LayoutElement>();
	protected List<LayoutElement> vanillaButtonContent = new ArrayList<LayoutElement>();
	protected List<LayoutVanillaButton> hidden = new ArrayList<LayoutVanillaButton>();
	protected Map<String, Boolean> audio = new HashMap<String, Boolean>();
	public Map<Long, String> vanillaButtonNames = new HashMap<Long, String>();
	public Map<Long, List<String>> vanillaButtonTextures = new HashMap<Long, List<String>>();
	public Map<Long, Integer> vanillaButtonClicks = new HashMap<Long, Integer>();
	public Map<Long, String> vanillaHoverLabels = new HashMap<Long, String>();
	public Map<Long, String> vanillaClickSounds = new HashMap<Long, String>();
	public Map<Long, String> vanillaHoverSounds = new HashMap<Long, String>();
	public Map<Long, Float> vanillaDelayAppearance = new HashMap<Long, Float>();
	public Map<Long, Boolean> vanillaDelayAppearanceFirstTime = new HashMap<Long, Boolean>();
	public Map<Long, Float> vanillaFadeIn = new HashMap<Long, Float>();
	public Map<Long, String> vanillaDescriptions = new HashMap<Long, String>();
	protected List<LayoutElement> focusedObjects = new ArrayList<LayoutElement>();
	protected List<LayoutElement> focusedObjectsCache = new ArrayList<LayoutElement>();
	
	protected List<IMenu> menus = new ArrayList<IMenu>();
	
	protected FMContextMenu multiselectRightclickMenu;
	protected LayoutPropertiesContextMenu propertiesRightclickMenu;
	
	protected IAnimationRenderer backgroundAnimation;
	public ExternalTextureResourceLocation backgroundTexture;
	public String backgroundTexturePath;
	public ExternalTexturePanoramaRenderer backgroundPanorama;
	public ExternalTextureSlideshowRenderer backgroundSlideshow;
	public List<String> backgroundAnimationNames = new ArrayList<String>();
	public boolean randomBackgroundAnimation = false;
	public boolean panorama = false;
	protected int panoTick = 0;
	protected double panoPos = 0.0;
	protected boolean panoMoveBack = false;
	protected boolean panoStop = false;

	protected String openAudio;
	protected String closeAudio;
	
	protected String renderorder = "foreground";
	protected String requiredmods;
	protected String minimumMC;
	protected String maximumMC;
	protected String minimumFM;
	protected String maximumFM;

	protected int biggerThanWidth = 0;
	protected int biggerThanHeight = 0;
	protected int smallerThanWidth = 0;
	protected int smallerThanHeight = 0;
	
	protected boolean randomMode = false;
	protected String randomGroup = "1";
	protected boolean randomOnlyFirstTime = false;

	protected int scale = 0;

	protected boolean multiselectStretchedX = false;
	protected boolean multiselectStretchedY = false;
	protected List<ContextMenu> multiselectChilds = new ArrayList<ContextMenu>();
	
	protected Map<String, Boolean> focusChangeBlocker = new HashMap<String, Boolean>();
	protected LayoutElement topObject;
	
	protected LayoutEditorUI ui = new LayoutEditorUI(this);
	
	public LayoutEditorScreen(Screen screenToCustomize) {
		super(new StringTextComponent(""));
		this.screen = screenToCustomize;

		if (!initDone) {
			KeyboardHandler.addKeyPressedListener(LayoutEditorScreen::onShortcutPressed);
			KeyboardHandler.addKeyPressedListener(LayoutEditorScreen::onArrowKeysPressed);
			initDone = true;
		}
	}

	@Override
	protected void init() {
		
		this.ui.updateUI();
		
		if (this.multiselectRightclickMenu != null) {
			this.multiselectRightclickMenu.closeMenu();
		}
		this.multiselectRightclickMenu = new LayoutEditorUI.MultiselectContextMenu(this);
		this.multiselectRightclickMenu.setAutoclose(false);
		this.multiselectRightclickMenu.setAlwaysOnTop(true);
		
		if (this.propertiesRightclickMenu != null) {
			this.propertiesRightclickMenu.closeMenu();
		}
		this.propertiesRightclickMenu = new LayoutPropertiesContextMenu(this, true);
		this.propertiesRightclickMenu.setAutoclose(false);
		this.propertiesRightclickMenu.setAlwaysOnTop(true);

		if (this.scale > 0) {
			Minecraft.getInstance().getMainWindow().setGuiScale(this.scale);
			this.height = Minecraft.getInstance().getMainWindow().getScaledHeight();
			this.width = Minecraft.getInstance().getMainWindow().getScaledWidth();
		} else {
			Minecraft.getInstance().getMainWindow().setGuiScale(Minecraft.getInstance().getMainWindow().calcGuiScale(Minecraft.getInstance().gameSettings.guiScale, Minecraft.getInstance().getForceUnicodeFont()));
			this.height = Minecraft.getInstance().getMainWindow().getScaledHeight();
			this.width = Minecraft.getInstance().getMainWindow().getScaledWidth();
		}

		this.focusedObjects.clear();
		this.updateContent();
		
		this.focusChangeBlocker.clear();
		
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	public boolean isLayoutEmpty() {
		return this.getAllProperties().isEmpty();
	}

	protected List<PropertiesSection> getAllProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		
		PropertiesSection meta = new PropertiesSection("customization-meta");
		meta.addEntry("identifier", this.getScreenToCustomizeIdentifier());
		meta.addEntry("renderorder", this.renderorder);
		
		meta.addEntry("randommode", "" + this.randomMode);
		meta.addEntry("randomgroup", this.randomGroup);
		meta.addEntry("randomonlyfirsttime", "" + this.randomOnlyFirstTime);
		
		if ((this.requiredmods != null) && !this.requiredmods.replace(" ", "").equals("")) {
			meta.addEntry("requiredmods", this.requiredmods);
		}
		if ((this.minimumMC != null) && !this.minimumMC.replace(" ", "").equals("")) {
			meta.addEntry("minimummcversion", this.minimumMC);
		}
		if ((this.maximumMC != null) && !this.maximumMC.replace(" ", "").equals("")) {
			meta.addEntry("maximummcversion", this.maximumMC);
		}
		if ((this.minimumFM != null) && !this.minimumFM.replace(" ", "").equals("")) {
			meta.addEntry("minimumfmversion", this.minimumFM);
		}
		if ((this.maximumFM != null) && !this.maximumFM.replace(" ", "").equals("")) {
			meta.addEntry("maximumfmversion", this.maximumFM);
		}
		if (this.biggerThanWidth != 0) {
			meta.addEntry("biggerthanwidth", "" + this.biggerThanWidth);
		}
		if (this.biggerThanHeight != 0) {
			meta.addEntry("biggerthanheight", "" + this.biggerThanHeight);
		}
		if (this.smallerThanWidth != 0) {
			meta.addEntry("smallerthanwidth", "" + this.smallerThanWidth);
		}
		if (this.smallerThanHeight != 0) {
			meta.addEntry("smallerthanheight", "" + this.smallerThanHeight);
		}
		l.add(meta);
		
		if (!this.backgroundAnimationNames.isEmpty()) {
			String names = this.backgroundAnimationNames.get(0);
			if (this.backgroundAnimationNames.size() > 1) {
				int i = 0;
				for (String s : this.backgroundAnimationNames) {
					if (i > 0) {
						names += ", " + s;
					}
					i++;
				}
			}
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "animatebackground");
			ps.addEntry("name", names);
			if (this.randomBackgroundAnimation) {
				ps.addEntry("random", "true");
			}
			l.add(ps);
		}

		if (this.backgroundPanorama != null) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "setbackgroundpanorama");
			ps.addEntry("name", this.backgroundPanorama.getName());
			l.add(ps);
		}

		if (this.backgroundSlideshow != null) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "setbackgroundslideshow");
			ps.addEntry("name", this.backgroundSlideshow.getName());
			l.add(ps);
		}
		
		if (this.backgroundTexture != null) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "texturizebackground");
			ps.addEntry("path", this.backgroundTexturePath);
			if (this.panorama) {
				ps.addEntry("wideformat", "true");
			}
			l.add(ps);
		}

		if (this.scale > 0) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "setscale");
			ps.addEntry("scale", "" + this.scale);
			l.add(ps);
		}

		if (this.openAudio != null) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "setopenaudio");
			ps.addEntry("path", this.openAudio);
			l.add(ps);
		}

		if (this.closeAudio != null) {
			PropertiesSection ps = new PropertiesSection("customization");
			ps.addEntry("action", "setcloseaudio");
			ps.addEntry("path", this.closeAudio);
			l.add(ps);
		}
		
		for (Map.Entry<String, Boolean> m : this.audio.entrySet()) {
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addaudio");
			s.addEntry("path", m.getKey());
			s.addEntry("loop", "" + m.getValue());
			l.add(s);
		}
		
		for (LayoutElement o : this.content) {
			l.addAll(o.getProperties());
		}
		return l;
	}
	
	/**
	 * Updates the LayoutObjects shown in the CreatorScreen.<br>
	 * The positions of all UNMODIFIED vanilla buttons will be updated to keep them at the correct position when the screen is getting resized.
	 */
	protected void updateContent() {
		List<LayoutElement> l = new ArrayList<LayoutElement>();
		for (LayoutElement o : this.content) {
			if (!(o instanceof LayoutVanillaButton)) {
				l.add(o);
			} else {
				if (!o.object.orientation.equals("original") || ((LayoutVanillaButton)o).hidden) {
					l.add(o);
				}
			}
		}
		
		ButtonCache.cacheFrom(this.screen, this.width, this.height);
		
		this.content.clear();
		
		//Sync labels, textures, auto-clicks and other stuff for vanilla buttons
		for (ButtonData b : ButtonCache.getButtons()) {
			if (!this.containsVanillaButton(l, b)) {
				LayoutVanillaButton v = new LayoutVanillaButton(b, this);
				
				if (this.vanillaButtonNames.containsKey(b.getId())) {
					v.object.value = this.vanillaButtonNames.get(b.getId()); 
				}
				if (this.vanillaButtonClicks.containsKey(b.getId())) {
					v.clicks = LayoutEditorScreen.this.vanillaButtonClicks.get(b.getId()); 
				}
				if (this.vanillaDelayAppearance.containsKey(b.getId())) {
					v.object.delayAppearance = true;
					v.object.delayAppearanceSec = this.vanillaDelayAppearance.get(b.getId());
					if (this.vanillaDelayAppearanceFirstTime.containsKey(b.getId())) {
						v.object.delayAppearanceEverytime = !this.vanillaDelayAppearanceFirstTime.get(b.getId());
					}
					if (this.vanillaFadeIn.containsKey(b.getId())) {
						v.object.fadeIn = true;
						v.object.fadeInSpeed = this.vanillaFadeIn.get(b.getId());
					}
				}
				if (this.vanillaHoverLabels.containsKey(b.getId())) {
					v.hoverLabel = this.vanillaHoverLabels.get(b.getId()); 
				}
				if (this.vanillaHoverSounds.containsKey(b.getId())) {
					v.hoverSound = this.vanillaHoverSounds.get(b.getId()); 
				}
				if (this.vanillaClickSounds.containsKey(b.getId())) {
					v.clicksound = this.vanillaClickSounds.get(b.getId()); 
				}
				if (this.vanillaButtonTextures.containsKey(b.getId())) {
					List<String> l2 = this.vanillaButtonTextures.get(b.getId());
					v.backNormal = l2.get(0);
					v.backHovered = l2.get(1);
					((LayoutButtonDummyCustomizationItem)v.object).setTexture(TextureHandler.getResource(l2.get(0)).getResourceLocation());
				}
				if (this.vanillaDescriptions.containsKey(b.getId())) {
					v.description = this.vanillaDescriptions.get(b.getId()); 
				}

				l.add(v);
			}
		}
		this.content.addAll(l);

		this.vanillaButtonContent.clear();
		
		for (LayoutElement o : this.content) {
			
			o.init();
			
			if (o instanceof LayoutVanillaButton) {
				this.vanillaButtonContent.add(o);
			}
			
		}
	}
	
	protected boolean containsVanillaButton(List<LayoutElement> l, ButtonData b) {
		for (LayoutElement o : l) {
			if (o instanceof LayoutVanillaButton) {
				if (((LayoutVanillaButton)o).button.getId() == b.getId()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isHidden(LayoutElement b) {
		return this.hidden.contains(b);
	}

	protected void closeMultiselectChildMenus() {
		for (ContextMenu m : this.multiselectChilds) {
			m.closeMenu();
		}
	}
	
	public void setVanillaButtonName(LayoutVanillaButton button, String text) {
		if ((this.vanillaButtonNames.get(button.button.getId()) == null) || !(this.vanillaButtonNames.get(button.button.getId()).equals(text))) {
			this.history.saveSnapshot(this.history.createSnapshot());
		}
		
		this.vanillaButtonNames.put(button.button.getId(), text);
		button.object.value = text;
	}

	public void setVanillaTexture(LayoutVanillaButton button, String backNormal, String backHover) {
		if ((backNormal != null) && (backHover != null)) {
			List<String> l = new ArrayList<String>();
			l.add(backNormal);
			l.add(backHover);

			this.vanillaButtonTextures.put(button.button.getId(), l);
		} else {
			if (this.vanillaButtonTextures.containsKey(button.button.getId())) {
				this.vanillaButtonTextures.remove(button.button.getId());
			}
		}
	}

	public void setVanillaClicks(LayoutVanillaButton button, int clicks) {
		if (clicks > 0) {
			this.vanillaButtonClicks.put(button.button.getId(), clicks);
		} else {
			if (this.vanillaButtonClicks.containsKey(button.button.getId())) {
				this.vanillaButtonClicks.remove(button.button.getId());
			}
		}
	}

//	public void setVanillaHideFor(LayoutVanillaButton button, double seconds) {
//		this.vanillaHideFor.put(button.button.getId(), seconds);
//	}

	public void setVanillaDescription(LayoutVanillaButton button, String desc) {
		if (desc == null) {
			this.vanillaDescriptions.remove(button.button.getId());
		} else {
			this.vanillaDescriptions.put(button.button.getId(), desc);
		}
	}
	
//	public void setVanillaDelayOnlyFirstTime(LayoutVanillaButton button, boolean onlyfirsttime) {
//		this.vanillaDelayOnlyFirstTime.put(button.button.getId(), onlyfirsttime);
//	}

	public void setVanillaHoverLabel(LayoutVanillaButton button, String label) {
		if (label != null) {
			this.vanillaHoverLabels.put(button.button.getId(), label);
		} else {
			if (this.vanillaHoverLabels.containsKey(button.button.getId())) {
				this.vanillaHoverLabels.remove(button.button.getId());
			}
		}
	}

	public void setVanillaHoverSound(LayoutVanillaButton button, String sound) {
		if (sound != null) {
			this.vanillaHoverSounds.put(button.button.getId(), sound);
		} else {
			if (this.vanillaHoverSounds.containsKey(button.button.getId())) {
				this.vanillaHoverSounds.remove(button.button.getId());
			}
		}
	}

	public void setVanillaClickSound(LayoutVanillaButton button, String sound) {
		if (sound != null) {
			this.vanillaClickSounds.put(button.button.getId(), sound);
		} else {
			if (this.vanillaClickSounds.containsKey(button.button.getId())) {
				this.vanillaClickSounds.remove(button.button.getId());
			}
		}
	}
	
	public void hideVanillaButton(LayoutVanillaButton b) {
		if (!this.hidden.contains(b) && this.content.contains(b)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			this.hidden.add(b);
			b.hidden = true;
			this.setObjectFocused(b, false, true);
			b.resetObjectStates();
		}
	}
	
	public void showVanillaButton(LayoutVanillaButton b) {
		if (this.hidden.contains(b)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			this.hidden.remove(b);
			b.hidden = false;
			b.resetObjectStates();
		}
	}
	
	public void addContent(LayoutElement object) {
		if (!this.content.contains(object)) {
			this.content.add(object);
		}
	}

	public void removeContent(LayoutElement object) {
		if (this.content.contains(object)) {
			if ((this.isFocused(object))) {
				this.focusedObjects.remove(object);
			}
			this.content.remove(object);
			this.updateContent();
		}
		this.focusChangeBlocker.clear();
	}
	
	public List<LayoutElement> getContent() {
		return this.content;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		
		//Handle object focus and update the top hovered object
		if (!MouseInput.isVanillaInputBlocked()) {
			if (!KeyboardHandler.isCtrlPressed() && !this.focusedObjects.isEmpty() && !this.isFocusedHovered() && !this.isFocusedDragged() && !this.isFocusedGrabberPressed() && !this.isFocusedGettingResized() && (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown())) {
				if (((this.multiselectRightclickMenu == null) || !this.multiselectRightclickMenu.isHovered()) && ((this.propertiesRightclickMenu == null) || !this.propertiesRightclickMenu.isHovered()) && !this.isFocusChangeBlocked() && !this.ui.bar.isHovered() && !this.ui.bar.isChildOpen()) {
					this.focusedObjects.clear();
				}
			}
			LayoutElement ob = null;
			LayoutElement top = null;
			for (LayoutElement o : this.content) {
				if (o.isHovered()) {
					top = o;
					if (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown()) {
						ob = o;
					}
				}
			}
			if (((KeyboardHandler.isCtrlPressed() && !this.isFocused(ob)) || (!this.isObjectFocused()) && (ob != null))) {
				this.setObjectFocused(ob, true, false);
			}
			this.topObject = top;
		} else {
			if (!this.ui.bar.isHovered() && !this.ui.bar.isChildOpen()) {
				this.focusedObjects.clear();
			}
		}

		this.renderCreatorBackground();

		if (this.renderorder.equalsIgnoreCase("foreground")) {
			this.renderVanillaButtons(mouseX, mouseY);
		}
		//Renders all layout objects. The focused object is always rendered on top of all other objects.
		for (LayoutElement l : this.content) {
			if (!(l instanceof LayoutVanillaButton)) {
				if (!this.isHidden(l)) {
					if (!this.isFocused(l)) {
						l.render(mouseX, mouseY);
					}
				}
			}
		}
		if (this.renderorder.equalsIgnoreCase("background")) {
			this.renderVanillaButtons(mouseX, mouseY);
		}
		
		for (LayoutElement o : this.getFocusedObjects()) {
			o.render(mouseX, mouseY);
		}
		
		super.render(mouseX, mouseY, partialTicks);
		
		//Handle multiselect rightclick menu
		if (this.multiselectRightclickMenu != null) {

			if ((this.focusedObjects.size() > 1) && this.isFocusedHovered()) {
				if (MouseInput.isRightMouseDown()) {
					UIBase.openScaledContextMenuAtMouse(this.multiselectRightclickMenu);
				}
			}

			if (!PopupHandler.isPopupActive()) {
				UIBase.renderScaledContextMenu(this.multiselectRightclickMenu);
			} else {
				this.multiselectRightclickMenu.closeMenu();
			}

			if (MouseInput.isLeftMouseDown() && !this.multiselectRightclickMenu.isHovered()) {
				this.multiselectRightclickMenu.closeMenu();
			}

			if (this.multiselectRightclickMenu.isOpen()) {
				this.setFocusChangeBlocked("editor.context.multiselect", true);
			} else {
				this.setFocusChangeBlocked("editor.context.multiselect", false);
			}

		}

		//Handle properties context menu
		if (this.propertiesRightclickMenu != null) {

			if (!this.isContentHovered() && MouseInput.isRightMouseDown()) {
				UIBase.openScaledContextMenuAtMouse(this.propertiesRightclickMenu);
			}

			if (!PopupHandler.isPopupActive()) {
				UIBase.renderScaledContextMenu(this.propertiesRightclickMenu);
			} else {
				this.propertiesRightclickMenu.closeMenu();
			}

			if (MouseInput.isLeftMouseDown() && !this.propertiesRightclickMenu.isHovered()) {
				this.propertiesRightclickMenu.closeMenu();
			}

			if (this.propertiesRightclickMenu.isOpen()) {
				this.setFocusChangeBlocked("editor.context.properties", true);
			} else {
				this.setFocusChangeBlocked("editor.context.properties", false);
			}

		}
				
		//Render rightclick menus of all layout elements
		for (LayoutElement e : this.content) {
			if (e.rightclickMenu != null) {
				if (!PopupHandler.isPopupActive()) {
					UIBase.renderScaledContextMenu(e.rightclickMenu);
				}
			}
		}
		
		//Render the editor UI
		this.ui.render(this);

		//Needs to be done after other object render stuff to prevent ConcurrentModificationExceptions.
		if (this.newContentMove != null) {
			this.history.saveSnapshot(this.history.createSnapshot());
			this.content = this.newContentMove;
			this.newContentMove = null;
		}
		if (!this.newContentPaste.isEmpty()) {
			this.content.addAll(this.newContentPaste);
			this.newContentPaste.clear();
		}
		if (!this.deleteContentQueue.isEmpty()) {
			this.history.saveSnapshot(this.history.createSnapshot());
			for (LayoutElement e : this.deleteContentQueue) {
				if (e.isDestroyable()) {
					this.removeContent(e);
				}
			}
			this.deleteContentQueue.clear();
		}
		
		for (Runnable r : this.postRenderTasks) {
			r.run();
		}
		this.postRenderTasks.clear();

	}

	protected void renderVanillaButtons(int mouseX, int mouseY) {
		for (LayoutElement l : this.vanillaButtonContent) {
			if (!this.isHidden(l)) {
				if (!this.isFocused(l)) {
					l.render(mouseX, mouseY);
				}
			}
		}
	}

	protected void renderCreatorBackground() {
		RenderSystem.enableBlend();
		fill(0, 0, this.width, this.height, new Color(38, 38, 38).getRGB());

		if (this.backgroundTexture != null) {
			Minecraft.getInstance().getTextureManager().bindTexture(this.backgroundTexture.getResourceLocation());
			
			if (!this.panorama) {
				blit(0, 0, 1.0F, 1.0F, this.width + 1, this.height + 1, this.width + 1, this.height + 1);
			} else {
				int w = this.backgroundTexture.getWidth();
				int h = this.backgroundTexture.getHeight();
				double ratio = (double) w / (double) h;
				int wfinal = (int)(this.height * ratio);

				//Check if the panorama background should move to the left side or to the right side
				if ((panoPos + (wfinal - this.width)) <= 0) {
					panoMoveBack = true;
				}
				if (panoPos >= 0) {
					panoMoveBack = false;
				}

				//Fix pos after resizing
				if (panoPos + (wfinal - this.width) < 0) {
					panoPos = 0 - (wfinal - this.width);
				}
				if (panoPos > 0) {
					panoPos = 0;
				}
				
				if (!panoStop) {
					if (panoTick >= 1) {
						panoTick = 0;
						if (panoMoveBack) {
							panoPos = panoPos + 0.5;
						} else {
							panoPos = panoPos - 0.5;
						}
						
						if (panoPos + (wfinal - this.width) == 0) {
							panoStop = true;
						}
						if (panoPos == 0) {
							panoStop = true;
						}
					} else {
						panoTick++;
					}
				} else {
					if (panoTick >= 300) {
						panoStop = false;
						panoTick = 0;
					} else {
						panoTick++;
					}
				}
				if (wfinal <= this.width) {
					blit(0, 0, 1.0F, 1.0F, this.width + 1, this.height + 1, this.width + 1, this.height + 1);
				} else {
					RenderUtils.doubleBlit(panoPos, 0, 1.0F, 1.0F, wfinal, this.height + 1);
				}
			}
		}
		RenderSystem.disableBlend();
		
		if (this.backgroundAnimation != null) {
			boolean b = this.backgroundAnimation.isStretchedToStreensize();
			this.backgroundAnimation.setStretchImageToScreensize(true);
			this.backgroundAnimation.render();
			this.backgroundAnimation.setStretchImageToScreensize(b);
		}

		if (this.backgroundPanorama != null) {
			this.backgroundPanorama.render();
		}

		if (this.backgroundSlideshow != null) {
			int sw = this.backgroundSlideshow.width;
			int sh = this.backgroundSlideshow.height;
			int sx = this.backgroundSlideshow.x;
			int sy = this.backgroundSlideshow.y;
			
			this.backgroundSlideshow.height = this.height;
			this.backgroundSlideshow.width = this.width;
			this.backgroundSlideshow.x = 0;
			this.backgroundSlideshow.y = 0;
			
			this.backgroundSlideshow.render();
			
			this.backgroundSlideshow.width = sw;
			this.backgroundSlideshow.height = sh;
			this.backgroundSlideshow.x = sx;
			this.backgroundSlideshow.y = sy;
		}
	}

	public boolean isFocused(LayoutElement object) {
		if (PopupHandler.isPopupActive()) {
			return false;
		}
		return (this.focusedObjects.contains(object));
	}

	public void setObjectFocused(LayoutElement object, boolean focused, boolean ignoreBlockedFocusChange) {
		if (this.isFocusChangeBlocked() && !ignoreBlockedFocusChange) {
			return;
		}
		if (!this.content.contains(object)) {
			return;
		}
		if (focused) {
			if (!this.focusedObjects.contains(object)) {
				this.focusedObjects.add(object);
			}
		} else {
			if (this.focusedObjects.contains(object)) {
				this.focusedObjects.remove(object);
			}
		}
	}

	public boolean isObjectFocused() {
		return (!this.focusedObjects.isEmpty());
	}

	public boolean isFocusedHovered() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isHovered()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedDragged() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isDragged()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedGrabberPressed() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isGrabberPressed()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedGettingResized() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isGettingResized()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a copy of the focused objects list.
	 */
	public List<LayoutElement> getFocusedObjects() {
		List<LayoutElement> l = new ArrayList<LayoutElement>();
		l.addAll(this.focusedObjects);
		return l;
	}

	public void clearFocusedObjects() {
		if (this.multiselectRightclickMenu != null) {
			this.multiselectRightclickMenu.closeMenu();
		}
		this.focusedObjects.clear();
	}
	
	public boolean isContentHovered() {
		for (LayoutElement o : this.content) {
			if (o.isHovered()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the LayoutObject the given object was moved above.
	 */
	public LayoutElement moveUp(LayoutElement o) {
		LayoutElement movedAbove = null;
		try {
			if (this.content.contains(o)) {
				List<LayoutElement> l = new ArrayList<LayoutElement>();
				int index = this.content.indexOf(o);
				int i = 0;
				if (index < this.content.size() - 1) {
					for (LayoutElement o2 : this.content) {
						if (o2 != o) {
							l.add(o2);
							if (i == index+1) {
								movedAbove = o2;
								l.add(o);
							}
						}
						i++;
					}
					
					this.newContentMove = l;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movedAbove;
	}

	/**
	 * Returns the LayoutObject the object was moved behind.<br>
	 * Will <b>NOT</b> move behind {@link LayoutVanillaButton}s, but will return the vanilla button the object would have been moved under.
	 */
	public LayoutElement moveDown(LayoutElement o) {
		LayoutElement movedBehind = null;
		try {
			if (this.content.contains(o)) {
				List<LayoutElement> l = new ArrayList<LayoutElement>();
				int index = this.content.indexOf(o);
				int i = 0;
				if (index > 0) {
					for (LayoutElement o2 : this.content) {
						if (o2 != o) {
							if (i == index-1) {
								l.add(o);
								movedBehind = o2;
							}
							l.add(o2);
						}
						i++;
					}
					
					if (!(movedBehind instanceof LayoutVanillaButton)) {
						this.newContentMove = l;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movedBehind;
	}
	
	protected void setButtonTexturesForFocusedObjects(boolean hover) {
		ChooseFilePopup cf = new ChooseFilePopup((call) -> {
			if (call != null) {
				File home = new File("");
				call = call.replace("\\", "/");
				File f = new File(call);
				String filename = CharacterFilter.getBasicFilenameCharacterFilter().filterForAllowedChars(f.getName());
				if (f.exists() && f.isFile() && (f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".png"))) {
					if (filename.equals(f.getName())) {
						if (call.startsWith(home.getAbsolutePath())) {
							call = call.replace(home.getAbsolutePath(), "");
							if (call.startsWith("\\") || call.startsWith("/")) {
								call = call.substring(1);
							}
						}

						this.history.saveSnapshot(this.history.createSnapshot());
						this.history.setPreventSnapshotSaving(true);
						
						for (LayoutElement o : this.focusedObjectsCache) {
							if (o instanceof LayoutVanillaButton) {
								
								LayoutVanillaButton vb = (LayoutVanillaButton) o;
								
								if (hover) {
									vb.backHovered = call;
									if (vb.backNormal == null) {
										vb.backNormal = call;
									}
								} else {
									vb.backNormal = call;
									if (vb.backHovered == null) {
										vb.backHovered = call;
									}
								}
								
								this.setVanillaTexture(vb, vb.backNormal, vb.backHovered);
								
								((LayoutButtonDummyCustomizationItem)vb.object).setTexture(TextureHandler.getResource(vb.backNormal).getResourceLocation());
								
							} else if (o instanceof LayoutButton) {
								
								LayoutButton lb = (LayoutButton) o;
								
								if (hover) {
									lb.backHovered = call;
									if (lb.backNormal == null) {
										lb.backNormal = call;
									}
								} else {
									lb.backNormal = call;
									if (lb.backHovered == null) {
										lb.backHovered = call;
									}
								}
								
								((LayoutButtonDummyCustomizationItem)lb.object).setTexture(TextureHandler.getResource(lb.backNormal).getResourceLocation());
								
							}
						}
						
						this.history.setPreventSnapshotSaving(false);
						
					} else {
						displayNotification(Locals.localize("helper.creator.textures.invalidcharacters"), "", "", "", "", "", "");
					}
				} else {
					displayNotification("§c§l" + Locals.localize("helper.creator.invalidimage.title"), "", Locals.localize("helper.creator.invalidimage.desc"), "", "", "", "", "", "");
				}
			}
		}, "jpg", "jpeg", "png");
		
		PopupHandler.displayPopup(cf);
	}

	protected void addTexture(String path) {
		File home = new File("");
		if (path == null) {
			return;
		}
		if (path.startsWith(home.getAbsolutePath())) {
			path = path.replace(home.getAbsolutePath(), "");
			if (path.startsWith("\\") || path.startsWith("/")) {
				path = path.substring(1);
			}
		}
		File f = new File(path);
		String filename = CharacterFilter.getBasicFilenameCharacterFilter().filterForAllowedChars(f.getName());
		if (f.exists()) {
			if (filename.equals(f.getName())) {
				this.history.saveSnapshot(this.history.createSnapshot());
				
				PropertiesSection sec = new PropertiesSection("customization");
				sec.addEntry("action", "addtexture");
				sec.addEntry("path", path);
				sec.addEntry("height", "100");
				sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
				
				TextureCustomizationItem i = new TextureCustomizationItem(sec);
				this.addContent(new LayoutTexture(i, this));

			} else {
				displayNotification(Locals.localize("helper.creator.textures.invalidcharacters"), "", "", "", "", "", "");
			}
		} else {
			displayNotification("§c§l" + Locals.localize("helper.creator.invalidimage.title"), "", Locals.localize("helper.creator.invalidimage.desc"), "", "", "", "", "", "");
		}
	}

	protected void addWebTexture(String url) {
		String finalUrl = null;
		if (url != null) {
			url = WebUtils.filterURL(url);
			finalUrl = DynamicValueHelper.convertFromRaw(url);
		}
		if (WebUtils.isValidUrl(finalUrl)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addwebtexture");
			s.addEntry("url", url);
			s.addEntry("height", "100");
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			this.addContent(new LayoutWebTexture(new WebTextureCustomizationItem(s), this));
		} else {
			displayNotification(Locals.localize("helper.creator.web.invalidurl"), "", "", "", "", "", "");
		}
	}

	protected void addSlideshow(String name) {
		if (name == null) {
			return;
		}
		if (SlideshowHandler.slideshowExists(name)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addslideshow");
			s.addEntry("name", name);
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			SlideshowCustomizationItem i = new SlideshowCustomizationItem(s);
			int w = SlideshowHandler.getSlideshow(name).width;
			int h = SlideshowHandler.getSlideshow(name).height;
			double ratio = (double) w / (double) h;
			i.height = 100;
			i.width = (int)(i.height * ratio);
			
			this.addContent(new LayoutSlideshow(i, this));

		} else {
			displayNotification(Locals.localize("helper.creator.slideshownotfound"), "", "", "", "");
		}
	}
	
	protected void addShape(Shape shape) {
		PropertiesSection s = new PropertiesSection("customization");
		s.addEntry("action", "addshape");
		s.addEntry("shape", shape.name);
		s.addEntry("width", "100");
		s.addEntry("height", "100");
		s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
		this.history.saveSnapshot(this.history.createSnapshot());
		this.addContent(new LayoutShape(new ShapeCustomizationItem(s), this));
	}
	
	protected void addMultiSplashText(String path) {
		File home = new File("");
		if (path == null) {
			return;
		}
		if (path.startsWith(home.getAbsolutePath())) {
			path = path.replace(home.getAbsolutePath(), "");
			if (path.startsWith("\\") || path.startsWith("/")) {
				path = path.substring(1);
			}
		}
		File f = new File(path);
		if (f.exists() && f.getPath().toLowerCase().endsWith(".txt")) {
			
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection sec = new PropertiesSection("customization");
			sec.addEntry("action", "addsplash");
			sec.addEntry("splashfilepath", path);
			sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			
			SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);
			this.addContent(new LayoutSplashText(i, this));
			
		} else {
			displayNotification(Locals.localize("helper.creator.error.invalidfile"));
		}
	}
	
	protected void addSingleSplashText(String content) {
		if (content == null) {
			return;
		}
		if (!content.replace(" ", "").equals("")) {
			
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection sec = new PropertiesSection("customization");
			sec.addEntry("action", "addsplash");
			sec.addEntry("text", content);
			sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			
			SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);
			this.addContent(new LayoutSplashText(i, this));
			
		} else {
			displayNotification("§c§l" + Locals.localize("helper.creator.texttooshort.title"), "", Locals.localize("helper.creator.texttooshort.desc"), "", "", "", "");
		}
	}
	
	protected void addPlayerEntity() {
		PropertiesSection s = new PropertiesSection("customization");
		s.addEntry("action", "addentity");
		LayoutPlayerEntity e = new LayoutPlayerEntity(new PlayerEntityCustomizationItem(s), this);
		e.setX(e.getWidth());
		e.setY(e.getHeight());
		this.history.saveSnapshot(this.history.createSnapshot());
		this.addContent(e);
	}
	
	protected void addAnimation(String name) {
		if (name == null) {
			return;
		}
		if (AnimationHandler.animationExists(name)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addanimation");
			s.addEntry("name", name);
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			AnimationCustomizationItem i = new AnimationCustomizationItem(s);
			int w = AnimationHandler.getAnimation(name).getWidth();
			int h = AnimationHandler.getAnimation(name).getHeight();
			double ratio = (double) w / (double) h;
			i.height = 100;
			i.width = (int)(i.height * ratio);
			AnimationHandler.getAnimation(name).resetAnimation();
			this.addContent(new LayoutAnimation(i, this));

		} else {
			displayNotification("§c§l" + Locals.localize("helper.creator.animationnotfound.title"), "", Locals.localize("helper.creator.animationnotfound.desc"), "", "", "");
		}
	}
	
	protected void addButton(String label) {
		if (label == null) {
			return;
		}
		this.history.saveSnapshot(this.history.createSnapshot());

		int w = 100;
		if (Minecraft.getInstance().fontRenderer.getStringWidth(label) + 10 > w) {
			w = Minecraft.getInstance().fontRenderer.getStringWidth(label) + 10;
		}
		LayoutButton b = new LayoutButton(w, 20, label, null, this);
		b.object.posY = (int)(this.ui.bar.getHeight() * UIBase.getUIScale());
		this.addContent(b);
	}
	
	protected void addWebText(String url) {
		String finalUrl = null;
		if (url != null) {
			url = WebUtils.filterURL(url);
			finalUrl = DynamicValueHelper.convertFromRaw(url);
		}
		if (WebUtils.isValidUrl(finalUrl)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addwebtext");
			s.addEntry("url", url);
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			this.addContent(new LayoutWebString(new WebStringCustomizationItem(s), this));
		} else {
			displayNotification(Locals.localize("helper.creator.web.invalidurl"), "", "", "", "", "", "");
		}
	}
	
	protected void addText(String text) {
		if (text == null) {
			return;
		}
		if (text.length() > 0) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addtext");
			s.addEntry("value", StringUtils.convertFormatCodes(text, "&", "§"));
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			StringCustomizationItem i = new StringCustomizationItem(s);
			this.addContent(new LayoutString(i, this));
		} else {
			displayNotification("§c§l" + Locals.localize("helper.creator.texttooshort.title"), "", Locals.localize("helper.creator.texttooshort.desc"), "", "", "", "");
		}
	}
	
	protected void addAudio(String path) {
		if (path != null) {
			File home = new File("");
			if (path.startsWith(home.getAbsolutePath())) {
				path = path.replace(home.getAbsolutePath(), "");
				if (path.startsWith("\\") || path.startsWith("/")) {
					path = path.substring(1);
				}
			}
			
			File f = new File(path);
			if (f.exists() && f.isFile() && f.getName().endsWith(".wav")) {
				if (!this.audio.containsKey(path)) {
					this.history.saveSnapshot(this.history.createSnapshot());

					MenuCustomization.registerSound(path, path);
					SoundHandler.playSound(path);
					this.audio.put(path, false);
				} else {
					displayNotification("§c§l" + Locals.localize("helper.creator.audioalreadyloaded.title"), "", Locals.localize("helper.creator.audioalreadyloaded.desc"), "", "", "", "", "", "");
				}
				
			} else {
				displayNotification("§c§l" + Locals.localize("helper.creator.invalidaudio.title"), "", Locals.localize("helper.creator.invalidaudio.desc"), "", "", "", "", "", "");
			}
		}
	}
	
	public void setBackgroundAnimations(String... names) {
		if ((names != null) && (names.length > 0)) {
			for (String s : names) {
				if (AnimationHandler.animationExists(s) && !this.backgroundAnimationNames.contains(s)) {
					this.backgroundAnimationNames.add(s);
				}
			}
			if (!this.backgroundAnimationNames.isEmpty()) {
				this.history.saveSnapshot(this.history.createSnapshot());

				this.backgroundPanorama = null;
				this.backgroundSlideshow = null;
				this.backgroundTexture = null;
				if (this.backgroundAnimation != null) {
					((AdvancedAnimation)this.backgroundAnimation).stopAudio();
				}
				this.backgroundAnimation = AnimationHandler.getAnimation(this.backgroundAnimationNames.get(0));
				this.backgroundAnimation.resetAnimation();
			}
		}
		if (names == null) {
			if (this.backgroundAnimation != null) {
				this.history.saveSnapshot(this.history.createSnapshot());
				
				((AdvancedAnimation)this.backgroundAnimation).stopAudio();
			}
			this.backgroundAnimation = null;
			this.backgroundAnimationNames.clear();
		}
	}
	
	public void setBackgroundTexture(String path) {
		if (path != null) {
			File home = new File("");
			if (path.startsWith(home.getAbsolutePath())) {
				path = path.replace(home.getAbsolutePath(), "");
				if (path.startsWith("\\") || path.startsWith("/")) {
					path = path.substring(1);
				}
			}
			
			File f = new File(path);
			String filename = CharacterFilter.getBasicFilenameCharacterFilter().filterForAllowedChars(f.getName());
			if (f.exists() && f.isFile() && (f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg") || f.getName().toLowerCase().endsWith(".png"))) {
				if (filename.equals(f.getName())) {
					this.history.saveSnapshot(this.history.createSnapshot());
					
					this.backgroundTexture = TextureHandler.getResource(path);
					this.backgroundTexturePath = path;
					if (this.backgroundAnimation != null) {
						((AdvancedAnimation)this.backgroundAnimation).stopAudio();
					}
					this.backgroundAnimation = null;
					this.backgroundAnimationNames.clear();

					this.backgroundPanorama = null;
					this.backgroundSlideshow = null;

				} else {
					displayNotification(Locals.localize("helper.creator.textures.invalidcharacters"), "", "", "", "", "", "");
				}
			} else {
				displayNotification("§c§l" + Locals.localize("helper.creator.invalidimage.title"), "", Locals.localize("helper.creator.invalidimage.desc"), "", "", "", "", "", "");
			}
		}
	}

	protected void deleteFocusedObjects() {
		List<LayoutElement> l = new ArrayList<LayoutElement>();
		l.addAll(this.focusedObjects);
		
		if (!l.isEmpty()) {
			if (l.size() == 1) {
				if (l.get(0).isDestroyable()) {
					l.get(0).destroyObject();
				} else {
					displayNotification("§c§l" + Locals.localize("helper.creator.cannotdelete.title"), "", Locals.localize("helper.creator.cannotdelete.desc"), "", "", "");
				}
			}
			if (l.size() > 1) {
				if (FancyMenu.config.getOrDefault("editordeleteconfirmation", true)) {
					PopupHandler.displayPopup(new FMYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
						if (call) {
							this.deleteContentQueue.addAll(l);
						}
					}, "§c§l" + Locals.localize("helper.creator.messages.sure"), "", Locals.localize("helper.creator.deleteselectedobjects"), "", "", "", "", ""));
				} else {
					this.deleteContentQueue.addAll(l);
				}
			}
		}
	}
	
	public Screen getScreenToCustomize() {
		return this.screen;
	}

	public String getScreenToCustomizeIdentifier() {
		if (!(this.screen instanceof CustomGuiBase)) {
			return this.screen.getClass().getName();
		} else {
			return ((CustomGuiBase)this.screen).getIdentifier();
		}
	}

	public void setFocusChangeBlocked(String id, Boolean b) {
		this.focusChangeBlocker.put(id, b);
	}

	public boolean isFocusChangeBlocked() {
		return this.focusChangeBlocker.containsValue(true);
	}
	
	public LayoutElement getTopHoverObject() {
		return this.topObject;
	}

	public void saveLayout() {

		if ((this instanceof PreloadedLayoutEditorScreen) && (((PreloadedLayoutEditorScreen)this).single != null)) {

			if (!CustomizationHelper.saveLayoutTo(this.getAllProperties(), ((PreloadedLayoutEditorScreen)this).single)) {
				this.saveLayoutAs();
			} else {
				Snapshot snap = this.history.createSnapshot();

				List<PropertiesSet> l = new ArrayList<PropertiesSet>();
				l.add(snap.snapshot);

				PreloadedLayoutEditorScreen neweditor = new PreloadedLayoutEditorScreen(this.screen, l);
				neweditor.history = this.history;
				this.history.editor = neweditor;
				neweditor.single = ((PreloadedLayoutEditorScreen)this).single;

				Minecraft.getInstance().displayGuiScreen(neweditor);
			}

		} else {
			this.saveLayoutAs();
		}

	}

	public void saveLayoutAs() {
		PopupHandler.displayPopup(new FMTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("helper.editor.ui.layout.saveas.entername"), null, 240, (call) -> {
			try {

				if ((call != null) && (call.length() > 0)) {

					String file = FancyMenu.getCustomizationPath().getPath() + "/" + call + ".txt";
					File f = new File(file);

					if (!f.exists()) {
						if (!CustomizationHelper.saveLayoutTo(this.getAllProperties(), file)) {
							PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.editor.ui.layout.saveas.failed")));
						} else {
							Snapshot snap = this.history.createSnapshot();

							List<PropertiesSet> l = new ArrayList<PropertiesSet>();
							l.add(snap.snapshot);

							PreloadedLayoutEditorScreen neweditor = new PreloadedLayoutEditorScreen(this.screen, l);
							neweditor.history = this.history;
							this.history.editor = neweditor;
							neweditor.single = file;

							Minecraft.getInstance().displayGuiScreen(neweditor);
						}
					} else {
						PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.editor.ui.layout.saveas.failed")));
					}
					
				} else {
					PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.editor.ui.layout.saveas.failed")));
				}

			} catch (Exception e) {
				e.printStackTrace();
				PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("helper.editor.ui.layout.saveas.failed")));
			}
		}));
	}

	public void copySelectedElements() {
		List<LayoutElement> l = this.getFocusedObjects();

		LayoutEditorScreen.COPIED_ELEMENT_CACHE.clear();
		for (LayoutElement e : l) {
			LayoutEditorScreen.COPIED_ELEMENT_CACHE.addAll(e.getProperties());
		}
	}

	public void pasteElements() {
		if (!LayoutEditorScreen.COPIED_ELEMENT_CACHE.isEmpty()) {

			PropertiesSet set = new PropertiesSet("menu");
			for (PropertiesSection s : LayoutEditorScreen.COPIED_ELEMENT_CACHE) {
				set.addProperties(s);
			}

			List<PropertiesSet> l = new ArrayList<PropertiesSet>();
			l.add(set);

			//Init dummy preloaded editor to use it's customization action serializer for building the copied elements
			PreloadedLayoutEditorScreen pe = new PreloadedLayoutEditorScreen(new CustomGuiBase("", "", false, null, null), l);

			for (LayoutElement e : pe.content) {
				e.handler = this;
				//Change the element position a bit to better see that the element was successfully pasted
				e.object.posX += 1;
			}
			this.history.saveSnapshot(this.history.createSnapshot());
			this.newContentPaste.addAll(pe.content);
			
			this.postRenderTasks.add(new Runnable() {
				@Override
				public void run() {
					LayoutEditorScreen.this.focusedObjects.clear();
					LayoutEditorScreen.this.focusedObjectsCache.clear();
					LayoutEditorScreen.this.focusedObjects.addAll(pe.content);
				}
			});

		}
	}

	protected static void onShortcutPressed(KeyboardData d) {
		Screen c = Minecraft.getInstance().currentScreen;
		
		if (c instanceof LayoutEditorScreen) {
			
			//CTRL + C
			if (d.keycode == 67) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).copySelectedElements();
					}
				}
			}
			
			//CTRL + V
			if (d.keycode == 86) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).pasteElements();
					}
				}
			}
			
			//CTRL + S
			if (d.keycode == 83) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).saveLayout();
					}
				}
			}
			
			//CTRL + Z
			if (d.keycode == 89) {
				if (KeyboardHandler.isCtrlPressed()) {
					((LayoutEditorScreen) c).history.stepBack();
				}
			}
			
			//CTRL + Y
			if (d.keycode == 90) {
				if (KeyboardHandler.isCtrlPressed()) {
					((LayoutEditorScreen) c).history.stepForward();
				}
			}
			
			//DEL
			if (((LayoutEditorScreen)c).isObjectFocused() && !PopupHandler.isPopupActive()) {
				if (d.keycode == 261) {
					((LayoutEditorScreen) c).deleteFocusedObjects();
				}
			}
			
		}
		
	}
	
	protected static void onArrowKeysPressed(KeyboardData d) {
		Screen c = Minecraft.getInstance().currentScreen;
		
		if (c instanceof LayoutEditorScreen) {
			
			if (((LayoutEditorScreen) c).isObjectFocused() && !PopupHandler.isPopupActive()) {
				
				if (!((d.keycode == 263) || (d.keycode == 262) || (d.keycode == 265) || (d.keycode == 264))) {
					return;
				}
				
				Snapshot snap = ((LayoutEditorScreen) c).history.createSnapshot();
				boolean saveSnap = false;
				
				for (LayoutElement o : ((LayoutEditorScreen) c).focusedObjects) {
					if ((o instanceof LayoutVanillaButton) && o.object.orientation.equals("original")) {
						((LayoutVanillaButton)o).displaySetOrientationNotification();
						continue;
					}
					if (d.keycode == 263) {
						saveSnap = true;
						
						o.setX(o.object.posX - 1);
					}
					if (d.keycode == 262) {
						saveSnap = true;
						
						o.setX(o.object.posX + 1);
					}
					if (d.keycode == 265) {
						saveSnap = true;
						
						o.setY(o.object.posY - 1);
					}
					if (d.keycode == 264) {
						saveSnap = true;
						
						o.setY(o.object.posY + 1);
					}
				}
				
				if (saveSnap) {
					((LayoutEditorScreen) c).history.saveSnapshot(snap);
				}
				
			}
			
		}
	}
	
	public static void displayNotification(String... strings) {
		PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, strings));
	}

}
