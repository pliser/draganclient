package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.BooleanSetting;
import ru.karatel.modules.settings.impl.ModeListSetting;
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

public class MultiBoxComponent extends Component {

    final ModeListSetting setting;
    private boolean isGlowing = false;

    float width = 0;
    float heightPadding = 0;

    public MultiBoxComponent(ModeListSetting setting) {
        this.setting = setting;
        setHeight(29);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {

        super.render(stack, mouseX, mouseY);
        Fonts.pro[15].drawString(stack, setting.getName(), getX() + 7, getY() + 2, ColorUtils.rgb(255, 255, 255));
        float offset = 0;
        float heightoff = 0;
        boolean plused = false;
        boolean anyHovered = false;
        for (BooleanSetting text : setting.get()) {
            float off = Fonts.pro[15].getWidth(text.getName()) + 2;
            if (offset + off >= (getWidth() - 10)) {
                offset = 0;
                heightoff += 12;
                plused = true;
            }
            float textWidth = Fonts.pro[15].getWidth(text.getName()) + 5;
            float textHeight = Fonts.pro[15].getFontHeight();
            float x = getX() + 8 + offset;
            float y = getY() + 11.5f + heightoff;
            if (!text.get()) {
                RenderUtils.drawRoundedRect(x - 0.5f, getY() + 11.5f + heightoff - 0.5f, textWidth + 1, textHeight + 1,3, new Color(45,29,61, 255).getRGB());
                RenderUtils.drawRoundedRect(x, getY() + 11.5f + heightoff, textWidth, textHeight, 3, new Color(37,24,48, 255).getRGB());
            }
            else {
                RenderUtils.drawRoundedRect(x - 0.5f, getY() + 11.5f + heightoff - 0.5f, textWidth + 1, textHeight + 1, 3,new Color(78,51,102, 255).getRGB());
                RenderUtils.drawRoundedRect(x, getY() + 11.5f + heightoff, textWidth, textHeight, 3, new Color(78,51,102, 255).getRGB());
            }

            // Проверяем наведение мыши
            if (MathUtil.isInRegion(mouseX, mouseY, x, y, textWidth, textHeight)) {
                anyHovered = true;
            }
            if (!text.get()) {
                Fonts.pro[15].drawString(stack, text.getName(), x +  2, y + textHeight - 2 - Fonts.pro[15].getFontHeight() / 2, ColorUtils.rgb(189, 184, 194));
            } else {
                Fonts.pro[15].drawString(stack, text.getName(), x + 2, y + textHeight - 2 - Fonts.pro[15].getFontHeight() / 2, ColorUtils.rgb(255, 255, 255));
            }
            offset += off + 5;
        }
        if (isHovered(mouseX, mouseY)) {
            if (anyHovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
        width = Fonts.pro[15].getWidth(setting.getName()) + 10;
        setHeight(32 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (BooleanSetting text : setting.get()) {
            float off = Fonts.pro[15].getWidth(text.getName()) + 2;
            if (offset + off >= (getWidth() - 10)) {
                offset = 0;
                heightoff += 12;

            }
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff,
                    Fonts.pro[15].getWidth(text.getName()), Fonts.pro[15].getFontHeight())) {
                text.set(!text.get());
            }
            offset += off + 5;
        }


        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
