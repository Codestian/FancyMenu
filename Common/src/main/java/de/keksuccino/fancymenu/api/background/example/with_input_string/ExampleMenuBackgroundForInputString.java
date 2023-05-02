package de.keksuccino.fancymenu.api.background.example.with_input_string;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.api.background.MenuBackground;
import de.keksuccino.fancymenu.api.background.MenuBackgroundType;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.fancymenu.rendering.texture.ExternalTextureHandler;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nonnull;
import java.io.File;

//This is an example menu background that renders an external image.
//It gets created by a background type that uses an input string to get the image file.
public class ExampleMenuBackgroundForInputString extends MenuBackground {

    private ExternalTextureResourceLocation imageLocation = null;

    public ExampleMenuBackgroundForInputString(@Nonnull MenuBackgroundType type, String imagePath) {
        //Identifiers aren't really used for backgrounds that don't get registered to a type (because the type uses the input string),
        //so just set something random here.
        super("unused_identifier", type);

        //Check if the image exists and has the correct file type, then load it.
        File imageFile = new File(imagePath);
        if (imageFile.exists() && (imageFile.getPath().toLowerCase().endsWith(".jpg") || imageFile.getPath().toLowerCase().endsWith(".jpeg") || imageFile.getPath().toLowerCase().endsWith(".png"))) {
            this.imageLocation = ExternalTextureHandler.INSTANCE.getTexture(imageFile.getPath());
            if (this.imageLocation != null) {
                this.imageLocation.loadTexture();
            }
        }

    }

    @Override
    public void onOpenMenu() {
        //Empty because everytime the menu gets opened, a new instance of the background will be created
        //when using input strings, so you don't need to reset stuff.
    }

    //Here you will render the background instance.
    //You should always render backgrounds over the full size of the screen, otherwise it will look ugly.
    @Override
    public void render(PoseStack matrix, Screen screen, boolean keepAspectRatio) {

        try {

            //Check if the image location is ready to get rendered
            if ((this.imageLocation != null) && this.imageLocation.isReady()) {

                RenderSystem.enableBlend();
                RenderUtils.bindTexture(this.imageLocation.getResourceLocation());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                //If the keep-aspect-ratio toggle is disabled, just stretch the image background to the full size of the screen it is rendered in
                if (!keepAspectRatio) {

                    GuiComponent.blit(matrix, 0, 0, 1.0F, 1.0F, screen.width, screen.height, screen.width, screen.height);

                //If the background image should keep its aspect ratio, try to keep the aspect ratio as long as possible.
                //As soon as it's not possible to keep the aspect ratio anymore, just stretch it.
                } else {

                    int w = this.imageLocation.getWidth();
                    int h = this.imageLocation.getHeight();
                    double ratio = (double) w / (double) h;
                    int wfinal = (int)(screen.height * ratio);
                    int screenCenterX = screen.width / 2;
                    if (wfinal < screen.width) {
                        GuiComponent.blit(matrix, 0, 0, 1.0F, 1.0F, screen.width, screen.height, screen.width, screen.height);
                    } else {
                        GuiComponent.blit(matrix, screenCenterX - (wfinal / 2), 0, 1.0F, 1.0F, wfinal, screen.height, wfinal, screen.height);
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
