package md.dragan.client.gui.dropdown.components;

import md.dragan.client.gui.dropdown.DropdownTheme;
import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class DropdownSliderSettingComponent extends DropdownSettingComponent {
    private final SliderSetting setting;
    private boolean dragging;

    public DropdownSliderSettingComponent(SliderSetting setting) {
        this.setting = setting;
        this.height = 26;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY);
        int bg = hovered ? DropdownTheme.PANEL_SETTING_HOVER : DropdownTheme.PANEL_SETTING;
        Render2DUtil.roundedRect(context, x, y, width, height, 4, bg);
        Render2DUtil.border(context, x, y, width, height, DropdownTheme.PANEL_OUTLINE);

        String valueText = String.format("%.2fx", setting.value());
        int valueWidth = Render2DUtil.textWidth(textRenderer, valueText);
        Render2DUtil.drawTextClipped(context, textRenderer, setting.name(), x + 6, y + 4, width / 2, DropdownTheme.TEXT_SECONDARY, false);
        Render2DUtil.drawText(context, textRenderer, valueText, x + width - valueWidth - 6, y + 4, DropdownTheme.TEXT_PRIMARY, false);

        int trackX = x + 6;
        int trackY = y + height - 8;
        int trackW = width - 12;
        Render2DUtil.roundedRect(context, trackX, trackY, trackW, 4, 2, 0xFF1A1522);
        int fillW = Math.max(4, Math.round(trackW * setting.normalized()));
        Render2DUtil.roundedRect(context, trackX, trackY, fillW, 4, 2, DropdownTheme.ACCENT_HIGHLIGHT);

        if (dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!contains(mouseX, mouseY) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        dragging = true;
        updateValue(mouseX);
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
        }
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(double mouseX) {
        int trackX = x + 6;
        int trackW = Math.max(1, width - 12);
        float normalized = (float) ((mouseX - trackX) / trackW);
        normalized = Math.max(0.0F, Math.min(1.0F, normalized));
        setting.setValue(setting.min() + (setting.max() - setting.min()) * normalized);
    }
}
