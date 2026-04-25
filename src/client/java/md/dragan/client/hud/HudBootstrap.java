package md.dragan.client.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import md.dragan.client.gui.modernclick.model.GuiModule;
import md.dragan.client.gui.modernclick.state.GuiStateStore;
import md.dragan.client.hud.elements.ArrayListElement;
import md.dragan.client.hud.elements.NotificationsElement;
import md.dragan.client.hud.elements.TargetHudElement;
import md.dragan.client.hud.elements.WatermarkElement;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;

public final class HudBootstrap {
    private static HudManager manager;
    private static NotificationsElement notifications;
    private static boolean initialized;
    private static boolean leftMouseDown;

    private HudBootstrap() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        GuiStateStore.bootstrap();
        HudRenderCallback.EVENT.register((context, tickCounter) -> onHudRender(context));
    }

    public static void notify(String title, String description, NotificationType type) {
        HudManager hud = ensureManager();
        if (hud == null || notifications == null) {
            return;
        }
        notifications.push(title, description, type, 2800L);
    }

    public static void setEditorMode(boolean enabled) {
        HudManager hud = ensureManager();
        if (hud == null) {
            return;
        }
        hud.setEditorMode(enabled);
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        HudManager hud = ensureManager();
        return hud != null && hud.mouseClicked(mouseX, mouseY, button);
    }

    public static boolean mouseDragged(double mouseX, double mouseY, int button) {
        HudManager hud = ensureManager();
        return hud != null && hud.mouseDragged(mouseX, mouseY, button);
    }

    public static boolean mouseReleased(double mouseX, double mouseY, int button) {
        HudManager hud = ensureManager();
        return hud != null && hud.mouseReleased(mouseX, mouseY, button);
    }

    private static void onHudRender(DrawContext context) {
        HudManager hud = ensureManager();
        if (hud == null) {
            return;
        }
        syncVisibility();
        tickChatDrag(hud);
        hud.update(1.0F / 60.0F);
        hud.render(context, 1.0F / 60.0F);
    }

    private static void tickChatDrag(HudManager hud) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) {
            leftMouseDown = false;
            return;
        }

        hud.setEditorMode(true);
        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();

        if (down && !leftMouseDown) {
            hud.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        } else if (down) {
            hud.mouseDragged(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        } else if (leftMouseDown) {
            hud.mouseReleased(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        leftMouseDown = down;
    }

    private static HudManager ensureManager() {
        if (manager != null) {
            return manager;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }

        manager = new HudManager(client, new MemoryPositionStore());
        WatermarkElement watermark = new WatermarkElement("Dragan", version(), 8.0F, 8.0F);
        ArrayListElement arrayList = new ArrayListElement(8.0F, 34.0F, new GuiModuleSource());
        notifications = new NotificationsElement(8.0F, 88.0F);
        TargetHudElement targetHud = new TargetHudElement(
            client.getWindow().getScaledWidth() / 2.0F - 80.0F,
            client.getWindow().getScaledHeight() / 2.0F + 40.0F
        );

        manager.register(watermark);
        manager.register(arrayList);
        manager.register(notifications);
        manager.register(targetHud);
        alignArrayListRight(client, arrayList);
        alignNotificationsBottomRight(client, notifications);
        manager.setEditorMode(false);
        return manager;
    }

    private static void alignArrayListRight(MinecraftClient client, HudElement element) {
        element.syncBounds(client);
        float x = client.getWindow().getScaledWidth() - element.width() - 8.0F;
        element.setPositionInternal(x, 8.0F);
        element.clampToScreen(client);
    }

    private static void alignNotificationsBottomRight(MinecraftClient client, HudElement element) {
        element.syncBounds(client);
        float x = client.getWindow().getScaledWidth() - element.width() - 8.0F;
        float y = client.getWindow().getScaledHeight() - element.height() - 8.0F;
        element.setPositionInternal(x, y);
        element.clampToScreen(client);
    }

    private static void syncVisibility() {
        GuiStateStore.bootstrap();
        for (HudElement element : manager.elements()) {
            switch (element.id()) {
                case "watermark" -> element.setVisible(GuiStateStore.isModuleEnabled("Watermark"));
                case "arraylist" -> element.setVisible(GuiStateStore.isModuleEnabled("ArrayList"));
                case "notifications" -> element.setVisible(GuiStateStore.isModuleEnabled("Notifications"));
                case "targethud" -> element.setVisible(GuiStateStore.isModuleEnabled("TargetHUD"));
                default -> {
                }
            }
        }
    }

    private static String version() {
        return FabricLoader
            .getInstance()
            .getModContainer("dragan")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");
    }

    private static final class GuiModuleSource implements ArrayListElement.ModuleStateSource {
        @Override
        public List<String> allModuleNames() {
            GuiStateStore.bootstrap();
            return GuiStateStore.modules().stream().map(GuiModule::name).toList();
        }

        @Override
        public boolean isEnabled(String moduleName) {
            return GuiStateStore.isModuleEnabled(moduleName);
        }
    }

    private static final class MemoryPositionStore implements HudManager.PositionStore {
        private final Map<String, float[]> data = new HashMap<>();

        @Override
        public float[] load(String id) {
            return data.get(id);
        }

        @Override
        public void save(String id, float x, float y) {
            data.put(id, new float[] { x, y });
        }

        @Override
        public void flush() {
        }
    }
}
