package md.dragan.client.hud;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class DragState {
    private HudElement activeElement;
    private boolean editorEnabled;
    private float offsetX;
    private float offsetY;
    private boolean positionsDirty;

    public void setEditorEnabled(boolean editorEnabled) {
        this.editorEnabled = editorEnabled;
        if (!editorEnabled) {
            clearActive();
        }
    }

    public boolean isEditorEnabled() {
        return editorEnabled;
    }

    public HudElement activeElement() {
        return activeElement;
    }

    public boolean beginDrag(MinecraftClient client, List<HudElement> elements, double mouseX, double mouseY, int button) {
        if (!editorEnabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || client == null) {
            return false;
        }

        clearActive();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (!element.isVisible()) {
                continue;
            }
            element.syncBounds(client);
            if (!element.isHovered(client, mouseX, mouseY)) {
                continue;
            }

            activeElement = element;
            activeElement.setDraggingInternal(true);
            offsetX = (float) (mouseX - element.x());
            offsetY = (float) (mouseY - element.y());
            return true;
        }

        return false;
    }

    public boolean dragTo(MinecraftClient client, double mouseX, double mouseY, int button) {
        if (!editorEnabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || client == null || activeElement == null) {
            return false;
        }

        activeElement.syncBounds(client);
        float nextX = (float) mouseX - offsetX;
        float nextY = (float) mouseY - offsetY;
        activeElement.setPositionInternal(nextX, nextY);
        activeElement.clampToScreen(client);
        positionsDirty = true;
        return true;
    }

    public boolean endDrag(int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || activeElement == null) {
            return false;
        }
        clearActive();
        return true;
    }

    public boolean consumePositionsDirty() {
        boolean dirty = positionsDirty;
        positionsDirty = false;
        return dirty;
    }

    public void resetDirty() {
        positionsDirty = false;
    }

    private void clearActive() {
        if (activeElement != null) {
            activeElement.setDraggingInternal(false);
            activeElement = null;
        }
        offsetX = 0.0F;
        offsetY = 0.0F;
    }
}
