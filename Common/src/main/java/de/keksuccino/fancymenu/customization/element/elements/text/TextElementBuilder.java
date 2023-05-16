
package de.keksuccino.fancymenu.customization.element.elements.text;

import de.keksuccino.fancymenu.api.item.CustomizationItem;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.editor.AbstractEditorElement;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.rendering.ui.UIBase;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.fancymenu.properties.PropertyContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class TextElementBuilder extends ElementBuilder {

    public TextElementBuilder() {
        super("fancymenu_customization_item_text");
    }

    @Override
    public @NotNull CustomizationItem buildDefaultInstance() {
        TextElement i = new TextElement(this, new PropertyContainer("dummy"));
        i.width = 200;
        i.height = 40;
        Screen s = Minecraft.getInstance().screen;
        if ((s != null) && (s instanceof LayoutEditorScreen)) {
            i.rawPosY = (int)(((LayoutEditorScreen)s).ui.topMenuBar.getHeight() * UIBase.getUIScale());
        }
        return i;
    }

    @Override
    public CustomizationItem deserializeElement(PropertyContainer serializedElement) {
        return new TextElement(this, serializedElement);
    }

    @Override
    public AbstractEditorElement buildEditorElementInstance(CustomizationItem item, LayoutEditorScreen handler) {
        return new TextEditorElement(this, (TextElement) item, handler);
    }

    @Override
    public @NotNull String getDisplayName() {
        return Locals.localize("fancymenu.customization.items.text");
    }

    @Override
    public String[] getDescription() {
        return StringUtils.splitLines(Locals.localize("fancymenu.customization.items.text.desc"), "%n%");
    }

}
