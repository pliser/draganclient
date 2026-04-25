package md.dragan.client.gui.modernclick.component;

import java.util.EnumMap;
import java.util.List;
import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.model.GuiCategory;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class CategoryPanel {
    private final List<GuiCategory> categories = List.of(GuiCategory.values());
    private final EnumMap<GuiCategory, Animation> rowHovers = new EnumMap<>(GuiCategory.class);
    private GuiRect bounds = new GuiRect(0, 0, 0, 0);

    public CategoryPanel() {
        for (GuiCategory category : categories) {
            rowHovers.put(category, new Animation(0.0F));
        }
    }

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public GuiRect bounds() {
        return bounds;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        int panelColor = Render2DUtil.multiplyAlpha(GuiTheme.PANEL_LEFT, alpha);
        int borderColor = Render2DUtil.multiplyAlpha(GuiTheme.BORDER, alpha);
        Render2DUtil.roundedRect(
            context,
            bounds.x(),
            bounds.y(),
            bounds.width(),
            bounds.height(),
            GuiMetrics.CORNER_RADIUS,
            panelColor
        );
        Render2DUtil.border(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), borderColor);

        int titleY = bounds.y() + GuiMetrics.INNER_PADDING;
        Render2DUtil.drawText(
            context,
            textRenderer,
            "Categories",
            bounds.x() + GuiMetrics.INNER_PADDING,
            titleY,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_PRIMARY, alpha),
            false
        );
        Render2DUtil.drawText(
            context,
            textRenderer,
            "sections",
            bounds.x() + GuiMetrics.INNER_PADDING,
            titleY + 12,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_MUTED, alpha),
            false
        );

        int rowX = bounds.x() + GuiMetrics.INNER_PADDING;
        int rowY = titleY + 24 + GuiMetrics.INNER_PADDING;
        int rowW = bounds.width() - GuiMetrics.INNER_PADDING * 2;

        for (GuiCategory category : categories) {
            Animation hover = rowHovers.get(category);
            float hoverValue = hover.value();
            boolean selected = GuiStateStore.selectedCategory() == category;

            if (hoverValue > 0.01F || selected) {
                int hoverColor = Render2DUtil.multiplyAlpha(
                    selected ? GuiTheme.SURFACE_ELEVATED : Render2DUtil.withAlpha(GuiTheme.HOVER_BG, (int) (255 * hoverValue)),
                    alpha
                );
                Render2DUtil.roundedRect(context, rowX, rowY, rowW, GuiMetrics.ROW_HEIGHT, 7, hoverColor);
                Render2DUtil.border(
                    context,
                    rowX,
                    rowY,
                    rowW,
                    GuiMetrics.ROW_HEIGHT,
                    Render2DUtil.multiplyAlpha(selected ? GuiTheme.ACCENT_DIM : GuiTheme.BORDER_SOFT, alpha)
                );
            }

            if (selected) {
                Render2DUtil.rect(
                    context,
                    rowX + 7,
                    rowY + 6,
                    3,
                    GuiMetrics.ROW_HEIGHT - 12,
                    Render2DUtil.multiplyAlpha(GuiTheme.ACCENT, alpha)
                );
                Render2DUtil.roundedRect(
                    context,
                    rowX + rowW - 22,
                    rowY + 6,
                    14,
                    GuiMetrics.ROW_HEIGHT - 12,
                    4,
                    Render2DUtil.multiplyAlpha(GuiTheme.ACCENT_DIM, alpha)
                );
            }

            int textColor = selected ? GuiTheme.TEXT_PRIMARY : GuiTheme.TEXT_SECONDARY;
            Render2DUtil.drawTextClipped(
                context,
                textRenderer,
                category.title(),
                rowX + 18,
                rowY + 9,
                rowW - 44,
                Render2DUtil.multiplyAlpha(textColor, alpha),
                false
            );
            rowY += GuiMetrics.ROW_HEIGHT + 6;
        }
    }

    public void tick(int mouseX, int mouseY, float deltaSeconds) {
        int rowX = bounds.x() + GuiMetrics.INNER_PADDING;
        int rowY = bounds.y() + GuiMetrics.INNER_PADDING + 24 + GuiMetrics.INNER_PADDING;
        int rowW = bounds.width() - GuiMetrics.INNER_PADDING * 2;
        float response = Animators.timeToResponse(140.0F);

        for (GuiCategory category : categories) {
            GuiRect row = new GuiRect(rowX, rowY, rowW, GuiMetrics.ROW_HEIGHT);
            Animation hover = rowHovers.get(category);
            hover.setTarget(row.contains(mouseX, mouseY) ? 1.0F : 0.0F);
            hover.tickSeconds(deltaSeconds, response);
            rowY += GuiMetrics.ROW_HEIGHT + 6;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        int rowX = bounds.x() + GuiMetrics.INNER_PADDING;
        int rowY = bounds.y() + GuiMetrics.INNER_PADDING + 24 + GuiMetrics.INNER_PADDING;
        int rowW = bounds.width() - GuiMetrics.INNER_PADDING * 2;
        for (GuiCategory category : categories) {
            GuiRect row = new GuiRect(rowX, rowY, rowW, GuiMetrics.ROW_HEIGHT);
            if (row.contains(mouseX, mouseY)) {
                GuiStateStore.setSelectedCategory(category);
                GuiModule first = GuiStateStore.firstModuleOf(category);
                if (first != null) {
                    GuiStateStore.setSelectedModule(first);
                }
                return true;
            }
            rowY += GuiMetrics.ROW_HEIGHT + 6;
        }

        return false;
    }
}
