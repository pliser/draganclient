package md.dragan.client.gui.modernclick.component;

import java.io.IOException;
import java.util.List;
import md.dragan.client.config.ClientConfigManager;
import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class ConfigPanel {
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int LABEL_HEIGHT = 16;
    private final String[] presets = {"default", "pvp", "visual", "legit"};
    private GuiRect bounds = new GuiRect(0, 0, 0, 0);

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public GuiRect bounds() {
        return bounds;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        int x = bounds.x();
        int y = bounds.y();
        int w = bounds.width();

        Render2DUtil.drawText(context, textRenderer, "Configs", x, y, Render2DUtil.multiplyAlpha(0xFFE9E9E9, alpha), false);
        y += LABEL_HEIGHT;

        drawButton(context, textRenderer, x, y, w, "Save default", alpha);
        drawButton(context, textRenderer, x, y + BUTTON_HEIGHT + GAP, w, "Load default", alpha);
        drawButton(context, textRenderer, x, y + (BUTTON_HEIGHT + GAP) * 2, w, "Open config dir", alpha);

        y += (BUTTON_HEIGHT + GAP) * 3 + 10;
        Render2DUtil.drawText(context, textRenderer, "Presets", x, y, Render2DUtil.multiplyAlpha(0xFFB9B9B9, alpha), false);
        y += LABEL_HEIGHT;
        for (int i = 0; i < presets.length; i++) {
            drawButton(context, textRenderer, x, y + i * (BUTTON_HEIGHT + GAP), w, presets[i], alpha);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !bounds.contains(mouseX, mouseY)) {
            return false;
        }

        int x = bounds.x();
        int y = bounds.y() + LABEL_HEIGHT;
        int w = bounds.width();

        try {
            if (inside(mouseX, mouseY, x, y, w, BUTTON_HEIGHT)) {
                ClientConfigManager.saveDefault();
                HudBootstrap.notify("Config", "Saved default", NotificationType.SUCCESS);
                return true;
            }
            if (inside(mouseX, mouseY, x, y + BUTTON_HEIGHT + GAP, w, BUTTON_HEIGHT)) {
                ClientConfigManager.loadDefault();
                HudBootstrap.notify("Config", "Loaded default", NotificationType.SUCCESS);
                return true;
            }
            if (inside(mouseX, mouseY, x, y + (BUTTON_HEIGHT + GAP) * 2, w, BUTTON_HEIGHT)) {
                HudBootstrap.notify("Config Dir", ClientConfigManager.configDirectory().toAbsolutePath().toString(), NotificationType.INFO);
                return true;
            }

            int presetY = y + (BUTTON_HEIGHT + GAP) * 3 + 10 + LABEL_HEIGHT;
            for (String preset : presets) {
                if (inside(mouseX, mouseY, x, presetY, w, BUTTON_HEIGHT)) {
                    if (ClientConfigManager.exists(preset)) {
                        ClientConfigManager.load(preset);
                        HudBootstrap.notify("Config", "Loaded " + preset, NotificationType.SUCCESS);
                    } else {
                        ClientConfigManager.save(preset);
                        HudBootstrap.notify("Config", "Saved " + preset, NotificationType.SUCCESS);
                    }
                    return true;
                }
                presetY += BUTTON_HEIGHT + GAP;
            }
        } catch (IOException exception) {
            HudBootstrap.notify("Config", exception.getMessage(), NotificationType.ERROR);
            return true;
        }

        return false;
    }

    public int preferredHeight() {
        return LABEL_HEIGHT + (BUTTON_HEIGHT + GAP) * 3 + 10 + LABEL_HEIGHT + presets.length * (BUTTON_HEIGHT + GAP);
    }

    public List<String> presetNames() {
        return List.of(presets);
    }

    private void drawButton(DrawContext context, TextRenderer textRenderer, int x, int y, int w, String text, float alpha) {
        Render2DUtil.roundedRect(context, x, y, w, BUTTON_HEIGHT, 3, Render2DUtil.multiplyAlpha(0xFF202020, alpha));
        Render2DUtil.drawTextClipped(context, textRenderer, text, x + 8, y + 6, w - 16, Render2DUtil.multiplyAlpha(0xFFE9E9E9, alpha), false);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
