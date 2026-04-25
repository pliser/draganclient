package md.dragan.client.gui.modernclick.model;

public final class SliderSetting extends GuiSetting {
    private final float min;
    private final float max;
    private final float step;
    private float value;

    public SliderSetting(String name, float min, float max, float step, float value) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = Math.max(0.001F, step);
        setValue(value);
    }

    public float min() {
        return min;
    }

    public float max() {
        return max;
    }

    public float step() {
        return step;
    }

    public float value() {
        return value;
    }

    public void setValue(float value) {
        float clamped = Math.max(min, Math.min(max, value));
        float snapped = Math.round((clamped - min) / step) * step + min;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    public float normalized() {
        if (max <= min) {
            return 0.0F;
        }
        return (value - min) / (max - min);
    }
}
