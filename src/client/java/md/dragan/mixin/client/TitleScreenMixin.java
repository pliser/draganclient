package md.dragan.mixin.client;

import md.dragan.client.alt.AltManagerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void dragan$addAltManagerButton(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int buttonWidth = 98;
        int buttonHeight = 20;
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 48;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Alt Manager"), button ->
            client.setScreen(new AltManagerScreen((Screen) (Object) this))
        ).dimensions(x, y, buttonWidth, buttonHeight).build());
    }
}
