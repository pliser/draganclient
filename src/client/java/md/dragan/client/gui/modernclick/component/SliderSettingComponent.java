package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class SliderSettingComponent extends SettingComponent {
    private final SliderSetting slider;
    private final Animation hover = new Animation(0.0F);
    private boolean dragging;

    public SliderSettingComponent(SliderSetting setting) {
        super(setting);
        this.slider = setting;
    }

    @Override
    public void tick(int mouseX, int mouseY, float deltaSeconds) {
        hover.setTarget(bounds.contains(mouseX, mouseY) ? 1.0F : 0.0F);
        hover.tickSeconds(deltaSeconds, Animators.timeToResponse(130.0F));
        if (dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public int height() {
        return GuiMetrics.ROW_HEIGHT + 8;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        if (hover.value() > 0.01F) {
            int hoverColor = Render2DUtil.withAlpha(0xFF222222, (int) (255 * hover.value() * 0.9F));
            Render2DUtil.roundedRect(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 2, Render2DUtil.multiplyAlpha(hoverColor, alpha));
        }

        String valueText = String.format("%.2fx", slider.value());
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            slider.name(),
            bounds.x() + 8,
            bounds.y() + 5,
            Math.max(10, bounds.width() / 2),
            Render2DUtil.multiplyAlpha(0xFFB9B9B9, alpha),
            false
        );
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            valueText,
            bounds.x() + bounds.width() / 2,
            bounds.y() + 5,
            Math.max(10, bounds.width() / 2 - 8),
            Render2DUtil.multiplyAlpha(0xFF4A90E2, alpha),
            false
        );

        int trackX = bounds.x() + 8;
        int trackY = bounds.y() + bounds.height() - 10;
        int trackW = bounds.width() - 16;
        Render2DUtil.roundedRect(context, trackX, trackY, trackW, 4, 2, Render2DUtil.multiplyAlpha(0xFF353535, alpha));
        int fillW = Math.max(4, Math.round(trackW * slider.normalized()));
        Render2DUtil.roundedRect(context, trackX, trackY, fillW, 4, 2, Render2DUtil.multiplyAlpha(0xFF4A90E2, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        dragging = true;
        updateValue(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX) {
        int trackX = bounds.x() + 8;
        int trackW = Math.max(1, bounds.width() - 16);
        float normalized = (float) ((mouseX - trackX) / trackW);
        normalized = Math.max(0.0F, Math.min(1.0F, normalized));
        slider.setValue(slider.min() + (slider.max() - slider.min()) * normalized);
    }
}
