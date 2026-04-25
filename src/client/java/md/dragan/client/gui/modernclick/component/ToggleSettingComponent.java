package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.model.ToggleSetting;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class ToggleSettingComponent extends SettingComponent {
    private final ToggleSetting toggle;
    private final Animation hover = new Animation(0.0F);

    public ToggleSettingComponent(ToggleSetting setting) {
        super(setting);
        this.toggle = setting;
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

        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            toggle.name(),
            bounds.x() + 8,
            bounds.y() + 7,
            Math.max(10, bounds.width() - 24),
            Render2DUtil.multiplyAlpha(0xFFB9B9B9, alpha),
            false
        );

        int dotColor = toggle.value() ? 0xFF4A90E2 : 0xFF444444;
        int dotX = bounds.x() + bounds.width() - 13;
        Render2DUtil.roundedRect(context, dotX, bounds.y() + 8, 6, 6, 2, Render2DUtil.multiplyAlpha(dotColor, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        toggle.setValue(!toggle.value());
        return true;
    }
}
