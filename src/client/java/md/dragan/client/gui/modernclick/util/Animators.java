package md.dragan.client.gui.modernclick.util;

public final class Animators {
    private Animators() {
    }

    public static float expAlpha(float response, float deltaSeconds) {
        if (response <= 0.0F || deltaSeconds <= 0.0F) {
            return 0.0F;
        }
        double alpha = 1.0D - Math.exp(-response * deltaSeconds);
        return clamp01((float) alpha);
    }

    public static float timeToResponse(float milliseconds) {
        if (milliseconds <= 0.0F) {
            return 18.0F;
        }
        return 1000.0F / milliseconds;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
