package de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMPopup;
import de.keksuccino.fancymenu.menu.panorama.PanoramaHandler;
import de.keksuccino.fancymenu.menu.slideshow.SlideshowHandler;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.HorizontalSwitcher;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.input.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class BackgroundOptionsPopup extends FMPopup {
	
	private LayoutEditorScreen handler;
	
	protected AdvancedButton doneButton;
	
	protected AdvancedButton chooseTextureButton;

	//TODO übernehmen
//	protected AdvancedButton panoramaButton;
//	protected AdvancedButton noPanoramaButton;

	protected AdvancedButton setPanoramaButton;
	protected AdvancedButton setSlideshowButton;
	protected AdvancedButton clearPanoramaButton;
	protected AdvancedButton clearSlideshowButton;
	protected AdvancedButton setAnimationButton;
	protected AdvancedButton clearAnimationButton;
	//TODO übernehmen
	protected AdvancedButton clearImageButton;
	
	protected HorizontalSwitcher typeSwitcher;
	protected HorizontalSwitcher animationSwitcher;
	protected HorizontalSwitcher panoramaSwitcher;
	protected HorizontalSwitcher slideshowSwitcher;
	
	public BackgroundOptionsPopup(LayoutEditorScreen handler) {
		super(240);
		
		this.handler = handler;

		this.typeSwitcher = new HorizontalSwitcher(120, true,
				Locals.localize("helper.creator.backgroundoptions.backgroundanimation"),
				Locals.localize("helper.creator.backgroundoptions.backgroundimage"),
				Locals.localize("helper.creator.backgroundoptions.backgroundpanorama"),
				Locals.localize("helper.creator.backgroundoptions.backgroundslideshow"));
		this.typeSwitcher.setButtonColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), UIBase.getButtonBorderIdleColor(), UIBase.getButtonBorderHoverColor(), 1);
		this.typeSwitcher.setValueBackgroundColor(UIBase.getButtonIdleColor());
		
		this.animationSwitcher = new HorizontalSwitcher(120, true);
		for (String s : AnimationHandler.getCustomAnimationNames()) {
			this.animationSwitcher.addValue(s);
		}
		this.animationSwitcher.setButtonColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), UIBase.getButtonBorderIdleColor(), UIBase.getButtonBorderHoverColor(), 1);
		this.animationSwitcher.setValueBackgroundColor(UIBase.getButtonIdleColor());

		this.panoramaSwitcher = new HorizontalSwitcher(120, true);
		for (String s : PanoramaHandler.getPanoramaNames()) {
			this.panoramaSwitcher.addValue(s);
		}
		this.panoramaSwitcher.setButtonColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), UIBase.getButtonBorderIdleColor(), UIBase.getButtonBorderHoverColor(), 1);
		this.panoramaSwitcher.setValueBackgroundColor(UIBase.getButtonIdleColor());

		this.slideshowSwitcher = new HorizontalSwitcher(120, true);
		for (String s : SlideshowHandler.getSlideshowNames()) {
			this.slideshowSwitcher.addValue(s);
		}
		this.slideshowSwitcher.setButtonColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), UIBase.getButtonBorderIdleColor(), UIBase.getButtonBorderHoverColor(), 1);
		this.slideshowSwitcher.setValueBackgroundColor(UIBase.getButtonIdleColor());
		
		this.chooseTextureButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.backgroundoptions.chooseimage"), true, (press) -> {
			ChooseFilePopup cf = new ChooseFilePopup((call) -> {
				BackgroundOptionsPopup.this.handler.setBackgroundTexture(call);
				//TODO übernehmen
				PopupHandler.displayPopup(this);
			}, "jpg", "jpeg", "png");
			if ((this.handler.backgroundTexture != null)) {
				cf.setText(this.handler.backgroundTexturePath);
			}
			PopupHandler.displayPopup(cf);
		});
		this.addButton(chooseTextureButton);

		//TODO übernehmen
