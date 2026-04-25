package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.BindSetting;
import ru.karatel.system.client.KeyStorage;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.math.Vector4i;
import ru.karatel.system.render.ColorUtils;
import ru.karatel.system.render.Cursors;
import ru.karatel.system.render.RenderUtils;
import ru.karatel.system.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(21);
    }

    boolean activated;
    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.pro[14].drawString(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, ColorUtils.rgb(255, 255, 255));
        String bind = KeyStorage.getKey(setting.get());

        if (bind == null || setting.get() == -1) {
            bind = "Нету";
        }
        boolean next = Fonts.pro[14].getWidth(bind) >= 16;
        float x = next ? getX() + 5 : getX() + getWidth() - 7 - Fonts.pro[14].getWidth(bind);
        float y = getY() + 5.5f / 2f + (5.5f / 2f) + (next ? 8 : 0);
        RenderUtils.drawRoundedRect(x - 3, y - 3.5F, Fonts.pro[14].getWidth(bind) + 6, 6.5f + 4, 4, new Color(45,29,61, 255).getRGB());
        RenderUtils.drawRoundedRect(x - 3 + 0.5F, y - 3, Fonts.pro[14].getWidth(bind) + 5, 5.5f + 4, 4, new Color(37,24,48, 255).getRGB());
        Fonts.pro[13].drawString(stack, bind, x, y + 0.5f, activated ? -1 : ColorUtils.rgb(255, 255, 255));

        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isInRegion(mouseX, mouseY, x - 2 + 0.5F, y, Fonts.pro[14].getWidth(bind) + 4, 5.5f + 4)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
        setHeight(next ? 32 : 21);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        // TODO Auto-generated method stub
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.set(-1);
                activated = false;
                return;
            }
            setting.set(key);
            activated = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            activated = !activated;
        }

        if (activated && mouse >= 1) {
            System.out.println(-100 + mouse);
            setting.set(-100 + mouse);
            activated = false;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
