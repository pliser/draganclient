package md.dragan.client.waypoint;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record Waypoint(String name, int x, int y, int z, RegistryKey<World> dimension) {
}
