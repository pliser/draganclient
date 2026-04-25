package md.dragan.client.gui.modernclick.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import md.dragan.client.gui.modernclick.model.GuiCategory;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.model.GuiSetting;
import md.dragan.client.gui.modernclick.model.ModeSetting;
import md.dragan.client.gui.modernclick.model.SliderSetting;
import md.dragan.client.gui.modernclick.model.TextSetting;
import md.dragan.client.gui.modernclick.model.ToggleSetting;

public final class GuiStateStore {
    private static final List<GuiModule> MODULES = new ArrayList<>();
    private static GuiCategory selectedCategory = GuiCategory.RENDER;
    private static GuiModule selectedModule;

    private GuiStateStore() {
    }

    public static void bootstrap() {
        if (!MODULES.isEmpty()) {
            return;
        }

        GuiModule triggerBot = register("TriggerBot", GuiCategory.COMBAT);
        triggerBot.settings().add(new ToggleSetting("Players Only", true));
        triggerBot.settings().add(new ToggleSetting("Require Weapon", false));
        triggerBot.settings().add(new ModeSetting("Attack Mode", List.of("Direct", "Mouse Emulation"), 0));
        GuiModule noFriendAttack = register("NoFriendAttack", GuiCategory.COMBAT);
        noFriendAttack.setEnabledSilent(true);
        GuiModule legitAim = register("LegitAim", GuiCategory.COMBAT);
        legitAim.settings().add(new SliderSetting("FOV", 8.0F, 120.0F, 1.0F, 34.0F));
        legitAim.settings().add(new SliderSetting("Range", 2.0F, 6.0F, 0.1F, 4.4F));
        legitAim.settings().add(new SliderSetting("Speed", 0.1F, 1.8F, 0.05F, 0.72F));
        legitAim.settings().add(new SliderSetting("Noise", 0.0F, 1.0F, 0.01F, 0.18F));
        legitAim.settings().add(new SliderSetting("Prediction", 0.0F, 1.0F, 0.05F, 0.28F));
        legitAim.settings().add(new SliderSetting("Manual Zone", 1.0F, 12.0F, 0.25F, 4.5F));
        legitAim.settings().add(new SliderSetting("Close Pull", 0.05F, 1.0F, 0.05F, 0.30F));
        legitAim.settings().add(new ModeSetting("Multipoint", List.of("Hitbox", "Adaptive", "Head", "Chest", "Pelvis"), 0));
        legitAim.settings().add(new ToggleSetting("Require Click", true));
        legitAim.settings().add(new ToggleSetting("Ignore Invisible", true));
        legitAim.settings().add(new ToggleSetting("Ignore Teams", true));
        legitAim.settings().add(new ToggleSetting("Raytrace", true));
        GuiModule legitNuker = register("LegitNuker", GuiCategory.COMBAT);
        legitNuker.settings().add(new SliderSetting("Range", 2.0F, 6.0F, 0.1F, 4.5F));
        legitNuker.settings().add(new SliderSetting("FOV", 10.0F, 140.0F, 1.0F, 64.0F));
        legitNuker.settings().add(new SliderSetting("Rotate Speed", 0.10F, 1.80F, 0.05F, 0.78F));
        legitNuker.settings().add(new SliderSetting("Noise", 0.0F, 1.0F, 0.01F, 0.14F));
        legitNuker.settings().add(new SliderSetting("Scan Limit", 256.0F, 12000.0F, 64.0F, 4096.0F));
        legitNuker.settings().add(new ModeSetting("Mode", List.of("Around", "Custom"), 0));
        legitNuker.settings().add(new ToggleSetting("Hold Click", true));
        legitNuker.settings().add(new ToggleSetting("Require Tool", false));
        legitNuker.settings().add(new TextSetting("X1", "0", 10));
        legitNuker.settings().add(new TextSetting("Y1", "64", 10));
        legitNuker.settings().add(new TextSetting("Z1", "0", 10));
        legitNuker.settings().add(new TextSetting("X2", "0", 10));
        legitNuker.settings().add(new TextSetting("Y2", "64", 10));
        legitNuker.settings().add(new TextSetting("Z2", "0", 10));

        GuiModule targetEsp = register("TargetESP", GuiCategory.RENDER);
        targetEsp.settings().add(new ModeSetting("Style", List.of(
            "Souls", "Ring", "Helix", "Pillar", "Pulse",
            "Orbit", "Flame", "Shield", "Vortex", "Sparkle"
        )));
        GuiModule targetHud = register("TargetHUD", GuiCategory.RENDER);
        targetHud.settings().add(new ModeSetting("Style", List.of(
            "Classic", "Modern", "Astolfo", "Compact", "Minimal",
            "Gradient", "Rounded", "Flat", "Neon", "Exhibition"
        )));
        GuiModule nametags = register("Nametags", GuiCategory.RENDER);
        nametags.setEnabledSilent(true);
        nametags.settings().add(new ToggleSetting("Health", true));
        nametags.settings().add(new ToggleSetting("Armor", true));
        nametags.settings().add(new ToggleSetting("Distance", true));
        nametags.settings().add(new ToggleSetting("Ping", false));
        nametags.settings().add(new ToggleSetting("Background", true));
        nametags.settings().add(new ToggleSetting("Shadow", true));
        nametags.settings().add(new ToggleSetting("Self", true));
        nametags.settings().add(new ToggleSetting("ThroughWalls", true));
        nametags.settings().add(new ToggleSetting("Hide Vanilla", true));
        nametags.settings().add(new ModeSetting("Font", List.of("Custom", "Minecraft"), 0));
        nametags.settings().add(new ModeSetting("Scale", List.of("Small", "Normal", "Large"), 1));
        nametags.settings().add(new SliderSetting("Bg Opacity", 0.0F, 255.0F, 1.0F, 120.0F));
        nametags.settings().add(new SliderSetting("Name Red", 0.0F, 255.0F, 1.0F, 242.0F));
        nametags.settings().add(new SliderSetting("Name Green", 0.0F, 255.0F, 1.0F, 242.0F));
        nametags.settings().add(new SliderSetting("Name Blue", 0.0F, 255.0F, 1.0F, 242.0F));
        nametags.settings().add(new SliderSetting("Stat Red", 0.0F, 255.0F, 1.0F, 153.0F));
        nametags.settings().add(new SliderSetting("Stat Green", 0.0F, 255.0F, 1.0F, 188.0F));
        nametags.settings().add(new SliderSetting("Stat Blue", 0.0F, 255.0F, 1.0F, 232.0F));
        GuiModule chams = register("Chams", GuiCategory.RENDER);
        chams.settings().add(new ToggleSetting("Fill", true));
        chams.settings().add(new ToggleSetting("Outline", true));
        chams.settings().add(new ToggleSetting("Self", true));
        chams.settings().add(new ModeSetting("Density", List.of("Low", "Normal", "High"), 1));
        chams.settings().add(new ModeSetting("Opacity", List.of("Low", "Normal", "High"), 1));
        register("Tracers", GuiCategory.RENDER);
        GuiModule arrows = register("Arrows", GuiCategory.RENDER);
        arrows.setEnabledSilent(true);
        arrows.settings().add(new SliderSetting("Radius", 40.0F, 180.0F, 2.0F, 78.0F));
        arrows.settings().add(new SliderSetting("Size", 6.0F, 24.0F, 1.0F, 11.0F));
        arrows.settings().add(new ToggleSetting("Friends", true));
        register("ItemESP", GuiCategory.RENDER);
        register("FullBright", GuiCategory.RENDER);
        GuiModule waypoints = register("Waypoints", GuiCategory.RENDER);
        waypoints.setEnabledSilent(true);
        waypoints.settings().add(new ToggleSetting("Distance", true));
        waypoints.settings().add(new ToggleSetting("Clamp", true));
        waypoints.settings().add(new ModeSetting("Font", List.of("Custom", "Minecraft"), 0));
        waypoints.settings().add(new SliderSetting("Scale", 0.7F, 1.8F, 0.05F, 1.0F));
        register("MotionBlur", GuiCategory.RENDER);
        GuiModule clickGui = register("ClickGUI", GuiCategory.MISC);
        clickGui.settings().add(new ModeSetting("Mode", List.of("Modern", "Dropdown"), 0));
        clickGui.settings().add(new SliderSetting("Scale", 0.75F, 1.50F, 0.05F, 1.00F));
        register("Watermark", GuiCategory.MISC);
        GuiModule notifications = register("Notifications", GuiCategory.MISC);
        notifications.setEnabledSilent(true);
        register("ArrayList", GuiCategory.MISC);
        register("Animations", GuiCategory.MISC);
        register("SoundFX", GuiCategory.MISC);
        register("Blur", GuiCategory.MISC);
        selectedModule = MODULES.getFirst();
    }

