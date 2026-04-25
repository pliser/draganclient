package md.dragan.client.gui.modernclick.component;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.layout.ScrollState;
import md.dragan.client.gui.modernclick.model.GuiCategory;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class ModulePanel {
    private final List<ModuleButton> allButtons = new ArrayList<>();
    private final ScrollState scroll = new ScrollState();
    private GuiRect bounds = new GuiRect(0, 0, 0, 0);
    private int contentHeight;

    public ModulePanel() {
        GuiStateStore.bootstrap();
        for (GuiModule module : GuiStateStore.modules()) {
            allButtons.add(new ModuleButton(module));
        }
    }

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public void tick(int mouseX, int mouseY, float deltaSeconds) {
        float scrollAlpha = Animators.expAlpha(Animators.timeToResponse(130.0F), deltaSeconds);
        scroll.tick(scrollAlpha);
        for (ModuleButton button : visibleButtons()) {
            boolean hovered = button.bounds().contains(mouseX, mouseY);
            button.tickHover(hovered, deltaSeconds);
        }
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        Render2DUtil.drawText(
            context,
            textRenderer,
            "Modules",
            bounds.x() + GuiMetrics.INNER_PADDING,
            bounds.y() + GuiMetrics.INNER_PADDING,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_PRIMARY, alpha),
            false
        );
        Render2DUtil.drawText(
            context,
            textRenderer,
            "toggle, inspect, bind",
            bounds.x() + GuiMetrics.INNER_PADDING,
            bounds.y() + GuiMetrics.INNER_PADDING + 12,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_MUTED, alpha),
            false
        );

        int viewX = bounds.x() + GuiMetrics.INNER_PADDING;
        int viewY = bounds.y() + GuiMetrics.INNER_PADDING + 26 + GuiMetrics.INNER_PADDING;
        int viewW = bounds.width() - GuiMetrics.INNER_PADDING * 2;
        int viewH = bounds.height() - (viewY - bounds.y()) - GuiMetrics.INNER_PADDING;

        int y = viewY - Math.round(scroll.value());
        contentHeight = 0;
        List<ModuleButton> buttons = visibleButtons();
        for (ModuleButton button : buttons) {
            int h = button.totalHeight();
            button.setBounds(new GuiRect(viewX, y, viewW, GuiMetrics.ROW_HEIGHT));
            y += h + 2;
            contentHeight += h + 2;
        }

        int maxScroll = Math.max(0, contentHeight - viewH);
        scroll.clamp(0.0F, maxScroll);

        Render2DUtil.pushScissor(context, viewX, viewY, viewW, viewH);
        for (ModuleButton button : buttons) {
            if (button.bounds().bottom() < viewY - 20 || button.bounds().y() > viewY + viewH) {
                continue;
            }
            boolean selected = GuiStateStore.selectedModule() == button.module();
            button.render(context, textRenderer, selected, alpha);
        }
        Render2DUtil.popScissor(context);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        for (ModuleButton moduleButton : visibleButtons()) {
            if (moduleButton.mouseClicked(mouseX, mouseY, button)) {
                GuiStateStore.setSelectedModule(moduleButton.module());
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        scroll.add((float) (-verticalAmount * 18.0F));
        return true;
    }

    private List<ModuleButton> visibleButtons() {
        GuiCategory selectedCategory = GuiStateStore.selectedCategory();
        List<ModuleButton> visible = new ArrayList<>();
        for (ModuleButton button : allButtons) {
            if (button.module().category() == selectedCategory) {
                visible.add(button);
            }
        }
        return visible;
    }
}
