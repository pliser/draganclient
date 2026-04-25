package md.dragan.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudElement {
    private static final float CLAMP_MARGIN = 2.0F;

    private final String id;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean dragging;
    protected boolean visible = true;

    private boolean boundsDirty = true;

    protected HudElement(String id, float defaultX, float defaultY) {
        this.id = id;
        this.x = defaultX;
        this.y = defaultY;
    }

    public final String id() {
        return id;
    }

    public final void syncBounds(MinecraftClient client) {
        measure(client);
        width = Math.max(1.0F, width);
        height = Math.max(1.0F, height);
        boundsDirty = false;
    }

    public final void markBoundsDirty() {
        boundsDirty = true;
    }

    public final boolean needsBoundsUpdate() {
        return boundsDirty;
    }

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final float x() {
        return x;
    }

    public final float y() {
        return y;
    }

    public final float width() {
        return width;
    }

    public final float height() {
        return height;
    }

    public final boolean isDragging() {
        return dragging;
    }

    public final boolean isHovered(MinecraftClient client, double mouseX, double mouseY) {
        if (needsBoundsUpdate()) {
            syncBounds(client);
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    final void setDraggingInternal(boolean dragging) {
        this.dragging = dragging;
    }

    final void setPositionInternal(float x, float y) {
        this.x = x;
        this.y = y;
    }

    final void clampToScreen(MinecraftClient client) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float maxX = Math.max(CLAMP_MARGIN, sw - width - CLAMP_MARGIN);
        float maxY = Math.max(CLAMP_MARGIN, sh - height - CLAMP_MARGIN);
        x = Math.max(CLAMP_MARGIN, Math.min(maxX, x));
        y = Math.max(CLAMP_MARGIN, Math.min(maxY, y));
    }

    protected abstract void measure(MinecraftClient client);

    public abstract void update();

    public abstract void render(DrawContext context, float delta);

    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    public boolean onMouseRelease(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    public boolean onMouseDrag(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }
}
