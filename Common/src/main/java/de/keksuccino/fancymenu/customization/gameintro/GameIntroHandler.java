package de.keksuccino.fancymenu.customization.gameintro;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.customization.animation.AnimationHandler;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;

public class GameIntroHandler {

	//TODO change this !!!!!!!!!!!
	public static boolean introDisplayed = true;
	
	public static void init() {
		EventHandler.INSTANCE.registerListenersOf(new GameIntroEvents());
	}
	
	/**
	 * Returns the game intro or null if no animation was set or the animation was not found.
	 */
	public static IAnimationRenderer getGameIntroAnimation() {
		String name = FancyMenu.getOptions().gameIntroAnimation.getValue();
		if (name.length() == 0) return null;
		if (AnimationHandler.animationExists(name)) {
			return AnimationHandler.getAnimation(name);
		}
		return null;
	}
	
}