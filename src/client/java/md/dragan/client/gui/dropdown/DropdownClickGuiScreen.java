package md.dragan.client.gui.dropdown;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.gui.modernclick.model.GuiCategory;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.FramebufferBlurRenderer;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudBootstrap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class DropdownClickGuiScreen extends Screen {
    private static final int OUTER_PADDING = 16;
    private static final int PANEL_WIDTH = 127;
    private static final int PANEL_HEIGHT = 300;
    private static final int PANEL_GAP = 7;
    private static final int SEARCH_HEIGHT = 16;
    private static final float MIN_SCALE = 0.55f;

    private final List<DropdownPanel> panels = new ArrayList<>();
    private final Animation openAnimation = new Animation(0.0F);
    private final FramebufferBlurRenderer blurRenderer = new FramebufferBlurRenderer();
    private DropdownSearchField searchField;
    private boolean closing;
    private float scale = 1.0f;

    public DropdownClickGuiScreen() {
        super(Text.literal("Dropdown ClickGUI"));
        GuiStateStore.bootstrap();
        for (GuiCategory category : GuiCategory.values()) {
            panels.add(new DropdownPanel(category));
        }
    }

    @Override
    protected void init() {
        super.init();
        openAnimation.snap(0.0F);
        openAnimation.setTarget(1.0F);
        closing = false;
        searchField = new DropdownSearchField(0, 0, PANEL_WIDTH, SEARCH_HEIGHT, "Search");
        HudBootstrap.setEditorMode(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float deltaSeconds = Math.max(0.001F, delta / 20.0F);
        openAnimation.tickSeconds(deltaSeconds, Animators.timeToResponse(160.0F));
        if (closing && openAnimation.value() <= 0.05F) {
            close();
            return;
        }

        Render2DUtil.rect(context, 0, 0, width, height, DropdownTheme.SCREEN_OVERLAY);

        updateScale();
        updatePanelPositions();

        Vec2 adjusted = adjustMouseCoordinates(mouseX, mouseY);
        int adjMouseX = adjusted.x;
        int adjMouseY = adjusted.y;

        for (DropdownPanel panel : panels) {
            panel.render(context, textRenderer, adjMouseX, adjMouseY, searchField.text(), blurRenderer);
        }

        if (searchField != null) {
            searchField.render(context, textRenderer, adjMouseX, adjMouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (HudBootstrap.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        Vec2 adjusted = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        if (searchField != null && searchField.mouseClicked(adjusted.x, adjusted.y, button)) {
            return true;
        }
        for (DropdownPanel panel : panels) {
            if (panel.mouseClicked(adjusted.x, adjusted.y, button, searchField.text())) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (HudBootstrap.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        Vec2 adjusted = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        for (DropdownPanel panel : panels) {
            panel.mouseReleased(adjusted.x, adjusted.y, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (HudBootstrap.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        Vec2 adjusted = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        for (DropdownPanel panel : panels) {
            panel.mouseDragged(adjusted.x, adjusted.y, button, searchField.text());
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Vec2 adjusted = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        for (DropdownPanel panel : panels) {
            if (panel.mouseScrolled(adjusted.x, adjusted.y, verticalAmount, searchField.text())) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField != null && searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        for (DropdownPanel panel : panels) {
            panel.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField != null && searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (DropdownPanel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            closing = true;
            openAnimation.setTarget(0.0F);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        HudBootstrap.setEditorMode(false);
        blurRenderer.close();
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void updateScale() {
        float totalWidth = panels.size() * (PANEL_WIDTH + PANEL_GAP) - PANEL_GAP;
        if (totalWidth <= 0) {
            scale = 1.0f;
            return;
        }
        float screenWidth = width;
        if (totalWidth >= screenWidth - OUTER_PADDING * 2) {
            scale = Math.max(MIN_SCALE, (screenWidth - OUTER_PADDING * 2) / totalWidth);
        } else {
            scale = 1.0f;
        }
    }

    private void updatePanelPositions() {
        float totalWidth = panels.size() * (PANEL_WIDTH + PANEL_GAP) - PANEL_GAP;
        float startX = width / 2.0f - totalWidth / 2.0f;
        float startY = height / 2.0f - PANEL_HEIGHT / 2.0f;

        float x = startX;
        for (DropdownPanel panel : panels) {
            panel.setBounds(x, startY, PANEL_WIDTH, PANEL_HEIGHT);
            x += PANEL_WIDTH + PANEL_GAP;
        }

        if (searchField != null) {
            float searchY = startY + PANEL_HEIGHT + 6;
            searchField.setBounds((int) (width / 2.0f - PANEL_WIDTH / 2.0f), (int) searchY, PANEL_WIDTH, SEARCH_HEIGHT);
        }
    }

    private Vec2 adjustMouseCoordinates(int mouseX, int mouseY) {
        return new Vec2(mouseX, mouseY);
    }

    private record Vec2(int x, int y) {
    }
}