//		String pan = Locals.localize("helper.creator.backgroundoptions.panorama");
//		String nPan = Locals.localize("helper.creator.backgroundoptions.nopanorama");
//		if (this.handler.panorama) {
//			pan = "§a" + pan;
//		} else {
//			nPan = "§a" + nPan;
//		}
//		this.panoramaButton = new AdvancedButton(0, 0, 100, 20, pan, true, (press) -> {
//			this.handler.panorama = true;
//			press.setMessage(new StringTextComponent("§a" + Locals.localize("helper.creator.backgroundoptions.panorama")));
//			this.noPanoramaButton.setMessage(Locals.localize("helper.creator.backgroundoptions.nopanorama"));
//		});
//		this.addButton(panoramaButton);
//
//		this.noPanoramaButton = new AdvancedButton(0, 0, 100, 20, nPan, true, (press) -> {
//			this.handler.panorama = false;
//			press.setMessage(new StringTextComponent("§a" + Locals.localize("helper.creator.backgroundoptions.nopanorama")));
//			this.panoramaButton.setMessage(Locals.localize("helper.creator.backgroundoptions.panorama"));
//		});
//		this.addButton(noPanoramaButton);
		
		this.setPanoramaButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.backgroundoptions.panoramas.set"), true, (press) -> {
			if (this.panoramaSwitcher.getSelectedValue() != null) {
				if (PanoramaHandler.panoramaExists(this.panoramaSwitcher.getSelectedValue())) {
					this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					this.handler.backgroundPanorama = PanoramaHandler.getPanorama(this.panoramaSwitcher.getSelectedValue());

					this.handler.setBackgroundAnimations((String[])null);
					this.handler.backgroundAnimationNames.clear();
					this.handler.backgroundTexture = null;
					this.handler.backgroundTexturePath = null;
					this.handler.backgroundSlideshow = null;
				}
			}
		});
		this.setPanoramaButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.backgroundoptions.panoramas.set.desc"), "%n%"));
		this.addButton(setPanoramaButton);
		if (this.panoramaSwitcher.getSelectedValue() == null) {
			this.setPanoramaButton.active = false;
		}

		this.clearPanoramaButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.backgroundoptions.panoramas.clear"), true, (press) -> {
			if (this.handler.backgroundPanorama != null) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.handler.backgroundPanorama = null;
		});
		this.clearPanoramaButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.backgroundoptions.panoramas.clear.desc"), "%n%"));
		this.addButton(clearPanoramaButton);
		if (this.panoramaSwitcher.getSelectedValue() == null) {
			this.clearPanoramaButton.active = false;
		}

		this.setSlideshowButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.backgroundoptions.backgroundslideshow.set"), true, (press) -> {
			if (this.slideshowSwitcher.getSelectedValue() != null) {
				if (SlideshowHandler.slideshowExists(this.slideshowSwitcher.getSelectedValue())) {
					this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					this.handler.backgroundSlideshow = SlideshowHandler.getSlideshow(this.slideshowSwitcher.getSelectedValue());

					this.handler.setBackgroundAnimations((String[])null);
					this.handler.backgroundAnimationNames.clear();
					this.handler.backgroundTexture = null;
					this.handler.backgroundTexturePath = null;
					this.handler.backgroundPanorama = null;
					
				}
			}
		});
		this.setSlideshowButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.backgroundoptions.backgroundslideshow.set.desc"), "%n%"));
		this.addButton(setSlideshowButton);
		if (this.slideshowSwitcher.getSelectedValue() == null) {
			this.setSlideshowButton.active = false;
		}

		this.clearSlideshowButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("helper.creator.backgroundoptions.backgroundslideshow.clear"), true, (press) -> {
			if (this.handler.backgroundSlideshow != null) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.handler.backgroundSlideshow = null;
		});
		this.clearSlideshowButton.setDescription(StringUtils.splitLines(Locals.localize("helper.creator.backgroundoptions.backgroundslideshow.clear.desc"), "%n%"));
		this.addButton(clearSlideshowButton);
		if (this.slideshowSwitcher.getSelectedValue() == null) {
			this.clearSlideshowButton.active = false;
		}

		this.setAnimationButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.helper.creator.backgroundoptions.backgroundanimation.set"), true, (press) -> {
			if (this.animationSwitcher.getSelectedValue() != null) {
				this.handler.backgroundAnimationNames.clear();
				this.handler.setBackgroundAnimations(this.animationSwitcher.getSelectedValue());
			}
		});
		this.setAnimationButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.creator.backgroundoptions.backgroundanimation.set.desc"), "%n%"));
		this.addButton(setAnimationButton);
		if (this.animationSwitcher.getSelectedValue() == null) {
			this.setAnimationButton.active = false;
		}

		this.clearAnimationButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.helper.creator.backgroundoptions.backgroundanimation.clear"), true, (press) -> {
			if (!this.handler.backgroundAnimationNames.isEmpty()) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.handler.setBackgroundAnimations((String[])null);
			this.handler.backgroundAnimationNames.clear();
		});
		this.clearAnimationButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.creator.backgroundoptions.backgroundanimation.clear.desc"), "%n%"));
		this.addButton(clearAnimationButton);
		if (this.animationSwitcher.getSelectedValue() == null) {
			this.clearAnimationButton.active = false;
		}

		//TODO übernehmen
		this.clearImageButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("fancymenu.helper.editor.layoutoptions.backgroundoptions.image.clear"), true, (press) -> {
			if (this.handler.backgroundTexture != null) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.handler.backgroundTexture = null;
			this.handler.backgroundTexturePath = null;
		});
		this.clearImageButton.setDescription(StringUtils.splitLines(Locals.localize("fancymenu.helper.editor.layoutoptions.backgroundoptions.image.clear.btn.desc"), "%n%"));
		this.addButton(clearImageButton);
		//-----------------
		
		this.doneButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.done"), true, (press) -> {
			this.setDisplayed(false);
		});
		this.addButton(this.doneButton);
		
		KeyboardHandler.addKeyPressedListener(this::onEnterPressed);
		KeyboardHandler.addKeyPressedListener(this::onEscapePressed);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
		super.render(matrix, mouseX, mouseY, renderIn);
		
		if (this.isDisplayed()) {

			//TODO übernehmen
			drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, "§l" + Locals.localize("fancymenu.helper.editor.layoutoptions.backgroundoptions.setbackground"), renderIn.width / 2, (renderIn.height / 2) - 110, -1);
			
			
			this.typeSwitcher.render(matrix, (renderIn.width / 2) - (this.typeSwitcher.getTotalWidth() / 2), (renderIn.height / 2) - 85);
			
			
			String s = this.typeSwitcher.getSelectedValue();
			if (s.equals(Locals.localize("helper.creator.backgroundoptions.backgroundanimation"))) {
				
				drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("helper.creator.backgroundoptions.animations"), renderIn.width / 2, (renderIn.height / 2) - 50, -1);
				
				this.animationSwitcher.render(matrix, (renderIn.width / 2) - (this.animationSwitcher.getTotalWidth() / 2), (renderIn.height / 2) - 35);

				this.setAnimationButton.x = (renderIn.width / 2) - (this.setAnimationButton.getWidth() / 2);
				this.setAnimationButton.y = (renderIn.height / 2) - 5;
				
				this.clearAnimationButton.x = (renderIn.width / 2) - (this.clearAnimationButton.getWidth() / 2);
				this.clearAnimationButton.y = (renderIn.height / 2) + 20;
				
				this.setAnimationButton.visible = true;
				this.clearAnimationButton.visible = true;

				this.chooseTextureButton.visible = false;
				//TODO übernehmen