    public static List<GuiModule> modules() {
        return MODULES;
    }

    public static GuiCategory selectedCategory() {
        return selectedCategory;
    }

    public static void setSelectedCategory(GuiCategory category) {
        selectedCategory = category;
    }

    public static GuiModule selectedModule() {
        return selectedModule;
    }

    public static void setSelectedModule(GuiModule module) {
        selectedModule = module;
    }

    public static GuiModule firstModuleOf(GuiCategory category) {
        for (GuiModule module : MODULES) {
            if (module.category() == category) {
                return module;
            }
        }
        return null;
    }

    public static GuiModule moduleByName(String name) {
        String key = key(name);
        for (GuiModule module : MODULES) {
            if (key(module.name()).equals(key)) {
                return module;
            }
        }
        return null;
    }

    public static boolean isModuleEnabled(String name) {
        GuiModule module = moduleByName(name);
        return module != null && module.enabled();
    }

    public static boolean toggleModule(String name) {
        GuiModule module = moduleByName(name);
        if (module == null) {
            return false;
        }
        module.setEnabled(!module.enabled());
        return true;
    }

    public static List<GuiModule> modulesWithBinds() {
        List<GuiModule> result = new ArrayList<>();
        for (GuiModule module : MODULES) {
            if (module.hasKeyBind()) {
                result.add(module);
            }
        }
        return result;
    }

