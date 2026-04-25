package md.dragan.client.gui.modernclick.util;

public final class Animation {
    private float value;
    private float target;

    public Animation(float initial) {
        this.value = initial;
        this.target = initial;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
    }

    public void tick(float speed) {
        value += (target - value) * clamp01(speed);
    }

    public void tickSeconds(float deltaSeconds, float response) {
        float alpha = Animators.expAlpha(response, deltaSeconds);
        value += (target - value) * alpha;
    }

    public void tickMs(float deltaMs, float response) {
        tickSeconds(deltaMs / 1000.0F, response);
    }

    public boolean isNear(float epsilon) {
        return Math.abs(target - value) <= Math.max(0.0001F, epsilon);
    }

    public float value() {
        return value;
    }

    public float target() {
        return target;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
