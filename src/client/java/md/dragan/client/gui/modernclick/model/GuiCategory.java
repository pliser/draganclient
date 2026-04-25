package md.dragan.client.gui.modernclick.model;

public enum GuiCategory {
    COMBAT("Combat"),
    RENDER("Render"),
    MISC("Misc");

    private final String title;

    GuiCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
