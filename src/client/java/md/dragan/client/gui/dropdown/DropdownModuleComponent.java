package md.dragan.client.gui.dropdown;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.gui.dropdown.components.DropdownModeSettingComponent;
import md.dragan.client.gui.dropdown.components.DropdownSettingComponent;
import md.dragan.client.gui.dropdown.components.DropdownSliderSettingComponent;
import md.dragan.client.gui.dropdown.components.DropdownToggleSettingComponent;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.model.GuiSetting;
import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.model.ToggleSetting;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

final class DropdownModuleComponent {
    private static final int ROW_HEIGHT = 20;
    private static final int SETTINGS_GAP = 4;

    private final GuiModule module;
    private final List<DropdownSettingComponent> settings = new ArrayList<>();
    private final Animation openAnimation = new Animation(0.0F);
    private final Animation toggleAnimation = new Animation(0.0F);

    private float x;
    private float y;
    private float width;

    DropdownModuleComponent(GuiModule module) {
        this.module = module;
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ToggleSetting toggle) {
                settings.add(new DropdownToggleSettingComponent(toggle));
            } else if (setting instanceof ModeSetting mode) {
                settings.add(new DropdownModeSettingComponent(mode));
            } else if (setting instanceof SliderSetting slider) {
                settings.add(new DropdownSliderSettingComponent(slider));
            }
        }
        openAnimation.snap(module.expanded() ? 1.0F : 0.0F);
        toggleAnimation.snap(module.enabled() ? 1.0F : 0.0F);
    }

    void setBounds(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    float render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        openAnimation.setTarget(module.expanded() ? 1.0F : 0.0F);
        openAnimation.tickSeconds(1.0F / 60.0F, Animators.timeToResponse(140.0F));
        toggleAnimation.setTarget(module.enabled() ? 1.0F : 0.0F);
        toggleAnimation.tickSeconds(1.0F / 60.0F, Animators.timeToResponse(140.0F));

        boolean hovered = contains(mouseX, mouseY, x, y, width, ROW_HEIGHT);
        int rowColor = hovered ? DropdownTheme.PANEL_ROW_HOVER : DropdownTheme.PANEL_ROW;
        Render2DUtil.roundedRect(context, (int) x, (int) y, (int) width, ROW_HEIGHT, 5, rowColor);
        Render2DUtil.border(context, (int) x, (int) y, (int) width, ROW_HEIGHT, DropdownTheme.PANEL_OUTLINE);
        if (toggleAnimation.value() > 0.01f) {
            int accent = DropdownTheme.ACCENT;
            Render2DUtil.roundedRect(context, (int) x, (int) y, (int) width, ROW_HEIGHT, 5, accent);
        }

        Render2DUtil.drawTextClipped(
            context,
            textRenderer,
            module.name(),
            (int) x + 8,
            (int) y + 6,
            (int) width - 18,
            DropdownTheme.TEXT_PRIMARY,
            false
        );
        if (!settings.isEmpty()) {
            Render2DUtil.drawText(context, textRenderer, "...", (int) (x + width - 16), (int) y + 6, DropdownTheme.TEXT_SECONDARY, false);
        }

        float totalHeight = ROW_HEIGHT;
        if (openAnimation.value() > 0.01f && !settings.isEmpty()) {
            float settingsY = y + ROW_HEIGHT + SETTINGS_GAP;
            float expandedHeight = 0;
            for (DropdownSettingComponent setting : settings) {
                setting.setBounds((int) x + 6, (int) settingsY, (int) width - 12);
                int settingHeight = setting.updateHeight(textRenderer);
                setting.render(context, textRenderer, mouseX, mouseY);
                settingsY += settingHeight + SETTINGS_GAP;
                expandedHeight += settingHeight + SETTINGS_GAP;
            }
            totalHeight += expandedHeight * openAnimation.value();
        }

        return totalHeight;
    }

    boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (contains(mouseX, mouseY, x, y, width, ROW_HEIGHT)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                module.setEnabled(!module.enabled());
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                module.setExpanded(!module.expanded());
                return true;
            }
        }
        if (module.expanded()) {
            for (DropdownSettingComponent setting : settings) {
                if (setting.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    void mouseReleased(int mouseX, int mouseY, int button) {
        for (DropdownSettingComponent setting : settings) {
            setting.mouseReleased(mouseX, mouseY, button);
        }
    }

    void mouseDragged(int mouseX, int mouseY, int button) {
        for (DropdownSettingComponent setting : settings) {
            setting.mouseDragged(mouseX, mouseY, button);
        }
    }

    void charTyped(char codePoint, int modifiers) {
        for (DropdownSettingComponent setting : settings) {
            setting.charTyped(codePoint, modifiers);
        }
    }

    void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (DropdownSettingComponent setting : settings) {
            setting.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    String name() {
        return module.name();
    }

    private static boolean contains(int mouseX, int mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