//				this.panoramaButton.visible = false;
//				this.noPanoramaButton.visible = false;
				this.setPanoramaButton.visible = false;
				this.clearPanoramaButton.visible = false;
				this.setSlideshowButton.visible = false;
				this.clearSlideshowButton.visible = false;
				//TODO übernehmen
				this.clearImageButton.visible = false;
			}
			
			if (s.equals(Locals.localize("helper.creator.backgroundoptions.backgroundimage"))) {
				this.chooseTextureButton.x = (renderIn.width / 2) - (this.chooseTextureButton.getWidth() / 2);
				this.chooseTextureButton.y = (renderIn.height / 2) - 25;
				this.chooseTextureButton.visible = true;

				//TODO übernehmen
//				AbstractGui.drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("helper.creator.backgroundoptions.setpanorama"), renderIn.width / 2, (renderIn.height / 2) + 13, Color.WHITE.getRGB());
//
//				this.panoramaButton.x = (renderIn.width / 2) - this.panoramaButton.getWidth() - 5;
//				this.panoramaButton.y = (renderIn.height / 2) + 30;
//				this.panoramaButton.visible = true;
//
//				this.noPanoramaButton.x = (renderIn.width / 2) + 5;
//				this.noPanoramaButton.y = (renderIn.height / 2) + 30;
//				this.noPanoramaButton.visible = true;

				//TODO übernehmen
				this.clearImageButton.x = (renderIn.width / 2) - (this.clearPanoramaButton.getWidth() / 2);
				this.clearImageButton.y = (renderIn.height / 2);
				this.clearImageButton.visible = true;
				//------------

				this.setAnimationButton.visible = false;
				this.clearAnimationButton.visible = false;
				this.setPanoramaButton.visible = false;
				this.clearPanoramaButton.visible = false;
				this.setSlideshowButton.visible = false;
				this.clearSlideshowButton.visible = false;
			}

			if (s.equals(Locals.localize("helper.creator.backgroundoptions.backgroundpanorama"))) {
				
				drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("helper.creator.backgroundoptions.panoramas"), renderIn.width / 2, (renderIn.height / 2) - 50, -1);
				
				this.panoramaSwitcher.render(matrix, (renderIn.width / 2) - (this.panoramaSwitcher.getTotalWidth() / 2), (renderIn.height / 2) - 35);
				
				this.setPanoramaButton.x = (renderIn.width / 2) - (this.setPanoramaButton.getWidth() / 2);
				this.setPanoramaButton.y = (renderIn.height / 2) - 5;
				
				this.clearPanoramaButton.x = (renderIn.width / 2) - (this.clearPanoramaButton.getWidth() / 2);
				this.clearPanoramaButton.y = (renderIn.height / 2) + 20;
				
				this.setPanoramaButton.visible = true;
				this.clearPanoramaButton.visible = true;

				this.setAnimationButton.visible = false;
				this.clearAnimationButton.visible = false;
				this.chooseTextureButton.visible = false;
				//TODO übernehmen
