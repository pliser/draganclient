package md.dragan.client.visual.render3d;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class ChamsModule {
    private static final ChamsModule INSTANCE = new ChamsModule();

    private final ChamsRenderer renderer = new ChamsRenderer();
    private boolean initialized;

    private ChamsModule() {
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
