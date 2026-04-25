package md.dragan.client.visual.render3d;

public final class FullBrightModule {
    private static final FullBrightModule INSTANCE = new FullBrightModule();
    private static final String MODULE = "FullBright";
    private boolean initialized;

    private FullBrightModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
    }

    public static boolean isActive() {
        return md.dragan.client.gui.modernclick.state.GuiStateStore.isModuleEnabled(MODULE);
    }
}
