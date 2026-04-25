package md.dragan.client.hud.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudElement;
import md.dragan.client.hud.NotificationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class NotificationsElement extends HudElement {
    private static final int MAX_VISIBLE = 5;
    private static final int PAD_X = 10;
    private static final int PAD_Y = 7;
    private static final int ACCENT_W = 3;
    private static final int ROW_H = 34;
    private static final int ROW_GAP = 6;
    private static final int BG = 0xE00D131B;
    private static final int INNER = 0xD7121B25;
    private static final int BORDER = 0xFF223344;
    private static final int TEXT_MAIN = 0xFFF4F8FC;
    private static final int TEXT_SUB = 0xFF9CB0C5;

    private final List<Entry> entries = new ArrayList<>();

    public NotificationsElement(float defaultX, float defaultY) {
        super("notifications", defaultX, defaultY);
    }

    public void push(String title, String description, NotificationType type, long durationMs) {
        if (title == null || title.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        entries.add(new Entry(title, description == null ? "" : description, type == null ? NotificationType.INFO : type, now, now + Math.max(500L, durationMs)));
        if (entries.size() > 32) {
            entries.removeFirst();
        }
        markBoundsDirty();
    }

    @Override
    protected void measure(MinecraftClient client) {
        int count = visibleCount();
        int maxText = 120;
        for (Entry entry : entries) {
            maxText = Math.max(maxText, Render2DUtil.textWidth(client.textRenderer, entry.title));
            maxText = Math.max(maxText, Render2DUtil.textWidth(client.textRenderer, entry.description));
        }
        width = ACCENT_W + PAD_X + Math.min(220, maxText) + PAD_X + 12;
        if (count <= 0) {
            height = ROW_H;
            return;
        }
        height = count * ROW_H + (count - 1) * ROW_GAP;
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        float alphaStep = Animators.expAlpha(Animators.timeToResponse(140.0F), 1.0F / 60.0F);
        float slideStep = Animators.expAlpha(Animators.timeToResponse(200.0F), 1.0F / 60.0F);

        for (Entry entry : entries) {
            boolean expired = now >= entry.expiresAt;
            entry.alpha.setTarget(expired ? 0.0F : 1.0F);
            entry.slide.setTarget(expired ? 200.0F : 0.0F);
            entry.alpha.tick(alphaStep);
            entry.slide.tick(slideStep);
        }

        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (now >= entry.expiresAt && entry.alpha.value() < 0.02F) {
                iterator.remove();
            }
        }

        while (visibleCount() > MAX_VISIBLE) {
            entries.removeFirst();
        }
        markBoundsDirty();
    }

    @Override
    public void render(DrawContext context, float delta) {
        if (entries.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int yOff = 0;
        int drawn = 0;

        for (int i = entries.size() - 1; i >= 0 && drawn < MAX_VISIBLE; i--) {
            Entry entry = entries.get(i);
            if (entry.alpha.value() < 0.02F) {
                continue;
            }
            int rowX = ix + Math.round(entry.slide.value());
            int rowY = iy + yOff;
            float alpha = entry.alpha.value();

            Render2DUtil.roundedRect(context, rowX - 2, rowY - 2, iw + 4, ROW_H + 4, 8, Render2DUtil.multiplyAlpha(0x14000000, alpha));
            Render2DUtil.roundedRect(context, rowX, rowY, iw, ROW_H, 6, Render2DUtil.multiplyAlpha(BG, alpha));
            Render2DUtil.roundedRect(context, rowX + 1, rowY + 1, iw - 2, ROW_H - 2, 5, Render2DUtil.multiplyAlpha(INNER, alpha));
            Render2DUtil.border(context, rowX, rowY, iw, ROW_H, Render2DUtil.multiplyAlpha(BORDER, alpha));
            Render2DUtil.rect(context, rowX + 7, rowY + 6, 18, 2, Render2DUtil.multiplyAlpha(entry.type.accent(), alpha));
            Render2DUtil.rect(context, rowX + 7, rowY + 10, 8, 1, Render2DUtil.multiplyAlpha(0x22FFFFFF, alpha));
            Render2DUtil.rect(context, rowX + iw - 8 - ACCENT_W, rowY + 6, ACCENT_W, ROW_H - 12, Render2DUtil.multiplyAlpha(entry.type.accent(), alpha));
            int textW = Math.max(10, iw - PAD_X * 2 - 10);
            Render2DUtil.drawTextClipped(
                context,
                client.textRenderer,
                entry.title,
                rowX + PAD_X,
                rowY + PAD_Y + 2,
                textW,
                Render2DUtil.multiplyAlpha(TEXT_MAIN, alpha),
                false
            );
            Render2DUtil.drawTextClipped(
                context,
                client.textRenderer,
                entry.description,
                rowX + PAD_X,
                rowY + PAD_Y + 13,
                textW,
                Render2DUtil.multiplyAlpha(TEXT_SUB, alpha),
                false
            );

            yOff += ROW_H + ROW_GAP;
            drawn++;
        }
    }

    private int visibleCount() {
        int count = 0;
        for (Entry entry : entries) {
            if (entry.alpha.value() > 0.02F) {
                count++;
            }
        }
        return Math.max(1, Math.min(MAX_VISIBLE, count));
    }

    private static final class Entry {
        private final String title;
        private final String description;
        private final NotificationType type;
        private final long expiresAt;
        private final Animation alpha = new Animation(0.0F);
        private final Animation slide = new Animation(200.0F);

        private Entry(String title, String description, NotificationType type, long createdAt, long expiresAt) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.expiresAt = Math.max(createdAt + 500L, expiresAt);
        }
    }
}
