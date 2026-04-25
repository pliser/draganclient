package md.dragan.client.gui.modernclick.component;

import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class ModuleButton {
    private static final int BASE_HEIGHT = 22;
    private static final int EXPAND_HEIGHT = 14;
    private final GuiModule module;
    private final Animation hover = new Animation(0.0F);
    private final Animation expand = new Animation(0.0F);
    private GuiRect bounds = new GuiRect(0, 0, 0, 0);

    public ModuleButton(GuiModule module) {
        this.module = module;
    }

    public GuiModule module() {
        return module;
    }

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public GuiRect bounds() {
        return bounds;
    }

    public int totalHeight() {
        return BASE_HEIGHT + Math.round(EXPAND_HEIGHT * expand.value());
    }

    public void tickHover(boolean hovered, float deltaSeconds) {
        hover.setTarget(hovered ? 1.0F : 0.0F);
        hover.tickSeconds(deltaSeconds, Animators.timeToResponse(130.0F));
        expand.setTarget(module.expanded() ? 1.0F : 0.0F);
        expand.tickSeconds(deltaSeconds, Animators.timeToResponse(150.0F));
    }

    public void render(DrawContext context, TextRenderer textRenderer, boolean selected, float alpha) {
        int rowColor = selected
            ? GuiTheme.SURFACE_ELEVATED
            : Render2DUtil.withAlpha(GuiTheme.HOVER_BG, (int) (255 * (0.10F + hover.value() * 0.78F)));
        Render2DUtil.roundedRect(context, bounds.x(), bounds.y(), bounds.width(), BASE_HEIGHT, 7, Render2DUtil.multiplyAlpha(rowColor, alpha));
        Render2DUtil.border(context, bounds.x(), bounds.y(), bounds.width(), BASE_HEIGHT, Render2DUtil.multiplyAlpha(selected ? GuiTheme.ACCENT_DIM : GuiTheme.BORDER_SOFT, alpha));
        Render2DUtil.rect(context, bounds.x() + 5, bounds.y() + 3, bounds.width() - 10, 1, Render2DUtil.multiplyAlpha(0x18FFFFFF, alpha));

        if (selected) {
            Render2DUtil.rect(
                context,
                bounds.x() + 7,
                bounds.y() + 5,
                3,
                BASE_HEIGHT - 10,
                Render2DUtil.multiplyAlpha(GuiTheme.ACCENT, alpha)
            );
        }

        String bindText = module.hasKeyBind()
            ? org.lwjgl.glfw.GLFW.glfwGetKeyName(module.keyBind(), 0) != null
                ? org.lwjgl.glfw.GLFW.glfwGetKeyName(module.keyBind(), 0).toUpperCase()
                : md.dragan.client.command.BindCommandHandler.formatKey(module.keyBind())
            : "NONE";
        int bindW = Math.min(52, Math.max(24, Render2DUtil.textWidth(textRenderer, bindText) + 10));
        int bindX = bounds.x() + bounds.width() - bindW - 10;
        Render2DUtil.roundedRect(
            context,
            bindX,
            bounds.y() + 4,
            bindW,
            14,
            5,
            Render2DUtil.multiplyAlpha(module.hasKeyBind() ? GuiTheme.ACCENT_DIM : GuiTheme.SURFACE_ALT, alpha)
        );
        Render2DUtil.border(
            context,
            bindX,
            bounds.y() + 4,
            bindW,
            14,
            Render2DUtil.multiplyAlpha(module.hasKeyBind() ? GuiTheme.ACCENT : GuiTheme.BORDER_SOFT, alpha * 0.65F)
        );
        Render2DUtil.drawCenteredText(
            context,
            textRenderer,
            bindText,
            bindX + bindW / 2,
            bounds.y() + 8,
            Render2DUtil.multiplyAlpha(module.hasKeyBind() ? GuiTheme.TEXT_PRIMARY : GuiTheme.TEXT_MUTED, alpha),
            false
        );

        int textColor = module.enabled() ? GuiTheme.TEXT_PRIMARY : GuiTheme.TEXT_SECONDARY;
        int textMax = Math.max(10, bounds.width() - bindW - 34);
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            module.name(),
            bounds.x() + 18,
            bounds.y() + 8,
            textMax,
            Render2DUtil.multiplyAlpha(textColor, alpha),
            false
        );

        int dot = module.enabled() ? GuiTheme.SUCCESS : GuiTheme.ACCENT_DIM;
        Render2DUtil.roundedRect(
            context,
            bindX - 12,
            bounds.y() + 8,
            6,
            6,
            2,
            Render2DUtil.multiplyAlpha(dot, alpha)
        );

        int extra = Math.round(EXPAND_HEIGHT * expand.value());
        if (extra > 0) {
            int extraY = bounds.y() + BASE_HEIGHT;
            Render2DUtil.rect(
                context,
                bounds.x() + 8,
                extraY,
                bounds.width() - 16,
                extra,
                Render2DUtil.multiplyAlpha(GuiTheme.SURFACE_ALT, alpha * expand.value())
            );
            Render2DUtil.drawTextClipped(
                context,
                textRenderer,
                "toggle / inspect",
                bounds.x() + 14,
                extraY + 3,
                textMax,
                Render2DUtil.multiplyAlpha(GuiTheme.TEXT_MUTED, alpha * expand.value()),
                false
            );
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiRect clickBounds = new GuiRect(bounds.x(), bounds.y(), bounds.width(), BASE_HEIGHT);
        if (!clickBounds.contains(mouseX, mouseY)) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            module.setEnabled(!module.enabled());
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            module.setExpanded(!module.expanded());
            return true;
        }
        return false;
    }
}
