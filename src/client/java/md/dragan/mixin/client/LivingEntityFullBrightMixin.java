package md.dragan.mixin.client;

import md.dragan.client.visual.render3d.FullBrightModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFullBrightMixin {
    @Unique
    private static final StatusEffectInstance DRAGAN_FULL_BRIGHT_NV =
        new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false);

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void dragan$hasNightVisionForFullBright(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (dragan$isLocalPlayer() && FullBrightModule.isActive() && effect == StatusEffects.NIGHT_VISION) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    private void dragan$getNightVisionForFullBright(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (dragan$isLocalPlayer() && FullBrightModule.isActive() && effect == StatusEffects.NIGHT_VISION) {
            cir.setReturnValue(DRAGAN_FULL_BRIGHT_NV);
        }
    }

    @Unique
    private boolean dragan$isLocalPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && (Object) this == client.player;
    }
}