//				this.panoramaButton.visible = false;
//				this.noPanoramaButton.visible = false;
				this.setSlideshowButton.visible = false;
				this.clearSlideshowButton.visible = false;
				//TODO übernehmen
				this.clearImageButton.visible = false;
				
			}

			if (s.equals(Locals.localize("helper.creator.backgroundoptions.backgroundslideshow"))) {
				
				drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("helper.creator.backgroundoptions.slideshows"), renderIn.width / 2, (renderIn.height / 2) - 50, -1);
				
				this.slideshowSwitcher.render(matrix, (renderIn.width / 2) - (this.slideshowSwitcher.getTotalWidth() / 2), (renderIn.height / 2) - 35);
				
				this.setSlideshowButton.x = (renderIn.width / 2) - (this.setSlideshowButton.getWidth() / 2);
				this.setSlideshowButton.y = (renderIn.height / 2) - 5;
				
				this.clearSlideshowButton.x = (renderIn.width / 2) - (this.clearSlideshowButton.getWidth() / 2);
				this.clearSlideshowButton.y = (renderIn.height / 2) + 20;
				
				this.setSlideshowButton.visible = true;
				this.clearSlideshowButton.visible = true;

				this.setAnimationButton.visible = false;
				this.clearAnimationButton.visible = false;
				this.chooseTextureButton.visible = false;
				//TODO übernehmen
//				this.panoramaButton.visible = false;
//				this.noPanoramaButton.visible = false;
				this.setPanoramaButton.visible = false;
				this.clearPanoramaButton.visible = false;
				//TODO übernehmen
				this.clearImageButton.visible = false;
				
			}
			
			this.doneButton.x = (renderIn.width / 2) - (this.doneButton.getWidth() / 2);
			this.doneButton.y = (renderIn.height / 2) + 85;
			
			this.renderButtons(matrix, mouseX, mouseY);
		}
	}
	
	public void onEnterPressed(KeyboardData d) {
		if ((d.keycode == 257) && this.isDisplayed()) {
			this.setDisplayed(false);
		}
	}
	
	public void onEscapePressed(KeyboardData d) {
		if ((d.keycode == 256) && this.isDisplayed()) {
			this.setDisplayed(false);
		}
	}

}
