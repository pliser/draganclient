package md.dragan.client.gui.modernclick.model;

import java.util.List;

public final class ModeSetting extends GuiSetting {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, List<String> modes, int defaultIndex) {
        super(name);
        this.modes = List.copyOf(modes);
        this.index = Math.max(0, Math.min(defaultIndex, modes.size() - 1));
    }

    public ModeSetting(String name, List<String> modes) {
        this(name, modes, 0);
    }

    public List<String> modes() {
        return modes;
    }

    public int index() {
        return index;
    }

    public void setIndex(int index) {
        this.index = Math.max(0, Math.min(index, modes.size() - 1));
    }

    public String value() {
        return modes.get(index);
    }

    public void cycle() {
        index = (index + 1) % modes.size();
    }

    public void cycleBack() {
        index = (index - 1 + modes.size()) % modes.size();
    }
}
