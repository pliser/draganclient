package md.dragan.mixin.client;

import md.dragan.client.combat.TriggerBotModule;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MinecraftClientHandleInputMixin {
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void dragan$runTriggerBotAtVanillaInputPoint(CallbackInfo ci) {
        TriggerBotModule.runQueuedAttack((MinecraftClient) (Object) this);
    }
}
