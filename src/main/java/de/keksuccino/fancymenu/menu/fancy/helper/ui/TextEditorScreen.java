//TODO übernehmen
package de.keksuccino.fancymenu.menu.fancy.helper.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.scrollbar.ScrollBar;
import de.keksuccino.fancymenu.mixin.client.IMixinEditBox;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextEditorScreen extends Screen {

    //TODO wenn text mit mehreren Zeilen eingefügt wird (checken auf \n, etc.), eigenes Handling nutzen und text über mehrere Zeilen einfügen (neue Zeilen nach selected adden)
    //TODO wenn Leerzeichen an Anfang von Zeile, neu geaddete Zeile gleiche Anzahl Leerzeichen adden
    //TODO wenn left-click in bereich > (Y + line width) von letzter Zeile, dann letzte Zeile fokussieren und wenn < Y von erster Zeile, dann erste Zeile fokussieren

    private static final Logger LOGGER = LogManager.getLogger();

    protected Screen parentScreen;
    protected CharacterFilter characterFilter;
    protected List<TextEditorInputBox> textFieldLines = new ArrayList<>();
    protected ScrollBar verticalScrollBar = new ScrollBar(ScrollBar.ScrollBarDirection.VERTICAL, 10, 40, 0, 0, 0, 0, UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor());
    protected ScrollBar horizontalScrollBar = new ScrollBar(ScrollBar.ScrollBarDirection.HORIZONTAL, 40, 10, 0, 0, 0, 0, UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor());
    protected int lastCursorPosSetByUser = 0;
    protected boolean justSwitchedLineByWordDeletion = false;
    protected boolean focusedLineWasTooHigh = false;
    public int headerHeight = 50;
    public int footerHeight = 50;
    public int lineHeight = 20;
    protected int currentLineWidth;
    protected final int keyPressedListenerId;
    protected int lastTickFocusedLineIndex = -1;

    public TextEditorScreen(Component name, @Nullable Screen parent, @Nullable CharacterFilter characterFilter) {
        super(name);
        this.parentScreen = parent;
        this.characterFilter = characterFilter;
        this.addLine();
        this.getLine(0).setFocus(true);
        this.keyPressedListenerId = KeyboardHandler.addKeyPressedListener(this::onKeyPress);
    }

    @Override
    protected void init() {
        super.init();
        this.verticalScrollBar.scrollAreaEndX = this.width;
        this.verticalScrollBar.scrollAreaEndY = this.height-10;
        this.horizontalScrollBar.scrollAreaEndX = this.width-10;
        this.horizontalScrollBar.scrollAreaEndY = this.height;
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partial) {

        this.justSwitchedLineByWordDeletion = false;

        this.renderBackground(matrix);

        this.updateCurrentLineWidth();

        this.updateLines((line) -> {
            line.render(matrix, mouseX, mouseY, partial);
        });

        this.verticalScrollBar.render(matrix);
        this.horizontalScrollBar.render(matrix);

        super.render(matrix, mouseX, mouseY, partial);

        this.lastTickFocusedLineIndex = this.getFocusedLineIndex();
        this.focusedLineWasTooHigh = false;

        //TODO remove debug
        TextEditorInputBox ib = this.getLine(0);
        TextEditorInputBox ib2 = this.getLine(this.getLineCount()-1);
        int totalHeight = (ib2.getY() + ib2.getHeight()) - ib.getY();
        fill(matrix, ib.getX()-1, ib.getY(), ib.getX(), ib.getY() + totalHeight, Color.RED.getRGB());
        //--------------

    }

    protected void updateLines(@Nullable Consumer<TextEditorInputBox> doAfterLineUpdate) {
        //Update positions and size of lines and render them
        int index = 0;
        for (TextEditorInputBox line : this.textFieldLines) {
            line.y = this.headerHeight + (20 * index) + this.getLineRenderOffsetY();
            line.x = 20 + this.getLineRenderOffsetX();
            line.setWidth(this.currentLineWidth);
            line.setHeight(this.lineHeight);
            ((IMixinEditBox)line).setDisplayPosFancyMenu(0);
            if (doAfterLineUpdate != null) {
                doAfterLineUpdate.accept(line);
            }
            index++;
        }
    }

    protected void updateCurrentLineWidth() {
        //Find width of the longest line and update current line width
        int longestTextWidth = 0;
        for (TextEditorInputBox f : this.textFieldLines) {
            int textWidth = Minecraft.getInstance().font.width(f.getValue());
            if (textWidth > longestTextWidth) {
                longestTextWidth = textWidth;
            }
        }
        this.currentLineWidth = longestTextWidth + 30;
    }

    protected int getLineRenderOffsetX() {
        return -(int)(((float)this.currentLineWidth / 100.0F) * (this.horizontalScrollBar.getScroll() * 100.0F));
    }

    protected int getLineRenderOffsetY() {
        return -(int)(((float)this.getTotalLineHeight() / 100.0F) * (this.verticalScrollBar.getScroll() * 100.0F));
    }

    protected int getTotalLineHeight() {
        return this.lineHeight * this.textFieldLines.size();
    }

    protected TextEditorInputBox addLineAtIndex(int index) {
        TextEditorInputBox f = new TextEditorInputBox(Minecraft.getInstance().font, 0, 0, 50, this.lineHeight, true, this.characterFilter, this);
        f.setMaxLength(Integer.MAX_VALUE);
        this.textFieldLines.add(index, f);
        return f;
    }

    protected TextEditorInputBox addLine() {
        return this.addLineAtIndex(this.getLineCount());
    }

    protected void removeLineAtIndex(int index) {
        if (index < 1) {
            return;
        }
        if (index <= this.getLineCount()-1) {
            this.textFieldLines.remove(index);
        }
    }

    protected void removeLastLine() {
        this.removeLineAtIndex(this.getLineCount()-1);
    }

    public int getLineCount() {
        return this.textFieldLines.size();
    }

    @Nullable
    public TextEditorInputBox getLine(int index) {
        return this.textFieldLines.get(index);
    }

    public int getLineIndex(TextEditorInputBox inputBox) {
        return this.textFieldLines.indexOf(inputBox);
    }

    protected void goUpLine() {
        if (this.isLineFocused()) {
            int current = Math.max(0, this.getFocusedLineIndex());
            if (current > 0) {
                TextEditorInputBox currentLine = this.getLine(current);
                this.setFocusedLine(current - 1);
                if (currentLine != null) {
                    this.getFocusedLine().moveCursorTo(this.lastCursorPosSetByUser);
                }
            }
        }
    }

    protected void goDownLine(boolean isNewLine) {
        if (this.isLineFocused()) {
            int current = Math.max(0, this.getFocusedLineIndex());
            if (isNewLine) {
                this.addLineAtIndex(current+1);
            }
            TextEditorInputBox currentLine = this.getLine(current);
            this.setFocusedLine(current+1);
            if (currentLine != null) {
                TextEditorInputBox nextLine = this.getFocusedLine();
                if (isNewLine) {
                    //Split content of currentLine at cursor pos and move text after cursor to next line if ENTER was pressed
                    String textBeforeCursor = currentLine.getValue().substring(0, currentLine.getCursorPosition());
                    String textAfterCursor = currentLine.getValue().substring(currentLine.getCursorPosition());
                    currentLine.setValue(textBeforeCursor);
                    nextLine.setValue(textAfterCursor);
                    nextLine.moveCursorTo(0);
                } else {
                    nextLine.moveCursorTo(this.lastCursorPosSetByUser);
                }
            }
        }
    }

    protected void setFocusedLine(int index) {
        if (index <= this.getLineCount()-1) {
            for (TextEditorInputBox f : this.textFieldLines) {
                f.setFocus(false);
            }
            this.getLine(index).setFocus(true);
        }
    }

    /**
     * Returns the index of the focused line or -1 if no line is focused.
     **/
    protected int getFocusedLineIndex() {
        int index = 0;
        for (TextEditorInputBox f : this.textFieldLines) {
            if (f.isFocused()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Nullable
    protected TextEditorInputBox getFocusedLine() {
        if (this.isLineFocused()) {
            return this.getLine(this.getFocusedLineIndex());
        }
        return null;
    }

    protected boolean isLineFocused() {
        return (this.getFocusedLineIndex() > -1);
    }

    protected void onKeyPress(KeyboardData d) {

        if ((Minecraft.getInstance().screen != null) && (Minecraft.getInstance().screen == this)) {

            //ENTER
            if (d.keycode == 257) {
                if (this.isLineFocused()) {
                    this.goDownLine(true);
                }
            }
            //ARROW UP
            if (d.keycode == InputConstants.KEY_UP) {
                this.goUpLine();
            }
            //ARROW DOWN
            if (d.keycode == InputConstants.KEY_DOWN) {
                this.goDownLine(false);
            }

        }

    }

    @Override
    public void onClose() {
        if (this.parentScreen != null) {
            Minecraft.getInstance().setScreen(this.parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        KeyboardHandler.removeKeyPressedListener(this.keyPressedListenerId);
        super.removed();
    }

    protected int getEditBoxCursorX(EditBox editBox) {
        try {
            IMixinEditBox b = (IMixinEditBox) editBox;
            String s = this.font.plainSubstrByWidth(editBox.getValue().substring(b.getDisplayPosFancyMenu()), editBox.getInnerWidth());
            int j = editBox.getCursorPosition() - b.getDisplayPosFancyMenu();
            boolean flag = j >= 0 && j <= s.length();
            boolean flag2 = editBox.getCursorPosition() < editBox.getValue().length() || editBox.getValue().length() >= b.getMaxLengthFancyMenu();
            int l = b.getBorderedFancyMenu() ? editBox.getX() + 4 : editBox.getX();
            int j1 = l;
            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 += this.font.width(b.getFormatterFancyMenu().apply(s1, b.getDisplayPosFancyMenu()));
            }
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + editBox.getWidth() : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }
            return k1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void fixYScrollAfterDeletingLine(int yBeforeRemoving) {
        if (this.isLineFocused()) {
            this.updateCurrentLineWidth();
            this.updateLines(null);
            float currentScrollY = this.verticalScrollBar.getScroll();
            int totalLineHeight = this.getTotalLineHeight();
            if ((yBeforeRemoving < this.getLine(0).y) && !this.focusedLineWasTooHigh) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>> FIXING Y SCROLL AFTER DELETING LINE!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                float diff = this.getLine(0).y - yBeforeRemoving;
                float f = diff / (float)totalLineHeight;
                this.verticalScrollBar.setScroll(currentScrollY + f);
            }
        }
        if (this.getFocusedLineIndex() == 0) {
            this.verticalScrollBar.setScroll(0.0F);
        }
    }

    protected void fixYScrollAfterAddingLine(int yBeforeAdding) {
        int totalLineHeight = this.getTotalLineHeight();
        float currentScrollY = this.verticalScrollBar.getScroll();
        if (yBeforeAdding > this.getLine(0).y) {
            float f = (float)(yBeforeAdding - this.getLine(0).y) / (float)totalLineHeight;
            this.verticalScrollBar.setScroll(currentScrollY - f);
        }
    }

    public static class TextEditorInputBox extends AdvancedTextField {

        public TextEditorScreen parent;
        protected String lastTickValue = "";
        protected int lastTickLineCount = -1;
        protected int lastTickTotalLineHeight = 0;
        protected boolean cursorPositionTicked = false;

        public TextEditorInputBox(Font font, int x, int y, int width, int height, boolean handleSelf, @Nullable CharacterFilter characterFilter, TextEditorScreen parent) {
            super(font, x, y, width, height, handleSelf, characterFilter);
            this.parent = parent;
        }

        @Override
        public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {

            super.render(p_93657_, p_93658_, p_93659_, p_93660_);

            this.lastTickValue = this.getValue();
            this.lastTickLineCount = this.parent.getLineCount();
            this.lastTickTotalLineHeight = this.parent.getTotalLineHeight();

        }

        public int getRealHeight() {
            int h = this.height;
            if (((IMixinEditBox)this).getBorderedFancyMenu()) {
                h += 2;
            }
            return h;
        }

        @Override
        public void setCursorPosition(int newPos) {

            super.setCursorPosition(newPos);

            if ((newPos != this.parent.lastCursorPosSetByUser) && this.isFocused()) {
                this.parent.lastCursorPosSetByUser = this.getCursorPosition();
            }

            if (this.parent.isLineFocused() && (this.parent.getFocusedLine() == this)) {

                int xStart = this.x;
                int yStart = this.parent.getLine(0).y;

                this.parent.updateCurrentLineWidth();
                this.parent.updateLines(null);

                //Make the lines scroll horizontally with the cursor position if the cursor is too far to the left or right
                int cursorX = this.parent.getEditBoxCursorX(this);
                int maxToRight = this.parent.width - 20;
                int maxToLeft = 20;
                float currentScrollX = this.parent.horizontalScrollBar.getScroll();
                boolean textGotDeleted = this.lastTickValue.length() > this.getValue().length();
                if (cursorX > maxToRight) {
                    float f = (float)(cursorX - maxToRight) / (float)this.parent.currentLineWidth;
                    this.parent.horizontalScrollBar.setScroll(currentScrollX + f);
                } else if (cursorX < maxToLeft) {
                    //By default, move back the line just a little when moving the cursor to the left side by using the mouse or arrow keys
                    float f = (float)(maxToLeft - cursorX) / (float)this.parent.currentLineWidth;
                    //But move it back a big chunk when deleting chars (by pressing backspace)
                    if (textGotDeleted) {
                        f = (float)(maxToRight - maxToLeft) / (float)this.parent.currentLineWidth;
                    }
                    this.parent.horizontalScrollBar.setScroll(currentScrollX - f);
                } else if ((this.x < 0) && textGotDeleted && (xStart < this.x)) {
                    float f = (float)(this.x - xStart) / (float)this.parent.currentLineWidth;
                    this.parent.horizontalScrollBar.setScroll(currentScrollX + f);
                } else if (xStart > this.x) {
                    float f = (float)(xStart - this.x) / (float)this.parent.currentLineWidth;
                    this.parent.horizontalScrollBar.setScroll(currentScrollX - f);
                }
                if (this.getCursorPosition() == 0) {
                    this.parent.horizontalScrollBar.setScroll(0.0F);
                }

                //Make the lines scroll vertically with the cursor position if the cursor is too far up or down
                float currentScrollY = this.parent.verticalScrollBar.getScroll();
                int totalLineHeight = this.parent.getTotalLineHeight();
                if (this.parent.justSwitchedLineByWordDeletion) {
                    totalLineHeight -= this.parent.lineHeight;
                }
                boolean isNewLine = this.parent.lastTickFocusedLineIndex != this.parent.getLineIndex(this);
                if (isNewLine && !this.cursorPositionTicked) {
                    LOGGER.info("-------------------");
                    LOGGER.info("TOTAL LINE HEIGHT USE/FINAL: " + totalLineHeight);
                    if (this.y < this.parent.headerHeight) {
                        //TODO fixen: y korrektur zu ungenau, wenn getriggert bei löschen von Zeile (liegt vermutlich daran, dass offset nicht richtig korrigiert wird)
                        //TODO fixen: y korrektur zu ungenau, wenn getriggert bei löschen von Zeile (liegt vermutlich daran, dass offset nicht richtig korrigiert wird)
                        //TODO fixen: y korrektur zu ungenau, wenn getriggert bei löschen von Zeile (liegt vermutlich daran, dass offset nicht richtig korrigiert wird)
                        //TODO fixen: y korrektur zu ungenau, wenn getriggert bei löschen von Zeile (liegt vermutlich daran, dass offset nicht richtig korrigiert wird)
                        // -> vllt versuchen, den Teil in yFix methode oben zu packen und hier check adden, dass nur gefixt wird, wenn NICHT justSwitchedLine
                        this.parent.focusedLineWasTooHigh = true;
                        LOGGER.info("################# Y < MIN HEIGHT");
                        if (this.parent.justSwitchedLineByWordDeletion) {
                            LOGGER.info(">> JUST SWITCHED LINE!");
                        }
                        int diff = this.parent.headerHeight - this.y;
                        float f = (float)diff / (float)totalLineHeight;
                        this.parent.verticalScrollBar.setScroll(currentScrollY - f);
                    }
                    else if (this.y > (this.parent.height - this.parent.footerHeight - this.parent.lineHeight)) {
                        LOGGER.info("################# Y > MAX HEIGHT");
                        int diff = this.y - (this.parent.height - this.parent.footerHeight - this.parent.lineHeight);
                        float f = (float)diff / (float)totalLineHeight;
                        this.parent.verticalScrollBar.setScroll(currentScrollY + f);
                    }
                    else {
                        this.parent.fixYScrollAfterAddingLine(yStart);
                    }
                    LOGGER.info("PREV Y: " + yStart);
                    LOGGER.info("Y: " + this.parent.getLine(0).y);
                    LOGGER.info("Y END: " + (this.parent.getLine(this.parent.getLineCount()-1).y + this.parent.getLine(this.parent.getLineCount()-1).height));
                    LOGGER.info("INDEX: " + this.parent.getLineIndex(this));
                }
                if (this.parent.getFocusedLineIndex() == 0) {
                    this.parent.verticalScrollBar.setScroll(0.0F);
                }

            }

            this.cursorPositionTicked = true;

        }

        @Override
        public void onTick() {

            this.cursorPositionTicked = false;

            super.onTick();

        }

        @Override
        public boolean keyPressed(int p_94132_, int p_94133_, int p_94134_) {
            //TODO if multiline hightlighted, block Screen.isPaste and other stuff and write custom code for it
            return super.keyPressed(p_94132_, p_94133_, p_94134_);
        }

        @Override
        public void deleteChars(int i) {
            //If the user presses BACKSPACE and the cursor pos is at 0, it will jump one line up,
            //adds the text behind the cursor at the end of the new line and deletes the old line
            if (!this.parent.justSwitchedLineByWordDeletion) {
                if ((this.getCursorPosition() == 0) && (this.parent.getFocusedLineIndex() > 0)) {
                    int lastLineIndex = this.parent.getFocusedLineIndex();
                    int yBeforeRemoving = this.parent.getLine(0).y;
                    this.parent.justSwitchedLineByWordDeletion = true;
                    this.parent.goUpLine();
                    this.parent.getFocusedLine().moveCursorToEnd();
                    this.parent.getFocusedLine().insertText(this.getValue());
                    this.parent.getFocusedLine().setCursorPosition(this.parent.getFocusedLine().getCursorPosition()-this.getValue().length());
                    this.parent.getFocusedLine().setHighlightPos(this.parent.getFocusedLine().getCursorPosition());
                    if (lastLineIndex > 0) {
                        this.parent.removeLineAtIndex(this.parent.getFocusedLineIndex()+1);
                    }
                    this.parent.fixYScrollAfterDeletingLine(yBeforeRemoving);
                } else {
                    super.deleteChars(i);
                }
            }
        }

    }

}