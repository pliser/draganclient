package md.dragan.client.visual.render3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import md.dragan.client.friend.FriendsManager;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ArrowsModule {
    private static final ArrowsModule INSTANCE = new ArrowsModule();
    private static final int COLOR_ENEMY = 0xFF6DD6FF;
    private static final int COLOR_FRIEND = 0xFF6EEB8E;
    private static final int COLOR_SHADOW = 0x94000000;
    private static final int COLOR_CORE = 0xEE0F1721;
    private static final int COLOR_BADGE_BG = 0xC9101721;
    private static final int COLOR_BADGE_TEXT = 0xFFEAF3FF;
    private static final long CACHE_TTL_MS = 2_400L;
    private static final float SMOOTH_FACTOR = 0.33F;

    private boolean initialized;
    private final Map<UUID, Float> smoothedAngles = new HashMap<>();
    private final Map<UUID, Long> lastSeenMs = new HashMap<>();

    private ArrowsModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context, tickCounter.getTickProgress(false)));
    }

    private void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !GuiStateStore.isModuleEnabled("Arrows")) {
            smoothedAngles.clear();
            lastSeenMs.clear();
            return;
        }

        float radius = GuiStateStore.getSliderSetting("Arrows", "Radius", 78.0F);
        float size = GuiStateStore.getSliderSetting("Arrows", "Size", 11.0F);
        boolean showFriends = GuiStateStore.getToggleSetting("Arrows", "Friends", true);
        float centerX = client.getWindow().getScaledWidth() * 0.5F;
        float centerY = client.getWindow().getScaledHeight() * 0.5F;
        long now = System.currentTimeMillis();
        float yawRad = client.player.getYaw(tickDelta) * MathHelper.RADIANS_PER_DEGREE;
        float forwardX = -MathHelper.sin(yawRad);
        float forwardZ = MathHelper.cos(yawRad);
        float rightX = MathHelper.cos(yawRad);
        float rightZ = MathHelper.sin(yawRad);

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.isRemoved() || player.isSpectator() || player.isInvisible()) {
                continue;
            }

            boolean friend = FriendsManager.isFriend(player);
            if (friend && !showFriends) {
                continue;
            }

            Vec3d delta = player.getLerpedPos(tickDelta).subtract(client.player.getLerpedPos(tickDelta));
            float dotForward = (float) (delta.x * forwardX + delta.z * forwardZ);
            float dotRight = (float) (delta.x * rightX + delta.z * rightZ);
            float rawAngle = (float) Math.atan2(dotRight, dotForward) - MathHelper.HALF_PI;
            rawAngle = normalizeAngle(rawAngle);
            float angle = smoothAngle(player.getUuid(), rawAngle, now);

            float x = centerX + MathHelper.cos(angle) * radius;
            float y = centerY + MathHelper.sin(angle) * radius;
            int color = friend ? COLOR_FRIEND : COLOR_ENEMY;
            drawArrow(context, client, x, y, angle, size, color, (float) delta.length());
        }

        pruneOldCache(now);
    }

    private void drawArrow(DrawContext context, MinecraftClient client, float x, float y, float angle, float size, int color, float distance) {
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float tipX = x + cos * size;
        float tipY = y + sin * size;
        float backX = x - cos * (size * 0.90F);
        float backY = y - sin * (size * 0.90F);
        float sideCos = MathHelper.cos(angle + MathHelper.HALF_PI);
        float sideSin = MathHelper.sin(angle + MathHelper.HALF_PI);
        float wing = size * 0.76F;

        OverlayShapeUtil.fillTriangle(
            context,
            tipX + cos * 1.6F,
            tipY + sin * 1.6F,
            backX + sideCos * (wing * 1.15F),
            backY + sideSin * (wing * 1.15F),
            backX - sideCos * (wing * 1.15F),
            backY - sideSin * (wing * 1.15F),
            COLOR_SHADOW
        );
        OverlayShapeUtil.fillTriangle(
            context,
            tipX,
            tipY,
            backX + sideCos * wing,
            backY + sideSin * wing,
            backX - sideCos * wing,
            backY - sideSin * wing,
            color
        );
        OverlayShapeUtil.fillTriangle(
            context,
            tipX - cos * 1.8F,
            tipY - sin * 1.8F,
            backX + sideCos * (wing * 0.56F),
            backY + sideSin * (wing * 0.56F),
            backX - sideCos * (wing * 0.56F),
            backY - sideSin * (wing * 0.56F),
            COLOR_CORE
        );

        int stemX = Math.round(x - cos * (size * 1.25F));
        int stemY = Math.round(y - sin * (size * 1.25F));
        int dotColor = Render2DUtil.multiplyAlpha(color, 0.90F);
        context.fill(stemX - 1, stemY - 1, stemX + 2, stemY + 2, COLOR_SHADOW);
        context.fill(stemX, stemY, stemX + 1, stemY + 1, dotColor);

        String text = Math.round(distance) + "m";
        int textWidth = client.textRenderer.getWidth(text);
        int badgeW = Math.max(14, textWidth + 6);
        int badgeH = 9;
        int badgeX = Math.round(stemX - badgeW * 0.5F);
        int badgeY = Math.round(stemY + size * 0.45F);
        context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, COLOR_BADGE_BG);
        context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 1, Render2DUtil.withAlpha(color, 170));
        context.drawText(client.textRenderer, text, badgeX + (badgeW - textWidth) / 2, badgeY + 1, COLOR_BADGE_TEXT, false);
    }

    private float smoothAngle(UUID targetId, float rawAngle, long nowMs) {
        Float prev = smoothedAngles.get(targetId);
        float next;
        if (prev == null) {
            next = rawAngle;
        } else {
            float diff = wrapRadians(rawAngle - prev);
            next = normalizeAngle(prev + diff * SMOOTH_FACTOR);
        }
        smoothedAngles.put(targetId, next);
        lastSeenMs.put(targetId, nowMs);
        return next;
    }

    private void pruneOldCache(long nowMs) {
        Iterator<Map.Entry<UUID, Long>> it = lastSeenMs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (nowMs - entry.getValue() > CACHE_TTL_MS) {
                smoothedAngles.remove(entry.getKey());
                it.remove();
            }
        }
    }

    private static float wrapRadians(float angle) {
        while (angle > Math.PI) {
            angle -= (float) (Math.PI * 2.0D);
        }
        while (angle < -Math.PI) {
            angle += (float) (Math.PI * 2.0D);
        }
        return angle;
    }

    private static float normalizeAngle(float angle) {
        return wrapRadians(angle);
    }
}
