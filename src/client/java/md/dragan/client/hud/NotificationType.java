package md.dragan.client.hud;

public enum NotificationType {
    INFO(0xFF4A90E2),
    SUCCESS(0xFF50C878),
    WARNING(0xFFF2B94B),
    ERROR(0xFFE05A5A);

    private final int accent;

    NotificationType(int accent) {
        this.accent = accent;
    }

    public int accent() {
        return accent;
    }
}
