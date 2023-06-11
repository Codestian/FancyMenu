package de.keksuccino.fancymenu.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface IMixinScreen {

    @Accessor("font") void setFontFancyMenu(Font font);

}
