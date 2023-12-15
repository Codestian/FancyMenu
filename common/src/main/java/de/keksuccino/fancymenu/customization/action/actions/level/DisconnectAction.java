package de.keksuccino.fancymenu.customization.action.actions.level;

import de.keksuccino.fancymenu.customization.action.Action;
import de.keksuccino.fancymenu.customization.customgui.CustomGuiHandler;
import de.keksuccino.fancymenu.customization.screen.identifier.ScreenIdentifierHandler;
import de.keksuccino.fancymenu.customization.screen.ScreenInstanceFactory;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.rendering.text.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisconnectAction extends Action {

    private static final Component SAVING_LEVEL = Components.translatable("menu.savingLevel");

    public DisconnectAction() {
        super("disconnect_server_or_world");
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public void execute(@Nullable String value) {
        if (value != null) {
            Minecraft mc = Minecraft.getInstance();
            try {
                if ((mc.level != null) && (mc.player != null)) {
                    Screen s;
                    if (CustomGuiHandler.guiExists(value)) {
                        s = CustomGuiHandler.constructInstance(value, null, null);
                    } else {
                        s = ScreenInstanceFactory.tryConstruct(ScreenIdentifierHandler.tryFixInvalidIdentifierWithNonUniversal(value));
                    }
                    if (s == null) {
                        s = new TitleScreen();
                    }
                    boolean singlePlayer = mc.isLocalServer();
                    mc.level.disconnect();
                    if (singlePlayer) {
                        mc.clearLevel(new GenericDirtMessageScreen(SAVING_LEVEL));
                    } else {
                        mc.clearLevel();
                    }
                    mc.setScreen(s);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public @NotNull Component getActionDisplayName() {
        return Components.translatable("fancymenu.editor.custombutton.config.actiontype.disconnect");
    }

    @Override
    public @NotNull Component[] getActionDescription() {
        return LocalizationUtils.splitLocalizedLines("fancymenu.editor.custombutton.config.actiontype.disconnect.desc");
    }

    @Override
    public Component getValueDisplayName() {
        return Components.translatable("fancymenu.editor.custombutton.config.actiontype.disconnect.desc.value");
    }

    @Override
    public String getValueExample() {
        return "example.menu.identifier";
    }

}