    public static String getModeSetting(String moduleName, String settingName) {
        GuiModule module = moduleByName(moduleName);
        if (module == null) {
            return "";
        }
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ModeSetting mode && setting.name().equalsIgnoreCase(settingName)) {
                return mode.value();
            }
        }
        return "";
    }

    public static boolean getToggleSetting(String moduleName, String settingName, boolean fallback) {
        GuiModule module = moduleByName(moduleName);
        if (module == null) {
            return fallback;
        }
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof ToggleSetting toggle && setting.name().equalsIgnoreCase(settingName)) {
                return toggle.value();
            }
        }
        return fallback;
    }

    public static float getSliderSetting(String moduleName, String settingName, float fallback) {
        GuiModule module = moduleByName(moduleName);
        if (module == null) {
            return fallback;
        }
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof SliderSetting slider && setting.name().equalsIgnoreCase(settingName)) {
                return slider.value();
            }
        }
        return fallback;
    }

    public static String getTextSetting(String moduleName, String settingName, String fallback) {
        GuiModule module = moduleByName(moduleName);
        if (module == null) {
            return fallback;
        }
        for (GuiSetting setting : module.settings()) {
            if (setting instanceof TextSetting text && setting.name().equalsIgnoreCase(settingName)) {
                return text.value();
            }
        }
        return fallback;
    }

    private static GuiModule register(String name, GuiCategory category) {
        GuiModule module = new GuiModule(name, category);
        module.settings().add(new ToggleSetting("Enabled", false));
        MODULES.add(module);
        return module;
    }

    private static String key(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
