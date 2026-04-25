package md.dragan.client.gui.dropdown;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.gui.dropdown.components.DropdownSettingComponent;
import md.dragan.client.gui.modernclick.model.GuiCategory;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

final class DropdownPanel {
    private static final int HEADER_HEIGHT = 24;
    private static final int INNER_PADDING = 8;
    private static final int ROW_GAP = 5;
    private static final int CORNER_RADIUS = 8;

    private final GuiCategory category;
    private final List<DropdownModuleComponent> modules = new ArrayList<>();
    private float x;
    private float y;
    private float width;
    private float height;
    private float scroll;
    private float animatedScroll;
    private float maxContent;

    DropdownPanel(GuiCategory category) {
        this.category = category;
        reloadModules();
    }

    void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    void render(
        DrawContext context,
        TextRenderer textRenderer,
        int mouseX,
        int mouseY,
        String search,
        md.dragan.client.gui.modernclick.util.FramebufferBlurRenderer blurRenderer
    ) {
        if (GuiStateStore.isModuleEnabled("Blur")) {
            blurRenderer.apply(
                net.minecraft.client.MinecraftClient.getInstance(),
                Math.round(x + 1),
                Math.round(y + 1),
                Math.round(width - 2),
                Math.round(height - 2)
            );
        }

        Render2DUtil.roundedRect(context, (int) x, (int) y, (int) width, (int) height, CORNER_RADIUS, DropdownTheme.PANEL_BG);
        Render2DUtil.border(context, (int) x, (int) y, (int) width, (int) height, DropdownTheme.PANEL_BORDER);
        Render2DUtil.border(context, (int) x - 1, (int) y - 1, (int) width + 2, (int) height + 2, DropdownTheme.PANEL_OUTLINE);
        Render2DUtil.rect(context, (int) x, (int) y, (int) width, HEADER_HEIGHT, DropdownTheme.PANEL_BG_ALT);
        Render2DUtil.border(context, (int) x, (int) y, (int) width, HEADER_HEIGHT, DropdownTheme.PANEL_BORDER);
        Render2DUtil.drawCenteredText(context, textRenderer, category.title(), (int) (x + width / 2), (int) (y + 6), DropdownTheme.TEXT_PRIMARY, false);

        int contentX = (int) x + INNER_PADDING;
        int contentY = (int) y + HEADER_HEIGHT + 4;
        int contentW = (int) width - INNER_PADDING * 2;
        int contentH = (int) height - HEADER_HEIGHT - INNER_PADDING;

        Render2DUtil.pushScissor(context, contentX, contentY, contentW, contentH);
        float currentY = contentY + animatedScroll;

        maxContent = 0;
        for (DropdownModuleComponent module : modules) {
            if (!matchesSearch(module, search)) {
                continue;
            }
            module.setBounds(contentX + 1, currentY, contentW - 2);
            float moduleHeight = module.render(context, textRenderer, mouseX, mouseY);
            currentY += moduleHeight + ROW_GAP;
            maxContent += moduleHeight + ROW_GAP;
        }

        clampScroll(contentH);
        animatedScroll += (scroll - animatedScroll) * 0.25f;

        renderScrollbar(context, contentX, contentY, contentW, contentH);
        Render2DUtil.popScissor(context);
    }

    boolean mouseClicked(int mouseX, int mouseY, int button, String search) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        for (DropdownModuleComponent module : modules) {
            if (!matchesSearch(module, search)) {
                continue;
            }
            if (module.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }

    void mouseReleased(int mouseX, int mouseY, int button) {
        for (DropdownModuleComponent module : modules) {
            module.mouseReleased(mouseX, mouseY, button);
        }
    }

    void mouseDragged(int mouseX, int mouseY, int button, String search) {
        for (DropdownModuleComponent module : modules) {
            if (!matchesSearch(module, search)) {
                continue;
            }
            module.mouseDragged(mouseX, mouseY, button);
        }
    }

    boolean mouseScrolled(int mouseX, int mouseY, double amount, String search) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (maxContent <= 0) {
            return false;
        }
        scroll += amount * 12.0f;
        return true;
    }

    void charTyped(char codePoint, int modifiers) {
        for (DropdownModuleComponent module : modules) {
            module.charTyped(codePoint, modifiers);
        }
    }

    void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (DropdownModuleComponent module : modules) {
            module.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    void reloadModules() {
        modules.clear();
        for (GuiModule module : GuiStateStore.modules()) {
            if (module.category() == category) {
                modules.add(new DropdownModuleComponent(module));
            }
        }
    }

    private boolean matchesSearch(DropdownModuleComponent module, String search) {
        if (search == null || search.isEmpty()) {
            return true;
        }
        return module.name().toLowerCase().contains(search.toLowerCase());
    }

    private boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void clampScroll(int contentH) {
        float maxScroll = Math.max(0, maxContent - contentH);
        if (scroll > 0) {
            scroll = 0;
        }
        if (-scroll > maxScroll) {
            scroll = -maxScroll;
        }
        if (-animatedScroll > maxScroll) {
            animatedScroll = -maxScroll;
        }
    }

    private void renderScrollbar(DrawContext context, int contentX, int contentY, int contentW, int contentH) {
        if (maxContent <= contentH + 1) {
            return;
        }
        float maxScroll = maxContent - contentH;
        float barHeight = Math.max(14, (contentH * contentH) / maxContent);
        float scrollProgress = -animatedScroll / maxScroll;
        float barY = contentY + scrollProgress * (contentH - barHeight);
        int barX = contentX + contentW - 3;
        Render2DUtil.roundedRect(context, barX, (int) barY, 2, (int) barHeight, 1, DropdownTheme.PANEL_OUTLINE);
    }
}
