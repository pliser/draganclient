package md.dragan.client.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class LegitAimService {
    private static final LegitAimService INSTANCE = new LegitAimService();
    private static final String MODULE = "LegitAim";
    private static final long MIN_OFFSET_MS = 200L;
    private static final long MAX_OFFSET_MS = 500L;
    private static final float AIM_DEADZONE = 0.08F;
    private static final float OFFSET_BLEND = 0.18F;
    private static final int TARGET_STICK_TICKS = 8;

    private final Map<UUID, TargetMemory> memory = new HashMap<>();
    private boolean initialized;
    private PlayerEntity currentTarget;
    private float yawVelocity;
    private float pitchVelocity;
    private double noiseTime;
    private float filteredTargetYaw;
    private float filteredTargetPitch;
    private int targetStickTicks;

    private LegitAimService() {
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
        if (client.player == null || client.world == null) {
            currentTarget = null;
            memory.clear();
            yawVelocity = 0.0F;
            pitchVelocity = 0.0F;
            targetStickTicks = 0;
            return;
        }
        updateTrackedPlayers(client);
    }

    private void onRenderFrame(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            currentTarget = null;
            targetStickTicks = 0;
            decayVelocity();
            return;
        }

        noiseTime += 0.0125D;
        if (!GuiStateStore.isModuleEnabled(MODULE) || client.currentScreen != null) {
            currentTarget = null;
            targetStickTicks = 0;
            decayVelocity();
            return;
        }

        if (GuiStateStore.getToggleSetting(MODULE, "Require Click", true) && !client.options.attackKey.isPressed()) {
            currentTarget = null;
            targetStickTicks = 0;
            decayVelocity();
            return;
        }

        TargetCandidate target = selectTarget(client);
        if (target == null) {
            currentTarget = null;
            targetStickTicks = 0;
            decayVelocity();
            return;
        }

        currentTarget = target.entity();
        applyAimStep(client.player, target.aim(), client);
    }

    private void updateTrackedPlayers(MinecraftClient client) {
        long now = System.currentTimeMillis();
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) {
                continue;
            }

            UUID id = player.getUuid();
            TargetMemory tracked = memory.computeIfAbsent(id, key -> new TargetMemory(player.getPos()));
            Vec3d currentPos = player.getPos();
            if (currentPos.squaredDistanceTo(tracked.lastPos) < 0.0004D) {
                tracked.stillTicks++;
            } else {
                tracked.stillTicks = 0;
            }

            Vec3d toTarget = currentPos.subtract(client.player.getPos());
            double forwardDot = client.player.getRotationVec(1.0F).normalize().dotProduct(toTarget.normalize());
            if (forwardDot < -0.18D) {
                tracked.behindTicks++;
            } else {
                tracked.behindTicks = Math.max(0, tracked.behindTicks - 2);
            }

            tracked.lastSeenAt = now;
            tracked.lastPos = currentPos;
        }

        Iterator<Map.Entry<UUID, TargetMemory>> iterator = memory.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TargetMemory> entry = iterator.next();
            if (now - entry.getValue().lastSeenAt > 4_000L) {
                iterator.remove();
            }
        }
    }

    private TargetCandidate selectTarget(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        float maxFov = GuiStateStore.getSliderSetting(MODULE, "FOV", 34.0F);
        float maxRange = GuiStateStore.getSliderSetting(MODULE, "Range", 4.4F);

        TargetCandidate best = null;
        double bestScore = Double.MAX_VALUE;
        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (!isTargetValid(client, player, candidate, maxRange)) {
                continue;
            }

            AimVector aim = computeAimVector(client, candidate);
            if (aim == null) {
                continue;
            }

            double angleError = Math.hypot(aim.yawDelta(), aim.pitchDelta());
            if (angleError > maxFov) {
                continue;
            }

            double distance = player.distanceTo(candidate);
            double score = angleError * 2.45D + distance * 0.55D;
            if (score < bestScore) {
                bestScore = score;
                best = new TargetCandidate(candidate, aim, score);
            }
        }

        if (best == null) {
            return null;
        }

        if (currentTarget != null
            && currentTarget.isAlive()
            && !currentTarget.isRemoved()
            && targetStickTicks > 0
            && best.entity() != currentTarget) {
            AimVector currentAim = computeAimVector(client, currentTarget);
            if (currentAim != null) {
                double currentScore = Math.hypot(currentAim.yawDelta(), currentAim.pitchDelta()) * 2.45D
                    + player.distanceTo(currentTarget) * 0.55D;
                if (currentScore <= best.score() * 1.18D) {
                    targetStickTicks--;
                    return new TargetCandidate(currentTarget, currentAim, currentScore);
                }
            }
        }

        if (best.entity() != currentTarget) {
            targetStickTicks = TARGET_STICK_TICKS;
            filteredTargetYaw = player.getYaw();
            filteredTargetPitch = player.getPitch();
        } else if (targetStickTicks > 0) {
            targetStickTicks--;
        }

        return best;
    }

    private boolean isTargetValid(MinecraftClient client, ClientPlayerEntity self, PlayerEntity candidate, float maxRange) {
        if (candidate == self || !candidate.isAlive() || candidate.isRemoved() || candidate.isSpectator()) {
            return false;
        }
        if (candidate.distanceTo(self) > maxRange) {
            return false;
        }
        if (GuiStateStore.getToggleSetting(MODULE, "Ignore Invisible", true) && candidate.isInvisible()) {
            return false;
        }
        if (GuiStateStore.getToggleSetting(MODULE, "Ignore Teams", true) && isTeammate(self, candidate)) {
            return false;
        }
        if (isAntiBot(client, self, candidate)) {
            return false;
        }
        return !GuiStateStore.getToggleSetting(MODULE, "Raytrace", true) || hasLineOfSight(client, candidate);
    }

    private boolean isAntiBot(MinecraftClient client, ClientPlayerEntity self, PlayerEntity candidate) {
        PlayerListEntry entry = client.getNetworkHandler() == null ? null : client.getNetworkHandler().getPlayerListEntry(candidate.getUuid());
        if (entry == null || entry.getLatency() <= 0) {
            return true;
        }

        TargetMemory tracked = memory.get(candidate.getUuid());
        if (tracked == null) {
            return false;
        }

        double spawnTravel = tracked.spawnPos.squaredDistanceTo(candidate.getPos());
        if (tracked.stillTicks > 30 && spawnTravel < 0.01D && candidate.age > 20) {
            return true;
        }

        Vec3d toCandidate = candidate.getPos().subtract(self.getPos());
        if (toCandidate.lengthSquared() > 1.0E-4D) {
            double behindDot = self.getRotationVec(1.0F).normalize().dotProduct(toCandidate.normalize());
            if (behindDot < -0.35D && tracked.behindTicks > 30) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeammate(ClientPlayerEntity self, PlayerEntity candidate) {
        AbstractTeam ownTeam = self.getScoreboardTeam();
        AbstractTeam targetTeam = candidate.getScoreboardTeam();
        return ownTeam != null && ownTeam == targetTeam;
    }

    private boolean hasLineOfSight(MinecraftClient client, PlayerEntity target) {
        Vec3d eyePos = client.player.getEyePos();
        Vec3d targetPoint = resolveAimPoint(client, target, false);
        BlockHitResult hitResult = client.world.raycast(new RaycastContext(
            eyePos,
            targetPoint,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            client.player
        ));
        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        return hitResult.getPos().squaredDistanceTo(eyePos) + 0.01D >= eyePos.squaredDistanceTo(targetPoint);
    }

    private AimVector computeAimVector(MinecraftClient client, PlayerEntity target) {
        Vec3d eyePos = client.player.getEyePos();
        Vec3d targetPoint = resolveAimPoint(client, target, true);
        if (targetPoint == null) {
            return null;
        }
        Vec3d predictedPoint = targetPoint;
        if (target.getVelocity().horizontalLengthSquared() > 0.0125D) {
            float prediction = GuiStateStore.getSliderSetting(MODULE, "Prediction", 0.28F);
            predictedPoint = targetPoint.add(target.getVelocity().multiply(0.55D + prediction * 0.85D));
        }

        if (GuiStateStore.getToggleSetting(MODULE, "Raytrace", true) && !isRaytraceClear(client, eyePos, predictedPoint)) {
            return null;
        }

        Vec3d delta = predictedPoint.subtract(eyePos);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float targetYaw = (float) (MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D));
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(delta.y, horizontal)));
        float yawDelta = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());
        float pitchDelta = MathHelper.wrapDegrees(targetPitch - client.player.getPitch());
        return new AimVector(targetYaw, targetPitch, yawDelta, pitchDelta);
    }

    private boolean isRaytraceClear(MinecraftClient client, Vec3d eyePos, Vec3d targetPoint) {
        BlockHitResult hitResult = client.world.raycast(new RaycastContext(
            eyePos,
            targetPoint,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            client.player
        ));
        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        double blockDistance = hitResult.getPos().squaredDistanceTo(eyePos);
        double targetDistance = targetPoint.squaredDistanceTo(eyePos);
        return blockDistance + 0.01D >= targetDistance;
    }

    private Vec3d resolveAimPoint(MinecraftClient client, PlayerEntity target, boolean preferVisiblePoint) {
        TargetMemory tracked = memory.computeIfAbsent(target.getUuid(), key -> new TargetMemory(target.getPos()));
        long now = System.currentTimeMillis();
        if (tracked.aimOffset == null || tracked.targetAimOffset == null || now >= tracked.nextOffsetAt) {
            tracked.targetAimOffset = chooseMultipointOffset(client, target, preferVisiblePoint);
            if (tracked.aimOffset == null) {
                tracked.aimOffset = tracked.targetAimOffset;
            }
            tracked.nextOffsetAt = now + ThreadLocalRandom.current().nextLong(MIN_OFFSET_MS, MAX_OFFSET_MS + 1L);
        }
        tracked.aimOffset = tracked.aimOffset.lerp(tracked.targetAimOffset, OFFSET_BLEND);
        return target.getPos().add(tracked.aimOffset);
    }

    private Vec3d chooseMultipointOffset(MinecraftClient client, PlayerEntity target, boolean preferVisiblePoint) {
        String mode = GuiStateStore.getModeSetting(MODULE, "Multipoint");
        if (mode.isEmpty()) {
            mode = "Hitbox";
        }

        Vec3d eyePos = client.player != null ? client.player.getEyePos() : Vec3d.ZERO;
        Vec3d[] candidates = multipointOffsets(target, mode);
        Vec3d bestOffset = candidates[0];
        double bestScore = Double.MAX_VALUE;
        boolean requireRaytrace = GuiStateStore.getToggleSetting(MODULE, "Raytrace", true);

        for (Vec3d offset : candidates) {
            Vec3d worldPoint = target.getPos().add(offset);
            boolean visible = !requireRaytrace || isRaytraceClear(client, eyePos, worldPoint);
            if (preferVisiblePoint && requireRaytrace && !visible) {
                continue;
            }

            Vec3d delta = worldPoint.subtract(eyePos);
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            float yaw = (float) (MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D));
            float pitch = (float) (-Math.toDegrees(Math.atan2(delta.y, horizontal)));
            float yawDelta = MathHelper.wrapDegrees(yaw - client.player.getYaw());
            float pitchDelta = MathHelper.wrapDegrees(pitch - client.player.getPitch());
            double score = Math.hypot(yawDelta, pitchDelta);
            score += bodyCenterPenalty(offset, target);
            if (!visible) {
                score += 10.0D;
            }
            if (score < bestScore) {
                bestScore = score;
                bestOffset = offset;
            }
        }

        return bestOffset;
    }

    private Vec3d[] multipointOffsets(PlayerEntity target, String mode) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Box box = target.getBoundingBox();
        double width = box.getLengthX() * 0.18D;
        double depth = box.getLengthZ() * 0.18D;
        double height = target.getDimensions(EntityPose.STANDING).height();
        double sideX = width * 0.48D;
        double frontZ = depth * 0.48D;

        if ("Hitbox".equalsIgnoreCase(mode)) {
            return fullHitboxOffsets(random, width, depth, height);
        }

        if ("Head".equalsIgnoreCase(mode)) {
            return new Vec3d[] {
                point(random, width * 0.65D, depth * 0.65D, height, 0.84D, 0.92D),
                new Vec3d(sideX, height * 0.87D, 0.0D),
                new Vec3d(-sideX, height * 0.87D, 0.0D),
                new Vec3d(0.0D, height * 0.88D, frontZ * 0.65D),
                new Vec3d(0.0D, height * 0.88D, -frontZ * 0.65D)
            };
        }
        if ("Chest".equalsIgnoreCase(mode)) {
            return new Vec3d[] {
                point(random, width * 0.55D, depth * 0.55D, height, 0.60D, 0.72D),
                new Vec3d(sideX * 0.72D, height * 0.66D, 0.0D),
                new Vec3d(-sideX * 0.72D, height * 0.66D, 0.0D),
                new Vec3d(0.0D, height * 0.66D, frontZ * 0.55D),
                new Vec3d(0.0D, height * 0.66D, -frontZ * 0.55D)
            };
        }
        if ("Pelvis".equalsIgnoreCase(mode)) {
            return new Vec3d[] {
                point(random, width * 0.50D, depth * 0.50D, height, 0.44D, 0.54D),
                new Vec3d(sideX * 0.65D, height * 0.48D, 0.0D),
                new Vec3d(-sideX * 0.65D, height * 0.48D, 0.0D),
                new Vec3d(0.0D, height * 0.48D, frontZ * 0.50D),
                new Vec3d(0.0D, height * 0.48D, -frontZ * 0.50D)
            };
        }

        return new Vec3d[] {
            point(random, width * 0.50D, depth * 0.50D, height, 0.64D, 0.70D),
            point(random, width * 0.45D, depth * 0.45D, height, 0.78D, 0.88D),
            point(random, width * 0.45D, depth * 0.45D, height, 0.46D, 0.54D),
            new Vec3d(sideX * 0.55D, height * 0.66D, 0.0D),
            new Vec3d(-sideX * 0.55D, height * 0.66D, 0.0D),
            new Vec3d(0.0D, height * 0.66D, frontZ * 0.45D),
            new Vec3d(0.0D, height * 0.66D, -frontZ * 0.45D)
        };
    }

    private Vec3d point(ThreadLocalRandom random, double width, double depth, double height, double minY, double maxY) {
        return new Vec3d(
            random.nextDouble(-width, width),
            height * random.nextDouble(minY, maxY),
            random.nextDouble(-depth, depth)
        );
    }

    private Vec3d[] fullHitboxOffsets(ThreadLocalRandom random, double width, double depth, double height) {
        double innerX = width * 0.35D;
        double midX = width * 0.72D;
        double outerX = width;
        double innerZ = depth * 0.35D;
        double midZ = depth * 0.72D;
        double outerZ = depth;

        return new Vec3d[] {
            point(random, innerX, innerZ, height, 0.80D, 0.92D),
            point(random, innerX, innerZ, height, 0.66D, 0.78D),
            point(random, innerX, innerZ, height, 0.52D, 0.64D),
            point(random, innerX, innerZ, height, 0.38D, 0.50D),

            new Vec3d(midX, height * 0.86D, 0.0D),
            new Vec3d(-midX, height * 0.86D, 0.0D),
            new Vec3d(0.0D, height * 0.86D, midZ),
            new Vec3d(0.0D, height * 0.86D, -midZ),

            new Vec3d(midX, height * 0.72D, 0.0D),
            new Vec3d(-midX, height * 0.72D, 0.0D),
            new Vec3d(0.0D, height * 0.72D, midZ),
            new Vec3d(0.0D, height * 0.72D, -midZ),

            new Vec3d(outerX * 0.78D, height * 0.58D, 0.0D),
            new Vec3d(-outerX * 0.78D, height * 0.58D, 0.0D),
            new Vec3d(0.0D, height * 0.58D, outerZ * 0.78D),
            new Vec3d(0.0D, height * 0.58D, -outerZ * 0.78D),

            new Vec3d(midX, height * 0.44D, 0.0D),
            new Vec3d(-midX, height * 0.44D, 0.0D),
            new Vec3d(0.0D, height * 0.44D, midZ),
            new Vec3d(0.0D, height * 0.44D, -midZ),

            new Vec3d(innerX, height * 0.70D, innerZ),
            new Vec3d(innerX, height * 0.70D, -innerZ),
            new Vec3d(-innerX, height * 0.70D, innerZ),
            new Vec3d(-innerX, height * 0.70D, -innerZ),

            new Vec3d(innerX, height * 0.54D, innerZ),
            new Vec3d(innerX, height * 0.54D, -innerZ),
            new Vec3d(-innerX, height * 0.54D, innerZ),
            new Vec3d(-innerX, height * 0.54D, -innerZ),

            point(random, width * 0.55D, depth * 0.55D, height, 0.42D, 0.88D),
            point(random, width * 0.55D, depth * 0.55D, height, 0.42D, 0.88D)
        };
    }

    private double bodyCenterPenalty(Vec3d offset, PlayerEntity target) {
        double height = target.getDimensions(EntityPose.STANDING).height();
        double halfWidth = Math.max(0.001D, target.getWidth() * 0.5D);
        double normalizedX = Math.abs(offset.x) / halfWidth;
        double normalizedZ = Math.abs(offset.z) / halfWidth;
        double hitboxMid = height * 0.62D;
        double normalizedY = Math.abs(offset.y - hitboxMid) / Math.max(0.22D, height * 0.42D);
        return normalizedX * 1.15D + normalizedZ * 1.15D + normalizedY * 0.30D;
    }

    private void applyAimStep(ClientPlayerEntity player, AimVector aim, MinecraftClient client) {
        float speed = GuiStateStore.getSliderSetting(MODULE, "Speed", 0.72F);
        float noiseAmount = GuiStateStore.getSliderSetting(MODULE, "Noise", 0.18F);
        float sensitivity = mouseSensitivityScale(client);
        float manualZone = GuiStateStore.getSliderSetting(MODULE, "Manual Zone", 4.5F);
        float closePull = GuiStateStore.getSliderSetting(MODULE, "Close Pull", 0.30F);
        float angleError = (float) Math.hypot(aim.yawDelta(), aim.pitchDelta());
        float assistBlend = smoothstep(MathHelper.clamp(angleError / Math.max(0.5F, manualZone), 0.0F, 1.0F));
        float pullStrength = MathHelper.lerp(assistBlend, closePull, 1.0F);
        float tracking = (0.22F + speed * 0.20F) * pullStrength;

        filteredTargetYaw = player.getYaw() + MathHelper.lerpAngleDegrees(
            tracking,
            0.0F,
            MathHelper.wrapDegrees(aim.targetYaw() - player.getYaw())
        );
        filteredTargetPitch = MathHelper.lerp(
            tracking,
            filteredTargetPitch == 0.0F && currentTarget != null ? player.getPitch() : filteredTargetPitch,
            aim.targetPitch()
        );

        float filteredYawDelta = MathHelper.wrapDegrees(filteredTargetYaw - player.getYaw());
        float filteredPitchDelta = MathHelper.wrapDegrees(filteredTargetPitch - player.getPitch());
        float yawStep = computeSmoothedStep(filteredYawDelta, speed, sensitivity, noiseAmount, assistBlend, true);
        float pitchStep = computeSmoothedStep(filteredPitchDelta, speed, sensitivity, noiseAmount, assistBlend, false);

        player.setYaw(player.getYaw() + yawStep);
        player.setPitch(MathHelper.clamp(player.getPitch() + pitchStep, -90.0F, 90.0F));
    }

    private float computeSmoothedStep(float delta, float speed, float sensitivity, float noiseAmount, float assistBlend, boolean yawAxis) {
        float abs = Math.abs(delta);
        if (abs < AIM_DEADZONE) {
            if (yawAxis) {
                yawVelocity *= 0.56F;
                return yawVelocity;
            }
            pitchVelocity *= 0.56F;
            return pitchVelocity;
        }
        if (abs < 0.001F) {
            return 0.0F;
        }

        float normalized = MathHelper.clamp(abs / 32.0F, 0.0F, 1.0F);
        float eased = cubicBezierEase(normalized, 0.24F, 0.0F, 0.76F, 1.0F);
        float logistic = (float) (1.0D / (1.0D + Math.exp(-(eased * 8.0D - 4.0D))));
        float currentVelocity = yawAxis ? yawVelocity : pitchVelocity;
        float spring = (0.18F + speed * 0.18F) + assistBlend * 0.20F;
        float damping = 0.50F + assistBlend * (0.14F + speed * 0.08F);
        float acceleration = delta * spring * (0.22F + logistic * 0.55F);
        float nextVelocity = (currentVelocity + acceleration) * damping;

        // Low-frequency noise keeps the movement organic without injecting frame-to-frame jerk.
        float drift = perlin1D(noiseTime * 0.75D + (yawAxis ? 0.0D : 19.0D)) * noiseAmount * (0.03F + assistBlend * 0.05F);
        float tremor = perlin1D(noiseTime * 1.8D + (yawAxis ? 7.0D : 31.0D)) * noiseAmount * (0.01F + assistBlend * 0.01F);
        nextVelocity += drift + tremor;

        float minStep = (0.01F + speed * 0.01F) + assistBlend * 0.03F;
        float maxStep = Math.max(0.05F + speed * 0.16F + assistBlend * 0.25F, abs * (0.18F + assistBlend * 0.34F));
        float magnitude = Math.min(Math.abs(nextVelocity), maxStep);
        if (magnitude < minStep && abs > AIM_DEADZONE * 1.4F) {
            magnitude = minStep;
        }
        float step = Math.copySign(magnitude, delta);
        if (Math.abs(step) > abs) {
            step = Math.copySign(abs, delta);
        }

        if (yawAxis) {
            yawVelocity = step;
        } else {
            pitchVelocity = step;
        }
        return step;
    }

    private float mouseSensitivityScale(MinecraftClient client) {
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
        return inv * inv * inv * a
            + 3.0F * inv * inv * t * b
            + 3.0F * inv * t * t * c
            + t * t * t * d;
    }

    private float cubicBezierDerivative(float t, float a, float b, float c, float d) {
        float inv = 1.0F - t;
        return 3.0F * inv * inv * (b - a)
            + 6.0F * inv * t * (c - b)
            + 3.0F * t * t * (d - c);
    }

    private float smoothstep(float x) {
        return x * x * (3.0F - 2.0F * x);
    }

    private float perlin1D(double x) {
        int x0 = MathHelper.floor(x);
        int x1 = x0 + 1;
        double t = x - x0;
        double fade = t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
        double g0 = gradient(x0);
        double g1 = gradient(x1);
        double v0 = g0 * (t);
        double v1 = g1 * (t - 1.0D);
        return (float) MathHelper.lerp(fade, v0, v1);
    }

    private double gradient(int x) {
        long hash = x * 0x9E3779B97F4A7C15L;
        hash ^= hash >>> 33;
        hash *= 0xC2B2AE3D27D4EB4FL;
        return ((hash >>> 63) == 0) ? 1.0D : -1.0D;
    }

    private void decayVelocity() {
        yawVelocity *= 0.58F;
        pitchVelocity *= 0.58F;
    }

    private record AimVector(float targetYaw, float targetPitch, float yawDelta, float pitchDelta) {
    }

    private record TargetCandidate(PlayerEntity entity, AimVector aim, double score) {
    }

    private static final class TargetMemory {
        private final Vec3d spawnPos;
        private Vec3d lastPos;
        private Vec3d aimOffset;
        private Vec3d targetAimOffset;
        private long nextOffsetAt;
        private long lastSeenAt;
        private int stillTicks;
        private int behindTicks;

        private TargetMemory(Vec3d spawnPos) {
            this.spawnPos = spawnPos;
            this.lastPos = spawnPos;
            this.lastSeenAt = System.currentTimeMillis();
        }
    }
}
