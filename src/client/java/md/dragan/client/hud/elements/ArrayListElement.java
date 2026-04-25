package md.dragan.client.hud.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import md.dragan.client.gui.modernclick.util.Animation;
import md.dragan.client.gui.modernclick.util.Animators;
import md.dragan.client.gui.modernclick.util.Render2DUtil;
import md.dragan.client.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ArrayListElement extends HudElement {
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 5;
    private static final int PAD_X = 9;
    private static final int PAD_Y = 0;
    private static final int ACCENT_W = 3;
    private static final int BG = 0xDE0E141D;
    private static final int BG_INNER = 0xD9131C27;
    private static final int ACCENT = 0xFF77D0FF;
    private static final int TEXT = 0xFFF4F8FC;

    private final ModuleStateSource source;
    private final Map<String, EntryState> states = new LinkedHashMap<>();
    private final Animation fade = new Animation(1.0F);

    public ArrayListElement(float defaultX, float defaultY, ModuleStateSource source) {
        super("arraylist", defaultX, defaultY);
        this.source = source;
    }

    @Override
    protected void measure(MinecraftClient client) {
        List<Row> rows = visibleRows(client);
        int maxTextWidth = 0;
        for (Row row : rows) {
            maxTextWidth = Math.max(maxTextWidth, row.textWidth);
        }

        width = ACCENT_W + PAD_X + maxTextWidth + PAD_X + 8;
        if (rows.isEmpty()) {
            height = ROW_HEIGHT + PAD_Y * 2;
            return;
        }
        height = PAD_Y * 2 + rows.size() * ROW_HEIGHT + (rows.size() - 1) * ROW_GAP;
    }

    @Override
    public void update() {
        fade.setTarget(isVisible() ? 1.0F : 0.0F);
        fade.tick(Animators.expAlpha(Animators.timeToResponse(140.0F), 1.0F / 60.0F));
        tickEntries();
        markBoundsDirty();
    }

    @Override
    public void render(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Row> rows = visibleRows(client);
        float rootAlpha = Math.max(0.0F, Math.min(1.0F, fade.value()));
        if (rootAlpha <= 0.01F) {
            return;
        }

        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int yOff = PAD_Y;
        for (Row row : rows) {
            int rowW = ACCENT_W + PAD_X + row.textWidth + PAD_X;
            int rowX = ix + iw - rowW + Math.round(row.slide);
            int rowY = iy + yOff;
            float alpha = rootAlpha * row.alpha;
            Render2DUtil.roundedRect(context, rowX - 1, rowY - 1, rowW + 2, ROW_HEIGHT + 2, 6, Render2DUtil.multiplyAlpha(0x12000000, alpha));
            Render2DUtil.roundedRect(context, rowX, rowY, rowW, ROW_HEIGHT, 5, Render2DUtil.multiplyAlpha(BG, alpha));
            Render2DUtil.roundedRect(context, rowX + 1, rowY + 1, rowW - 2, ROW_HEIGHT - 2, 4, Render2DUtil.multiplyAlpha(BG_INNER, alpha));
            Render2DUtil.rect(context, rowX + rowW - ACCENT_W - 4, rowY + 3, ACCENT_W, ROW_HEIGHT - 6, Render2DUtil.multiplyAlpha(ACCENT, alpha));
            Render2DUtil.rect(context, rowX + 6, rowY + 3, 10, 1, Render2DUtil.multiplyAlpha(0x1FFFFFFF, alpha));
            Render2DUtil.drawTextClipped(
                context,
                client.textRenderer,
                row.name,
                rowX + PAD_X,
                rowY + 5,
                Math.max(10, rowW - PAD_X * 2 - ACCENT_W - 6),
                Render2DUtil.multiplyAlpha(TEXT, alpha),
                false
            );
            yOff += ROW_HEIGHT + ROW_GAP;
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

    private void tickEntries() {
        float alphaStep = Animators.expAlpha(Animators.timeToResponse(130.0F), 1.0F / 60.0F);
        float slideStep = Animators.expAlpha(Animators.timeToResponse(150.0F), 1.0F / 60.0F);

        for (String moduleName : source.allModuleNames()) {
            EntryState state = states.computeIfAbsent(moduleName, key -> new EntryState());
            boolean enabled = source.isEnabled(moduleName);
            state.alpha.setTarget(enabled ? 1.0F : 0.0F);
            state.slide.setTarget(enabled ? 0.0F : 10.0F);
            state.alpha.tick(alphaStep);
            state.slide.tick(slideStep);
        }

        states.entrySet().removeIf(entry -> entry.getValue().alpha.value() < 0.01F && !source.isEnabled(entry.getKey()));
    }

    private List<Row> visibleRows(MinecraftClient client) {
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<String, EntryState> entry : states.entrySet()) {
            float alpha = entry.getValue().alpha.value();
            if (alpha < 0.01F && !source.isEnabled(entry.getKey())) {
                continue;
            }
            int textWidth = Render2DUtil.textWidth(client.textRenderer, entry.getKey());
            rows.add(new Row(entry.getKey(), textWidth, alpha, entry.getValue().slide.value()));
        }
        rows.sort(Comparator.comparingInt((Row row) -> row.textWidth).reversed());
        return rows;
    }

    public interface ModuleStateSource {
        List<String> allModuleNames();

        boolean isEnabled(String moduleName);
    }

    private static final class EntryState {
        private final Animation alpha = new Animation(0.0F);
        private final Animation slide = new Animation(10.0F);
    }

    private record Row(String name, int textWidth, float alpha, float slide) {
    }
}
