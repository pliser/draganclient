package md.dragan.client.gui.modernclick.model;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;

public final class GuiModule {
    private final String name;
    private final GuiCategory category;
    private final List<GuiSetting> settings = new ArrayList<>();
    private boolean enabled;
    private boolean expanded;
    private int keyBind = -1;

    public GuiModule(String name, GuiCategory category) {
        this.name = name;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public GuiCategory category() {
        return category;
    }

    public List<GuiSetting> settings() {
        return settings;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            showNotification(enabled);
        }
    }

    public void setEnabledSilent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean expanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public int keyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public boolean hasKeyBind() {
        return keyBind >= 0;
    }

    private void showNotification(boolean enabled) {
        String status = enabled ? "Enabled" : "Disabled";
        String statusRu = enabled ? "Включен" : "Выключен";
        HudBootstrap.notify(
            name,
            status + " • " + statusRu,
            enabled ? NotificationType.SUCCESS : NotificationType.INFO
        );
    }
}
