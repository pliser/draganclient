package md.dragan.client.gui.dropdown.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class DropdownSettingComponent {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int updateHeight(TextRenderer textRenderer) {
        return height();
    }

    public int height() {
        return height;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
    }

    public void charTyped(char codePoint, int modifiers) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    protected boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
