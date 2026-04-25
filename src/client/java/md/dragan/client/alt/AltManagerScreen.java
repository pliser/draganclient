package md.dragan.client.alt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import md.dragan.mixin.client.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class AltManagerScreen extends Screen {
    private final Screen parent;
    private final List<AltAccount> accounts = new ArrayList<>();
    private TextFieldWidget nameField;
    private ButtonWidget loginButton;
    private ButtonWidget removeButton;
    private ButtonWidget addButton;
    private int selectedIndex = -1;
    private float scroll;
    private String status = "Offline session manager";

    public AltManagerScreen(Screen parent) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        loadAccounts();

        int panelX = this.width / 2 - 150;
        int panelY = 34;
        int panelW = 300;

        nameField = new TextFieldWidget(this.textRenderer, panelX + 12, panelY + 28, panelW - 24, 20, Text.literal("Username"));
        nameField.setMaxLength(16);
        nameField.setPlaceholder(Text.literal("Enter offline username"));
        this.addDrawableChild(nameField);
        this.setInitialFocus(nameField);

        addButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), button -> addAccount())
            .dimensions(panelX + 12, panelY + 56, 66, 20)
            .build());
        loginButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Login"), button -> loginSelected())
            .dimensions(panelX + 82, panelY + 56, 66, 20)
            .build());
        removeButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Remove"), button -> removeSelected())
            .dimensions(panelX + 152, panelY + 56, 66, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
            .dimensions(panelX + 222, panelY + 56, 66, 20)
            .build());

        syncButtons();
    }

    @Override
    public void tick() {
        super.tick();
        syncButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0101318);

        int panelX = this.width / 2 - 150;
        int panelY = 34;
        int panelW = 300;
        int panelH = this.height - 68;
        int listX = panelX + 12;
        int listY = panelY + 88;
        int listW = panelW - 24;
        int listH = panelH - 112;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xE0141720);
        context.drawBorder(panelX, panelY, panelW, panelH, 0xFF334359);
        context.drawTextWithShadow(this.textRenderer, this.title, panelX + 12, panelY + 10, 0xFFE8EEF8);

        String current = client == null ? "unknown" : client.getSession().getUsername();
        context.drawTextWithShadow(this.textRenderer, "Current: " + current, panelX + 170, panelY + 10, 0xFF7FB2FF);
        context.drawTextWithShadow(this.textRenderer, status, panelX + 12, panelY + panelH - 18, 0xFF93A5BC);

        super.render(context, mouseX, mouseY, delta);

        context.fill(listX, listY, listX + listW, listY + listH, 0xCC0F141B);
        context.drawBorder(listX, listY, listW, listH, 0xFF2C394D);

        int rowY = listY + 4 - Math.round(scroll);
        for (int i = 0; i < accounts.size(); i++) {
            boolean selected = i == selectedIndex;
            int rowColor = selected ? 0xFF213248 : 0xFF151C25;
            context.fill(listX + 4, rowY, listX + listW - 4, rowY + 20, rowColor);
            context.drawBorder(listX + 4, rowY, listW - 8, 20, selected ? 0xFF68C1FF : 0xFF263140);
            context.drawTextWithShadow(this.textRenderer, accounts.get(i).username(), listX + 10, rowY + 6, 0xFFF0F4F8);
            rowY += 22;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int panelX = this.width / 2 - 150;
        int panelY = 34;
        int panelW = 300;
        int panelH = this.height - 68;
        int listX = panelX + 12;
        int listY = panelY + 88;
        int listW = panelW - 24;
        int listH = panelH - 112;

        if (mouseX < listX || mouseX > listX + listW || mouseY < listY || mouseY > listY + listH) {
            return false;
        }

        int localY = (int) Math.floor(mouseY - listY + scroll - 4);
        int index = localY / 22;
        if (index >= 0 && index < accounts.size()) {
            selectedIndex = index;
            syncButtons();
            if (button == 0 && index == selectedIndex) {
                nameField.setText(accounts.get(index).username());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int visibleHeight = this.height - 68 - 112;
        int maxScroll = Math.max(0, accounts.size() * 22 - visibleHeight + 8);
        scroll = MathHelper.clamp(scroll - (float) verticalAmount * 16.0F, 0.0F, maxScroll);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            addAccount();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return nameField != null && nameField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void addAccount() {
        String username = AltStorage.sanitizeUsername(nameField.getText());
        if (username.isEmpty()) {
            status = "Enter a valid username";
            return;
        }
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).username().equalsIgnoreCase(username)) {
                selectedIndex = i;
                status = "Alt already exists";
                syncButtons();
                return;
            }
        }
        accounts.add(new AltAccount(username));
        selectedIndex = accounts.size() - 1;
        saveAccounts();
        status = "Added alt: " + username;
        syncButtons();
    }

    private void loginSelected() {
        if (client == null || selectedIndex < 0 || selectedIndex >= accounts.size()) {
            return;
        }
        AltAccount account = accounts.get(selectedIndex);
        Session session = new Session(
            account.username(),
            offlineUuid(account.username()),
            "",
            java.util.Optional.empty(),
            java.util.Optional.empty(),
            Session.AccountType.LEGACY
        );
        ((MinecraftClientAccessor) client).dragan$setSession(session);
        status = "Logged in as " + account.username();
    }

    private void removeSelected() {
        if (selectedIndex < 0 || selectedIndex >= accounts.size()) {
            return;
        }
        String removed = accounts.remove(selectedIndex).username();
        if (selectedIndex >= accounts.size()) {
            selectedIndex = accounts.size() - 1;
        }
        saveAccounts();
        status = "Removed alt: " + removed;
        syncButtons();
    }

    private void syncButtons() {
        boolean hasSelection = selectedIndex >= 0 && selectedIndex < accounts.size();
        if (loginButton != null) {
            loginButton.active = hasSelection;
        }
        if (removeButton != null) {
            removeButton.active = hasSelection;
        }
        if (addButton != null) {
            addButton.active = nameField != null && !AltStorage.sanitizeUsername(nameField.getText()).isEmpty();
        }
    }

    private void loadAccounts() {
        try {
            accounts.clear();
            accounts.addAll(AltStorage.load());
            status = accounts.isEmpty() ? "No saved alts" : "Loaded " + accounts.size() + " alts";
        } catch (IOException exception) {
            status = "Failed to load alts: " + exception.getMessage();
        }
    }

    private void saveAccounts() {
        try {
            AltStorage.save(accounts);
        } catch (IOException exception) {
            status = "Failed to save alts: " + exception.getMessage();
        }
    }

    private static UUID offlineUuid(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
