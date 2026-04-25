package md.dragan.client.hud.elements;

import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class TargetHudElement extends HudElement {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 52;
    private static final int BG = 0xE00C1219;
    private static final int INNER = 0xD6121B25;
    private static final int BORDER = 0xFF223344;
    private static final int TEXT_NAME = 0xFFF4F8FC;
    private static final int TEXT_META = 0xFF9CB0C5;
    private static final int HP_BG = 0xFF18232F;
    private static final int ARMOR_BG = 0xFF141E28;
    private static final int ACCENT = 0xFF77D0FF;
    private static final int ACCENT_WARM = 0xFFFFBE73;

    private final Animation alpha = new Animation(0.0F);
    private final Animation hpAnim = new Animation(0.0F);

    private LivingEntity currentTarget;
    private String targetName = "";
    private float targetHp;
    private float targetMaxHp;

    public TargetHudElement(float defaultX, float defaultY) {
        super("targethud", defaultX, defaultY);
    }

    @Override
    protected void measure(MinecraftClient client) {
        width = WIDTH;
        height = HEIGHT;
    }

    @Override
    public void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        float step = 1.0F / 60.0F;

        LivingEntity target = findTarget(client);
        if (target != null) {
            currentTarget = target;
            targetName = target.getName().getString();
            targetHp = target.getHealth();
            targetMaxHp = Math.max(1.0F, target.getMaxHealth());
            alpha.setTarget(1.0F);
        } else {
            alpha.setTarget(0.0F);
        }

        alpha.tick(Animators.expAlpha(Animators.timeToResponse(180.0F), step));
        hpAnim.setTarget(targetMaxHp <= 0.0F ? 0.0F : targetHp / targetMaxHp);
        hpAnim.tick(Animators.expAlpha(Animators.timeToResponse(250.0F), step));

        if (alpha.value() < 0.01F) {
            currentTarget = null;
        }

        markBoundsDirty();
    }

    @Override
    public void render(DrawContext context, float delta) {
        float a = Math.max(0.0F, Math.min(1.0F, alpha.value()));
        if (a < 0.01F || currentTarget == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int ix = Math.round(x);
        int iy = Math.round(y);
        int hpColor = getHpColor(Math.max(0.0F, Math.min(1.0F, hpAnim.value())));

        Render2DUtil.roundedRect(context, ix - 3, iy - 3, WIDTH + 6, HEIGHT + 6, 10, Render2DUtil.multiplyAlpha(0x12000000, a));
        Render2DUtil.roundedRect(context, ix, iy, WIDTH, HEIGHT, 8, Render2DUtil.multiplyAlpha(BG, a));
        Render2DUtil.roundedRect(context, ix + 1, iy + 1, WIDTH - 2, HEIGHT - 2, 7, Render2DUtil.multiplyAlpha(INNER, a));
        Render2DUtil.border(context, ix, iy, WIDTH, HEIGHT, Render2DUtil.multiplyAlpha(BORDER, a));
        Render2DUtil.rect(context, ix + 11, iy + 8, 30, 2, Render2DUtil.multiplyAlpha(ACCENT, a));
        Render2DUtil.rect(context, ix + 45, iy + 8, 14, 2, Render2DUtil.multiplyAlpha(ACCENT_WARM, a));

        renderHead(context, client, ix + 10, iy + 14, 28, a);

        int textX = ix + 46;
        int textW = WIDTH - 58;
        Render2DUtil.drawTextClipped(context, client.textRenderer, targetName, textX, iy + 13, textW, Render2DUtil.multiplyAlpha(TEXT_NAME, a), false);

        String meta = String.format("%.1f HP  /  %.0f%%", targetHp, hpAnim.value() * 100.0F);
        Render2DUtil.drawTextClipped(context, client.textRenderer, meta, textX, iy + 24, textW, Render2DUtil.multiplyAlpha(TEXT_META, a), false);

        int barX = textX;
        int barY = iy + 35;
        int barW = WIDTH - 58;
        Render2DUtil.roundedRect(context, barX, barY, barW, 7, 3, Render2DUtil.multiplyAlpha(HP_BG, a));
        int hpW = Math.max(4, Math.round(barW * Math.max(0.0F, Math.min(1.0F, hpAnim.value()))));
        Render2DUtil.roundedRect(context, barX, barY, hpW, 7, 3, Render2DUtil.multiplyAlpha(hpColor, a));
        Render2DUtil.rect(context, barX + 2, barY + 1, Math.max(1, hpW - 4), 1, Render2DUtil.multiplyAlpha(0x22FFFFFF, a));

        float armorFrac = currentTarget instanceof PlayerEntity player ? Math.max(0.0F, Math.min(1.0F, player.getArmor() / 20.0F)) : 0.0F;
        Render2DUtil.roundedRect(context, barX, iy + 45, barW, 3, 2, Render2DUtil.multiplyAlpha(ARMOR_BG, a));
        int armorW = Math.round(barW * armorFrac);
        if (armorW > 0) {
            Render2DUtil.roundedRect(context, barX, iy + 45, armorW, 3, 2, Render2DUtil.multiplyAlpha(ACCENT, a * 0.8F));
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, int button, boolean editorEnabled) {
        return false;
    }

    private void renderHead(DrawContext context, MinecraftClient client, int x, int y, int size, float alpha) {
        Render2DUtil.roundedRect(context, x - 1, y - 1, size + 2, size + 2, 6, Render2DUtil.multiplyAlpha(0xEE0D141C, alpha));
        Render2DUtil.border(context, x - 1, y - 1, size + 2, size + 2, Render2DUtil.multiplyAlpha(BORDER, alpha));
        if (currentTarget instanceof PlayerEntity player) {
            PlayerListEntry entry = client.getNetworkHandler() != null ? client.getNetworkHandler().getPlayerListEntry(player.getUuid()) : null;
            if (entry != null) {
                PlayerSkinDrawer.draw(context, entry.getSkinTextures(), x, y, size);
                return;
            }
        }
        Render2DUtil.roundedRect(context, x, y, size, size, 5, Render2DUtil.multiplyAlpha(0xFF293847, alpha));
        Render2DUtil.drawCenteredText(context, client.textRenderer, "?", x + size / 2, y + size / 2 - 4, Render2DUtil.multiplyAlpha(TEXT_META, alpha), false);
    }

    private static int getHpColor(float fraction) {
        if (fraction > 0.66F) {
            return 0xFF79E69A;
        }
        if (fraction > 0.33F) {
            return 0xFFFFBE73;
        }
        return 0xFFFF7A7A;
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return null;
        }
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (entity instanceof LivingEntity living && living != client.player) {
                return living;
            }
        }
        return null;
    }
}
