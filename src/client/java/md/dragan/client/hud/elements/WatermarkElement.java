package md.dragan.client.hud.elements;

import md.dragan.client.hud.HudElement;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class WatermarkElement extends HudElement {
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 7;
    private static final int PANEL_BG = 0xE20D131B;
    private static final int PANEL_INNER = 0xD3131B25;
    private static final int PANEL_BORDER = 0xFF223344;
    private static final int TEXT_MAIN = 0xFFF4F8FC;
    private static final int TEXT_SUB = 0xFF9CB0C5;
    private static final int ACCENT = 0xFF77D0FF;
    private static final int ACCENT_ALT = 0xFFFFBE73;

    private final String clientName;
    private final String version;
    private final Animation fade = new Animation(1.0F);

    private boolean showFps = true;
    private String cachedTitle = "";
    private String cachedInfo = "";

    public WatermarkElement(String clientName, String version, float defaultX, float defaultY) {
        super("watermark", defaultX, defaultY);
        this.clientName = clientName;
        this.version = version;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
        markBoundsDirty();
    }

    @Override
    protected void measure(MinecraftClient client) {
        rebuildText(client);
        int titleWidth = Render2DUtil.textWidth(client.textRenderer, cachedTitle);
        int infoWidth = Render2DUtil.textWidth(client.textRenderer, cachedInfo);
        int contentWidth = Math.max(titleWidth, infoWidth);
        width = contentWidth + PADDING_X * 2 + 20;
        height = 30;
    }

    @Override
    public void update() {
        fade.setTarget(isVisible() ? 1.0F : 0.0F);
        fade.tick(Animators.expAlpha(Animators.timeToResponse(140.0F), 1.0F / 60.0F));
        markBoundsDirty();
    }

    @Override
    public void render(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        rebuildText(client);

        float alpha = Math.max(0.0F, Math.min(1.0F, fade.value()));
        if (alpha <= 0.01F) {
            return;
        }

        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);

        Render2DUtil.roundedRect(context, ix - 2, iy - 2, iw + 4, ih + 4, 8, Render2DUtil.multiplyAlpha(0x18000000, alpha));
        Render2DUtil.roundedRect(context, ix, iy, iw, ih, 7, Render2DUtil.multiplyAlpha(PANEL_BG, alpha));
        Render2DUtil.roundedRect(context, ix + 1, iy + 1, iw - 2, ih - 2, 6, Render2DUtil.multiplyAlpha(PANEL_INNER, alpha));
        Render2DUtil.border(context, ix, iy, iw, ih, Render2DUtil.multiplyAlpha(PANEL_BORDER, alpha));
        Render2DUtil.rect(context, ix + 8, iy + 8, 24, 2, Render2DUtil.multiplyAlpha(ACCENT, alpha));
        Render2DUtil.rect(context, ix + 36, iy + 8, 12, 2, Render2DUtil.multiplyAlpha(ACCENT_ALT, alpha));

        int textX = ix + PADDING_X;
        int titleY = iy + 12;
        int infoY = titleY + 10;
        int textW = Math.max(10, iw - PADDING_X * 2);
        Render2DUtil.drawTextClipped(
            context,
            client.textRenderer,
            cachedTitle,
            textX,
            titleY,
            textW,
            Render2DUtil.multiplyAlpha(TEXT_MAIN, alpha),
            false
        );
        Render2DUtil.drawTextClipped(
            context,
            client.textRenderer,
            cachedInfo,
            textX,
            infoY,
            textW,
            Render2DUtil.multiplyAlpha(TEXT_SUB, alpha),
            false
        );
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    private void rebuildText(MinecraftClient client) {
        cachedTitle = clientName + " " + version;
        if (showFps) {
            cachedInfo = "FPS: " + client.getCurrentFps();
        } else {
            cachedInfo = "HUD";
        }
    }
}
