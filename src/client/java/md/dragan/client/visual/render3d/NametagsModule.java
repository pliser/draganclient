package md.dragan.client.visual.render3d;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class NametagsModule {
    private static final NametagsModule INSTANCE = new NametagsModule();

    private final NametagsRenderer renderer = new NametagsRenderer();
    private boolean initialized;

    private NametagsModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        HudRenderCallback.EVENT.register((context, tickCounter) -> renderer.render(context));
    }
}
