package md.dragan.client.visual.render3d;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ChamsRenderer {
    private static final String MODULE = "Chams";
    private static final float MAX_DISTANCE = 128.0F;
    private static final float FADE_SPEED = 0.20F;

    private final ConcurrentHashMap<UUID, Float> alphaByPlayer = new ConcurrentHashMap<>();

    public void render(WorldRenderContext context) {
        if (context.world() == null || context.matrixStack() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !isEnabled()) {
            alphaByPlayer.clear();
            return;
        }

        boolean showSelf = GuiStateStore.getToggleSetting(MODULE, "Self", true);
        boolean fill = GuiStateStore.getToggleSetting(MODULE, "Fill", true);
        boolean outline = GuiStateStore.getToggleSetting(MODULE, "Outline", true);
        float opacity = opacityFor(GuiStateStore.getModeSetting(MODULE, "Opacity"));
        float tickDelta = Render3DUtil.tickDelta(context);
        float time = context.world().getTime() + tickDelta;

        VertexConsumerProvider provider = context.consumers() != null
            ? context.consumers()
            : client.getBufferBuilders().getEntityVertexConsumers();
        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        for (PlayerEntity player : context.world().getPlayers()) {
            if (skip(client, player, showSelf)) {
                continue;
            }

            float distance = client.player.distanceTo(player);
            if (distance > MAX_DISTANCE) {
                continue;
            }

            float targetAlpha = 1.0F - MathHelper.clamp(distance / MAX_DISTANCE, 0.0F, 1.0F) * 0.55F;
            float currentAlpha = alphaByPlayer.getOrDefault(player.getUuid(), 0.0F);
            float alpha = Render3DUtil.lerp(currentAlpha, targetAlpha, FADE_SPEED);
            alphaByPlayer.put(player.getUuid(), alpha);

            if (alpha < 0.02F) {
                continue;
            }
            renderBoxChams(matrices, provider, player, tickDelta, time, alpha * opacity, fill, outline);
        }

        matrices.pop();
        if (provider instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private void renderBoxChams(
        MatrixStack matrices,
        VertexConsumerProvider provider,
        PlayerEntity player,
        float tickDelta,
        float time,
        float alpha,
        boolean fill,
        boolean outline
    ) {
        Vec3d pos = Render3DUtil.interpolatedPos(player, tickDelta);
        float w = player.getWidth() * 0.55F;
        float h = player.getHeight() + 0.08F;
        float pulse = 0.84F + 0.16F * MathHelper.sin(time * 0.16F + player.age * 0.05F);

        float r = 0.29F;
        float g = 0.56F;
        float b = 0.89F;

        Box outer = new Box(pos.x - w, pos.y, pos.z - w, pos.x + w, pos.y + h, pos.z + w);
        Box inner = outer.shrink(0.045D, 0.045D, 0.045D);

        if (fill) {
            VertexConsumer fillConsumer = provider.getBuffer(RenderLayer.getDebugFilledBox());
            VertexRendering.drawFilledBox(
                matrices,
                fillConsumer,
                (float) outer.minX,
                (float) outer.minY,
                (float) outer.minZ,
                (float) outer.maxX,
                (float) outer.maxY,
                (float) outer.maxZ,
                r,
                g,
                b,
                alpha * 0.20F * pulse
            );
            VertexRendering.drawFilledBox(
                matrices,
                fillConsumer,
                (float) inner.minX,
                (float) inner.minY,
                (float) inner.minZ,
                (float) inner.maxX,
                (float) inner.maxY,
                (float) inner.maxZ,
                0.50F,
                0.72F,
                1.0F,
                alpha * 0.10F * pulse
            );
        }

        if (outline) {
            VertexConsumer lineConsumer = provider.getBuffer(RenderLayer.getLines());
            VertexRendering.drawBox(
                matrices,
                lineConsumer,
                outer.minX,
                outer.minY,
                outer.minZ,
                outer.maxX,
                outer.maxY,
                outer.maxZ,
                r,
                g,
                b,
                alpha * 0.95F
            );
            VertexRendering.drawBox(
                matrices,
                lineConsumer,
                inner.minX,
                inner.minY,
                inner.minZ,
                inner.maxX,
                inner.maxY,
                inner.maxZ,
                0.68F,
                0.83F,
                1.0F,
                alpha * 0.60F
            );
        }
    }

    private static boolean skip(MinecraftClient client, PlayerEntity player, boolean showSelf) {
        if (player == null || client.player == null || player.isRemoved() || player.isSpectator()) {
            return true;
        }
        return !showSelf && player == client.player;
    }

    private static boolean isEnabled() {
        return GuiStateStore.isModuleEnabled(MODULE)
            || GuiStateStore.getToggleSetting(MODULE, "Enabled", false);
    }

    private static float opacityFor(String mode) {
        return switch (mode) {
            case "Low" -> 0.62F;
            case "High" -> 1.0F;
            default -> 0.82F;
        };
    }
}
