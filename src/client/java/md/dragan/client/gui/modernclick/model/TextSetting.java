package md.dragan.client.gui.modernclick.model;

public final class TextSetting extends GuiSetting {
    private final int maxLength;
    private String value;

    public TextSetting(String name, String value, int maxLength) {
        super(name);
        this.maxLength = Math.max(1, maxLength);
        setValue(value);
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maxLength) {
            normalized = normalized.substring(0, maxLength);
        }
        this.value = normalized;
    }

    public int maxLength() {
        return maxLength;
    }
}
