package md.dragan.client.visual.render3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import md.dragan.client.friend.FriendsManager;
import md.dragan.client.gui.modernclick.util.GuiFontRenderer;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class NametagsRenderer {
    private static final String MODULE = "Nametags";
    private static final float MAX_RENDER_DISTANCE = 128.0F;
    private static final int FRIEND_GREEN = 0xFF66E088;

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !GuiStateStore.isModuleEnabled(MODULE)) {
            return;
        }

        boolean showHealth = GuiStateStore.getToggleSetting(MODULE, "Health", true);
        boolean showArmor = GuiStateStore.getToggleSetting(MODULE, "Armor", true);
        boolean showDistance = GuiStateStore.getToggleSetting(MODULE, "Distance", true);
        boolean showPing = GuiStateStore.getToggleSetting(MODULE, "Ping", false);
        boolean showBackground = GuiStateStore.getToggleSetting(MODULE, "Background", true);
        boolean showSelf = GuiStateStore.getToggleSetting(MODULE, "Self", true);
        boolean throughWalls = GuiStateStore.getToggleSetting(MODULE, "ThroughWalls", true);
        boolean shadow = GuiStateStore.getToggleSetting(MODULE, "Shadow", true);
        boolean customFont = !"Minecraft".equalsIgnoreCase(GuiStateStore.getModeSetting(MODULE, "Font"));
        float scale = scaleForMode(GuiStateStore.getModeSetting(MODULE, "Scale"));

        int nameColor = colorFromSettings("Name");
        int statColor = colorFromSettings("Stat");
        int bgAlpha = MathHelper.clamp(Math.round(GuiStateStore.getSliderSetting(MODULE, "Bg Opacity", 120.0F)), 0, 255);
        int bgColor = (bgAlpha << 24) | 0x0A0A0A;

        List<TagEntry> tags = new ArrayList<>();
        for (PlayerEntity player : client.world.getPlayers()) {
            if (skip(client, player, showSelf, throughWalls)) {
                continue;
            }
            float distance = client.player.distanceTo(player);
            if (distance > MAX_RENDER_DISTANCE) {
                continue;
            }
            ScreenPoint point = project(client, player, scale);
            if (point == null) {
                continue;
            }
            String nameLine = buildNameLine(player);
            String statsLine = buildStatsLine(client, player, showHealth, showArmor, showDistance, showPing);
            int effectiveNameColor = FriendsManager.isFriend(player) ? FRIEND_GREEN : nameColor;
            tags.add(new TagEntry(player, nameLine, statsLine, point, effectiveNameColor, statColor, bgColor));
        }

        tags.sort(Comparator.comparingDouble((TagEntry entry) -> entry.point().distance()).reversed());
        for (TagEntry entry : tags) {
            drawTag(context, client, entry, customFont, shadow, showBackground);
        }
    }

    private void drawTag(DrawContext context, MinecraftClient client, TagEntry entry, boolean customFont, boolean shadow, boolean showBackground) {
        int nameWidth = textWidth(client, entry.nameLine(), customFont);
        int statsWidth = entry.statsLine().isEmpty() ? 0 : textWidth(client, entry.statsLine(), customFont);
        int totalWidth = Math.max(nameWidth, statsWidth) + 12;
        int x = Math.round(entry.point().x()) - totalWidth / 2;
        int y = Math.round(entry.point().y());

        if (showBackground) {
            int height = entry.statsLine().isEmpty() ? 14 : 24;
            context.fill(x, y, x + totalWidth, y + height, entry.bgColor());
            context.drawBorder(x, y, totalWidth, height, 0xAA1F2C39);
        }

        drawText(context, client, entry.nameLine(), x + 6, y + 3, entry.nameColor(), shadow, customFont);
        if (!entry.statsLine().isEmpty()) {
            drawText(context, client, entry.statsLine(), x + 6, y + 13, entry.statColor(), false, customFont);
        }
    }

    private ScreenPoint project(MinecraftClient client, PlayerEntity player, float scale) {
        Camera camera = client.gameRenderer.getCamera();
        if (camera == null) {
            return null;
        }

        Vec3d pos = player.getLerpedPos(client.getRenderTickCounter().getTickProgress(false))
            .add(0.0D, player.getHeight() + (player.isSneaking() ? 0.34D : 0.52D), 0.0D);
        Vec3d relative = pos.subtract(camera.getPos());
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
        if (Math.abs(ndcX) > 1.3F || Math.abs(ndcY) > 1.3F) {
            return null;
        }

        float screenX = (ndcX * 0.5F + 0.5F) * contextWidth(client);
        float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * contextHeight(client);
        screenY -= 14.0F * scale;
        return new ScreenPoint(screenX, screenY, client.player.distanceTo(player));
    }

    private static int contextWidth(MinecraftClient client) {
        return client.getWindow().getScaledWidth();
    }

    private static int contextHeight(MinecraftClient client) {
        return client.getWindow().getScaledHeight();
    }

    private String buildNameLine(PlayerEntity player) {
        return FriendsManager.isFriend(player) ? "[F] " + player.getName().getString() : player.getName().getString();
    }

    private String buildStatsLine(MinecraftClient client, PlayerEntity player, boolean showHealth, boolean showArmor, boolean showDistance, boolean showPing) {
        StringBuilder line = new StringBuilder();
        if (showHealth) {
            append(line, String.format("HP %.1f", player.getHealth() + player.getAbsorptionAmount()));
        }
        if (showArmor) {
            append(line, "ARM " + player.getArmor());
        }
        if (showDistance && client.player != null) {
            append(line, String.format("%.1fm", client.player.distanceTo(player)));
        }
        if (showPing && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            append(line, "PING " + (entry == null ? "-" : entry.getLatency() + "ms"));
        }
        if (player.isSneaking()) {
            append(line, "SNEAK");
        }
        return line.toString();
    }

    private void append(StringBuilder line, String part) {
        if (line.length() > 0) {
            line.append(" | ");
        }
        line.append(part);
    }

    private int textWidth(MinecraftClient client, String text, boolean customFont) {
        return customFont ? GuiFontRenderer.get().width(text) : client.textRenderer.getWidth(text);
    }

    private void drawText(DrawContext context, MinecraftClient client, String text, int x, int y, int color, boolean shadow, boolean customFont) {
        if (customFont) {
            GuiFontRenderer.get().draw(context, text, x, y, color, shadow);
        } else {
            context.drawText(client.textRenderer, text, x, y, color, shadow);
        }
    }

    private static boolean skip(MinecraftClient client, PlayerEntity player, boolean showSelf, boolean throughWalls) {
        if (player == null || player.isRemoved() || player.isSpectator() || player.isInvisible()) {
            return true;
        }
        if (!showSelf && player == client.player) {
            return true;
        }
        return !throughWalls && client.player != null && !client.player.canSee(player);
    }

    private static float scaleForMode(String mode) {
        return switch (mode) {
            case "Small" -> 0.92F;
            case "Large" -> 1.18F;
            default -> 1.0F;
        };
    }

    private static int colorFromSettings(String prefix) {
        int r = MathHelper.clamp(Math.round(GuiStateStore.getSliderSetting(MODULE, prefix + " Red", 255.0F)), 0, 255);
        int g = MathHelper.clamp(Math.round(GuiStateStore.getSliderSetting(MODULE, prefix + " Green", 255.0F)), 0, 255);
        int b = MathHelper.clamp(Math.round(GuiStateStore.getSliderSetting(MODULE, prefix + " Blue", 255.0F)), 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private record ScreenPoint(float x, float y, float distance) {
    }

    private record TagEntry(PlayerEntity player, String nameLine, String statsLine, ScreenPoint point, int nameColor, int statColor, int bgColor) {
    }
}
