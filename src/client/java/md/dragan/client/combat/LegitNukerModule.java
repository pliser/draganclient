package md.dragan.client.combat;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class LegitNukerModule {
    private static final LegitNukerModule INSTANCE = new LegitNukerModule();
    private static final String MODULE = "LegitNuker";
    private static final float ANGLE_BREAK_GATE = 9.5F;

    private boolean initialized;
    private BlockPos currentBlock;
    private float progressT;
    private double noiseTime;

    private LegitNukerModule() {
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
        WorldRenderEvents.START.register(this::onRenderFrame);
    }

    private void onTick(MinecraftClient client) {
        if (!isReady(client)) {
            currentBlock = null;
            progressT = 0.0F;
            return;
        }

        BlockPos next = findBestBlock(client);
        if (!Objects.equals(next, currentBlock)) {
            currentBlock = next;
            progressT = 0.0F;
            if (client.interactionManager != null) {
                client.interactionManager.cancelBlockBreaking();
            }
        }
        if (currentBlock == null) {
            return;
        }

        Vec3d center = Vec3d.ofCenter(currentBlock);
        Rotation target = toRotation(client.player.getEyePos(), center);
        float yawError = Math.abs(MathHelper.wrapDegrees(target.yaw() - client.player.getYaw()));
        float pitchError = Math.abs(MathHelper.wrapDegrees(target.pitch() - client.player.getPitch()));
        if (Math.hypot(yawError, pitchError) > ANGLE_BREAK_GATE) {
            return;
        }

        Direction side = facingFor(client.player.getEyePos(), center);
        if (client.interactionManager == null) {
            return;
        }
        client.interactionManager.attackBlock(currentBlock, side);
        client.interactionManager.updateBlockBreakingProgress(currentBlock, side);
        if ((client.player.age & 1) == 0) {
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void onRenderFrame(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!isReady(client) || currentBlock == null) {
            return;
        }

        Vec3d center = Vec3d.ofCenter(currentBlock);
        Rotation target = toRotation(client.player.getEyePos(), center);
        float speed = GuiStateStore.getSliderSetting(MODULE, "Rotate Speed", 0.78F);
        float noise = GuiStateStore.getSliderSetting(MODULE, "Noise", 0.14F);
        float sensitivity = sensitivityScale(client);

        float dyaw = MathHelper.wrapDegrees(target.yaw() - client.player.getYaw());
        float dpitch = MathHelper.wrapDegrees(target.pitch() - client.player.getPitch());
        float total = (float) Math.hypot(dyaw, dpitch);
        float normalized = MathHelper.clamp(total / 95.0F, 0.06F, 1.0F);

        progressT = MathHelper.clamp(progressT + (0.028F + speed * 0.070F) * normalized, 0.0F, 1.0F);
        float eased = cubicBezierEase(progressT, 0.20F, 0.0F, 0.78F, 1.0F);

        noiseTime += 0.013D;
        float yawNoise = (gaussian(noise) + perlin1D(noiseTime * 1.4D) * noise * 0.45F) * (0.06F + speed * 0.09F);
        float pitchNoise = (gaussian(noise) + perlin1D(noiseTime * 1.4D + 11.0D) * noise * 0.45F) * (0.05F + speed * 0.07F);

        float maxYawStep = (0.22F + speed * 1.28F) * sensitivity;
        float maxPitchStep = (0.18F + speed * 0.96F) * sensitivity;
        float yawStep = clampSigned(dyaw * eased + yawNoise, maxYawStep);
        float pitchStep = clampSigned(dpitch * eased + pitchNoise, maxPitchStep);

        client.player.setYaw(client.player.getYaw() + yawStep);
        client.player.setPitch(MathHelper.clamp(client.player.getPitch() + pitchStep, -90.0F, 90.0F));
    }

    private BlockPos findBestBlock(MinecraftClient client) {
        float range = GuiStateStore.getSliderSetting(MODULE, "Range", 4.5F);
        boolean requireTool = GuiStateStore.getToggleSetting(MODULE, "Require Tool", false);
        boolean clickOnly = GuiStateStore.getToggleSetting(MODULE, "Hold Click", true);
        String mode = GuiStateStore.getModeSetting(MODULE, "Mode");

        if (clickOnly && !client.options.attackKey.isPressed()) {
            return null;
        }
        if (requireTool && !isToolLike(client.player.getMainHandStack())) {
            return null;
        }

        BoxBounds bounds = "Custom".equalsIgnoreCase(mode) ? readCustomBounds() : aroundPlayer(client.player, range);
        if (bounds == null) {
            return null;
        }
        int scanLimit = Math.max(64, Math.round(GuiStateStore.getSliderSetting(MODULE, "Scan Limit", 4096.0F)));
        bounds = clampBoundsToLimit(bounds, scanLimit, client.player.getBlockPos());

        BlockPos.Mutable cursor = new BlockPos.Mutable();
        Vec3d eye = client.player.getEyePos();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        float maxFov = GuiStateStore.getSliderSetting(MODULE, "FOV", 64.0F);
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    cursor.set(x, y, z);
                    BlockState state = client.world.getBlockState(cursor);
                    if (state.isAir() || state.getHardness(client.world, cursor) < 0.0F) {
                        continue;
                    }

                    Vec3d center = Vec3d.ofCenter(cursor);
                    double distance = eye.distanceTo(center);
                    if (distance > range + 0.05D) {
                        continue;
                    }

                    Rotation rotation = toRotation(eye, center);
                    float yawError = Math.abs(MathHelper.wrapDegrees(rotation.yaw() - client.player.getYaw()));
                    float pitchError = Math.abs(MathHelper.wrapDegrees(rotation.pitch() - client.player.getPitch()));
                    double angle = Math.hypot(yawError, pitchError);
                    if (angle > maxFov) {
                        continue;
                    }

                    double score = angle * 1.75D + distance * 0.50D;
                    if (score < bestScore) {
                        bestScore = score;
                        best = cursor.toImmutable();
                    }
                }
            }
        }
        return best;
    }

    private BoxBounds aroundPlayer(PlayerEntity player, float range) {
        int r = Math.max(1, Math.round(range));
        BlockPos center = player.getBlockPos();
        return new BoxBounds(center.getX() - r, center.getY() - r, center.getZ() - r, center.getX() + r, center.getY() + r, center.getZ() + r);
    }

    private BoxBounds readCustomBounds() {
        Integer x1 = parseInt(GuiStateStore.getTextSetting(MODULE, "X1", ""));
        Integer y1 = parseInt(GuiStateStore.getTextSetting(MODULE, "Y1", ""));
        Integer z1 = parseInt(GuiStateStore.getTextSetting(MODULE, "Z1", ""));
        Integer x2 = parseInt(GuiStateStore.getTextSetting(MODULE, "X2", ""));
        Integer y2 = parseInt(GuiStateStore.getTextSetting(MODULE, "Y2", ""));
        Integer z2 = parseInt(GuiStateStore.getTextSetting(MODULE, "Z2", ""));
        if (x1 == null || y1 == null || z1 == null || x2 == null || y2 == null || z2 == null) {
            return null;
        }
        return new BoxBounds(
            Math.min(x1, x2),
            Math.min(y1, y2),
            Math.min(z1, z2),
            Math.max(x1, x2),
            Math.max(y1, y2),
            Math.max(z1, z2)
        );
    }

    private BoxBounds clampBoundsToLimit(BoxBounds bounds, int maxBlocks, BlockPos center) {
        long dx = (long) bounds.maxX() - bounds.minX() + 1L;
        long dy = (long) bounds.maxY() - bounds.minY() + 1L;
        long dz = (long) bounds.maxZ() - bounds.minZ() + 1L;
        if (dx * dy * dz <= maxBlocks) {
            return bounds;
        }

        int half = Math.max(1, (int) Math.cbrt(maxBlocks) / 2);
        return new BoxBounds(
            center.getX() - half,
            center.getY() - half,
            center.getZ() - half,
            center.getX() + half,
            center.getY() + half,
            center.getZ() + half
        );
    }

    private Direction facingFor(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        return Direction.getFacing(dx, dy, dz).getOpposite();
    }

    private Rotation toRotation(Vec3d from, Vec3d to) {
        Vec3d delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float pitch = (float) (-Math.toDegrees(Math.atan2(delta.y, horizontal)));
        return new Rotation(yaw, pitch);
    }

    private boolean isReady(MinecraftClient client) {
        return client.player != null
            && client.world != null
            && client.currentScreen == null
            && GuiStateStore.isModuleEnabled(MODULE);
    }

    private boolean isToolLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String id = String.valueOf(stack.getItem().getRegistryEntry().registryKey().getValue());
        return id.contains("pickaxe") || id.contains("shovel") || id.contains("axe") || id.contains("hoe");
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private float sensitivityScale(MinecraftClient client) {
        double raw = client.options.getMouseSensitivity().getValue();
        double curve = raw * 0.6D + 0.2D;
        return (float) Math.max(0.18D, curve * curve * curve * 8.0D);
    }

    private float cubicBezierEase(float x, float p1x, float p1y, float p2x, float p2y) {
        float t = x;
        for (int i = 0; i < 5; i++) {
            float currentX = cubicBezier(t, 0.0F, p1x, p2x, 1.0F);
            float derivative = cubicBezierDerivative(t, 0.0F, p1x, p2x, 1.0F);
            if (Math.abs(derivative) < 1.0E-5F) {
                break;
            }
            t = MathHelper.clamp(t - (currentX - x) / derivative, 0.0F, 1.0F);
        }
        return cubicBezier(t, 0.0F, p1y, p2y, 1.0F);
    }

    private float cubicBezier(float t, float a, float b, float c, float d) {
        float inv = 1.0F - t;
        return inv * inv * inv * a + 3.0F * inv * inv * t * b + 3.0F * inv * t * t * c + t * t * t * d;
    }

    private float cubicBezierDerivative(float t, float a, float b, float c, float d) {
        float inv = 1.0F - t;
        return 3.0F * inv * inv * (b - a) + 6.0F * inv * t * (c - b) + 3.0F * t * t * (d - c);
    }

    private float perlin1D(double x) {
        int x0 = MathHelper.floor(x);
        int x1 = x0 + 1;
        double t = x - x0;
        double fade = t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
        double g0 = gradient(x0);
        double g1 = gradient(x1);
        double v0 = g0 * t;
        double v1 = g1 * (t - 1.0D);
        return (float) MathHelper.lerp(fade, v0, v1);
    }

    private double gradient(int x) {
        long hash = x * 0x9E3779B97F4A7C15L;
        hash ^= hash >>> 33;
        hash *= 0xC2B2AE3D27D4EB4FL;
        return ((hash >>> 63) == 0) ? 1.0D : -1.0D;
    }

    private float gaussian(float noise) {
        return (float) (ThreadLocalRandom.current().nextGaussian() * noise * 0.16D);
    }

    private float clampSigned(float value, float maxAbs) {
        return Math.copySign(Math.min(Math.abs(value), Math.max(0.01F, maxAbs)), value);
    }

    private record Rotation(float yaw, float pitch) {
    }

    private record BoxBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
