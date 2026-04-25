package md.dragan.client.gui.modernclick.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class GuiFontRenderer {
    private static final Identifier TEXTURE_ID = Identifier.of("dragan", "runtime/gui_font");
    private static final int ATLAS_SIZE = 1024;
    private static final int FONT_SIZE = 24;
    private static final float UI_SCALE = 0.46F;
    private static final String FONT_RESOURCE = "/assets/dragan/font/SFPRODISPLAYBOLD.OTF";
    private static final String FALLBACK_FONT_RESOURCE = "/assets/dragan/font/montserrat_wght.ttf";
    private static final char[] CHARSET = buildCharset();
    private static GuiFontRenderer instance;

    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private NativeImageBackedTexture texture;
    private boolean ready;
    private int lineHeight = 18;

    private GuiFontRenderer() {
    }

    public static GuiFontRenderer get() {
        if (instance == null) {
            instance = new GuiFontRenderer();
        }
        instance.ensureReady();
        return instance;
    }

    public int width(String text) {
        ensureReady();
        if (!ready || text == null || text.isEmpty()) {
            return 0;
        }
        float width = 0.0F;
        for (int i = 0; i < text.length(); i++) {
            width += glyph(text.charAt(i)).advance();
        }
        return Math.round(width * UI_SCALE);
    }

    public int lineHeight() {
        ensureReady();
        return Math.round(lineHeight * UI_SCALE);
    }

    public String clip(String text, int maxWidth) {
        ensureReady();
        String safe = text == null ? "" : text;
        if (safe.isEmpty() || maxWidth <= 0) {
            return "";
        }
        if (width(safe) <= maxWidth) {
            return safe;
        }

        String ellipsis = "...";
        if (width(ellipsis) >= maxWidth) {
            return "";
        }

        int low = 0;
        int high = safe.length();
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            String candidate = safe.substring(0, mid) + ellipsis;
            if (width(candidate) <= maxWidth) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return safe.substring(0, low) + ellipsis;
    }

    public void draw(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        ensureReady();
        if (!ready || text == null || text.isEmpty()) {
            return;
        }
        if (shadow) {
            drawInternal(context, text, x + 1, y + 1, shadowColor(color));
        }
        drawInternal(context, text, x, y, color);
    }

    private void drawInternal(DrawContext context, String text, int x, int y, int color) {
        float cursor = x;
        float drawY = y;
        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyph(text.charAt(i));
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE_ID,
                Math.round(cursor),
                Math.round(drawY),
                glyph.u(),
                glyph.v(),
                Math.round(glyph.width() * UI_SCALE),
                Math.round(lineHeight * UI_SCALE),
                glyph.width(),
                lineHeight,
                ATLAS_SIZE,
                ATLAS_SIZE,
                color
            );
            cursor += glyph.advance() * UI_SCALE;
        }
    }

    private Glyph glyph(char c) {
        return glyphs.getOrDefault(c, glyphs.getOrDefault('?', new Glyph(0, 0, 8, 8)));
    }

    private void ensureReady() {
        if (ready) {
            return;
        }
        try {
            buildAtlas();
            ready = true;
        } catch (IOException | FontFormatException exception) {
            ready = false;
        }
    }

    private void buildAtlas() throws IOException, FontFormatException {
        Font baseFont = loadFont(FONT_RESOURCE);
        if (baseFont == null) {
            baseFont = loadFont(FALLBACK_FONT_RESOURCE);
        }
        if (baseFont == null) {
            throw new IOException("Missing font resources");
        }
        baseFont = baseFont.deriveFont(Font.BOLD, FONT_SIZE);

            BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D probeGraphics = probe.createGraphics();
            applyHints(probeGraphics);
            probeGraphics.setFont(baseFont);
            FontMetrics metrics = probeGraphics.getFontMetrics();
            lineHeight = metrics.getHeight() + 4;
            probeGraphics.dispose();

            BufferedImage atlas = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = atlas.createGraphics();
            applyHints(graphics);
            graphics.setFont(baseFont);
            graphics.setColor(Color.WHITE);
            metrics = graphics.getFontMetrics();

            int x = 2;
            int y = 2;
            for (char c : CHARSET) {
                int glyphWidth = Math.max(4, metrics.charWidth(c) + 4);
                if (x + glyphWidth >= ATLAS_SIZE - 2) {
                    x = 2;
                    y += lineHeight + 2;
                }
                if (y + lineHeight >= ATLAS_SIZE - 2) {
                    break;
                }

                graphics.drawString(String.valueOf(c), x + 1, y + metrics.getAscent());
                glyphs.put(c, new Glyph(x, y, glyphWidth, Math.max(4, metrics.charWidth(c) + 1)));
                x += glyphWidth + 2;
            }
            graphics.dispose();

            NativeImage image = new NativeImage(NativeImage.Format.RGBA, ATLAS_SIZE, ATLAS_SIZE, false);
            for (int py = 0; py < ATLAS_SIZE; py++) {
                for (int px = 0; px < ATLAS_SIZE; px++) {
                    int argb = atlas.getRGB(px, py);
                    int a = (argb >>> 24) & 0xFF;
                    int r = (argb >>> 16) & 0xFF;
                    int g = (argb >>> 8) & 0xFF;
                    int b = argb & 0xFF;
                    image.setColorArgb(px, py, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }

            texture = new NativeImageBackedTexture(() -> "dragan_gui_font", image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(TEXTURE_ID, texture);
    }

    private Font loadFont(String resourcePath) {
        Font font = null;
        try (InputStream stream = GuiFontRenderer.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, stream);
            } catch (FontFormatException ignored) {
                try (InputStream retry = GuiFontRenderer.class.getResourceAsStream(resourcePath)) {
                    if (retry != null) {
                        font = Font.createFont(Font.TYPE1_FONT, retry);
                    }
                } catch (FontFormatException e) {
                    // Keep font as null
                }
            }
        } catch (IOException exception) {
            // Keep font as null
        }
        return font;
    }

    private void applyHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static int shadowColor(int color) {
        int alpha = Math.max(0, ((color >>> 24) & 0xFF) - 70);
        return (alpha << 24);
    }

    private static char[] buildCharset() {
        StringBuilder builder = new StringBuilder();
        for (char c = 32; c < 127; c++) {
            builder.append(c);
        }
        for (char c = 0x0400; c <= 0x045F; c++) {
            builder.append(c);
        }
        return builder.toString().toCharArray();
    }

    private record Glyph(int u, int v, int width, int advance) {
    }
}
