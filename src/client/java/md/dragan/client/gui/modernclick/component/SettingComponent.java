package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.model.GuiSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class SettingComponent {
    protected final GuiSetting setting;
    protected GuiRect bounds = new GuiRect(0, 0, 0, 0);

    protected SettingComponent(GuiSetting setting) {
        this.setting = setting;
    }

    public GuiSetting setting() {
        return setting;
    }

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public GuiRect bounds() {
        return bounds;
    }

    public void tick(int mouseX, int mouseY, float deltaSeconds) {
    }

    public abstract int height();

    public abstract void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha);

    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}
