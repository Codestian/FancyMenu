package de.keksuccino.fancymenu.customization.action.actions.screen;

import de.keksuccino.fancymenu.customization.action.Action;
import de.keksuccino.fancymenu.customization.customgui.CustomGuiHandler;
import de.keksuccino.fancymenu.customization.screen.identifier.ScreenIdentifierHandler;
import de.keksuccino.fancymenu.customization.screen.ScreenInstanceFactory;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.rendering.text.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenScreenAction extends Action {

    private static final Logger LOGGER = LogManager.getLogger();

    public OpenScreenAction() {
        super("opengui");
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public void execute(@Nullable String value) {
        if (value != null) {
            value = ScreenIdentifierHandler.tryFixInvalidIdentifierWithNonUniversal(value);
            if (CustomGuiHandler.guiExists(value)) {
                Screen custom = CustomGuiHandler.constructInstance(value, Minecraft.getInstance().screen, null);
                if (custom != null) Minecraft.getInstance().setScreen(custom);
            } else {
                Screen s = ScreenInstanceFactory.tryConstruct(value);
                if (s != null) {
                    Minecraft.getInstance().setScreen(s);
                } else {
                    LOGGER.error("[FANCYMENU] Unable to construct screen instance for '" + value + "'!");
                }
            }
        }
    }

    @Override
    public @NotNull Component getActionDisplayName() {
        return Components.translatable("fancymenu.editor.custombutton.config.actiontype.opengui");
    }

    @Override
    public @NotNull Component[] getActionDescription() {
        return LocalizationUtils.splitLocalizedLines("fancymenu.editor.custombutton.config.actiontype.opengui.desc");
    }

    @Override
    public Component getValueDisplayName() {
        return Components.translatable("fancymenu.editor.custombutton.config.actiontype.opengui.desc.value");
    }

    @Override
    public String getValueExample() {
        return "example.menu.identifier";
    }

}