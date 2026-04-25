package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.BooleanSetting;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.math.Vector4i;
import ru.karatel.system.render.ColorUtils;
import ru.karatel.system.render.Cursors;
import ru.karatel.system.render.RenderUtils;
import ru.karatel.system.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.awt.*;

/**
 * BooleanComponent
 */
public class BooleanComponent extends Component {

    private final BooleanSetting setting;
    final ResourceLocation enable = new ResourceLocation("Delight/images/check.png");
    final ResourceLocation disable = new ResourceLocation("Delight/images/xmark.png");

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(19);
        animation = animation.animate(setting.get() ? 1 : 0, 0.2, Easings.CIRC_OUT);
    }

    private Animation animation = new Animation();
    private float width, height;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
       float y = getY() + 6.5f / 2f - 1;

        super.render(stack, mouseX, mouseY);
        animation.update();
        setHeight(23);
        Fonts.pro[15].drawString(stack, setting.getName(), getX() + 6.5f, y + 1.5f, ColorUtils.rgb(255, 255, 255));
        width = 7.5f;
        height = 7.5f;
                RenderUtils.drawRoundedRect(getX() + getWidth() - width - 7 - 0.5f, y - 0.5f, width + 1,
                height + 1, new Vector4f(4,4,4,4),new Color(45,29,61, 255).getRGB());
        RenderUtils.drawRoundedRect(getX() + getWidth() - width - 7, y, width,
                height, new Vector4f(4,4,4,4),new Color(37,24,48, 255).getRGB());
        if (setting.get()) {
            RenderUtils.drawImage(enable,getX() + getWidth() - width - 7 + 1f, y + 1f, width - 2,
                    height - 2,new Color(0,255,0, 255).getRGB());
        } else {
            RenderUtils.drawImage(disable,getX() + getWidth() - width - 7 + 2f, y + 2f, width - 4,
                    height - 4,new Color(255,0,0, 255).getRGB());
        }

        
        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + getWidth() - width - 7, y, width,
                    height)) {
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
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float y = getY() + 6.5f / 2f - 1;
        if (MathUtil.isInRegion(mouseX, mouseY, getX() + getWidth() - width - 7, y , width,
                height)) {
            setting.set(!setting.get());
            animation = animation.animate(setting.get() ? 1 : 0, 1, Easings.CIRC_OUT);
        }
        super.mouseClick(mouseX, mouseY, mouse);        
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}