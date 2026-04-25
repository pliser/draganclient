package md.dragan.client.gui.dropdown;

import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

final class DropdownSearchField {
    private int x;
    private int y;
    private int width;
    private int height;
    private final String placeholder;
    private final StringBuilder text = new StringBuilder();
    private boolean focused;

    DropdownSearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
    }

    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        boolean hover = contains(mouseX, mouseY);
        int bg = hover ? DropdownTheme.PANEL_SETTING_HOVER : DropdownTheme.PANEL_SETTING;
        Render2DUtil.roundedRect(context, x, y, width, height, 4, bg);
        Render2DUtil.border(context, x, y, width, height, DropdownTheme.PANEL_OUTLINE);
        String display = text.length() == 0 ? placeholder : text.toString();
        int color = text.length() == 0 ? DropdownTheme.TEXT_SECONDARY : DropdownTheme.TEXT_PRIMARY;
        Render2DUtil.drawTextClipped(context, textRenderer, display, x + 6, y + 4, width - 12, color, false);
    }

    boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        focused = contains(mouseX, mouseY);
        return focused;
    }

    boolean charTyped(char codePoint, int modifiers) {
        if (!focused) {
            return false;
        }
        if (Character.isISOControl(codePoint)) {
            return false;
        }
        text.append(codePoint);
        return true;
    }

    boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return true;
        }
        return false;
    }

    String text() {
        return text.toString();
    }

    private boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
