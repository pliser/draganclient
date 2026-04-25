package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.SliderSetting;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.math.Vector4i;
import ru.karatel.system.render.ColorUtils;
import ru.karatel.system.render.Cursors;
import ru.karatel.system.render.RenderUtils;
import ru.karatel.system.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/**
 * SliderComponent
 */
public class  SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(21);
    }

    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        setHeight(27);
        Fonts.pro[15].drawString(stack, setting.getName(), getX() + 5, getY() + 1.5f / 2f + 1,
                ColorUtils.rgb(255, 255, 255));
        Fonts.pro[15].drawString(stack, String.valueOf(setting.get()), getX() + getWidth() - 5 - Fonts.pro[15].getWidth(String.valueOf(setting.get())), getY() + 4.5f / 2f + 1,
                ColorUtils.rgb(255, 255, 255));
        Vector4i color1 = new Vector4i(
                ColorUtils.setAlpha(ColorUtils.rgb(153, 153, 153), 15),
                ColorUtils.setAlpha(ColorUtils.rgb(153, 153, 153), 15),
                ColorUtils.setAlpha(ColorUtils.rgb(153, 153, 153), 0),
                ColorUtils.setAlpha(ColorUtils.rgb(153, 153, 153), 0)
        );        RenderUtils.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 2, new Vector4f(0.6f,0.6f,0.6f,0.6f), color1);
        anim = MathUtil.fast(anim, (getWidth() - 10) * (setting.get() - setting.min) / (setting.max - setting.min), 20);
        float sliderWidth = anim;
        RenderUtils.drawRoundedRect(getX() + 5, getY() + 11, sliderWidth, 3, 1f, new Color(118,76,153).getRGB());
        RenderUtils.drawCircle(getX() + 5 + sliderWidth, getY() + 12, 7,  new Color(255, 255, 255).getRGB());
        if (drag) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            setting.set((float) MathHelper.clamp(MathUtil.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.max - setting.min) + setting.min, setting.increment), setting.min, setting.max));
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.RESIZEH);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        if (MathUtil.isInRegion(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}