package md.dragan.client.gui.modernclick.model;

public final class ToggleSetting extends GuiSetting {
    private boolean value;

    public ToggleSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
