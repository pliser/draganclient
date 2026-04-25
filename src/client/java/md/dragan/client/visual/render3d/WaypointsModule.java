package md.dragan.client.visual.render3d;

import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.gui.modernclick.util.GuiFontRenderer;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.waypoint.Waypoint;
import md.dragan.client.waypoint.WaypointManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class WaypointsModule {
    private static final WaypointsModule INSTANCE = new WaypointsModule();
    private static final int PIN_GREEN = 0xFF72F59A;
    private static final int PIN_DARK = 0xFF0D1911;
    private static final int CARD = 0xD610141B;
    private static final int CARD_BORDER = 0xA75BF08C;
    private static final int CARD_SHADOW = 0x5A000000;
    private static final int TEXT_PRIMARY = 0xFFF4F8FC;
    private static final int TEXT_SECONDARY = 0xFFA0B5A8;

    private boolean initialized;

    private WaypointsModule() {
    }

    public static void init() {
        INSTANCE.initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !GuiStateStore.isModuleEnabled("Waypoints")) {
            return;
        }

        Waypoint waypoint = WaypointManager.current().orElse(null);
        if (waypoint == null) {
            return;
        }

        boolean customFont = !"Minecraft".equalsIgnoreCase(GuiStateStore.getModeSetting("Waypoints", "Font"));
        boolean showDistance = GuiStateStore.getToggleSetting("Waypoints", "Distance", true);
        boolean clamp = GuiStateStore.getToggleSetting("Waypoints", "Clamp", true);
        float scale = GuiStateStore.getSliderSetting("Waypoints", "Scale", 1.0F);
        Vec3d target = new Vec3d(waypoint.x() + 0.5D, waypoint.y(), waypoint.z() + 0.5D);
        String distanceText = String.format("%.1fm", client.player.getPos().distanceTo(target));

        OverlayProjectionUtil.ScreenPoint point = OverlayProjectionUtil.project(client, target);
        float x;
        float y;
        if (point == null || clamp && (Math.abs(point.ndcX()) > 1.0F || Math.abs(point.ndcY()) > 1.0F)) {
            double dx = target.x - client.player.getX();
            double dz = target.z - client.player.getZ();
            float angle = (float) Math.atan2(dz, dx) - (client.player.getYaw() + 90.0F) * MathHelper.RADIANS_PER_DEGREE;
            float radius = Math.min(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight()) * 0.34F;
            x = client.getWindow().getScaledWidth() * 0.5F + MathHelper.cos(angle) * radius;
            y = client.getWindow().getScaledHeight() * 0.5F + MathHelper.sin(angle) * radius;
        } else {
            x = point.x();
            y = point.y();
        }

        int ix = Math.round(x);
        int iy = Math.round(y);
        renderPin(context, ix, iy, scale);

        String title = WaypointManager.sameDimension(client)
            ? waypoint.name() + " [" + waypoint.x() + " " + waypoint.y() + " " + waypoint.z() + "]"
            : waypoint.name() + " [other dim]";
        renderCard(context, client, ix, iy + Math.round(16.0F * scale), title, distanceText, customFont, showDistance);
    }

    private void renderPin(DrawContext context, int x, int y, float scale) {
        int glowRadius = Math.max(8, Math.round(8.0F * scale));
        OverlayShapeUtil.fillCircle(context, x, y + 1, glowRadius + 6, 0x22000000);
        OverlayShapeUtil.fillCircle(context, x, y + 1, glowRadius + 3, 0x32000000);
        OverlayShapeUtil.fillMapPin(context, x, y, scale * 1.08F, 0x70000000, 0x22000000);
        OverlayShapeUtil.fillMapPin(context, x, y, scale, 0xF012171E, PIN_DARK);
        OverlayShapeUtil.fillMapPin(context, x, y, scale * 0.82F, PIN_GREEN, PIN_DARK);
        OverlayShapeUtil.softGlowCircle(context, x, y, Math.max(2, Math.round(2.5F * scale)), Render2DUtil.multiplyAlpha(PIN_GREEN, 0.9F));
    }

    private void renderCard(
        DrawContext context,
        MinecraftClient client,
        int centerX,
        int y,
        String title,
        String distanceText,
        boolean customFont,
        boolean showDistance
    ) {
        int titleWidth = customFont ? GuiFontRenderer.get().width(title) : client.textRenderer.getWidth(title);
        int distanceWidth = showDistance ? (customFont ? GuiFontRenderer.get().width(distanceText) : client.textRenderer.getWidth(distanceText)) : 0;
        int width = Math.max(titleWidth, distanceWidth) + 16;
        int height = showDistance ? 24 : 15;
        int x = centerX - width / 2;

        OverlayShapeUtil.roundedPanel(context, x, y, width, height, 5, CARD, CARD_BORDER);
        Render2DUtil.rect(context, x + 3, y + 2, Math.max(1, width - 6), 1, 0x22FFFFFF);
        Render2DUtil.rect(context, x + 5, y + 4, 8, 1, Render2DUtil.withAlpha(PIN_GREEN, 160));
        Render2DUtil.rect(context, x + width - 13, y + 4, 8, 1, Render2DUtil.withAlpha(PIN_GREEN, 160));

        drawText(context, client, title, centerX, y + 3, TEXT_PRIMARY, false, customFont, true);
        if (showDistance) {
            int badgeWidth = distanceWidth + 10;
            int badgeX = centerX - badgeWidth / 2;
            int badgeY = y + 13;
            OverlayShapeUtil.roundedPanel(context, badgeX, badgeY, badgeWidth, 9, 4, 0xEE0D151D, 0x7044B969);
            drawText(context, client, distanceText, centerX, badgeY + 1, TEXT_SECONDARY, false, customFont, true);
        }
    }

    private void drawText(DrawContext context, MinecraftClient client, String text, int centerX, int y, int color, boolean shadow, boolean customFont, boolean centered) {
        int width = customFont ? GuiFontRenderer.get().width(text) : client.textRenderer.getWidth(text);
        int x = centered ? centerX - width / 2 : centerX;
        if (customFont) {
            GuiFontRenderer.get().draw(context, text, x, y, color, shadow);
        } else {
            context.drawText(client.textRenderer, text, x, y, color, shadow);
        }
    }
}
