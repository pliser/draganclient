package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.ModeSetting;
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

public class ModeComponent extends Component {

    final ModeSetting setting;

    float width = 0;
    float heightplus = 0;

    public ModeComponent(ModeSetting setting) {
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
        for (String text : setting.strings) {
            float off = Fonts.pro[15].getWidth(text) + 2;
            if (offset + off >= (getWidth() - 10)) {
                offset = 0;
                heightoff += 12; // Increased vertical spacing
                plused = true;
            }
            float textWidth = Fonts.pro[15].getWidth(text);
            float textHeight = Fonts.pro[15].getFontHeight();
            float x = getX() + 8 + offset; // Increased horizontal spacing
            float y = getY() + 11.5f + heightoff; // Updated coordinate from old version
            if (!text.equals(setting.get())) {
                RenderUtils.drawRoundedRect(x - 0.5f, y - 0.5f, textWidth + 6, textHeight + 1, 3, new Color(45,29,61, 255).getRGB());
                RenderUtils.drawRoundedRect(x, y, textWidth + 5, textHeight, 3, new Color(37,24,48, 255).getRGB());
            } else {
                RenderUtils.drawRoundedRect(x - 0.5f, y - 0.5f, textWidth + 6, textHeight + 1, 3, new Color(78,51,102, 255).getRGB());
                RenderUtils.drawRoundedRect(x, y, textWidth + 5, textHeight, 3, new Color(78,51,102, 255).getRGB());
            }

            // Check for mouse hover
            if (MathUtil.isInRegion(mouseX, mouseY, x, y, textWidth, textHeight)) {
                anyHovered = true;
            }
            if (!text.equals(setting.get())) {
                Fonts.pro[15].drawString(stack, text, x + 2, y + textHeight - 2 - Fonts.pro[15].getFontHeight() / 2, ColorUtils.rgb(189, 184, 194));
            } else {
                Fonts.pro[15].drawString(stack, text, x + 2, y + textHeight - 2 - Fonts.pro[15].getFontHeight() / 2, ColorUtils.rgb(255, 255, 255));
            }

            offset += off + 6; // Increased horizontal spacing
        }
        if (isHovered(mouseX, mouseY)) {
            if (anyHovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
        width = plused ? getWidth() - 15 : offset;
        setHeight(32 + heightoff);
        heightplus = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (String text : setting.strings) {
            float off = Fonts.pro[15].getWidth(text) + 2;
            if (offset + off >= (getWidth() - 10)) {
                offset = 0;
                heightoff += 12; // Increased vertical spacing
            }
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff,
                    Fonts.pro[15].getWidth(text), Fonts.pro[15].getFontHeight())) {
                setting.set(text);
            }
            offset += off + 6; // Increased horizontal spacing
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
