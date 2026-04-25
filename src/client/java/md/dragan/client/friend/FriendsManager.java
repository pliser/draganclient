package md.dragan.client.friend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public final class FriendsManager {
    private static final Set<String> FRIENDS = new LinkedHashSet<>();
    private static boolean loaded;

    private FriendsManager() {
    }

    public static synchronized boolean add(String username) {
        ensureLoaded();
        String normalized = normalize(username);
        if (normalized.isEmpty()) {
            return false;
        }
        boolean added = FRIENDS.add(normalized);
        if (added) {
            saveQuietly();
        }
        return added;
    }

    public static synchronized boolean remove(String username) {
        ensureLoaded();
        boolean removed = FRIENDS.remove(normalize(username));
        if (removed) {
            saveQuietly();
        }
        return removed;
    }

    public static synchronized void clear() {
        ensureLoaded();
        FRIENDS.clear();
        saveQuietly();
    }

    public static synchronized List<String> list() {
        ensureLoaded();
        return FRIENDS.stream().toList();
    }

    public static synchronized boolean isFriend(PlayerEntity player) {
        return player != null && isFriend(player.getName().getString());
    }

    public static synchronized boolean isFriend(String username) {
        ensureLoaded();
        return FRIENDS.contains(normalize(username));
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                return;
            }
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String normalized = normalize(line);
                if (!normalized.isEmpty()) {
                    FRIENDS.add(normalized);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void saveQuietly() {
        try {
            Files.createDirectories(filePath().getParent());
            Files.write(filePath(), FRIENDS.stream().sorted().collect(Collectors.toList()), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static String normalize(String username) {
        if (username == null) {
            return "";
        }
        String sanitized = username.trim().replaceAll("[^A-Za-z0-9_]", "");
        if (sanitized.length() > 16) {
            sanitized = sanitized.substring(0, 16);
        }
        return sanitized.toLowerCase(Locale.ROOT);
    }

    private static Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("dragan").resolve("friends.txt");
    }
}
