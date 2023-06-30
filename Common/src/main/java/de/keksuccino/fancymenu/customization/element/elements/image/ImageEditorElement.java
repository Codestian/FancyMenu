package de.keksuccino.fancymenu.customization.element.elements.image;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.element.editor.AbstractEditorElement;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.file.FileFilter;
import de.keksuccino.fancymenu.util.rendering.ui.popup.FMTextInputPopup;
import de.keksuccino.fancymenu.util.rendering.ui.screen.filebrowser.ChooseFileScreen;
import de.keksuccino.fancymenu.util.ListUtils;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class ImageEditorElement extends AbstractEditorElement {

    public ImageEditorElement(@NotNull AbstractElement element, @NotNull LayoutEditorScreen editor) {
        super(element, editor);
    }

    @Override
    public void init() {

        super.init();

        this.addCycleContextMenuEntryTo(this.rightClickMenu, "set_mode", ListUtils.build(ImageElement.SourceMode.WEB, ImageElement.SourceMode.LOCAL),
                consumes -> (consumes instanceof ImageEditorElement),
                consumes -> ((ImageElement)consumes.element).sourceMode,
                (element1, sourceMode) -> {
                    ((ImageElement)element1.element).sourceMode = sourceMode;
                    ((ImageElement)element1.element).source = null;
                },
                (menu, entry, switcherValue) -> {
                    if (switcherValue == ImageElement.SourceMode.LOCAL) {
                        return Component.translatable("fancymenu.elements.image.source_mode.local");
                    } else {
                        return Component.translatable("fancymenu.elements.image.source_mode.web");
                    }
                });

        this.rightClickMenu.addClickableEntry("set_source", Component.translatable("fancymenu.elements.image.set_source"), (menu, entry) -> {
            if (((ImageElement)this.element).sourceMode == ImageElement.SourceMode.LOCAL) {
                ChooseFileScreen s = new ChooseFileScreen(FancyMenu.ASSETS_DIR, FancyMenu.ASSETS_DIR, (call) -> {
                    if (call != null) {
                        this.editor.history.saveSnapshot();
                        ((ImageElement)this.element).source = ScreenCustomization.getPathWithoutGameDirectory(call.getAbsolutePath());
                    }
                    Minecraft.getInstance().setScreen(this.editor);
                });
                s.setFileFilter(FileFilter.IMAGE_AND_GIF_FILE_FILTER);
                Minecraft.getInstance().setScreen(s);
            } else {
                FMTextInputPopup p = new FMTextInputPopup(new Color(0,0,0,0), I18n.get("fancymenu.elements.image.set_source"), null, 240, (call) -> {
                    if (call != null) {
                        this.editor.history.saveSnapshot();
                        ((ImageElement)this.element).source = call;
                    }
                });
                if (((ImageElement)this.element).source != null) {
                    p.setText(((ImageElement)this.element).source);
                }
                PopupHandler.displayPopup(p);
            }
        });

        this.rightClickMenu.addSeparatorEntry("image_separator_1");

        this.rightClickMenu.addClickableEntry("restore_aspect_ratio", Component.translatable("fancymenu.elements.image.restore_aspect_ratio"), (menu, entry) -> {
            List<AbstractEditorElement> selectedElements = ListUtils.filterList(this.editor.getSelectedElements(), consumes -> (consumes instanceof ImageEditorElement));
            this.editor.history.saveSnapshot();
            for (AbstractEditorElement e : selectedElements) {
                ((ImageElement)e.element).restoreAspectRatio();
            }
        }).setStackable(true);

    }

}
