package md.dragan.client.gui.modernclick.util;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public final class FramebufferBlurRenderer {
    private SimpleFramebuffer half;
    private SimpleFramebuffer quarter;
    private SimpleFramebuffer full;
    private int baseWidth = -1;
    private int baseHeight = -1;

    public void apply(MinecraftClient client, int x, int y, int width, int height) {
        if (client == null || width <= 1 || height <= 1) {
            return;
        }

        Framebuffer main = client.getFramebuffer();
        if (main == null || main.getColorAttachment() == null || main.getColorAttachmentView() == null) {
            return;
        }

        ensureBuffers(main.textureWidth, main.textureHeight);
        if (half == null || quarter == null || full == null) {
            return;
        }

        main.setFilter(FilterMode.LINEAR);
        half.setFilter(FilterMode.LINEAR);
        quarter.setFilter(FilterMode.LINEAR);
        full.setFilter(FilterMode.LINEAR);

        // Downsample scene -> half -> quarter, then upsample back.
        main.drawBlit(half.getColorAttachmentView());
        half.drawBlit(quarter.getColorAttachmentView());
        quarter.drawBlit(half.getColorAttachmentView());
        half.drawBlit(full.getColorAttachmentView());

        int scaledWidth = Math.max(1, client.getWindow().getScaledWidth());
        int scaledHeight = Math.max(1, client.getWindow().getScaledHeight());
        float scaleX = main.textureWidth / (float) scaledWidth;
        float scaleY = main.textureHeight / (float) scaledHeight;

        int pixelX = Math.round(x * scaleX);
        int pixelYTop = Math.round(y * scaleY);
        int pixelW = Math.max(1, Math.round(width * scaleX));
        int pixelH = Math.max(1, Math.round(height * scaleY));
        int pixelY = main.textureHeight - (pixelYTop + pixelH);

        int clampedX = Math.max(0, Math.min(main.textureWidth - 1, pixelX));
        int clampedY = Math.max(0, Math.min(main.textureHeight - 1, pixelY));
        int maxW = Math.max(0, main.textureWidth - clampedX);
        int maxH = Math.max(0, main.textureHeight - clampedY);
        int copyW = Math.min(pixelW, maxW);
        int copyH = Math.min(pixelH, maxH);
        if (copyW <= 0 || copyH <= 0) {
            return;
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(
            full.getColorAttachment(),
            main.getColorAttachment(),
            0,
            clampedX,
            clampedY,
            clampedX,
            clampedY,
            copyW,
            copyH
        );
    }

    public void close() {
        delete(half);
        delete(quarter);
        delete(full);
        half = null;
        quarter = null;
        full = null;
        baseWidth = -1;
        baseHeight = -1;
    }

    private void ensureBuffers(int width, int height) {
        if (width == baseWidth && height == baseHeight && half != null && quarter != null && full != null) {
            return;
        }

        close();
        baseWidth = width;
        baseHeight = height;

        int halfW = Math.max(2, width / 2);
        int halfH = Math.max(2, height / 2);
        int quarterW = Math.max(2, width / 4);
        int quarterH = Math.max(2, height / 4);

        half = new SimpleFramebuffer("dragan_gui_blur_half", halfW, halfH, false);
        quarter = new SimpleFramebuffer("dragan_gui_blur_quarter", quarterW, quarterH, false);
        full = new SimpleFramebuffer("dragan_gui_blur_full", width, height, false);
    }

    private static void delete(Framebuffer framebuffer) {
        if (framebuffer != null) {
            framebuffer.delete();
        }
    }
}
