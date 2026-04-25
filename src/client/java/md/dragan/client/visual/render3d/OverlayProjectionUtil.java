package md.dragan.client.visual.render3d;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class OverlayProjectionUtil {
    private OverlayProjectionUtil() {
    }

    public static ScreenPoint project(MinecraftClient client, Vec3d worldPos) {
        Camera camera = client.gameRenderer.getCamera();
        if (camera == null) {
            return null;
        }

        Vec3d relative = worldPos.subtract(camera.getPos());
        Vector3f cameraSpace = new Vector3f((float) relative.x, (float) relative.y, (float) relative.z);
        cameraSpace.rotate(new Quaternionf(camera.getRotation()).conjugate());
        if (cameraSpace.z >= -0.05F) {
            return null;
        }

        Matrix4f projection = client.gameRenderer.getBasicProjectionMatrix(client.options.getFov().getValue().floatValue());
        Vector4f clip = new Vector4f(cameraSpace.x, cameraSpace.y, cameraSpace.z, 1.0F).mul(projection);
        if (clip.w <= 0.0F) {
            return null;
        }

        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        float screenX = (ndcX * 0.5F + 0.5F) * client.getWindow().getScaledWidth();
        float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * client.getWindow().getScaledHeight();
        return new ScreenPoint(screenX, screenY, -cameraSpace.z, ndcX, ndcY);
    }

    public static Vec3d entityTop(Entity entity, float tickDelta, double yOffset) {
        return entity.getLerpedPos(tickDelta).add(0.0D, entity.getHeight() + yOffset, 0.0D);
    }

    public record ScreenPoint(float x, float y, float depth, float ndcX, float ndcY) {
    }
}
