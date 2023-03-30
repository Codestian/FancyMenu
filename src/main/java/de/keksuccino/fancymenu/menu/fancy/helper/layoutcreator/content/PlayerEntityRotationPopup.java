package de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMPopup;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.math.MathUtils;

public class PlayerEntityRotationPopup extends FMPopup {

	private LayoutEditorScreen handler;
	private LayoutPlayerEntity object;
	
	private AdvancedTextField bodyX;
	private AdvancedTextField bodyY;
	private AdvancedTextField headX;
	private AdvancedTextField headY;
	
	private AdvancedButton doneButton;
	private AdvancedButton applyButton;
	
	public PlayerEntityRotationPopup(LayoutEditorScreen handler, LayoutPlayerEntity object) {
		super(240);
		this.handler = handler;
		this.object = object;
		
		Font font = Minecraft.getInstance().font;
		
		this.bodyX = new AdvancedTextField(font, 0, 0, 150, 20, true, CharacterFilter.getDoubleCharacterFiler());
		this.bodyX.setValue("" + object.getObject().bodyRotationX);
		
		this.bodyY = new AdvancedTextField(font, 0, 0, 150, 20, true, CharacterFilter.getDoubleCharacterFiler());
		this.bodyY.setValue("" + object.getObject().bodyRotationY);
		
		this.headX = new AdvancedTextField(font, 0, 0, 150, 20, true, CharacterFilter.getDoubleCharacterFiler());
		this.headX.setValue("" + object.getObject().headRotationX);
		
		this.headY = new AdvancedTextField(font, 0, 0, 150, 20, true, CharacterFilter.getDoubleCharacterFiler());
		this.headY.setValue("" + object.getObject().headRotationY);
		
		this.doneButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.done"), true, (call) -> {
			this.updateValues();
			this.setDisplayed(false);
		});
		this.colorizePopupButton(doneButton);
		this.addButton(doneButton);
		
		this.applyButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.apply"), true, (call) -> {
			this.updateValues();
		});
		this.colorizePopupButton(applyButton);
		this.addButton(applyButton);
		
		KeyboardHandler.addKeyPressedListener(this::onEscapePressed);
		KeyboardHandler.addKeyPressedListener(this::onEnterPressed);
		
	}
	
	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, Screen renderIn) {
		super.render(matrix, mouseX, mouseY, renderIn);
		
		Font font = Minecraft.getInstance().font;
		int midX = renderIn.width / 2;
		int midY = renderIn.height / 2;
		float partial = Minecraft.getInstance().getFrameTime();
		
		drawCenteredString(matrix, font, Locals.localize("helper.creator.items.playerentity.rotation.custom.bodyx"), midX, midY - 83, Color.WHITE.getRGB());
		
		this.bodyX.x = midX - (this.bodyX.getWidth() / 2);
		this.bodyX.y = midY - 70;
		this.bodyX.render(matrix, mouseX, mouseY, partial);
		
		drawCenteredString(matrix, font, Locals.localize("helper.creator.items.playerentity.rotation.custom.bodyy"), midX, midY - 43, Color.WHITE.getRGB());
		
		this.bodyY.x = midX - (this.bodyY.getWidth() / 2);
		this.bodyY.y = midY - 30;
		this.bodyY.render(matrix, mouseX, mouseY, partial);

		drawCenteredString(matrix, font, Locals.localize("helper.creator.items.playerentity.rotation.custom.headx"), midX, midY - 3, Color.WHITE.getRGB());
		
		this.headX.x = midX - (this.headX.getWidth() / 2);
		this.headX.y = midY + 10;
		this.headX.render(matrix, mouseX, mouseY, partial);

		drawCenteredString(matrix, font, Locals.localize("helper.creator.items.playerentity.rotation.custom.heady"), midX, midY + 37, Color.WHITE.getRGB());
		
		this.headY.x = midX - (this.headY.getWidth() / 2);
		this.headY.y = midY + 50;
		this.headY.render(matrix, mouseX, mouseY, partial);
		
		this.doneButton.x = midX - this.doneButton.getWidth() - 5;
		this.doneButton.y = midY + 80;
		
		this.applyButton.x = midX + 5;
		this.applyButton.y = midY + 80;
		
		this.renderButtons(matrix, mouseX, mouseY);
	}
	
	protected void updateValues() {
		this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
		
		this.object.getObject().autoRotation = false;
		
		if ((this.bodyX.getValue() != null) && MathUtils.isFloat(this.bodyX.getValue())) {
			this.object.getObject().bodyRotationX = Float.parseFloat(this.bodyX.getValue());
		} else {
			this.object.getObject().bodyRotationX = 0;
		}
		
		if ((this.bodyY.getValue() != null) && MathUtils.isFloat(this.bodyY.getValue())) {
			this.object.getObject().bodyRotationY = Float.parseFloat(this.bodyY.getValue());
		} else {
			this.object.getObject().bodyRotationY = 0;
		}
		
		if ((this.headX.getValue() != null) && MathUtils.isFloat(this.headX.getValue())) {
			this.object.getObject().headRotationX = Float.parseFloat(this.headX.getValue());
		} else {
			this.object.getObject().headRotationX = 0;
		}
		
		if ((this.headY.getValue() != null) && MathUtils.isFloat(this.headY.getValue())) {
			this.object.getObject().headRotationY = Float.parseFloat(this.headY.getValue());
		} else {
			this.object.getObject().headRotationY = 0;
		}
	}
	
	protected void onEnterPressed(KeyboardData d) {
		if ((d.keycode == 257) && this.isDisplayed()) {
			this.updateValues();
			this.setDisplayed(false);
		}
	}
	
	protected void onEscapePressed(KeyboardData d) {
		if ((d.keycode == 256) && this.isDisplayed()) {
			this.setDisplayed(false);
		}
	}

}
