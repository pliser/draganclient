package md.dragan.client.visual.render3d;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class TargetEspModule {
    private static final TargetEspModule INSTANCE = new TargetEspModule();

    private final TargetEspRenderer renderer = new TargetEspRenderer();
    private boolean initialized;

    private TargetEspModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        WorldRenderEvents.AFTER_ENTITIES.register(renderer::render);
    }
}
