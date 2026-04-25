package md.dragan.client.gui.modernclick.layout;

public record GuiRect(int x, int y, int width, int height) {
    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
    }
}
