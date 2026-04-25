package ru.karatel.desing.ClickGui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import ru.karatel.Delight;
import ru.karatel.desing.ClickGui.DropDown;
import ru.karatel.desing.ClickGui.Panel;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.desing.ClickGui.components.settings.*;
import ru.karatel.modules.api.Module;
import ru.karatel.modules.api.Type;
import ru.karatel.modules.settings.Setting;
import ru.karatel.modules.settings.impl.*;
import ru.karatel.system.client.KeyStorage;
import ru.karatel.system.font.Fonts;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.math.Vector4i;
import ru.karatel.system.render.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ru.karatel.system.client.IMinecraft.mc;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(3.5F, 3.5F, 3.5F, 3.5F);
    private final float ANIMATION_SPEED = 1f;
    private final ru.karatel.modules.api.Module function;
    public Animation animation = new Animation();
    public Animation buttonAnimation = new Animation();
    public boolean open;
    private boolean bind;

    // Alpha property for transparency control
    @Setter
    private float alpha = 1.0f;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module function) {
        this.function = function;
        for (Setting<?> setting : function.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof ColorSetting color) {
                components.add(new ColorPickerComponent(color));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
        }
        animation = animation.animate(open ? 1 : 0, 0.5f, Easings.CIRC_OUT);
        buttonAnimation = buttonAnimation.animate(function.isState() ? 1 : 0, 0.5f, Easings.CIRC_OUT);
    }

    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        if (animation.getValue() > 0) {
            if (animation.getValue() > 1 && components.stream().filter(Component::isVisible).count() >= 1) {
            }

            Stencil.initStencilToWrite();
            RenderUtils.drawRoundedRect(getX() + 0.5f, getY(), getWidth() - 1, getHeight() + 9F,
                    ROUNDING_VECTOR, ColorUtils.rgba(18, 19, 30, (int)(38 * alpha)));
            Stencil.readStencilBuffer(1);

            float y = getY() + 26;
            for (Component component : components) {
                if (component.isVisible()) {
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY);
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        function.getAnimation().update();
        buttonAnimation.update();
        buttonAnimation = buttonAnimation.animate(function.isState() ? 1 : 0, 0.5f, Easings.CIRC_OUT);

        super.render(stack, mouseX, mouseY);

        // Draw module button with animated effects
        drawModuleButton(stack, mouseX, mouseY);
        drawText(stack);
        drawComponents(stack, mouseX, mouseY);
    }

    private void drawModuleButton(MatrixStack stack, float mouseX, float mouseY) {
        Vector4i color = new Vector4i(new Color(46, 49, 87, 38).getRGB(), new Color(46, 49, 87, 38).getRGB(), new Color(46, 49, 87, 38).getRGB(), new Color(46, 49, 87, 38).getRGB());
        RenderUtils.drawOutlinedRoundedRect(getX() - 0.5f, getY() - 0.5f, getWidth() + 1, getHeight() + 1, 5,2f,new Color(45,29,64,125).getRGB(),new Color(33,22,45,1).getRGB());
        RenderUtils.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 5, new Color(5,4,3, 125).getRGB());
        if (function.isState()) {
            RenderUtils.drawOutlinedRoundedRect(getX() - 0.5f, getY() - 0.5f, getWidth() + 1, getHeight() + 1, 5,2f,new Color(45,29,64,125).getRGB(),new Color(33,22,45,1).getRGB());
            RenderUtils.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 5, new Color(32,21,44, 125).getRGB());
        }
        DropDown clickGui = Delight.getInstance().getDropDown();
        for (Panel panelRender : clickGui.panels) {
            if (MathUtil.isInRegion(mouseX, mouseY, panelRender.getX(), panelRender.getY() + 25, panelRender.getWidth(), panelRender.getHeight() - 25)) {
                if (isHovered(mouseX, mouseY, 20)) {
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
    }

    private void drawText(MatrixStack stack) {
        // Calculate text color based on state
        int textColor = function.isState() ?
                ColorUtils.rgba(255, 255, 255, (int)(255 * alpha)) :
                ColorUtils.rgba(255, 255, 255, (int)(255 * alpha));

        // Draw module name
        if (!bind) {
            Fonts.pro[18].drawString(stack, function.getName(), (int)(getX() + getWidth() / 13f),
                    getY() + 6.0f, textColor);
        }

        // Draw settings indicator or bind
        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            if (bind) {
                String bindText = (function.getBind() == 0) ? "Bind" : KeyStorage.getReverseKey(function.getBind());
                Fonts.zenith[15].drawCenteredString(stack, bindText,
                        getX() + getWidth() / 2f, getY() + 7.0F,
                        ColorUtils.setAlpha(ColorUtils.rgb(255, 255, 255), (int)(255 * alpha)));
            } else {
                // Draw arrow indicator
                Fonts.msSemiBold[16].drawString(stack, "...", getX() + getWidth() - 15,
                        getY() + 5.0f, ColorUtils.rgba(255,255,255,255));
            }
        } else if (bind) {
            String bindText = (function.getBind() == 0) ? "Bind" : KeyStorage.getReverseKey(function.getBind());
            Fonts.zenith[13].drawCenteredString(stack, bindText,
                    getX() + getWidth() / 2f, getY() + 6.5F,
                    ColorUtils.setAlpha(ColorUtils.rgb(255, 255, 255), (int)(255 * alpha)));
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        DropDown clickGui = Delight.getInstance().getDropDown();
        for (Panel panelRender : clickGui.panels) {
            if (MathUtil.isInRegion(mouseX, mouseY, panelRender.getX(), panelRender.getY() + 25, panelRender.getWidth(), panelRender.getHeight() - 25)) {
                if (isHovered(mouseX, mouseY, 20)) {
                    if (button == 0) {
                        function.toggle();
                        buttonAnimation = buttonAnimation.animate(function.isState() ? 1 : 0, 1f, Easings.CIRC_OUT);
                    }
                    if (button == 1) {
                        open = !open;
                        animation = animation.animate(open ? 1 : 0, 0.5f, Easings.CIRC_OUT);
                    }
                    if (button == 2) {
                        bind = !bind;
                    }
                }
                if (isHovered(mouseX, mouseY)) {
                    if (open) {
                        for (Component component : components) {
                            if (component.isVisible())
                                component.mouseClick(mouseX, mouseY, button);
                        }
                    }
                }
            }
            super.mouseClick(mouseX, mouseY, button);
        }
    }


    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                function.setBind(0);
            } else function.setBind(key);
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }
}