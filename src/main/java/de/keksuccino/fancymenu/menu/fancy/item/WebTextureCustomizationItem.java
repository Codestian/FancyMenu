package de.keksuccino.fancymenu.menu.fancy.item;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.fancymenu.menu.fancy.DynamicValueHelper;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class WebTextureCustomizationItem extends CustomizationItemBase {

	public volatile WebTextureResourceLocation texture;
	public String rawURL = "";
	public volatile boolean ready = false;

	public WebTextureCustomizationItem(PropertiesSection item) {
		super(item);

		if ((this.action != null) && this.action.equalsIgnoreCase("addwebtexture")) {
			this.value = item.getEntryValue("url");
			if (this.value != null) {
				this.rawURL = this.value;
				this.value = DynamicValueHelper.convertFromRaw(this.value);

				if ((this.width <= 0) && (this.height <= 0)) {
					this.setWidth(100);
				}

				new Thread(() -> {
					try {

						if (isValidUrl(this.value)) {
							this.texture = TextureHandler.getWebResource(this.value);

							if ((this.texture == null) || !this.texture.isReady()) {
								if (this.width <= 0) {
									this.setWidth(100);
								}
								if (this.height <= 0) {
									this.setHeight(100);
								}
								this.ready = true;
								return;
							}

							int w = this.texture.getWidth();
							int h = this.texture.getHeight();
							double ratio = (double) w / (double) h;

							//Calculate missing width
							if ((this.getWidth() < 0) && (this.getHeight() >= 0)) {
								this.setWidth((int)(this.getHeight() * ratio));
							}
							//Calculate missing height
							if ((this.getHeight() < 0) && (this.getWidth() >= 0)) {
								this.setHeight((int)(this.getWidth() / ratio));
							}
						}

						this.ready = true;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();

			}
		}

	}

	public void render(MatrixStack matrix, Screen menu) throws IOException {
		if (this.shouldRender()) {

			int x = this.getPosX(menu);
			int y = this.getPosY(menu);

			if (this.texture != null) {
				RenderUtils.bindTexture(this.texture.getResourceLocation());
				RenderSystem.enableBlend();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.opacity);
				drawTexture(matrix, x, y, 0.0F, 0.0F, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
				RenderSystem.disableBlend();
			} else if (isEditorActive()) {
				fill(matrix, this.getPosX(menu), this.getPosY(menu), this.getPosX(menu) + this.getWidth(), this.getPosY(menu) + this.getHeight(), Color.MAGENTA.getRGB());
				if (this.ready) {
					drawCenteredString(matrix, MinecraftClient.getInstance().textRenderer, "§lMISSING", this.getPosX(menu) + (this.width / 2), this.getPosY(menu) + (this.height / 2) - (MinecraftClient.getInstance().textRenderer.fontHeight / 2), -1);
				}
			}

			if (!this.ready && isEditorActive()) {
				drawCenteredString(matrix, MinecraftClient.getInstance().textRenderer, "§lLOADING TEXTURE..", this.getPosX(menu) + (this.width / 2), this.getPosY(menu) + (this.height / 2) - (MinecraftClient.getInstance().textRenderer.fontHeight / 2), -1);
			}

		}
	}

	@Override
	public boolean shouldRender() {
		if ((this.getWidth() < 0) || (this.getHeight() < 0)) {
			return false;
		}
		return super.shouldRender();
	}

	public static boolean isValidUrl(String url) {
		if ((url != null) && (url.startsWith("http://") || url.startsWith("https://"))) {
			try {
				URL u = new URL(url);
				HttpURLConnection c = (HttpURLConnection)u.openConnection();
				c.addRequestProperty("User-Agent", "Mozilla/4.0");
				c.setRequestMethod("HEAD");
				int r = c.getResponseCode();
				if (r == 200) {
					return true;
				}
			} catch (Exception e1) {
				try {
					URL u = new URL(url);
					HttpURLConnection c = (HttpURLConnection)u.openConnection();
					c.addRequestProperty("User-Agent", "Mozilla/4.0");
					int r = c.getResponseCode();
					if (r == 200) {
						return true;
					}
				} catch (Exception e2) {}
			}
			return false;
		}
		return false;
	}

}