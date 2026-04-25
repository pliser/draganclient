package md.dragan.client.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HudManager {
    private final MinecraftClient client;
    private final DragState dragState = new DragState();
    private final PositionStore positionStore;
    private final List<HudElement> elements = new ArrayList<>();

    public HudManager(MinecraftClient client, PositionStore positionStore) {
        this.client = client;
        this.positionStore = positionStore;
    }

    public void register(HudElement element) {
        elements.add(element);
        if (positionStore != null) {
            float[] saved = positionStore.load(element.id());
            if (saved != null && saved.length >= 2) {
                element.setPositionInternal(saved[0], saved[1]);
            }
        }
        element.syncBounds(client);
        element.clampToScreen(client);
    }

    public List<HudElement> elements() {
        return Collections.unmodifiableList(elements);
    }

    public DragState dragState() {
        return dragState;
    }

    public void setEditorMode(boolean enabled) {
        dragState.setEditorEnabled(enabled);
    }

    public boolean isEditorMode() {
        return dragState.isEditorEnabled();
    }

    public void update(float delta) {
        for (HudElement element : elements) {
            if (element.needsBoundsUpdate()) {
                element.syncBounds(client);
            }
            element.update();
            element.clampToScreen(client);
        }
    }

    public void render(DrawContext context, float delta) {
        for (HudElement element : elements) {
            if (!element.isVisible()) {
                continue;
            }
            if (element.needsBoundsUpdate()) {
                element.syncBounds(client);
            }
            element.render(context, delta);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!dragState.isEditorEnabled()) {
            return false;
        }
        if (dragState.beginDrag(client, elements, mouseX, mouseY, button)) {
            return true;
        }
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (element.onMouseClick(mouseX, mouseY, button, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!dragState.isEditorEnabled()) {
            return false;
        }
        if (dragState.dragTo(client, mouseX, mouseY, button)) {
            persistPositionsIfDirty();
            return true;
        }
        for (HudElement element : elements) {
            if (element.onMouseDrag(mouseX, mouseY, button, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragState.isEditorEnabled()) {
            return false;
        }
        if (dragState.endDrag(button)) {
            persistPositionsIfDirty();
            return true;
        }
        for (HudElement element : elements) {
            if (element.onMouseRelease(mouseX, mouseY, button, true)) {
                return true;
            }
        }
        return false;
    }

    public void saveAllPositions() {
        if (positionStore == null) {
            return;
        }
        for (HudElement element : elements) {
            positionStore.save(element.id(), element.x(), element.y());
        }
        positionStore.flush();
    }

    private void persistPositionsIfDirty() {
        if (!dragState.consumePositionsDirty() || positionStore == null) {
            return;
        }
        for (HudElement element : elements) {
            positionStore.save(element.id(), element.x(), element.y());
        }
        positionStore.flush();
    }

    public interface PositionStore {
        float[] load(String id);

        void save(String id, float x, float y);

        void flush();
    }
}
