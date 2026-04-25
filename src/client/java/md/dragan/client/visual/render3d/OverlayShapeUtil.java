package md.dragan.client.visual.render3d;

import md.dragan.client.gui.modernclick.util.Render2DUtil;
import net.minecraft.client.gui.DrawContext;

public final class OverlayShapeUtil {
    private OverlayShapeUtil() {
    }

    public static void fillTriangle(DrawContext context, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        float minY = Math.min(y1, Math.min(y2, y3));
        float maxY = Math.max(y1, Math.max(y2, y3));
        for (int y = (int) Math.floor(minY); y <= (int) Math.ceil(maxY); y++) {
            float fy = y + 0.5F;
            float[] intersections = new float[3];
            int count = 0;
            count = intersection(x1, y1, x2, y2, fy, intersections, count);
            count = intersection(x2, y2, x3, y3, fy, intersections, count);
            count = intersection(x3, y3, x1, y1, fy, intersections, count);
            if (count < 2) {
                continue;
            }
            float left = Math.min(intersections[0], intersections[1]);
            float right = Math.max(intersections[0], intersections[1]);
            context.fill((int) Math.floor(left), y, (int) Math.ceil(right), y + 1, color);
        }
    }

    public static void fillCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int span = (int) Math.floor(Math.sqrt(radius * radius - y * y));
            context.fill(cx - span, cy + y, cx + span + 1, cy + y + 1, color);
        }
    }

    public static void fillMapPin(DrawContext context, int x, int y, float scale, int outerColor, int innerColor) {
        int radius = Math.max(5, Math.round(6.0F * scale));
        fillCircle(context, x, y, radius, outerColor);
        fillTriangle(context, x, y + radius + 7.0F * scale, x - radius * 0.76F, y + radius - 1.0F, x + radius * 0.76F, y + radius - 1.0F, outerColor);
        fillCircle(context, x, y, Math.max(2, Math.round(radius * 0.43F)), innerColor);
    }

    public static void softGlowCircle(DrawContext context, int x, int y, int radius, int color) {
        fillCircle(context, x, y, radius + 6, Render2DUtil.multiplyAlpha(color, 0.12F));
        fillCircle(context, x, y, radius + 3, Render2DUtil.multiplyAlpha(color, 0.22F));
        fillCircle(context, x, y, radius, color);
    }

    public static void roundedPanel(DrawContext context, int x, int y, int width, int height, int radius, int fill, int border) {
        Render2DUtil.roundedRect(context, x, y, width, height, radius, fill);
        if (width >= 3 && height >= 3) {
            Render2DUtil.roundedRect(context, x + 1, y + 1, width - 2, height - 2, Math.max(0, radius - 1), Render2DUtil.multiplyAlpha(fill, 0.82F));
        }
        drawRoundedOutline(context, x, y, width, height, radius, border);
    }

    public static void drawRoundedOutline(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        if (width <= 1 || height <= 1) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            context.drawBorder(x, y, width, height, color);
            return;
        }

        context.fill(x + r, y, x + width - r, y + 1, color);
        context.fill(x + r, y + height - 1, x + width - r, y + height, color);
        context.fill(x, y + r, x + 1, y + height - r, color);
        context.fill(x + width - 1, y + r, x + width, y + height - r, color);

        for (int row = 0; row < r; row++) {
            int inset = cornerInset(r, row);
            int topY = y + row;
            int bottomY = y + height - row - 1;
            context.fill(x + inset, topY, x + inset + 1, topY + 1, color);
            context.fill(x + width - inset - 1, topY, x + width - inset, topY + 1, color);
            context.fill(x + inset, bottomY, x + inset + 1, bottomY + 1, color);
            context.fill(x + width - inset - 1, bottomY, x + width - inset, bottomY + 1, color);
        }
    }

    public static void fillDiamond(DrawContext context, float centerX, float centerY, float width, float height, int color) {
        fillTriangle(context, centerX, centerY - height * 0.5F, centerX + width * 0.5F, centerY, centerX, centerY + height * 0.5F, color);
        fillTriangle(context, centerX, centerY - height * 0.5F, centerX - width * 0.5F, centerY, centerX, centerY + height * 0.5F, color);
    }

    private static int intersection(float x1, float y1, float x2, float y2, float y, float[] out, int index) {
        if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
            float t = (y - y1) / (y2 - y1);
            out[index++] = x1 + t * (x2 - x1);
        }
        return index;
    }

    private static int cornerInset(int radius, int rowFromEdge) {
        float dy = (radius - 1) - rowFromEdge;
        return radius - (int) Math.floor(Math.sqrt(Math.max(0.0F, radius * radius - dy * dy)));
    }
}
