package md.dragan.client.gui.modernclick;

import md.dragan.client.gui.modernclick.component.CategoryPanel;
import md.dragan.client.gui.modernclick.component.ModulePanel;
import md.dragan.client.gui.modernclick.component.SettingsPanel;
import md.dragan.client.gui.modernclick.layout.GuiLayout;
import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.FramebufferBlurRenderer;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudBootstrap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiScreen extends Screen {
    private final CategoryPanel categoryPanel = new CategoryPanel();
    private final ModulePanel modulePanel = new ModulePanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final Animation openAnimation = new Animation(0.0F);
    private final FramebufferBlurRenderer blurRenderer = new FramebufferBlurRenderer();
    private GuiLayout layout;
    private int frameX;
    private int frameY;
    private int frameW;
    private int frameH;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public ClickGuiScreen() {
        super(Text.literal("Modern ClickGUI"));
        GuiStateStore.bootstrap();
    }

    @Override
    protected void init() {
        super.init();
        frameW = GuiLayout.frameWidthFor(width);
        frameH = GuiLayout.frameHeightFor(height);
        frameX = (width - frameW) / 2;
        frameY = (height - frameH) / 2;
        rebuildLayout();
        openAnimation.snap(0.0F);
        openAnimation.setTarget(1.0F);
        HudBootstrap.setEditorMode(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float deltaSeconds = Math.max(0.001F, delta / 20.0F);
        openAnimation.tickSeconds(deltaSeconds, Animators.timeToResponse(140.0F));
        categoryPanel.tick(mouseX, mouseY, deltaSeconds);
        modulePanel.tick(mouseX, mouseY, deltaSeconds);
        settingsPanel.tick(mouseX, mouseY, deltaSeconds);

        float alpha = clamp01(openAnimation.value());
        int overlay = GuiStateStore.isModuleEnabled("Blur") ? 0x55121212 : GuiTheme.SCREEN_BG;
        Render2DUtil.rect(context, 0, 0, width, height, Render2DUtil.multiplyAlpha(overlay, alpha));

        GuiRect frame = layout.frame();
        if (GuiStateStore.isModuleEnabled("Blur")) {
            blurRenderer.apply(client, frame.x() + 1, frame.y() + 1, frame.width() - 2, frame.height() - 2);
        }

        Render2DUtil.roundedRect(context, frame.x() - 6, frame.y() - 6, frame.width() + 12, frame.height() + 12, GuiMetrics.CORNER_RADIUS + 2, Render2DUtil.multiplyAlpha(0x44192D3E, alpha));
        Render2DUtil.roundedRect(context, frame.x() - 10, frame.y() - 10, frame.width() + 20, frame.height() + 20, GuiMetrics.CORNER_RADIUS + 6, Render2DUtil.multiplyAlpha(0x16000000, alpha));
        Render2DUtil.roundedRect(context, frame.x(), frame.y(), frame.width(), frame.height(), GuiMetrics.CORNER_RADIUS, Render2DUtil.multiplyAlpha(GuiTheme.CONTAINER_BG, alpha));
        Render2DUtil.border(context, frame.x(), frame.y(), frame.width(), frame.height(), Render2DUtil.multiplyAlpha(GuiTheme.BORDER, alpha));
        Render2DUtil.rect(context, frame.x() + 10, frame.y() + 10, frame.width() - 20, 1, Render2DUtil.multiplyAlpha(0x18FFFFFF, alpha));

        GuiRect header = layout.header();
        Render2DUtil.roundedRect(context, header.x(), header.y(), header.width(), header.height(), GuiMetrics.CORNER_RADIUS, Render2DUtil.multiplyAlpha(0xF0111821, alpha));
        Render2DUtil.border(context, header.x(), header.y(), header.width(), header.height(), Render2DUtil.multiplyAlpha(GuiTheme.BORDER_SOFT, alpha));
        Render2DUtil.rect(context, header.x() + 12, header.y() + 10, 36, 2, Render2DUtil.multiplyAlpha(GuiTheme.ACCENT, alpha));
        Render2DUtil.rect(context, header.x() + 52, header.y() + 10, 14, 2, Render2DUtil.multiplyAlpha(GuiTheme.ACCENT_WARM, alpha));
        Render2DUtil.drawText(context, textRenderer, "Dragan", header.x() + 12, header.y() + 15, Render2DUtil.multiplyAlpha(GuiTheme.TEXT_PRIMARY, alpha), false);
        Render2DUtil.drawText(context, textRenderer, "combat surface", header.x() + 68, header.y() + 15, Render2DUtil.multiplyAlpha(GuiTheme.TEXT_MUTED, alpha), false);
        int pillW = 60;
        int pillX = header.x() + header.width() - pillW - 12;
        Render2DUtil.roundedRect(context, pillX, header.y() + 8, pillW, 18, 6, Render2DUtil.multiplyAlpha(GuiTheme.ACCENT_DIM, alpha));
        Render2DUtil.border(context, pillX, header.y() + 8, pillW, 18, Render2DUtil.multiplyAlpha(GuiTheme.ACCENT, alpha * 0.5F));
        Render2DUtil.drawCenteredText(context, textRenderer, "RSHIFT", pillX + pillW / 2, header.y() + 13, Render2DUtil.multiplyAlpha(GuiTheme.TEXT_PRIMARY, alpha), false);
        int hintX = header.x() + 220;
        int hintW = Math.max(20, pillX - hintX - 10);
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            "drag window  /  lmb toggle  /  rmb inspect  /  bind on right",
            hintX,
            header.y() + 16,
            hintW,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_SECONDARY, alpha),
            false
        );

        drawColumnBackground(context, layout.modules(), GuiTheme.PANEL_CENTER, alpha);
        drawColumnBackground(context, layout.settings(), GuiTheme.PANEL_RIGHT, alpha);
        categoryPanel.render(context, textRenderer, mouseX, mouseY, alpha);
        modulePanel.render(context, textRenderer, mouseX, mouseY, alpha);
        settingsPanel.render(context, textRenderer, mouseX, mouseY, alpha);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (layout == null) {
            return false;
        }
        if (openAnimation.value() < 0.95F) {
            return true;
        }
        if (HudBootstrap.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && layout.header().contains(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - frameX;
            dragOffsetY = mouseY - frameY;
            return true;
        }
        if (categoryPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (modulePanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (settingsPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (HudBootstrap.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (settingsPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (HudBootstrap.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            frameX = (int) Math.round(mouseX - dragOffsetX);
            frameY = (int) Math.round(mouseY - dragOffsetY);
            clampFrame();
            rebuildLayout();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (modulePanel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        if (settingsPanel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingsPanel.keyPressed(keyCode, scanCode)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (settingsPanel.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        clampFrame();
        rebuildLayout();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        HudBootstrap.setEditorMode(false);
        blurRenderer.close();
        super.removed();
    }

    private void rebuildLayout() {
        layout = new GuiLayout(frameX, frameY, frameW, frameH);
        categoryPanel.setBounds(layout.categories());
        modulePanel.setBounds(layout.modules());
        settingsPanel.setBounds(layout.settings());
    }

    private void clampFrame() {
        frameX = Math.max(4, Math.min(width - frameW - 4, frameX));
        frameY = Math.max(4, Math.min(height - frameH - 4, frameY));
    }

    private void drawColumnBackground(DrawContext context, GuiRect rect, int color, float alpha) {
        Render2DUtil.roundedRect(context, rect.x(), rect.y(), rect.width(), rect.height(), GuiMetrics.CORNER_RADIUS, Render2DUtil.multiplyAlpha(color, alpha));
        Render2DUtil.border(context, rect.x(), rect.y(), rect.width(), rect.height(), Render2DUtil.multiplyAlpha(GuiTheme.BORDER, alpha));
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
