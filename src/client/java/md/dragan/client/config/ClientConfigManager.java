package md.dragan.client.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.model.GuiSetting;
import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.model.TextSetting;
import md.dragan.client.gui.modernclick.model.ToggleSetting;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfigManager {
    private static final String DEFAULT_CONFIG = "default";

    private ClientConfigManager() {
    }

    public static Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("dragan");
    }

    public static Path configPath(String name) {
        return configDirectory().resolve(sanitizeName(name) + ".cfg");
    }

    public static void ensureDirectory() throws IOException {
        Files.createDirectories(configDirectory());
    }

    public static void saveDefault() throws IOException {
        save(DEFAULT_CONFIG);
    }

    public static void save(String name) throws IOException {
        ensureDirectory();
        Files.writeString(configPath(name), serialize(), StandardCharsets.UTF_8);
    }

    public static void loadDefault() throws IOException {
        load(DEFAULT_CONFIG);
    }

    public static void load(String name) throws IOException {
        ensureDirectory();
        String content = Files.readString(configPath(name), StandardCharsets.UTF_8);
        deserialize(content);
    }

    public static boolean exists(String name) {
        return Files.exists(configPath(name));
    }

    public static List<String> list() throws IOException {
        ensureDirectory();
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(configDirectory())) {
            stream
                .filter(path -> path.getFileName().toString().endsWith(".cfg"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(path -> {
                    String file = path.getFileName().toString();
                    names.add(file.substring(0, file.length() - 4));
                });
        }
        return names;
    }

    private static String serialize() {
        StringBuilder out = new StringBuilder();
        for (GuiModule module : GuiStateStore.modules()) {
            out.append("module|")
                .append(module.name())
                .append('|')
                .append(module.enabled())
                .append('|')
                .append(module.keyBind())
                .append('\n');

            for (GuiSetting setting : module.settings()) {
                if (setting instanceof ToggleSetting toggle) {
                    out.append("toggle|")
                        .append(module.name())
                        .append('|')
                        .append(toggle.name())
                        .append('|')
                        .append(toggle.value())
                        .append('\n');
                } else if (setting instanceof ModeSetting mode) {
                    out.append("mode|")
                        .append(module.name())
                        .append('|')
                        .append(mode.name())
                        .append('|')
                        .append(mode.index())
                        .append('\n');
                } else if (setting instanceof SliderSetting slider) {
                    out.append("slider|")
                        .append(module.name())
                        .append('|')
                        .append(slider.name())
                        .append('|')
                        .append(slider.value())
                        .append('\n');
                } else if (setting instanceof TextSetting text) {
                    out.append("text|")
                        .append(module.name())
                        .append('|')
                        .append(text.name())
                        .append('|')
                        .append(text.value().replace("|", "").replace("\n", "").replace("\r", ""))
                        .append('\n');
                }
            }
        }
        return out.toString();
    }

    private static void deserialize(String content) {
        GuiStateStore.bootstrap();
        String[] lines = content.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|");
            if (parts.length < 4) {
                continue;
            }

            String type = parts[0];
            String moduleName = parts[1];
            GuiModule module = GuiStateStore.moduleByName(moduleName);
            if (module == null) {
                continue;
            }

            switch (type) {
                case "module" -> {
                    module.setEnabledSilent(Boolean.parseBoolean(parts[2]));
                    module.setKeyBind(parseInt(parts[3], -1));
                }
                case "toggle" -> applyToggle(module, parts[2], parts[3]);
                case "mode" -> applyMode(module, parts[2], parts[3]);
                case "slider" -> applySlider(module, parts[2], parts[3]);
                case "text" -> applyText(module, parts[2], parts[3]);
                default -> {
                }
            }
        }
    }

    private static void applyToggle(GuiModule module, String settingName, String value) {
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ToggleSetting toggle && toggle.name().equalsIgnoreCase(settingName)) {
                toggle.setValue(Boolean.parseBoolean(value));
                if ("Enabled".equalsIgnoreCase(toggle.name())) {
                    module.setEnabledSilent(toggle.value());
                }
                return;
            }
        }
    }

    private static void applyMode(GuiModule module, String settingName, String value) {
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ModeSetting mode && mode.name().equalsIgnoreCase(settingName)) {
                mode.setIndex(parseInt(value, mode.index()));
                return;
            }
        }
    }

    private static void applySlider(GuiModule module, String settingName, String value) {
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof SliderSetting slider && slider.name().equalsIgnoreCase(settingName)) {
                slider.setValue(parseFloat(value, slider.value()));
                return;
            }
        }
    }

    private static void applyText(GuiModule module, String settingName, String value) {
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof TextSetting text && text.name().equalsIgnoreCase(settingName)) {
                text.setValue(value);
                return;
            }
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_CONFIG;
        }
        return name.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
    }
}
