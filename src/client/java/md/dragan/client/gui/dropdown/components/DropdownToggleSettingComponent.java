package md.dragan.client.gui.dropdown.components;

import md.dragan.client.gui.dropdown.DropdownTheme;
import md.dragan.client.gui.modernclick.model.ToggleSetting;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class DropdownToggleSettingComponent extends DropdownSettingComponent {
    private final ToggleSetting setting;

    public DropdownToggleSettingComponent(ToggleSetting setting) {
        this.setting = setting;
        this.height = 23;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY);
        int bg = hovered ? DropdownTheme.PANEL_SETTING_HOVER : DropdownTheme.PANEL_SETTING;
        Render2DUtil.roundedRect(context, x, y, width, height, 4, bg);
        Render2DUtil.border(context, x, y, width, height, DropdownTheme.PANEL_OUTLINE);
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            setting.name(),
            x + 6,
            y + 6,
            width - 22,
            DropdownTheme.TEXT_SECONDARY,
            false
        );
        int dotColor = setting.value() ? DropdownTheme.ACCENT_HIGHLIGHT : 0xFF2B2436;
        Render2DUtil.roundedRect(context, x + width - 12, y + 7, 6, 6, 2, dotColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!contains(mouseX, mouseY) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        setting.setValue(!setting.value());
        return true;
    }
}
