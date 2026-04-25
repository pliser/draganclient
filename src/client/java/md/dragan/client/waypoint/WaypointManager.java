package md.dragan.client.waypoint;

import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public final class WaypointManager {
    private static Waypoint current;

    private WaypointManager() {
    }

    public static void set(String name, int x, int y, int z, RegistryKey<World> dimension) {
        current = new Waypoint(name, x, y, z, dimension);
    }

    public static void clear() {
        current = null;
    }

    public static Optional<Waypoint> current() {
        return Optional.ofNullable(current);
    }

    public static boolean sameDimension(MinecraftClient client) {
        return current != null && client.world != null && current.dimension() == client.world.getRegistryKey();
    }
}
