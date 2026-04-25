package ru.karatel.desing.ClickGui;

import com.mojang.blaze3d.matrix.MatrixStack;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import ru.karatel.Delight;
import ru.karatel.desing.ClickGui.components.ModuleComponent;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.desing.ClickGui.components.builder.IBuilder;
import ru.karatel.modules.api.Module;
import ru.karatel.modules.api.Type;
import ru.karatel.system.font.Fonts;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.render.*;

import java.util.ArrayList;
import java.util.List;

import static ru.karatel.system.client.IMinecraft.mc;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Type category;
    protected float x;
    protected float y;
    protected final float width = 127;
    protected float height = 600 / 2f;
    private float scroll, animatedScrool;
    private boolean draggingScrollbar = false;
    private float lastMouseY;
    private final Vector4f ROUNDING_VECTOR = new Vector4f(8.0f, 8.0f, 8.0f, 8.0f);
    private List<ModuleComponent> modules = new ArrayList<>();
    private RenderUtils RectUtils;


    public Panel(Type category) {
        this.category = category;

        for (Module function : Delight.getInstance().getModuleManager().getFunctions()) {
            if (function.getType() == category) {
                ModuleComponent component = new ModuleComponent(function);
                component.setPanel(this);
                modules.add(component);
            }
        }

    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animatedScrool = MathUtil.fast(animatedScrool, scroll, 15);
        float headerFont = 9;
        float header = 55 / 2.3f;

        float textWidth = Fonts.pro[24].getWidth(category.name());
        float centerX = x + width / 2f;
        float centerY = y + header / 2f - Fonts.pro[18].getFontHeight() / 2f;
        float heightCategory = 50 / 2f;
        Fonts.zenith[23].drawCenteredString(stack, category.name(), x + width / 2f, y + 23 - Fonts.zenith[23].getFontHeight(), ColorUtils.setAlpha(-1,255));

        drawComponents(stack, mouseX, mouseY);

        drawOutline();
    }

    protected void drawOutline() {
        Stencil.initStencilToWrite();

        Stencil.readStencilBuffer(0);

        Stencil.uninitStencilBuffer();
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float animationValue = (float) DropDown.getAnimation().getValue() * DropDown.scale;

        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float height = getHeight();
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + 55 / 2f + (height * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = height * animationValue;


        testX = testX * animationValue + ((Minecraft.getInstance().getMainWindow().getScaledWidth() - testW) *
                halfAnimationValueRest);

        Scissor.push();
        Scissor.setFromComponentCoordinates(testX, testY, testW, testH - 34 - 13);
        float offset = 0;
        float header = 55 / 2f;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        }
        else {
            scroll = 0;
            animatedScrool = 0;
        }
        for (ModuleComponent component : modules) {
            if(Delight.getInstance().getDropDown().searchCheck(component.getFunction().getName())){
                continue;
            }
            component.setX(getX() + 8.5f);
            component.setY(getY() + header + offset  + animatedScrool + 3);
            component.setWidth(getWidth() - 17);
            component.setHeight(17);
            component.animation.update();
            if (component.animation.getValue() > 0) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }

                componentOffset *= component.animation.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }
            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 5.5f;
            Scissor.setFromComponentCoordinates(testX, testY, testW, testH );
        }
        animatedScrool = MathUtil.fast(animatedScrool, scroll, 10);
        float scrollbarHeight = MathHelper.clamp((height - header - 10) * (height - header - 10) / max, 10, height - header - 10);
        float scrollbarY = getY() + header + (-getScroll() / (max - height + header + 4)) * (height - header - 4 - scrollbarHeight);
        scrollbarHeight = MathHelper.clamp(scrollbarHeight, 10, height - header - 10);
        scrollbarY = MathHelper.clamp(scrollbarY, getY() + header, getY() + height - scrollbarHeight - 4);
        if (max > height - header - 10) {
            setScroll(MathHelper.clamp(getScroll(), -max + height - header - 300 , 0));
            setAnimatedScrool(MathHelper.clamp(animatedScrool, -max + height - header - 300, 0));

            if (scroll >= 0) {
                setScroll(0);
                setAnimatedScrool(0);
            }


        } else {
            setScroll(0);
            setAnimatedScrool(0);
        }

        max = offset + 10;

        Scissor.unset();
        Scissor.pop();

    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {

        for (ModuleComponent component : modules) {
            if(Delight.getInstance().getDropDown().searchCheck(component.getFunction().getName())){
                continue;
            }
            component.mouseClick(mouseX, mouseY, button);
        }

        if (button == 0) { // ЛКМ
            float header = 55 / 2f;
            float scrollbarHeight = MathHelper.clamp((height - header - 10) * (height - header - 10) / max, 10, height - header - 10);
            float scrollbarY = getY() + header + (-getScroll() / (max - height + header + 4)) * (height - header - 4 - scrollbarHeight);
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 20, height - header - 10);
            scrollbarY = MathHelper.clamp(scrollbarY, getY() + header, getY() + height - scrollbarHeight - 4);

            if (mouseX >= getX() + getWidth() - 2.5f && mouseX <= getX() + getWidth() + 1.0f && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
                draggingScrollbar = true;
                lastMouseY = mouseY;
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
        if (button == 0) { // ЛКМ
            draggingScrollbar = false;
        }

    }

}