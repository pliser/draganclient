package md.dragan.client.visual.render3d;

import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class Render3DUtil {
    private Render3DUtil() {
    }

    public static float tickDelta(WorldRenderContext context) {
        return context.tickCounter().getTickProgress(false);
    }

    public static Vec3d interpolatedPos(Entity entity, float tickDelta) {
        return entity.getLerpedPos(tickDelta);
    }

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * clamp01(speed);
    }

    public static void billboardSprite(
        WorldRenderContext context,
        Vec3d worldPos,
        float size,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        VertexConsumerProvider provider = context.consumers();
        if (provider == null) {
            provider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        }
        billboardSprite(context, provider, worldPos, size, red, green, blue, alpha);
    }

    public static void billboardSprite(
        WorldRenderContext context,
        VertexConsumerProvider provider,
        Vec3d worldPos,
        float size,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        MatrixStack matrices = Objects.requireNonNull(context.matrixStack(), "matrixStack");
        Camera camera = context.camera();
        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getLightning());
        Vec3d camPos = camera.getPos();
        float half = size * 0.5F;
        float r = clamp01(red);
        float g = clamp01(green);
        float b = clamp01(blue);
        float a = clamp01(alpha);

        matrices.push();
        matrices.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
        matrices.multiply(camera.getRotation());
        Matrix4f pos = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(pos, -half, -half, 0.0F).color(r, g, b, a);
        vertexConsumer.vertex(pos, half, -half, 0.0F).color(r, g, b, a);
        vertexConsumer.vertex(pos, half, half, 0.0F).color(r, g, b, a);
        vertexConsumer.vertex(pos, -half, half, 0.0F).color(r, g, b, a);
        matrices.pop();
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
