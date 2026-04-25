package ru.karatel.desing.ClickGui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import ru.karatel.modules.api.Type;
import ru.karatel.system.CustomFramebuffer;
import ru.karatel.system.client.ClientUtil;
import ru.karatel.system.client.IMinecraft;
import ru.karatel.system.client.Vec2i;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.render.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DropDown extends Screen implements IMinecraft {
    private static final Animation gradientAnimation = new Animation();
    public final List<Panel> panels = new ArrayList<>();

    @Getter
    private static final Animation globalAnim = new Animation();
    @Getter
    private static Animation animation = new Animation();
    @Getter
    private static final Animation imageAnimation = new Animation();
    private boolean exit = false, open = false;
    public static float scale = 1.0f;
    public SearchField searchField;
    // ��������� �����
    private float blurRadius = 15.0f; // ������ ��������
    private float blurAlpha = 0.7f; // ������������ ������� �����
    private boolean useBlur = true; // ���� ��� ���������/���������� �����

    public DropDown(String titleIn) {
        super(ITextComponent.getTextComponentOrEmpty(titleIn));
        for (Type category : Type.values()) {
            panels.add(new Panel(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {

        gradientAnimation.animate(1, 0.5f, Easings.NONE);
        imageAnimation.animate(1, 0.5f, Easings.CIRC_OUT);
        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtil.calc(mc.getMainWindow().getScaledHeight());

        float x = (windowWidth / 2f) - (panels.size() * (120) / 2f) + 2 * (120) - 10;
        float y = windowHeight / 2f + (600 / 2f) / 2f + 30;

        searchField = new SearchField((int) x, (int) y + 5, 120, 16, "Поиск");

        exit = false;
        open = true;

        animation.animate(1, 0.5f, Easings.CIRC_OUT);
        super.init();
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
    }
    private Vec2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();

        float adjustedMouseX = (mouseX - windowWidth / 2f) / scale + windowWidth / 2f;
        float adjustedMouseY = (mouseY - windowHeight / 2f) / scale + windowHeight / 2f;

        return new Vec2i((int) adjustedMouseX, (int) adjustedMouseY);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (Panel panel : panels) {
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getHeight())) {
                panel.setScroll((float) (panel.getScroll() + (delta * 10)));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        for (Panel panel : panels) {
            panel.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        mc.gameRenderer.setupOverlayRendering(2);
        Stream.of(animation, imageAnimation, gradientAnimation).forEach(Animation::update);

        if (Stream.of(animation, imageAnimation, gradientAnimation).allMatch(anim -> anim.getValue() <= 0.1 && anim.isDone())) {
            closeScreen();
        }

        if (animation.getValue() < 0.1) {
            closeScreen();
        }

        final float off = 7.5f;
        float width = panels.size() * (130 + off);

        updateScaleBasedOnScreenWidth();

        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtil.calc(mc.getMainWindow().getScaledHeight());

        Vec2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        // ��������� �������� ���� �� ���� �����
        if (useBlur) {
        }

        // ��������� ���� � ��������� ���������
        Stencil.initStencilToWrite();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);

        // ������ �������, ������� ����� �������
        for (Panel panel : panels) {
            panel.setY(windowHeight / 2f - (600 / 2) / 2f);
            panel.setX((windowWidth / 2f - 5) - (width / 2f) + panel.getCategory().ordinal() * (130 + off) + off / 2f);
        }

        GlStateManager.popMatrix();

        Stencil.readStencilBuffer(1);

        // ������ �������� ���
        if (useBlur) {
            GlStateManager.bindTexture(-1);
            CustomFramebuffer.drawTexture();
        } else {
            for (Panel panel : panels) {
                panel.setY(windowHeight / 2f - (600 / 2) / 2f);
                panel.setX((windowWidth / 2f - 5) - (width / 2f) + panel.getCategory().ordinal() * (130 + off) + off / 2f);

                if (panel.getCategory().equals(Type.Combat)) {
                } else if (panel.getCategory().equals(Type.Movement)) {
                } else if (panel.getCategory().equals(Type.Visuals)) {
                } else if (panel.getCategory().equals(Type.Misc)) {
                } else if (panel.getCategory().equals(Type.Player)) {
                }
            }
        }

        Stencil.uninitStencilBuffer();

        // Render panels
        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);

        for (Panel panel : panels) {
            panel.setY(windowHeight / 2f - (600 / 2) / 2f);
            panel.setX((windowWidth / 2f - 5) - (width / 2f) + panel.getCategory().ordinal() * (127f + off) + off / 2f);
            float animationValue = (float) animation.getValue() * scale;
            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = panel.getX() + (panel.getWidth() * halfAnimationValueRest);
            float testY = panel.getY() + (panel.getHeight() * halfAnimationValueRest);
            float testW = panel.getWidth() * animationValue;
            float testH = 0;
            testH = panel.getHeight() * animationValue;
            // ������ �������������� ���� �������
            Color panelBgColor = new Color(17, 17, 25, 115);
            RenderUtils.drawOutlinedRoundedRect(panel.getX() - 0.5f, panel.getY() - 0.5f, panel.getWidth() + 1, panel.getHeight()  - 13 + 1, 8, 0.1f,new Color(31,20,43, 255).getRGB(),new Color(31,20,43, 255).getRGB());
            RenderUtils.drawRoundedRect(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight() - 13, 8, new Color(17,11,22, 255).getRGB());

            testX = testX * animationValue + ((windowWidth - testW) * halfAnimationValueRest);

            Scissor.push();
            Scissor.setFromComponentCoordinates(testX - 9, testY - 9, testW + 20, testH + 20);
            panel.render(matrixStack, mouseX, mouseY);
            Scissor.unset();
            Scissor.pop();
        }
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering();
    }
    public boolean isSearching() {
        return !this.searchField.isEmpty();
    }

    public String getSearchText() {
        return this.searchField.getText();
    }

    public boolean searchCheck(String string) {
        return this.isSearching() && !string.replaceAll(" ", "").toLowerCase().contains(this.getSearchText().replaceAll(" ", "").toLowerCase());
    }
    // ��������� ����� ��� ���������� ����������� �����
    public void setBlurSettings(float radius, float alpha, boolean useBlur) {
        this.blurRadius = radius;
        this.blurAlpha = alpha;
        this.useBlur = useBlur;
    }

    private void updateScaleBasedOnScreenWidth() {
        final float PANEL_WIDTH = 115;
        final float MARGIN = 10;
        final float MIN_SCALE = 0.5f;

        float totalPanelWidth = panels.size() * (PANEL_WIDTH + MARGIN);
        float screenWidth = mc.getMainWindow().getScaledWidth();

        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, MIN_SCALE, 1.0f);
        } else {
            scale = 1f;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0.5f, Easings.CIRC_OUT);
            imageAnimation.animate(0.0, 0.5f, Easings.CIRC_OUT);
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (Panel panel : panels) {
            panel.mouseClick((float) mouseX, (float) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}