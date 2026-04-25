package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.model.TextSetting;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class TextSettingComponent extends SettingComponent {
    private static TextSettingComponent focused;

    private final TextSetting text;
    private final Animation hover = new Animation(0.0F);

    public TextSettingComponent(TextSetting setting) {
        super(setting);
        this.text = setting;
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
            int hoverColor = Render2DUtil.withAlpha(0xFF222222, (int) (255 * hover.value() * 0.75F));
            Render2DUtil.roundedRect(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 4, Render2DUtil.multiplyAlpha(hoverColor, alpha));
        }

        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            text.name(),
            bounds.x() + 8,
            bounds.y() + 8,
            Math.max(10, bounds.width() / 2 - 10),
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_SECONDARY, alpha),
            false
        );

        int fieldW = Math.max(76, bounds.width() / 2 - 8);
        int fieldX = bounds.x() + bounds.width() - fieldW - 8;
        int fieldY = bounds.y() + 4;
        int fieldH = bounds.height() - 8;
        boolean active = focused == this;
        Render2DUtil.roundedRect(
            context,
            fieldX,
            fieldY,
            fieldW,
            fieldH,
            4,
            Render2DUtil.multiplyAlpha(active ? GuiTheme.SURFACE_ELEVATED : GuiTheme.SURFACE_ALT, alpha)
        );
        Render2DUtil.border(
            context,
            fieldX,
            fieldY,
            fieldW,
            fieldH,
            Render2DUtil.multiplyAlpha(active ? GuiTheme.ACCENT : GuiTheme.BORDER_SOFT, alpha)
        );

        String textValue = text.value();
        if (active && (System.currentTimeMillis() / 450L) % 2L == 0L) {
            textValue += "_";
        }
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            textValue,
            fieldX + 6,
            fieldY + 4,
            fieldW - 12,
            Render2DUtil.multiplyAlpha(active ? GuiTheme.TEXT_PRIMARY : GuiTheme.TEXT_MUTED, alpha),
            false
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!bounds.contains(mouseX, mouseY)) {
            if (focused == this) {
                focused = null;
            }
            return false;
        }
        focused = this;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode) {
        if (focused != this) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            focused = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            String value = text.value();
            if (!value.isEmpty()) {
                text.setValue(value.substring(0, value.length() - 1));
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            text.setValue("");
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focused != this) {
            return false;
        }
        if (!isAllowed(chr)) {
            return false;
        }
        String current = text.value();
        if (current.length() >= text.maxLength()) {
            return true;
        }
        text.setValue(current + chr);
        return true;
    }

    private static boolean isAllowed(char chr) {
        return (chr >= '0' && chr <= '9') || chr == '-' || chr == '+';
    }
}
