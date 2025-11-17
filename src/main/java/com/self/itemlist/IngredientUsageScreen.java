package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.ArrayList;

public class IngredientUsageScreen extends Screen {
    private final CustomItem ingredient;
    private final Screen parent;
    private List<CustomItem> usageItems = new ArrayList<>();
    private final int gridCols = 8;
    private final int gridRows = 8;
    private int currentPage = 0;
    private final int itemsPerPage = gridCols * gridRows;

    public IngredientUsageScreen(CustomItem ingredient, Screen parent) {
        super(Text.literal("Items using " + ingredient.getName()));
        this.ingredient = ingredient;
        this.parent = parent;
        // Find all items that use this ingredient in their recipes
        for (CustomItem item : ItemRegistry.getAllItems()) {
            if (item.getRecipes().isEmpty()) continue;
            boolean found = false;
            for (CustomItem.CraftingRecipe recipe : item.getRecipes()) {
                for (int r = 0; r < recipe.getRows(); r++) {
                    for (int c = 0; c < recipe.getCols(); c++) {
                        CustomItem.RecipeIngredient ri = recipe.getPattern()[r][c];
                        if (ri != null && ri.isCustomItem() && ri.getMaterial().equals(ingredient.getId())) {
                            usageItems.add(item);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (found) break;
            }
        }
    }

    @Override
    protected void init() {
        // No buttons
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Solid background
        context.fill(0, 0, this.width, this.height, 0xC0000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // Calculate grid position
        int slotSize = 18;
        int gridWidth = gridCols * slotSize;
        int gridHeight = gridRows * slotSize;
        int gridX = (this.width - gridWidth) / 2;
        int gridY = (this.height - gridHeight) / 2;

        // Draw grid background
        context.fill(gridX - 5, gridY - 5, gridX + gridWidth + 5, gridY + gridHeight + 5, 0xC0000000);
        context.drawBorder(gridX - 5, gridY - 5, gridWidth + 10, gridHeight + 10, 0xFF8B8B8B);

        // Render items
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, usageItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % gridCols;
            int row = index / gridCols;

            int x = gridX + col * slotSize;
            int y = gridY + row * slotSize;

            // Draw slot
            context.fill(x, y, x + 16, y + 16, 0xFF3F3F3F);
            context.drawBorder(x, y, 16, 16, 0xFF8B8B8B);

            // Render item
            CustomItem item = usageItems.get(i);
            ItemStack stack = item.toItemStack();
            context.drawItem(stack, x, y);
        }

        // Render tooltips
        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % gridCols;
            int row = index / gridCols;

            int x = gridX + col * slotSize;
            int y = gridY + row * slotSize;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                CustomItem item = usageItems.get(i);
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(item.getName()));
                if (!item.getDescription().isEmpty()) {
                    tooltip.add(Text.literal(item.getDescription()));
                }
                context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
                break;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int slotSize = 18;
            int gridWidth = gridCols * slotSize;
            int gridHeight = gridRows * slotSize;
            int gridX = (this.width - gridWidth) / 2;
            int gridY = (this.height - gridHeight) / 2;

            int startIndex = currentPage * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, usageItems.size());

            for (int i = startIndex; i < endIndex; i++) {
                int index = i - startIndex;
                int col = index % gridCols;
                int row = index / gridCols;

                int x = gridX + col * slotSize;
                int y = gridY + row * slotSize;

                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    CustomItem item = usageItems.get(i);
                    MinecraftClient.getInstance().setScreen(new RecipeViewerScreen(item, this));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
