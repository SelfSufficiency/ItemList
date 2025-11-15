package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ItemListScreen {
    private static boolean isVisible = false;
    private static int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 128; // 8 columns x 16 rows
    private static final int COLUMNS = 8;
    private static final int ROWS = 16;
    private static final int SLOT_SIZE = 18;

    private static List<CustomItem> filteredItems = new ArrayList<>();
    private static CustomItem selectedItem = null;
    private static TextFieldWidget searchField = null;
    private static String searchQuery = "";

    public static void onScreenOpened(Screen screen) {
        if (screen instanceof HandledScreen) {
            isVisible = true;
            currentPage = 0;
            selectedItem = null;
            updateFilteredItems();
        } else {
            isVisible = false;
            searchField = null;
        }
    }

    public static void render(DrawContext context, int mouseX, int mouseY, float delta, Screen screen) {
        if (!isVisible || !(screen instanceof HandledScreen)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate panel position (right side)
        int panelWidth = COLUMNS * SLOT_SIZE + 20;
        int panelHeight = ROWS * SLOT_SIZE + 60;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - panelHeight) / 2;

        // Draw background panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xC0000000);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF8B8B8B);

        // Draw title
        context.drawText(client.textRenderer, Text.literal("Item List"), panelX + 10, panelY + 8, 0xFFFFFF, true);

        // Initialize search field if needed
        if (searchField == null) {
            searchField = new TextFieldWidget(client.textRenderer, panelX + 10, panelY + 22, panelWidth - 20, 16, Text.literal("Search"));
            searchField.setMaxLength(100);
            searchField.setText(searchQuery);
            searchField.setChangedListener(text -> {
                searchQuery = text;
                currentPage = 0;
                updateFilteredItems();
            });
        } else {
            searchField.setX(panelX + 10);
            searchField.setY(panelY + 22);
        }

        searchField.render(context, mouseX, mouseY, delta);

        // Calculate item grid position
        int gridX = panelX + 10;
        int gridY = panelY + 45;

        // Render items
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;

            // Draw slot background
            context.fill(x, y, x + 16, y + 16, 0x8B000000);

            // Highlight if hovered
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                context.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
            }

            // Render item
            CustomItem item = filteredItems.get(i);
            ItemStack stack = item.toItemStack();
            context.drawItem(stack, x, y);
        }

        // Draw page info
        int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);
        String pageText = "Page " + (currentPage + 1) + "/" + Math.max(1, totalPages);
        int pageTextWidth = client.textRenderer.getWidth(pageText);
        context.drawText(client.textRenderer, Text.literal(pageText),
                panelX + (panelWidth - pageTextWidth) / 2, panelY + panelHeight - 15, 0xFFFFFF, true);

        // Render left panel with item details if item is selected
        if (selectedItem != null) {
            renderItemDetails(context, client, selectedItem, panelX);
        }
    }

    private static void renderItemDetails(DrawContext context, MinecraftClient client, CustomItem item, int rightPanelX) {
        int detailPanelWidth = 200;
        int detailPanelHeight = 300;
        int detailPanelX = rightPanelX - detailPanelWidth - 10;
        int detailPanelY = (client.getWindow().getScaledHeight() - detailPanelHeight) / 2;

        // Draw background
        context.fill(detailPanelX, detailPanelY, detailPanelX + detailPanelWidth,
                detailPanelY + detailPanelHeight, 0xC0000000);
        context.drawBorder(detailPanelX, detailPanelY, detailPanelWidth, detailPanelHeight, 0xFF8B8B8B);

        int contentX = detailPanelX + 10;
        int contentY = detailPanelY + 10;

        // Draw item name
        List<Text> wrappedName = wrapText(client, item.getName(), detailPanelWidth - 20);
        for (Text line : wrappedName) {
            context.drawText(client.textRenderer, line, contentX, contentY, 0xFFFFFF, true);
            contentY += 10;
        }

        contentY += 5;

        // Draw lore
        for (String loreLine : item.getLore()) {
            List<Text> wrapped = wrapText(client, loreLine, detailPanelWidth - 20);
            for (Text line : wrapped) {
                context.drawText(client.textRenderer, line, contentX, contentY, 0xAAAAAA, false);
                contentY += 10;
            }
        }

        contentY += 10;

        // Draw description header
        context.drawText(client.textRenderer, Text.literal("Description:"), contentX, contentY, 0xFFFF55, true);
        contentY += 12;

        // Draw description
        List<Text> wrappedDesc = wrapText(client, item.getDescription(), detailPanelWidth - 20);
        for (Text line : wrappedDesc) {
            context.drawText(client.textRenderer, line, contentX, contentY, 0xCCCCCC, false);
            contentY += 10;
        }

        contentY += 10;

        // Draw recipe section
        if (!item.getRecipes().isEmpty()) {
            context.drawText(client.textRenderer, Text.literal("Recipe:"), contentX, contentY, 0xFFFF55, true);
            contentY += 12;
            context.drawText(client.textRenderer, Text.literal("(Recipe display coming soon)"),
                    contentX, contentY, 0x888888, false);
        }
    }

    private static List<Text> wrapText(MinecraftClient client, String text, int maxWidth) {
        List<Text> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (client.textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(Text.literal(currentLine.toString()));
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(Text.literal(currentLine.toString()));
        }

        return lines;
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, Screen screen) {
        if (!isVisible || !(screen instanceof HandledScreen)) {
            return false;
        }

        // Handle search field click
        if (searchField != null && searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int panelWidth = COLUMNS * SLOT_SIZE + 20;
        int panelHeight = ROWS * SLOT_SIZE + 60;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - panelHeight) / 2;

        int gridX = panelX + 10;
        int gridY = panelY + 45;

        // Check if clicked on an item
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                selectedItem = filteredItems.get(i);
                return true;
            }
        }

        return false;
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isVisible) {
            return false;
        }

        // Handle search field input
        if (searchField != null && searchField.isFocused()) {
            return searchField.keyPressed(keyCode, scanCode, modifiers);
        }

        // Page navigation
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            if (currentPage > 0) {
                currentPage--;
                return true;
            }
        }

        return false;
    }

    public static boolean charTyped(char chr, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            return searchField.charTyped(chr, modifiers);
        }
        return false;
    }

    private static void updateFilteredItems() {
        filteredItems = ItemRegistry.searchItems(searchQuery);
    }
}