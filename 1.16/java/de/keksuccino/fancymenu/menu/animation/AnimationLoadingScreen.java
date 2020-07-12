package de.keksuccino.fancymenu.menu.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.core.gui.screens.SimpleLoadingScreen;
import de.keksuccino.core.rendering.animation.IAnimationRenderer;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class AnimationLoadingScreen extends SimpleLoadingScreen {

	private Screen fallback;
	private List<IAnimationRenderer> renderers = new ArrayList<IAnimationRenderer>();
	private boolean ready = false;
	private int cachedFPS;
	private boolean cachedLoop;
	private boolean done = false;
	private volatile boolean preparing = false;

	public AnimationLoadingScreen(@Nullable Screen fallbackGui, IAnimationRenderer... renderer) {
		super(Minecraft.getInstance());
		this.renderers.addAll(Arrays.asList(renderer));
		this.fallback = fallbackGui;
		
		this.setDarkmode(FancyMenu.config.getOrDefault("loadingscreendarkmode", false));
	}

	@Override
	public void render(MatrixStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
		IAnimationRenderer current = this.getCurrentRenderer();
		
		if (current == null) {
			this.done = true;
			this.onFinished();
			if (this.fallback != null) {
				Minecraft.getInstance().displayGuiScreen(this.fallback);
			}
		} else {
			if (!this.ready) {
				this.cachedFPS = current.getFPS();
				this.cachedLoop = current.isGettingLooped();
				current.setFPS(-1);
				current.setLooped(false);
				this.ready = true;
			}
			
			if (!current.isReady()) {
				if (!this.preparing) {
					AnimationLoadingScreen loading = this;
					this.preparing = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (FancyMenu.config.getOrDefault("showanimationloadingstatus", true)) {
								loading.setStatusText(Locals.localize("loading.animation.loadingframes", current.getPath().replace("/", ".").replace("\\", ".")));
							}
							current.prepareAnimation();
							System.gc();
							loading.preparing = false;
						}
					}).start();
				}
			} else {
				if (!current.isFinished()) {
					if (FancyMenu.config.getOrDefault("showanimationloadingstatus", true)) {
						this.setStatusText(Locals.localize("loading.animation.prerendering", current.getPath().replace("/", ".").replace("\\", ".")));
					}
					if (current instanceof AdvancedAnimation) {
						((AdvancedAnimation)current).setMuteAudio(true);
					}
					current.render(matrix);
					if (current instanceof AdvancedAnimation) {
						((AdvancedAnimation)current).setMuteAudio(false);
					}
				} else {
					current.setFPS(this.cachedFPS);
					current.setLooped(this.cachedLoop);
					current.resetAnimation();
					this.renderers.remove(0);
					this.ready = false;
				}
			}
		}
		
		super.render(matrix, p_render_1_, p_render_2_, p_render_3_);
	}
	
	private IAnimationRenderer getCurrentRenderer() {
		if (!this.renderers.isEmpty()) {
			return this.renderers.get(0);
		}
		return null;
	}
	
	public void onFinished() {
		this.setStatusText(Locals.localize("loading.animation.done"));
	}

	public boolean loadingFinished() {
		return this.done;
	}
}
