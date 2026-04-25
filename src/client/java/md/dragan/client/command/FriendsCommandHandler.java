package md.dragan.client.command;

import java.util.List;
import md.dragan.client.friend.FriendsManager;
import md.dragan.client.hud.HudBootstrap;
import md.dragan.client.hud.NotificationType;

public final class FriendsCommandHandler {
    private FriendsCommandHandler() {
    }

    public static boolean handle(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim();
        if (!trimmed.startsWith(".friends") && !trimmed.startsWith(".friend")) {
            return false;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            notify("Friends", usage(), NotificationType.INFO);
            return true;
        }

        String action = parts[1].toLowerCase();
        switch (action) {
            case "add" -> {
                if (parts.length < 3) {
                    notify("Friends", ".friends add <name>", NotificationType.INFO);
                } else if (FriendsManager.add(parts[2])) {
                    notify("Friends", "Added " + parts[2], NotificationType.SUCCESS);
                } else {
                    notify("Friends", "Failed to add " + parts[2], NotificationType.ERROR);
                }
                return true;
            }
            case "remove", "del", "delete" -> {
                if (parts.length < 3) {
                    notify("Friends", ".friends remove <name>", NotificationType.INFO);
                } else if (FriendsManager.remove(parts[2])) {
                    notify("Friends", "Removed " + parts[2], NotificationType.INFO);
                } else {
                    notify("Friends", "Not found: " + parts[2], NotificationType.ERROR);
                }
                return true;
            }
            case "list" -> {
                List<String> friends = FriendsManager.list();
                notify("Friends", friends.isEmpty() ? "No friends" : String.join(", ", friends), NotificationType.INFO);
                return true;
            }
            case "clear" -> {
                FriendsManager.clear();
                notify("Friends", "Friend list cleared", NotificationType.INFO);
                return true;
            }
            default -> {
                notify("Friends", usage(), NotificationType.INFO);
                return true;
            }
        }
    }

    private static String usage() {
        return ".friends add <name> | remove <name> | list | clear";
    }

    private static void notify(String title, String message, NotificationType type) {
        HudBootstrap.notify(title, message, type);
    }
}
