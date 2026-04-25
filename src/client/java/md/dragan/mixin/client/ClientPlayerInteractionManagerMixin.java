package md.dragan.mixin.client;

import md.dragan.client.friend.FriendsManager;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public final class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void dragan$cancelFriendAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (GuiStateStore.isModuleEnabled("NoFriendAttack") && target instanceof PlayerEntity friend && FriendsManager.isFriend(friend)) {
            ci.cancel();
        }
    }
}
