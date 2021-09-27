package de.keksuccino.fancymenu.menu.fancy.item;

import java.io.IOException;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.fancymenu.menu.fancy.DynamicValueHelper;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.item.visibilityrequirements.VisibilityRequirementContainer;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;

public abstract class CustomizationItemBase extends AbstractGui {
	
	/**
	 * This value CANNOT BE NULL!<br>
	 * If null, {@link CustomizationItemBase#shouldRender()} will never return true.
	 */
	public String value;
	public String action;
	/**
	 * NOT similar to {@link CustomizationItemBase#getPosX(Screen)}! This is the raw value without the defined orientation and scale!
	 */
	public int posX = 0;
	/**
	 * NOT similar to {@link CustomizationItemBase#getPosY(Screen)}! This is the raw value without the defined orientation and scale!
	 */
	public int posY = 0;
	public String orientation = "top-left";
	public int width = -1;
	public int height = -1;

	public volatile boolean delayAppearance = false;
	public volatile boolean delayAppearanceEverytime = false;
	public volatile float delayAppearanceSec = 1.0F;
	public volatile boolean visible = true;
	public volatile boolean fadeIn = false;
	public volatile float fadeInSpeed = 1.0F;
	public volatile float opacity = 1.0F;

	//TODO übernehmen
	public VisibilityRequirementContainer visibilityRequirementContainer;

	protected String actionId;
	
	public CustomizationItemBase(PropertiesSection item) {
		
		this.action = item.getEntryValue("action");

		this.actionId = item.getEntryValue("actionid");
		if (this.actionId == null) {
			this.actionId = MenuCustomization.generateRandomActionId();
		}

		String fi = item.getEntryValue("fadein");
		if ((fi != null) && fi.equalsIgnoreCase("true")) {
			this.fadeIn = true;
		}
		String fis = item.getEntryValue("fadeinspeed");
		if ((fis != null) && MathUtils.isFloat(fis)) {
			this.fadeInSpeed = Float.parseFloat(fis);
		}
		String da = item.getEntryValue("delayappearance");
		if ((da != null) && da.equalsIgnoreCase("true")) {
			this.delayAppearance = true;
		}
		String legacyDa = item.getEntryValue("hideforseconds");
		if (legacyDa != null) {
			this.delayAppearance = true;
		}
		String dae = item.getEntryValue("delayappearanceeverytime");
		if ((dae != null) && dae.equalsIgnoreCase("true")) {
			this.delayAppearanceEverytime = true;
		}
		String legacyDae = item.getEntryValue("delayonlyfirsttime");
		if ((legacyDae != null) && legacyDae.equalsIgnoreCase("false")) {
			this.delayAppearanceEverytime = true;
		}
		String das = item.getEntryValue("delayappearanceseconds");
		if ((das != null) && MathUtils.isFloat(das)) {
			this.delayAppearanceSec = Float.parseFloat(das);
		}
		if ((legacyDa != null) && MathUtils.isFloat(legacyDa)) {
			this.delayAppearanceSec = Float.parseFloat(legacyDa);
		}

		String x = item.getEntryValue("x");
		String y = item.getEntryValue("y");
		if (x != null) {
			x = DynamicValueHelper.convertFromRaw(x);
			if (MathUtils.isInteger(x)) {
				this.posX = Integer.parseInt(x);
			}
		}
		if (y != null) {
			y = DynamicValueHelper.convertFromRaw(y);
			if (MathUtils.isInteger(y)) {
				this.posY = Integer.parseInt(y);
			}
		}
	
		String o = item.getEntryValue("orientation");
		if (o != null) {
			this.orientation = o;
		}

		String w = item.getEntryValue("width");
		if (w != null) {
			w = DynamicValueHelper.convertFromRaw(w);
			if (MathUtils.isInteger(w)) {
				this.width = Integer.parseInt(w);
			}
			if (this.width < 0) {
				this.width = 0;
			}
		}

		String h = item.getEntryValue("height");
		if (h != null) {
			h = DynamicValueHelper.convertFromRaw(h);
			if (MathUtils.isInteger(h)) {
				this.height = Integer.parseInt(h);
			}
			if (this.height < 0) {
				this.height = 0;
			}
		}

		//TODO übernehmen
		this.visibilityRequirementContainer = new VisibilityRequirementContainer(item, this);

	}

	public abstract void render(MatrixStack matrix, Screen menu) throws IOException;
	
	/**
	 * Should be used to get the REAL and final X-position of this item.<br>
	 * NOT similar to {@code MenuCustomizationItem.posX}! 
	 */
	public int getPosX(Screen menu) {
		int w = menu.width;
		int x = this.posX;

		if (orientation.equalsIgnoreCase("top-centered")) {
			x += (w / 2);
		}
		if (orientation.equalsIgnoreCase("mid-centered")) {
			x += (w / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-centered")) {
			x += (w / 2);
		}
		//-----------------------------
		if (orientation.equalsIgnoreCase("top-right")) {
			x += w;
		}
		if (orientation.equalsIgnoreCase("mid-right")) {
			x += w;
		}
		if (orientation.equalsIgnoreCase("bottom-right")) {
			x += w;
		}
		
		return x;
	}
	
	/**
	 * Should be used to get the REAL and final Y-position of this item.<br>
	 * NOT similar to {@code MenuCustomizationItem.posY}! 
	 */
	public int getPosY(Screen menu) {
		int h = menu.height;
		int y = this.posY;

		if (orientation.equalsIgnoreCase("mid-left")) {
			y += (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-left")) {
			y += h;
		}
		//----------------------------
		if (orientation.equalsIgnoreCase("mid-centered")) {
			y += (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-centered")) {
			y += h;
		}
		//-----------------------------
		if (orientation.equalsIgnoreCase("top-right")) {
		}
		if (orientation.equalsIgnoreCase("mid-right")) {
			y += (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-right")) {
			y += h;
		}
		
		return y;
	}
	
	public boolean shouldRender() {
		if (this.value == null) {
			return false;
		}
		//TODO übernehmen
		if (!this.visibilityRequirementsMet()) {
			return false;
		}
		return this.visible;
	}

	public String getActionId() {
		return this.actionId;
	}

	//TODO übernehmen
	public void setActionId(String id) {
		this.actionId = id;
	}

	//TODO übernehmen
	protected static boolean isEditorActive() {
		return (Minecraft.getInstance().currentScreen instanceof LayoutEditorScreen);
	}

	//TODO übernehmen
	protected boolean visibilityRequirementsMet() {
		if (isEditorActive()) {
			return true;
		}
		return this.visibilityRequirementContainer.isVisible();
	}

	//TODO übernehmen
	public int getWidth() {
		return this.width;
	}

	//TODO übernehmen
	public void setWidth(int width) {
		this.width = width;
	}

	//TODO übernehmen
	public int getHeight() {
		return this.height;
	}

	//TODO übernehmen
	public void setHeight(int height) {
		this.height = height;
	}

	public static enum Alignment {
		
		LEFT("left"),
		RIGHT("right"),
		CENTERED("centered");
		
		public final String key;
		
		private Alignment(String key) {
			this.key = key;
		}
		
	}

}