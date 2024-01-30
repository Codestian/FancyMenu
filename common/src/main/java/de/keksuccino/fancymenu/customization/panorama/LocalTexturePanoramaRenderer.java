package de.keksuccino.fancymenu.customization.panorama;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import de.keksuccino.fancymenu.util.ScreenUtils;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.resource.ResourceSource;
import de.keksuccino.fancymenu.util.resource.ResourceSourceType;
import de.keksuccino.fancymenu.util.resource.ResourceSupplier;
import de.keksuccino.fancymenu.util.resource.resources.texture.ITexture;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.fancymenu.util.properties.PropertyContainer;
import de.keksuccino.fancymenu.util.properties.PropertiesParser;
import de.keksuccino.fancymenu.util.properties.PropertyContainerSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class LocalTexturePanoramaRenderer implements Widget {

	private static final Logger LOGGER = LogManager.getLogger();

	@NotNull
	public File propertiesFile;
	@NotNull
	public File panoramaImageDir;
	@Nullable
	public File overlayImageFile;
	protected String name = null;
	protected final List<ResourceSupplier<ITexture>> panoramaImageSuppliers = new ArrayList<>();
	@Nullable
	protected ResourceSupplier<ITexture> overlayTextureSupplier;
	protected float speed = 1.0F;
	protected double fov = 85.0D;
	protected float angle = 25.0F;
	public float opacity = 1.0F;
	protected float frameTime;

	@Nullable
	public static LocalTexturePanoramaRenderer build(@NotNull File propertiesFile, @NotNull File panoramaImageDir, @Nullable File overlayImageFile) {
		LocalTexturePanoramaRenderer renderer = new LocalTexturePanoramaRenderer(propertiesFile, panoramaImageDir, overlayImageFile);
		try {
			if (renderer.propertiesFile.isFile() && renderer.panoramaImageDir.isDirectory()) {
				PropertyContainerSet panoProperties = PropertiesParser.deserializeSetFromFile(renderer.propertiesFile.getAbsolutePath());
				if (panoProperties != null) {
					PropertyContainer panoMeta = panoProperties.getFirstContainerOfType("panorama-meta");
					if (panoMeta != null) {
						renderer.name = panoMeta.getValue("name");
						if (renderer.name == null) {
							LOGGER.error("[FANCYMENU] Unable to load panorama! Missing 'name' value in panorama meta section: " + renderer.propertiesFile.getAbsolutePath(), new NullPointerException());
							return null;
						}
						String sp = panoMeta.getValue("speed");
						if ((sp != null) && MathUtils.isFloat(sp)) {
							renderer.speed = Float.parseFloat(sp);
						}
						String fo = panoMeta.getValue("fov");
						if ((fo != null) && MathUtils.isDouble(fo)) {
							renderer.fov = Double.parseDouble(fo);
						}
						String an = panoMeta.getValue("angle");
						if ((an != null) && MathUtils.isFloat(an)) {
							renderer.angle = Float.parseFloat(an);
						}
						renderer.prepare();
						return renderer;
					} else {
						LOGGER.error("[FANCYMENU] Unable to load panorama! Missing 'panorama-meta' section in properties instance: " + renderer.propertiesFile.getAbsolutePath(), new NullPointerException());
					}
				} else {
					LOGGER.error("[FANCYMENU] Unable to load panorama! Parsed properties instance was NULL: " + renderer.propertiesFile.getAbsolutePath(), new NullPointerException());
				}
			}
		} catch (Exception ex) {
			LOGGER.error("[FANCYMENU] An error happened while trying to build a cubic panorama!", ex);
		}
		return null;
	}

	protected LocalTexturePanoramaRenderer(@NotNull File propertiesFile, @NotNull File panoramaImageDir, @Nullable File overlayImageFile) {
		this.propertiesFile = Objects.requireNonNull(propertiesFile);
		this.panoramaImageDir = Objects.requireNonNull(panoramaImageDir);
		this.overlayImageFile = overlayImageFile;
	}

	protected void prepare() {

		this.panoramaImageSuppliers.clear();
		this.overlayTextureSupplier = null;

		for (int i = 0; i < 6; i++) {
			File panoImage = new File(this.panoramaImageDir, "panorama_" + i + ".png");
			if (panoImage.isFile()) {
				this.panoramaImageSuppliers.add(ResourceSupplier.image(ResourceSource.of(panoImage.getAbsolutePath(), ResourceSourceType.LOCAL).getSourceWithPrefix()));
			} else {
				LOGGER.error("[FANCYMENU] Unable to load panorama! Missing panorama image 'panorama_" + i + ".png': " + this.name);
				return;
			}
		}

		if ((this.overlayImageFile != null) && this.overlayImageFile.isFile()) {
			this.panoramaImageSuppliers.add(ResourceSupplier.image(ResourceSource.of(this.overlayImageFile.getAbsolutePath(), ResourceSourceType.LOCAL).getSourceWithPrefix()));
		}

	}

	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
		if (this.panoramaImageSuppliers.size() < 6) {
			RenderSystem.enableBlend();
			RenderingUtils.resetShaderColor();
			RenderingUtils.bindTexture(ITexture.MISSING_TEXTURE_LOCATION);
			GuiComponent.blit(pose, 0, 0, 0.0F, 0.0F, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
			RenderingUtils.resetShaderColor();
		} else {
			this.renderRaw(pose, this.opacity);
		}
	}

	public void renderRaw(@NotNull PoseStack pose, float panoramaAlpha) {

		Minecraft mc = Minecraft.getInstance();

		this.frameTime += Minecraft.getInstance().getDeltaFrameTime() * this.speed;

		int screenW = ScreenUtils.getScreenWidth();
		int screenH = ScreenUtils.getScreenHeight();

		float pitch = Mth.sin(this.frameTime * 0.001F) * 5.0F + this.angle;
		float yaw = -this.frameTime * 0.1F;

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		Matrix4f matrix4f = Matrix4f.perspective(this.fov, (float)mc.getWindow().getWidth() / (float)mc.getWindow().getHeight(), 0.05F, 10.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f);
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.setIdentity();
		modelViewStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.opacity);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();

		for(int j = 0; j < 4; ++j) {
			modelViewStack.pushPose();
			float k = ((float)(j % 2) / 2.0f - 0.5f) / 256.0f;
			float l = ((float)(j / 2) / 2.0f - 0.5f) / 256.0f;
			modelViewStack.translate(k, l, 0.0f);
			modelViewStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
			modelViewStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
			RenderSystem.applyModelViewMatrix();
			for (int n = 0; n < 6; ++n) {
				ResourceLocation location = null;
				if (this.panoramaImageSuppliers.size() >= (n + 1)) {
					ResourceSupplier<ITexture> texSupplier = this.panoramaImageSuppliers.get(n);
					ITexture texture = texSupplier.get();
					if (texture != null) {
						location = texture.getResourceLocation();
					}
				}
				if (location == null) location = ITexture.MISSING_TEXTURE_LOCATION;
				RenderSystem.setShaderTexture(0, location);
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
				int o = Math.round(255.0F * panoramaAlpha) / (j + 1);
				if (n == 0) {
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				if (n == 1) {
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				if (n == 2) {
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				if (n == 3) {
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				if (n == 4) {
					bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				if (n == 5) {
					bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0F, 0.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0F, 1.0F).color(255, 255, 255, o).endVertex();
					bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0F, 0.0F).color(255, 255, 255, o).endVertex();
				}
				tesselator.end();
			}
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.colorMask(true, true, true, false);
		}

		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.restoreProjectionMatrix();
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();

		if (this.overlayTextureSupplier != null) {
			ITexture texture = this.overlayTextureSupplier.get();
			if (texture != null) {
				ResourceLocation location = texture.getResourceLocation();
				if (location != null) {
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.opacity);
					RenderSystem.enableBlend();
					RenderingUtils.bindTexture(location);
					GuiComponent.blit(pose, 0, 0, 0.0F, 0.0F, screenW, screenH, screenW, screenH);
				}
			}
		}

		RenderingUtils.resetShaderColor();

	}

	public String getName() {
		return this.name;
	}

	public void setSpeed(float speed) {
		if (speed < 0.0F) {
			speed = 0.0F;
		}
		this.speed = speed;
	}

	public void setFov(double fov) {
		if (fov > 179.0D) {
			fov = 179.0D;
		}
		this.fov = fov;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

}