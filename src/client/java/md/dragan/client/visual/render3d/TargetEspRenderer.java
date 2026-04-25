package md.dragan.client.visual.render3d;

import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class TargetEspRenderer {
    private static final float FADE_SPEED = 0.18F;

    private PlayerEntity trackedTarget;
    private float alpha;

    public void render(WorldRenderContext context) {
        if (context.matrixStack() == null || context.world() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity aimed = resolveAimedPlayer(client);
        boolean enabled = GuiStateStore.isModuleEnabled("TargetESP");
        if (!enabled) {
            aimed = null;
        }

        if (aimed != null) {
            trackedTarget = aimed;
        }
        if (trackedTarget == null) {
            return;
        }
        if (trackedTarget.isRemoved() || trackedTarget.getWorld() != client.world) {
            trackedTarget = null;
            alpha = 0.0F;
            return;
        }

        float targetAlpha = aimed != null ? 1.0F : 0.0F;
        alpha = Render3DUtil.lerp(alpha, targetAlpha, FADE_SPEED);
        if (alpha < 0.01F && aimed == null) {
            trackedTarget = null;
            return;
        }

        float tickDelta = Render3DUtil.tickDelta(context);
        Vec3d pos = Render3DUtil.interpolatedPos(trackedTarget, tickDelta);
        float time = (context.world().getTime() + tickDelta);
        float h = trackedTarget.getHeight();
        float w = trackedTarget.getWidth();

        String style = GuiStateStore.getModeSetting("TargetESP", "Style");
        if (style.isEmpty()) style = "Souls";

        switch (style) {
            case "Souls" -> renderSouls(context, pos, time, h, w);
            case "Ring" -> renderRing(context, pos, time, h, w);
            case "Helix" -> renderHelix(context, pos, time, h, w);
            case "Pillar" -> renderPillar(context, pos, time, h, w);
            case "Pulse" -> renderPulse(context, pos, time, h, w);
            case "Orbit" -> renderOrbit(context, pos, time, h, w);
            case "Flame" -> renderFlame(context, pos, time, h, w);
            case "Shield" -> renderShield(context, pos, time, h, w);
            case "Vortex" -> renderVortex(context, pos, time, h, w);
            case "Sparkle" -> renderSparkle(context, pos, time, h, w);
            default -> renderSouls(context, pos, time, h, w);
        }
    }

    // ─── 1: Souls — 8 glowing orbs orbiting at chest height ────────
    private void renderSouls(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d c = pos.add(0, h * 0.62, 0);
        for (int i = 0; i < 8; i++) {
            float phase = i * 0.785F; // 2PI/8
            float angle = phase + t * 0.09F;
            float r = 0.66F + MathHelper.sin(t * 0.065F + i * 0.73F) * 0.06F;
            float bob = MathHelper.sin(t * 0.12F + i * 1.37F) * 0.16F;
            Vec3d p = c.add(MathHelper.cos(angle) * r, bob, MathHelper.sin(angle) * r);
            float pulse = 0.75F + 0.25F * MathHelper.sin(t * 0.16F + i);
            float a = alpha * (0.26F + 0.22F * pulse);
            float s = 0.19F + 0.045F * pulse;
            Render3DUtil.billboardSprite(ctx, p, s, 0.30F, 0.58F, 0.95F, a);
            Render3DUtil.billboardSprite(ctx, p, s * 0.58F, 0.68F, 0.82F, 1.0F, a * 0.62F);
        }
    }

    // ─── 2: Ring — dense ring of particles at feet + head, rotating ─
    private void renderRing(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        float radius = w * 0.7F + 0.25F;
        for (int ring = 0; ring < 2; ring++) {
            float y = ring == 0 ? 0.05F : h;
            float rot = t * (ring == 0 ? 0.08F : -0.08F);
            for (int i = 0; i < 40; i++) {
                float angle = i * 0.157F + rot; // 2PI/40
                float px = MathHelper.cos(angle) * radius;
                float pz = MathHelper.sin(angle) * radius;
                Vec3d p = pos.add(px, y, pz);
                float bright = 0.5F + 0.5F * MathHelper.sin(t * 0.2F + i * 0.3F);
                Render3DUtil.billboardSprite(ctx, p, 0.06F, 0.2F * bright, 0.6F * bright, 1.0F * bright, alpha * 0.4F * bright);
            }
        }
        // Connecting vertical particles
        for (int i = 0; i < 4; i++) {
            float angle = i * 1.571F + t * 0.05F; // PI/2
            for (float y = 0.1F; y < h; y += 0.3F) {
                Vec3d p = pos.add(MathHelper.cos(angle) * radius, y, MathHelper.sin(angle) * radius);
                Render3DUtil.billboardSprite(ctx, p, 0.04F, 0.3F, 0.7F, 1.0F, alpha * 0.15F);
            }
        }
    }

    // ─── 3: Helix — double helix DNA spiral, dense particles ────────
    private void renderHelix(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        float radius = w * 0.5F + 0.2F;
        for (int i = 0; i < 60; i++) {
            float frac = i / 60.0F;
            float y = frac * h;
            float angle1 = frac * 12.566F + t * 0.1F; // 4*PI
            float angle2 = angle1 + 3.14159F;
            Vec3d p1 = pos.add(MathHelper.cos(angle1) * radius, y, MathHelper.sin(angle1) * radius);
            Vec3d p2 = pos.add(MathHelper.cos(angle2) * radius, y, MathHelper.sin(angle2) * radius);
            float a = alpha * (0.2F + 0.15F * MathHelper.sin(t * 0.15F + i * 0.2F));
            Render3DUtil.billboardSprite(ctx, p1, 0.08F, 1.0F, 0.2F, 0.3F, a);
            Render3DUtil.billboardSprite(ctx, p2, 0.08F, 0.3F, 0.2F, 1.0F, a);
            // Cross-links
            if (i % 8 == 0) {
                Vec3d mid = p1.add(p2).multiply(0.5);
                Render3DUtil.billboardSprite(ctx, mid, 0.12F, 0.8F, 0.8F, 0.8F, a * 0.5F);
            }
        }
    }

    // ─── 4: Pillar — vertical beam of light with glow ───────────────
    private void renderPillar(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d center = pos.add(0, 0, 0);
        float beamH = h + 2.0F;
        // Central beam
        for (float y = -0.5F; y < beamH; y += 0.08F) {
            float pulse = 0.6F + 0.4F * MathHelper.sin(t * 0.15F + y * 2.0F);
            float size = 0.15F * pulse;
            float a = alpha * 0.25F * pulse * (1.0F - Math.abs(y - h * 0.5F) / (beamH * 0.6F));
            a = Math.max(0, a);
            Vec3d p = center.add(0, y, 0);
            Render3DUtil.billboardSprite(ctx, p, size, 0.9F, 0.9F, 1.0F, a);
        }
        // Outer glow
        for (float y = 0; y < beamH; y += 0.2F) {
            float size = 0.4F + 0.15F * MathHelper.sin(t * 0.1F + y);
            float a = alpha * 0.08F;
            Vec3d p = center.add(0, y, 0);
            Render3DUtil.billboardSprite(ctx, p, size, 0.5F, 0.5F, 1.0F, a);
        }
    }

    // ─── 5: Pulse — expanding sphere of particles ───────────────────
    private void renderPulse(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d center = pos.add(0, h * 0.5, 0);
        for (int wave = 0; wave < 3; wave++) {
            float phase = (t * 0.05F + wave * 0.33F) % 1.0F;
            float radius = 0.1F + phase * 1.5F;
            float waveAlpha = alpha * (1.0F - phase) * 0.35F;
            // Fibonacci sphere points
            for (int i = 0; i < 30; i++) {
                float phi = (float) Math.acos(1.0F - 2.0F * (i + 0.5F) / 30);
                float theta = (float) (Math.PI * (1.0 + Math.sqrt(5.0)) * i);
                float x = MathHelper.sin(phi) * MathHelper.cos(theta) * radius;
                float y = MathHelper.cos(phi) * radius;
                float z = MathHelper.sin(phi) * MathHelper.sin(theta) * radius;
                Vec3d p = center.add(x, y, z);
                Render3DUtil.billboardSprite(ctx, p, 0.06F, 0.2F, 0.9F, 0.5F, waveAlpha);
            }
        }
    }

    // ─── 6: Orbit — 3 large orbs on tilted orbits with trails ───────
    private void renderOrbit(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d center = pos.add(0, h * 0.5, 0);
        float[][] colors = {{1.0F, 0.3F, 0.1F}, {0.1F, 1.0F, 0.3F}, {0.3F, 0.1F, 1.0F}};
        float[] tilts = {0.0F, 1.05F, 2.09F}; // ~60 degree offsets
        float radius = w * 0.5F + 0.5F;

        for (int orb = 0; orb < 3; orb++) {
            float speed = 0.08F + orb * 0.02F;
            float angle = t * speed + orb * 2.094F;
            float tilt = tilts[orb];

            // Main orb
            float x = MathHelper.cos(angle) * radius;
            float y = MathHelper.sin(angle) * MathHelper.sin(tilt) * radius * 0.5F;
            float z = MathHelper.sin(angle) * MathHelper.cos(tilt) * radius;
            Vec3d p = center.add(x, y, z);
            Render3DUtil.billboardSprite(ctx, p, 0.22F, colors[orb][0], colors[orb][1], colors[orb][2], alpha * 0.5F);
            Render3DUtil.billboardSprite(ctx, p, 0.35F, colors[orb][0], colors[orb][1], colors[orb][2], alpha * 0.15F);

            // Trail (8 fading particles behind)
            for (int trail = 1; trail <= 8; trail++) {
                float trailAngle = angle - trail * 0.12F;
                float tx = MathHelper.cos(trailAngle) * radius;
                float ty = MathHelper.sin(trailAngle) * MathHelper.sin(tilt) * radius * 0.5F;
                float tz = MathHelper.sin(trailAngle) * MathHelper.cos(tilt) * radius;
                Vec3d tp = center.add(tx, ty, tz);
                float trailA = alpha * 0.3F * (1.0F - trail / 9.0F);
                float trailS = 0.15F * (1.0F - trail / 10.0F);
                Render3DUtil.billboardSprite(ctx, tp, trailS, colors[orb][0], colors[orb][1], colors[orb][2], trailA);
            }
        }
    }

    // ─── 7: Flame — fire rising from feet with embers ───────────────
    private void renderFlame(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        // Base glow
        for (int i = 0; i < 12; i++) {
            float angle = i * 0.524F + t * 0.03F;
            float r = w * 0.4F;
            Vec3d p = pos.add(MathHelper.cos(angle) * r, 0.05F, MathHelper.sin(angle) * r);
            Render3DUtil.billboardSprite(ctx, p, 0.2F, 1.0F, 0.3F, 0.05F, alpha * 0.2F);
        }
        // Rising flames
        for (int i = 0; i < 24; i++) {
            float seed = i * 1.618F;
            float phase = (t * 0.12F + seed) % 2.0F;
            float y = phase * h * 0.6F;
            float life = 1.0F - phase / 2.0F;
            float spread = w * 0.35F * life;
            float xOff = MathHelper.sin(seed * 13.37F) * spread;
            float zOff = MathHelper.cos(seed * 7.13F) * spread;
            // Flicker
            float flicker = 0.7F + 0.3F * MathHelper.sin(t * 0.5F + seed * 3.0F);
            Vec3d p = pos.add(xOff, y, zOff);
            float a = alpha * life * 0.4F * flicker;
            float size = (0.12F + 0.08F * life) * flicker;
            // Color: orange at bottom → yellow at top
            float red = 1.0F;
            float green = 0.2F + 0.6F * (1.0F - life);
            Render3DUtil.billboardSprite(ctx, p, size, red, green, 0.05F, a);
            // Inner bright core
            Render3DUtil.billboardSprite(ctx, p, size * 0.4F, 1.0F, 0.9F, 0.3F, a * 0.6F);
        }
    }

    // ─── 8: Shield — sphere shell of hexagonal-like pattern ─────────
    private void renderShield(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d center = pos.add(0, h * 0.5, 0);
        float radius = Math.max(w, h) * 0.55F + 0.15F;
        float rot = t * 0.02F;

        // Fibonacci sphere — 64 points
        for (int i = 0; i < 64; i++) {
            float phi = (float) Math.acos(1.0F - 2.0F * (i + 0.5F) / 64);
            float theta = (float) (Math.PI * (1.0 + Math.sqrt(5.0)) * i) + rot;
            float x = MathHelper.sin(phi) * MathHelper.cos(theta) * radius;
            float y = MathHelper.cos(phi) * radius;
            float z = MathHelper.sin(phi) * MathHelper.sin(theta) * radius;
            Vec3d p = center.add(x, y, z);
            float pulse = 0.6F + 0.4F * MathHelper.sin(t * 0.08F + i * 0.3F);
            float a = alpha * 0.2F * pulse;
            Render3DUtil.billboardSprite(ctx, p, 0.07F, 0.2F, 0.6F + 0.4F * pulse, 1.0F, a);
        }
        // Larger glow at equator
        for (int i = 0; i < 16; i++) {
            float angle = i * 0.393F + rot * 2.0F;
            Vec3d p = center.add(MathHelper.cos(angle) * radius, 0, MathHelper.sin(angle) * radius);
            Render3DUtil.billboardSprite(ctx, p, 0.12F, 0.3F, 0.8F, 1.0F, alpha * 0.15F);
        }
    }

    // ─── 9: Vortex — tornado funnel of particles ────────────────────
    private void renderVortex(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        for (int i = 0; i < 80; i++) {
            float frac = i / 80.0F;
            float y = frac * (h + 1.0F);
            float radius = 0.05F + frac * (w * 0.5F + 0.6F);
            float angle = frac * 25.133F + t * 0.15F; // 8*PI
            float x = MathHelper.cos(angle) * radius;
            float z = MathHelper.sin(angle) * radius;
            Vec3d p = pos.add(x, y, z);
            float a = alpha * (0.1F + 0.25F * frac);
            float size = 0.04F + 0.06F * frac;
            // Purple to pink gradient
            float red = 0.5F + 0.5F * frac;
            float green = 0.05F + 0.15F * frac;
            float blue = 1.0F - 0.2F * frac;
            Render3DUtil.billboardSprite(ctx, p, size, red, green, blue, a);
        }
    }

    // ─── 10: Sparkle — random twinkling stars around entity ─────────
    private void renderSparkle(WorldRenderContext ctx, Vec3d pos, float t, float h, float w) {
        Vec3d center = pos.add(0, h * 0.5, 0);
        for (int i = 0; i < 30; i++) {
            float seed = i * 2.618F;
            // Fixed pseudo-random positions
            float x = MathHelper.sin(seed * 13.37F) * (w * 0.5F + 0.5F);
            float y = MathHelper.sin(seed * 7.13F) * h * 0.55F;
            float z = MathHelper.cos(seed * 11.71F) * (w * 0.5F + 0.5F);

            // Twinkle: sharp on/off with smooth fade
            float twinklePhase = (t * 0.2F + seed * 5.0F) % 6.283F;
            float twinkle = Math.max(0, MathHelper.sin(twinklePhase));
            twinkle = twinkle * twinkle; // Sharper peaks
            if (twinkle < 0.05F) continue;

            Vec3d p = center.add(x, y, z);
            float a = alpha * twinkle * 0.6F;
            float size = 0.05F + 0.1F * twinkle;

            // White core
            Render3DUtil.billboardSprite(ctx, p, size, 1.0F, 1.0F, 1.0F, a);
            // Colored halo
            float hue = (seed * 137.508F) % 360.0F;
            float[] rgb = hsbToRgb(hue);
            Render3DUtil.billboardSprite(ctx, p, size * 1.8F, rgb[0], rgb[1], rgb[2], a * 0.3F);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private static float[] hsbToRgb(float hue) {
        float h = (hue % 360) / 60.0F;
        float c = 1.0F;
        float x = c * (1 - Math.abs(h % 2 - 1));
        float r, g, b;
        if (h < 1) { r = c; g = x; b = 0; }
        else if (h < 2) { r = x; g = c; b = 0; }
        else if (h < 3) { r = 0; g = c; b = x; }
        else if (h < 4) { r = 0; g = x; b = c; }
        else if (h < 5) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return new float[]{r, g, b};
    }

    private PlayerEntity resolveAimedPlayer(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return null;
        }
        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof EntityHitResult entityHit)) {
            return null;
        }
        if (!(entityHit.getEntity() instanceof PlayerEntity target)) {
            return null;
        }
        return target == client.player ? null : target;
    }
}
