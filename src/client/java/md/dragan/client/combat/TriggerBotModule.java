package md.dragan.client.combat;

import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.mixin.client.MinecraftClientAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class TriggerBotModule {
    private static final TriggerBotModule INSTANCE = new TriggerBotModule();
    private static final double MAX_ATTACK_DISTANCE = 3.0D;
    private static final long ATTACK_REQUEST_TTL_MS = 150L;
    private boolean initialized;
    private long lastAttackAt;
    private Entity pendingTarget;
    private long pendingAttackAt;
    private String pendingAttackMode = "Direct";

    private TriggerBotModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null) {
            return;
        }

        if (!GuiStateStore.isModuleEnabled("TriggerBot")) {
            return;
        }

        boolean playersOnly = GuiStateStore.getToggleSetting("TriggerBot", "Players Only", true);
        boolean requireWeapon = GuiStateStore.getToggleSetting("TriggerBot", "Require Weapon", false);

        if (requireWeapon && !isWeaponLike(client.player.getMainHandStack())) {
            return;
        }

        if (client.player.getAttackCooldownProgress(0.0F) < 1.0F) {
            return;
        }

        if (client.interactionManager == null || !client.player.isAlive()) {
            return;
        }

        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return;
        }

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        if (playersOnly && !(target instanceof PlayerEntity)) {
            return;
        }

        if (target.isRemoved() || !target.isAlive()) {
            return;
        }

        if (!isValidAttackDistance(client, target)) {
            return;
        }

        if (!hasLineOfSight(client, target)) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAttackAt < 80L) {
            return;
        }

        pendingTarget = target;
        pendingAttackAt = now;
        pendingAttackMode = GuiStateStore.getModeSetting("TriggerBot", "Attack Mode");
    }

    public static void runQueuedAttack(MinecraftClient client) {
        INSTANCE.flushQueuedAttack(client);
    }

    private void flushQueuedAttack(MinecraftClient client) {
        if (pendingTarget == null || client.player == null || client.world == null || client.interactionManager == null) {
            clearPending();
            return;
        }

        long now = System.currentTimeMillis();
        Entity target = pendingTarget;
        if (now - pendingAttackAt > ATTACK_REQUEST_TTL_MS || !target.isAlive() || target.isRemoved()) {
            clearPending();
            return;
        }

        if (!isValidAttackDistance(client, target) || !hasLineOfSight(client, target) || client.player.getAttackCooldownProgress(0.0F) < 1.0F) {
            clearPending();
            return;
        }

        if ("Mouse Emulation".equalsIgnoreCase(pendingAttackMode)) {
            emulateClick(client, target);
        } else {
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
        }
        lastAttackAt = now;
        clearPending();
    }

    private static void emulateClick(MinecraftClient client, Entity target) {
        if (client.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getEntity() == target) {
            ((MinecraftClientAccessor) client).dragan$doAttack();
            return;
        }
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private void clearPending() {
        pendingTarget = null;
        pendingAttackAt = 0L;
        pendingAttackMode = "Direct";
    }

    private static boolean isWeaponLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String itemId = String.valueOf(stack.getItem().getRegistryEntry().registryKey().getValue());
        return itemId.contains("sword")
            || itemId.contains("axe")
            || itemId.contains("mace")
            || itemId.contains("trident");
    }

    private static boolean isValidAttackDistance(MinecraftClient client, Entity target) {
        return client.player.squaredDistanceTo(target) <= MAX_ATTACK_DISTANCE * MAX_ATTACK_DISTANCE;
    }

    private static boolean hasLineOfSight(MinecraftClient client, Entity target) {
        Vec3d eyePos = client.player.getEyePos();
        Vec3d targetPos = target.getBoundingBox().getCenter();
        return target.getBoundingBox().raycast(eyePos, targetPos).isPresent()
            || client.player.canSee(target);
    }
}
