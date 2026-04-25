package md.dragan.client.input;

import md.dragan.client.command.BindCommandHandler;
import md.dragan.client.command.ConfigCommandHandler;
import md.dragan.client.command.FriendsCommandHandler;
import md.dragan.client.command.WaypointCommandHandler;
import md.dragan.client.gui.dropdown.DropdownClickGuiScreen;
import md.dragan.client.gui.modernclick.ClickGuiScreen;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ClientKeybinds {
    private static final java.util.Set<Integer> PRESSED_BINDS = new java.util.HashSet<>();
    private static final KeyBinding OPEN_CLICK_GUI = KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "key.dragan.click_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.dragan.interface"
        )
    );

    private ClientKeybinds() {
    }

    public static void init() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (BindCommandHandler.handle(message)) {
                return false;
            }
            if (FriendsCommandHandler.handle(message)) {
                return false;
            }
            if (WaypointCommandHandler.handle(message)) {
                return false;
            }
            return !ConfigCommandHandler.handle(message);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CLICK_GUI.wasPressed()) {
                if (client.currentScreen instanceof ClickGuiScreen || client.currentScreen instanceof DropdownClickGuiScreen) {
                    client.setScreen(null);
                } else {
                    String mode = GuiStateStore.getModeSetting("ClickGUI", "Mode");
                    if ("Dropdown".equalsIgnoreCase(mode)) {
                        client.setScreen(new DropdownClickGuiScreen());
                    } else {
                        client.setScreen(new ClickGuiScreen());
                    }
                }
            }

            if (client.currentScreen != null) {
                return;
            }

            long window = client.getWindow().getHandle();
            for (GuiModule module : GuiStateStore.modulesWithBinds()) {
                int key = module.keyBind();
                if (key < 0) {
                    continue;
                }

                boolean pressed = InputUtil.isKeyPressed(window, key);
                if (pressed && PRESSED_BINDS.add(key)) {
                    module.setEnabled(!module.enabled());
                } else if (!pressed) {
                    PRESSED_BINDS.remove(key);
                }
            }
        });
    }
}
