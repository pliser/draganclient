package ru.karatel.desing.ClickGui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.karatel.desing.ClickGui.components.builder.Component;
import ru.karatel.modules.settings.impl.ColorSetting;
import ru.karatel.system.math.MathUtil;
import ru.karatel.system.math.Vector4i;
import ru.karatel.system.render.ColorUtils;
import ru.karatel.system.render.RenderUtils;
import ru.karatel.system.font.Fonts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.*;

@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColorPickerComponent extends Component {

    final ColorSetting colorSetting;

    float colorRectX, colorRectY, colorRectWidth, colorRectHeight;
    float pickerX, pickerY, pickerWidth, pickerHeight;
    float sliderX, sliderY, sliderWidth, sliderHeight;
    float alphaSliderX, alphaSliderY, alphaSliderWidth, alphaSliderHeight;

    final float padding = 5;
    float textX, textY;
    final int textColor = ColorUtils.rgb(160, 163, 175);
    private float[] hsba = new float[4]; // H, S, B, A values

    boolean panelOpened;
    boolean draggingHue, draggingPicker, draggingAlpha;



    public ColorPickerComponent(ColorSetting colorSetting) {
        this.colorSetting = colorSetting;
        Color color = new Color(colorSetting.get(), true); // true to include alpha
        hsba = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsba = new float[]{hsba[0], hsba[1], hsba[2], color.getAlpha() / 255f}; // теперь массив имеет 4 элемента
        setHeight(25);

    }



    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        renderTextAndColorRect(stack);

        if (panelOpened) {
            // Обновление настройки цвета с учетом альфа
            int alphaValue = (int) (hsba[3] * 255);
            int rgbColor = Color.HSBtoRGB(hsba[0], hsba[1], hsba[2]);
            this.colorSetting.set((alphaValue << 24) | (rgbColor & 0x00FFFFFF)); // Объединение альфа с RGB
            renderSlider(mouseX, mouseY);
            renderPickerPanel(mouseX, mouseY);
            setHeight(26 + pickerHeight + alphaSliderHeight + padding * 2);
        } else {
            setHeight(22);
        }

        super.render(stack, mouseX, mouseY);
    }

    private void renderTextAndColorRect(MatrixStack stack) {
        String settingName = colorSetting.getName();
        int colorValue = colorSetting.get();

        this.textX = this.getX() + padding;
        this.textY = this.getY() + 2;

        this.colorRectX = this.getX() + padding;
        this.colorRectY = this.getY() + 4 + (padding);
        this.colorRectWidth = this.getWidth() - (padding * 2);
        this.colorRectHeight = padding * 2;

        this.pickerX = this.getX() + padding;
        this.pickerY = this.getY() + 4 + (padding) + 16;
        this.pickerWidth = this.getWidth() - (padding * 4);
        this.pickerHeight = 60;

        this.sliderX = pickerX + pickerWidth + padding;
        this.sliderY = pickerY;
        this.sliderWidth = 3;
        this.sliderHeight = pickerHeight;

        this.alphaSliderX = this.getX() + padding;
        this.alphaSliderY = this.pickerY + this.pickerHeight + padding;
        this.alphaSliderWidth = this.getWidth() - (padding * 2); // Ширина слайдера альфа-канала
        this.alphaSliderHeight = 10; // Высота слайдера альфа-канала

        Fonts.gilroy[15].drawString(stack, settingName, textX, textY, textColor);
        RenderUtils.drawRoundedRect(this.colorRectX, this.colorRectY, this.colorRectWidth, this.colorRectHeight, 3.5f, colorValue);
    }

    private void renderPickerPanel(float mouseX, float mouseY) {
        Vector4i vector4i = new Vector4i(Color.WHITE.getRGB(),
                Color.BLACK.getRGB(),
                Color.getHSBColor(hsba[0], 1, 1).getRGB(),
                Color.BLACK.getRGB());

        float offset = 4;
        float xRange = pickerWidth - 8;
        float yRange = pickerHeight - 8;

        if (draggingPicker) {
            float saturation = MathHelper.clamp((mouseX - pickerX - offset), 0, xRange) / (xRange);
            float brightness = MathHelper.clamp((mouseY - pickerY - offset), 0, yRange) / (yRange);
            hsba[1] = saturation;
            hsba[2] = 1 - brightness;
        }

        RenderUtils.drawRoundedRect(this.pickerX, this.pickerY, this.pickerWidth, this.pickerHeight, new Vector4f(6, 6, 6, 6), vector4i);

        float circleX = pickerX + offset + hsba[1] * (xRange);
        float circleY = pickerY + offset + (1 - hsba[2]) * (yRange);

        RenderUtils.drawCircle(circleX, circleY, 8, Color.BLACK.getRGB());
        RenderUtils.drawCircle(circleX, circleY, 6, Color.WHITE.getRGB());
    }

    private void renderSlider(float mouseX, float mouseY) {
        for (int i = 0; i < sliderHeight; i++) {
            float hue = i / sliderHeight;
            RenderUtils.drawCircle(this.sliderX + 1f, sliderY + i, 3, Color.HSBtoRGB(hue, 1, 1));
        }
        RenderUtils.drawCircle(this.sliderX + sliderWidth - 2F, this.sliderY + (hsba[0] * sliderHeight), 8, Color.BLACK.getRGB());
        RenderUtils.drawCircle(this.sliderX + sliderWidth - 2F, this.sliderY + (hsba[0] * sliderHeight), 6, -1);
        if (draggingHue) {
            float hue = (mouseY - sliderY) / sliderHeight;
            hsba[0] = MathHelper.clamp(hue, 0, 1);
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (MathUtil.isInRegion(mouseX, mouseY, colorRectX, colorRectY, colorRectWidth, colorRectHeight) && mouse == 1) {
            panelOpened = !panelOpened;
        }

        if (panelOpened) {
            if (MathUtil.isInRegion(mouseX, mouseY, sliderX - 2, sliderY, sliderWidth + 4, pickerHeight - 12)) {
                draggingHue = true;
            } else if (MathUtil.isInRegion(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight)) {
                draggingPicker = true;
            } else if (MathUtil.isInRegion(mouseX, mouseY, alphaSliderX, alphaSliderY, alphaSliderWidth, alphaSliderHeight)) {
                draggingAlpha = true;
            }
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        if (draggingHue) {
            draggingHue = false;
        }
        if (draggingPicker) {
            draggingPicker = false;
        }
        if (draggingAlpha) {
            draggingAlpha = false;
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }
}
