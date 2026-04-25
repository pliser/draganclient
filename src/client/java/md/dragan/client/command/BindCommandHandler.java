package md.dragan.client.command;

import java.util.ArrayList;
import java.util.List;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class BindCommandHandler {
    private BindCommandHandler() {
    }

    public static boolean handle(String message) {
        if (message == null) {
            return false;
        }

        String trimmed = message.trim();
        if (!trimmed.startsWith(".bind")) {
            return false;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            notifyInfo("Bind", usage());
            return true;
        }

        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "list" -> {
                listBinds();
                return true;
            }
            case "remove", "del", "clear" -> {
                if (parts.length < 3) {
                    notifyInfo("Bind", ".bind remove <module>");
                    return true;
                }
                removeBind(parts[2]);
                return true;
            }
            default -> {
                if (parts.length < 3) {
                    notifyInfo("Bind", usage());
                    return true;
                }
                bindModule(parts[1], parts[2]);
                return true;
            }
        }
    }

    private static void bindModule(String moduleName, String keyName) {
        GuiModule module = GuiStateStore.moduleByName(moduleName);
        if (module == null) {
            notifyError("Bind", "Module not found: " + moduleName);
            return;
        }

        int keyCode = parseKey(keyName);
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            notifyError("Bind", "Unknown key: " + keyName);
            return;
        }

        module.setKeyBind(keyCode);
        notifySuccess("Bind", module.name() + " -> " + formatKey(keyCode));
    }

    private static void removeBind(String moduleName) {
        GuiModule module = GuiStateStore.moduleByName(moduleName);
        if (module == null) {
            notifyError("Bind", "Module not found: " + moduleName);
            return;
        }

        module.setKeyBind(-1);
        notifyInfo("Bind", "Removed bind from " + module.name());
    }

    private static void listBinds() {
        List<GuiModule> modules = new ArrayList<>(GuiStateStore.modulesWithBinds());
        if (modules.isEmpty()) {
            notifyInfo("Bind", "No active binds");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            GuiModule module = modules.get(i);
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(module.name()).append(": ").append(formatKey(module.keyBind()));
        }
        notifyInfo("Bind List", sb.toString());
    }

    private static int parseKey(String keyName) {
        String normalized = keyName.trim().toUpperCase();
        if (!normalized.startsWith("KEY_")) {
            normalized = "KEY_" + normalized;
        }

        try {
            return GLFW.class.getField("GLFW_" + normalized).getInt(null);
        } catch (ReflectiveOperationException ignored) {
            return InputUtil.UNKNOWN_KEY.getCode();
        }
    }

    public static String formatKey(int keyCode) {
        if (keyCode < 0) {
            return "NONE";
        }
        return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString().toUpperCase();
    }

    private static String usage() {
        return ".bind <module> <key> | .bind list | .bind remove <module>";
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
