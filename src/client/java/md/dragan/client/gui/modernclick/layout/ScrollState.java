package md.dragan.client.gui.modernclick.layout;

public final class ScrollState {
    private float value;
    private float target;

    public void add(float delta) {
        target += delta;
    }

    public void clamp(float min, float max) {
        target = Math.max(min, Math.min(max, target));
        value = Math.max(min, Math.min(max, value));
    }

    public void tick(float speed) {
        value += (target - value) * speed;
    }

    public float value() {
        return value;
    }
}
