package md.dragan.client.alt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

public final class AltStorage {
    private AltStorage() {
    }

    public static List<AltAccount> load() throws IOException {
        Path path = filePath();
        ensureDirectory();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<AltAccount> result = new ArrayList<>();
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String username = sanitizeUsername(line);
            if (!username.isEmpty()) {
                result.add(new AltAccount(username));
            }
        }
        return result;
    }

    public static void save(List<AltAccount> accounts) throws IOException {
        ensureDirectory();
        List<String> lines = new ArrayList<>();
        for (AltAccount account : accounts) {
            String username = sanitizeUsername(account.username());
            if (!username.isEmpty()) {
                lines.add(username);
            }
        }
        Files.write(filePath(), lines, StandardCharsets.UTF_8);
    }

    public static String sanitizeUsername(String username) {
        if (username == null) {
            return "";
        }
        String sanitized = username.trim().replaceAll("[^A-Za-z0-9_]", "");
        if (sanitized.length() > 16) {
            sanitized = sanitized.substring(0, 16);
        }
        return sanitized.toLowerCase(Locale.ROOT).equals("none") ? "" : sanitized;
    }

    private static void ensureDirectory() throws IOException {
        Files.createDirectories(directory());
    }

    private static Path directory() {
        return FabricLoader.getInstance().getConfigDir().resolve("dragan");
    }

    private static Path filePath() {
        return directory().resolve("alts.txt");
    }
}
