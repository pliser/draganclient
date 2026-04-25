package md.dragan.client.gui.modernclick.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Render2DUtil {
    private Render2DUtil() {
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return (clamp8(alpha) << 24) | (clamp8(red) << 16) | (clamp8(green) << 8) | clamp8(blue);
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp8(alpha) << 24) | (color & 0x00FFFFFF);
    }

    public static int multiplyAlpha(int color, float factor) {
        int alpha = (color >>> 24) & 0xFF;
        return withAlpha(color, clamp8((int) (alpha * factor)));
    }

    public static void rect(DrawContext context, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.fill(x, y, x + width, y + height, color);
    }

    public static void border(DrawContext context, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.drawBorder(x, y, width, height, color);
    }

    public static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            rect(context, x, y, width, height, color);
            return;
        }

        rect(context, x + r, y, width - r * 2, height, color);
        rect(context, x, y + r, r, height - r * 2, color);
        rect(context, x + width - r, y + r, r, height - r * 2, color);

        for (int row = 0; row < r; row++) {
            int inset = cornerInset(r, row);
            rect(context, x + inset, y + row, width - inset * 2, 1, color);
            rect(context, x + inset, y + height - row - 1, width - inset * 2, 1, color);
        }
    }

    public static void drawText(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        int color,
        boolean shadow
    ) {
        GuiFontRenderer.get().draw(context, text, x, y, color, shadow);
    }

    public static void drawTextClipped(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        int maxWidth,
        int color,
        boolean shadow
    ) {
        if (maxWidth <= 0) {
            return;
        }
        drawText(context, textRenderer, clipText(textRenderer, text, maxWidth), x, y, color, shadow);
    }

    public static void drawCenteredText(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int centerX,
        int y,
        int color,
        boolean shadow
    ) {
        int drawX = centerX - textWidth(textRenderer, text) / 2;
        drawText(context, textRenderer, text, drawX, y, color, shadow);
    }

    public static int textWidth(TextRenderer textRenderer, String text) {
        return GuiFontRenderer.get().width(text);
    }

    public static void pushScissor(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.enableScissor(x, y, x + width, y + height);
    }

    public static void popScissor(DrawContext context) {
        context.disableScissor();
    }

    private static String clipText(TextRenderer textRenderer, String text, int maxWidth) {
        return GuiFontRenderer.get().clip(text, maxWidth);
    }

    private static int cornerInset(int radius, int rowFromEdge) {
        float dy = (radius - 1) - rowFromEdge;
        return radius - (int) Math.floor(Math.sqrt(Math.max(0.0F, radius * radius - dy * dy)));
    }

    private static int clamp8(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
