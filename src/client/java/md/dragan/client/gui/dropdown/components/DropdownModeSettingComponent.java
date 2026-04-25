package md.dragan.client.gui.dropdown.components;

import md.dragan.client.gui.dropdown.DropdownTheme;
import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class DropdownModeSettingComponent extends DropdownSettingComponent {
    private final ModeSetting setting;
    private int cachedHeight = 29;

    public DropdownModeSettingComponent(ModeSetting setting) {
        this.setting = setting;
        this.height = 29;
    }

    @Override
    public int updateHeight(TextRenderer textRenderer) {
        int offsetX = 0;
        int rows = 1;
        int maxWidth = Math.max(10, width - 16);
        for (String mode : setting.modes()) {
            int chipWidth = Render2DUtil.textWidth(textRenderer, mode) + 6;
            if (offsetX + chipWidth >= maxWidth) {
                offsetX = 0;
                rows++;
            }
            offsetX += chipWidth + 6;
        }
        cachedHeight = 20 + rows * 12;
        return cachedHeight;
    }

    @Override
    public int height() {
        return cachedHeight;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        Render2DUtil.drawText(context, textRenderer, setting.name(), x + 6, y + 3, DropdownTheme.TEXT_PRIMARY, false);

        int offsetX = 0;
        int offsetY = 12;
        int maxWidth = Math.max(10, width - 16);
        for (String mode : setting.modes()) {
            int textWidth = Render2DUtil.textWidth(textRenderer, mode);
            int chipWidth = textWidth + 6;
            if (offsetX + chipWidth >= maxWidth) {
                offsetX = 0;
                offsetY += 12;
            }
            int chipX = x + 8 + offsetX;
            int chipY = y + offsetY;
            boolean selected = mode.equals(setting.value());
            int chipBg = selected ? DropdownTheme.ACCENT_HIGHLIGHT : DropdownTheme.ACCENT;
            Render2DUtil.roundedRect(context, chipX - 1, chipY - 1, chipWidth + 2, 10, 3, DropdownTheme.PANEL_OUTLINE);
            Render2DUtil.roundedRect(context, chipX, chipY, chipWidth, 9, 3, chipBg);
            int color = selected ? DropdownTheme.TEXT_PRIMARY : DropdownTheme.TEXT_SECONDARY;
            Render2DUtil.drawText(context, textRenderer, mode, chipX + 3, chipY + 1, color, false);
            offsetX += chipWidth + 6;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int offsetX = 0;
        int offsetY = 12;
        int maxWidth = Math.max(10, width - 16);
        for (String mode : setting.modes()) {
            int textWidth = Render2DUtil.textWidth(net.minecraft.client.MinecraftClient.getInstance().textRenderer, mode);
            int chipWidth = textWidth + 6;
            if (offsetX + chipWidth >= maxWidth) {
                offsetX = 0;
                offsetY += 12;
            }
            int chipX = x + 8 + offsetX;
            int chipY = y + offsetY;
            if (mouseX >= chipX && mouseX <= chipX + chipWidth && mouseY >= chipY && mouseY <= chipY + 9) {
                setting.setIndex(setting.modes().indexOf(mode));
                return true;
            }
            offsetX += chipWidth + 6;
        }
        return false;
    }
}
