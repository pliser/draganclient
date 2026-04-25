package md.dragan.client;

import md.dragan.client.combat.TriggerBotModule;
import md.dragan.client.combat.LegitAimService;
import md.dragan.client.combat.LegitNukerModule;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.input.ClientKeybinds;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.visual.render3d.ChamsModule;
import md.dragan.client.visual.render3d.FullBrightModule;
import md.dragan.client.visual.render3d.NametagsModule;
import md.dragan.client.visual.render3d.ArrowsModule;
import md.dragan.client.visual.render3d.TargetEspModule;
import md.dragan.client.visual.render3d.WaypointsModule;
import net.fabricmc.api.ClientModInitializer;

public class DraganClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GuiStateStore.bootstrap();
        HudBootstrap.init();
        ClientKeybinds.init();
        TriggerBotModule.init();
        LegitAimService.init();
        LegitNukerModule.init();
        TargetEspModule.init();
        ChamsModule.init();
        FullBrightModule.init();
        NametagsModule.init();
        ArrowsModule.init();
        WaypointsModule.init();
    }
}
