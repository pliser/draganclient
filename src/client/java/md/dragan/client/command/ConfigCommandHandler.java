package md.dragan.client.command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import md.dragan.client.config.ClientConfigManager;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;

public final class ConfigCommandHandler {
    private ConfigCommandHandler() {
    }

    public static boolean handle(String message) {
        if (message == null) {
            return false;
        }

        String trimmed = message.trim();
        if (!trimmed.startsWith(".cfg")) {
            return false;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            notifyInfo("Config", usage());
            return true;
        }

        String action = parts[1].toLowerCase();
        String name = parts.length >= 3 ? parts[2] : "default";

        try {
            switch (action) {
                case "save" -> save(name);
                case "load" -> load(name);
                case "list" -> list();
                case "dir", "folder" -> dir();
                default -> notifyInfo("Config", usage());
            }
        } catch (IOException exception) {
            notifyError("Config", exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        return true;
    }

    private static void save(String name) throws IOException {
        ClientConfigManager.save(name);
        notifySuccess("Config", "Saved: " + name);
    }

    private static void load(String name) throws IOException {
        if (!ClientConfigManager.exists(name)) {
            notifyError("Config", "Not found: " + name);
            return;
        }
        ClientConfigManager.load(name);
        notifySuccess("Config", "Loaded: " + name);
    }

    private static void list() throws IOException {
        List<String> configs = ClientConfigManager.list();
        if (configs.isEmpty()) {
            notifyInfo("Config", "No configs found");
            return;
        }
        notifyInfo("Config List", String.join(" | ", configs));
    }

    private static void dir() throws IOException {
        Path dir = ClientConfigManager.configDirectory();
        ClientConfigManager.ensureDirectory();
        try {
            java.awt.Desktop.getDesktop().open(dir.toFile());
            notifySuccess("Config Dir", "Opened: " + dir.toAbsolutePath());
        } catch (Exception exception) {
            notifyInfo("Config Dir", dir.toAbsolutePath().toString());
        }
    }

    private static String usage() {
        return ".cfg load <name> | .cfg save <name> | .cfg list | .cfg dir";
    }

    private static void notifyInfo(String title, String message) {
        HudBootstrap.notify(title, message, NotificationType.INFO);
    }

    private static void notifySuccess(String title, String message) {
        HudBootstrap.notify(title, message, NotificationType.SUCCESS);
    }

    private static void notifyError(String title, String message) {
        HudBootstrap.notify(title, message, NotificationType.ERROR);
    }
}
