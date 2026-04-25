package md.dragan.client.gui.modernclick.model;

public abstract class GuiSetting {
    private final String name;

    protected GuiSetting(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
