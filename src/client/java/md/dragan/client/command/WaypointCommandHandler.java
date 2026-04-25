package md.dragan.client.command;

import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;
import md.dragan.client.waypoint.WaypointManager;
import net.minecraft.client.MinecraftClient;

public final class WaypointCommandHandler {
    private WaypointCommandHandler() {
    }

    public static boolean handle(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim();
        if (!trimmed.startsWith(".way")) {
            return false;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length == 2 && "clear".equalsIgnoreCase(parts[1])) {
            WaypointManager.clear();
            HudBootstrap.notify("Waypoint", "Cleared", NotificationType.INFO);
            return true;
        }

        if (parts.length < 4) {
            HudBootstrap.notify("Waypoint", ".way <x> <y> <z> | .way clear", NotificationType.INFO);
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            HudBootstrap.notify("Waypoint", "World not loaded", NotificationType.ERROR);
            return true;
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            WaypointManager.set("Waypoint", x, y, z, client.world.getRegistryKey());
            HudBootstrap.notify("Waypoint", "Set to " + x + " " + y + " " + z, NotificationType.SUCCESS);
        } catch (NumberFormatException exception) {
            HudBootstrap.notify("Waypoint", "Invalid coordinates", NotificationType.ERROR);
        }
        return true;
    }
}
