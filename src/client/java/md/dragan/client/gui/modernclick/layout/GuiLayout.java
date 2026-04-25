package md.dragan.client.gui.modernclick.layout;

import md.dragan.client.gui.modernclick.theme.GuiMetrics;

public final class GuiLayout {
    private final GuiRect frame;
    private final GuiRect header;
    private final GuiRect categories;
    private final GuiRect modules;
    private final GuiRect settings;

    public GuiLayout(int frameX, int frameY, int frameWidth, int frameHeight) {
        this.frame = new GuiRect(frameX, frameY, frameWidth, frameHeight);
        this.header = new GuiRect(frameX, frameY, frameWidth, GuiMetrics.HEADER_HEIGHT);

        int bodyX = frameX + GuiMetrics.OUTER_PADDING;
        int bodyY = frameY + GuiMetrics.HEADER_HEIGHT + GuiMetrics.OUTER_PADDING;
        int bodyW = frameWidth - GuiMetrics.OUTER_PADDING * 2;
        int bodyH = frameHeight - GuiMetrics.HEADER_HEIGHT - GuiMetrics.OUTER_PADDING * 2;

        int leftW = GuiMetrics.LEFT_COLUMN_WIDTH;
        int middleW = GuiMetrics.MIDDLE_COLUMN_WIDTH;
        int rightW = bodyW - leftW - middleW - GuiMetrics.COLUMN_GAP * 2;

        this.categories = new GuiRect(bodyX, bodyY, leftW, bodyH);
        this.modules = new GuiRect(bodyX + leftW + GuiMetrics.COLUMN_GAP, bodyY, middleW, bodyH);
        this.settings = new GuiRect(modules.right() + GuiMetrics.COLUMN_GAP, bodyY, rightW, bodyH);
    }

    public GuiRect frame() {
        return frame;
    }

    public GuiRect header() {
        return header;
    }

    public GuiRect categories() {
        return categories;
    }

    public GuiRect modules() {
        return modules;
    }

    public GuiRect settings() {
        return settings;
    }

    public static int frameWidthFor(int screenWidth) {
        return Math.max(500, Math.min(620, screenWidth - 210));
    }

    public static int frameHeightFor(int screenHeight) {
        return Math.max(340, Math.min(410, screenHeight - 130));
    }
}
