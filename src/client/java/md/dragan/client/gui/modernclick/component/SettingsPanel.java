package md.dragan.client.gui.modernclick.component;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.command.BindCommandHandler;
import md.dragan.client.gui.modernclick.layout.GuiRect;
import md.dragan.client.gui.modernclick.layout.ScrollState;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.model.GuiSetting;
import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.model.TextSetting;
import md.dragan.client.gui.modernclick.model.ToggleSetting;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.theme.GuiMetrics;
import md.dragan.client.gui.modernclick.theme.GuiTheme;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class SettingsPanel {
    private final ScrollState scroll = new ScrollState();
    private final List<SettingComponent> components = new ArrayList<>();
    private GuiRect bounds = new GuiRect(0, 0, 0, 0);
    private GuiModule cachedModule;
    private int contentHeight;
    private boolean listeningForBind;
    private GuiRect bindBox = new GuiRect(0, 0, 0, 0);

    public void setBounds(GuiRect bounds) {
        this.bounds = bounds;
    }

    public void tick(int mouseX, int mouseY, float deltaSeconds) {
        ensureComponents();
        float scrollAlpha = Animators.expAlpha(Animators.timeToResponse(130.0F), deltaSeconds);
        scroll.tick(scrollAlpha);
        for (SettingComponent component : components) {
            component.tick(mouseX, mouseY, deltaSeconds);
        }
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        ensureComponents();
        GuiModule module = GuiStateStore.selectedModule();

        Render2DUtil.drawText(
            context,
            textRenderer,
            "Settings",
            bounds.x() + GuiMetrics.INNER_PADDING,
            bounds.y() + GuiMetrics.INNER_PADDING,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_PRIMARY, alpha),
            false
        );
        Render2DUtil.drawText(
            context,
            textRenderer,
            listeningForBind ? "press a key to bind" : "configure and assign keybind",
            bounds.x() + GuiMetrics.INNER_PADDING,
            bounds.y() + GuiMetrics.INNER_PADDING + 12,
            Render2DUtil.multiplyAlpha(listeningForBind ? GuiTheme.ACCENT_WARM : GuiTheme.TEXT_MUTED, alpha),
            false
        );

        int infoY = bounds.y() + GuiMetrics.INNER_PADDING + 27;
        if (module == null) {
            Render2DUtil.drawText(
                context,
                textRenderer,
                "Select module",
                bounds.x() + GuiMetrics.INNER_PADDING,
                infoY,
                Render2DUtil.multiplyAlpha(GuiTheme.TEXT_SECONDARY, alpha),
                false
            );
            return;
        }

        syncEnabledSettingFromModule(module);
        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            module.name(),
            bounds.x() + GuiMetrics.INNER_PADDING,
            infoY,
            bounds.width() - GuiMetrics.INNER_PADDING * 2 - 64,
            Render2DUtil.multiplyAlpha(GuiTheme.TEXT_SECONDARY, alpha),
            false
        );

        String bindText = listeningForBind ? "..." : BindCommandHandler.formatKey(module.keyBind());
        int bindW = Math.max(50, Render2DUtil.textWidth(textRenderer, bindText) + 18);
        bindBox = new GuiRect(bounds.x() + bounds.width() - GuiMetrics.INNER_PADDING - bindW, infoY - 3, bindW, 15);
        Render2DUtil.roundedRect(
            context,
            bindBox.x(),
            bindBox.y(),
            bindBox.width(),
            bindBox.height(),
            4,
            Render2DUtil.multiplyAlpha(listeningForBind ? GuiTheme.ACCENT_DIM : GuiTheme.SURFACE_ALT, alpha)
        );
        Render2DUtil.border(
            context,
            bindBox.x(),
            bindBox.y(),
            bindBox.width(),
            bindBox.height(),
            Render2DUtil.multiplyAlpha(listeningForBind ? GuiTheme.ACCENT : GuiTheme.BORDER, alpha)
        );
        Render2DUtil.drawCenteredText(
            context,
            textRenderer,
            bindText,
            bindBox.x() + bindBox.width() / 2,
            bindBox.y() + 4,
            Render2DUtil.multiplyAlpha(listeningForBind ? GuiTheme.TEXT_PRIMARY : GuiTheme.TEXT_SECONDARY, alpha),
            false
        );

        int viewX = bounds.x() + GuiMetrics.INNER_PADDING;
        int viewY = infoY + 18 + GuiMetrics.INNER_PADDING;
        int viewW = bounds.width() - GuiMetrics.INNER_PADDING * 2;
        int viewH = bounds.height() - (viewY - bounds.y()) - GuiMetrics.INNER_PADDING;
        int y = viewY - Math.round(scroll.value());
        contentHeight = 0;

        for (SettingComponent component : components) {
            int h = component.height();
            component.setBounds(new GuiRect(viewX, y, viewW, h));
            y += h + 2;
            contentHeight += h + 2;
        }

        int maxScroll = Math.max(0, contentHeight - viewH);
        scroll.clamp(0.0F, maxScroll);

        Render2DUtil.pushScissor(context, viewX, viewY, viewW, viewH);
        for (SettingComponent component : components) {
            GuiRect row = component.bounds();
            if (row.bottom() < viewY - 20 || row.y() > viewY + viewH) {
                continue;
            }
            component.render(context, textRenderer, mouseX, mouseY, alpha);
        }
        Render2DUtil.popScissor(context);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && bindBox.contains(mouseX, mouseY)) {
            listeningForBind = true;
            return true;
        }
        for (SettingComponent component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
                syncModuleFromEnabledSetting(GuiStateStore.selectedModule());
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

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingComponent component : components) {
            if (component.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        if (!listeningForBind) {
            for (SettingComponent component : components) {
                if (component.keyPressed(keyCode, scanCode)) {
                    return true;
                }
            }
            return false;
        }
        GuiModule module = GuiStateStore.selectedModule();
        listeningForBind = false;
        if (module == null) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            module.setKeyBind(-1);
            HudBootstrap.notify("Bind", "Removed bind from " + module.name(), NotificationType.INFO);
            return true;
        }
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            HudBootstrap.notify("Bind", "Unknown key", NotificationType.ERROR);
            return true;
        }
        module.setKeyBind(keyCode);
        HudBootstrap.notify("Bind", module.name() + " -> " + BindCommandHandler.formatKey(keyCode), NotificationType.SUCCESS);
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        for (SettingComponent component : components) {
            if (component.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    private void ensureComponents() {
        GuiModule selected = GuiStateStore.selectedModule();
        if (selected == cachedModule) {
            return;
        }
        cachedModule = selected;
        listeningForBind = false;
        components.clear();
        if (selected == null) {
            return;
        }
        for (GuiSetting setting : selected.settings()) {
            if (setting instanceof ToggleSetting toggleSetting) {
                components.add(new ToggleSettingComponent(toggleSetting));
            } else if (setting instanceof ModeSetting modeSetting) {
                components.add(new ModeSettingComponent(modeSetting));
            } else if (setting instanceof SliderSetting sliderSetting) {
                components.add(new SliderSettingComponent(sliderSetting));
            } else if (setting instanceof TextSetting textSetting) {
                components.add(new TextSettingComponent(textSetting));
            }
        }
        scroll.clamp(0.0F, 0.0F);
    }

    private void syncEnabledSettingFromModule(GuiModule module) {
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ToggleSetting toggle && "Enabled".equalsIgnoreCase(toggle.name())) {
                toggle.setValue(module.enabled());
            }
        }
    }

    private void syncModuleFromEnabledSetting(GuiModule module) {
        if (module == null) {
            return;
        }
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ToggleSetting toggle && "Enabled".equalsIgnoreCase(toggle.name())) {
                module.setEnabled(toggle.value());
            }
        }
    }
}
