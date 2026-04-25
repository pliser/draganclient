package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class ModeSettingComponent extends SettingComponent {
    private final ModeSetting mode;
    private final Animation hover = new Animation(0.0F);

    public ModeSettingComponent(ModeSetting setting) {
        super(setting);
        this.mode = setting;
    }

    @Override
    public void tick(int mouseX, int mouseY, float deltaSeconds) {
        hover.setTarget(bounds.contains(mouseX, mouseY) ? 1.0F : 0.0F);
        hover.tickSeconds(deltaSeconds, Animators.timeToResponse(130.0F));
    }

    @Override
    public int height() {
        return GuiMetrics.ROW_HEIGHT;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        if (hover.value() > 0.01F) {
            int hoverColor = Render2DUtil.withAlpha(0xFF222222, (int) (255 * hover.value() * 0.9F));
            Render2DUtil.roundedRect(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 2, Render2DUtil.multiplyAlpha(hoverColor, alpha));
        }

        // Setting name on the left
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            mode.name(),
            bounds.x() + 8,
            bounds.y() + 7,
            Math.max(10, bounds.width() / 2 - 8),
            Render2DUtil.multiplyAlpha(0xFFB9B9B9, alpha),
            false
        );

        // Current value on the right with accent color
        String valueText = "< " + mode.value() + " >";
        int valueWidth = Render2DUtil.textWidth(textRenderer, valueText);
        int valueX = bounds.x() + bounds.width() - valueWidth - 8;
        Render2DUtil.drawText(
            context,
            textRenderer,
            valueText,
            valueX,
            bounds.y() + 7,
            Render2DUtil.multiplyAlpha(0xFF4A90E2, alpha),
            false
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            mode.cycle();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            mode.cycleBack();
            return true;
        }
        return false;
    }
}
