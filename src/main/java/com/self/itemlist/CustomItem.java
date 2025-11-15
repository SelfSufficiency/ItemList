package com.self.itemlist;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.ArrayList;

public class CustomItem {
    private String id;
    private String name;
    private String material;
    private List<String> lore;
    private String description;
    private List<CraftingRecipe> recipes;

    public CustomItem(String id, String name, String material, List<String> lore, String description) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.description = description != null ? description : "";
        this.recipes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDescription() {
        return description;
    }

    public List<CraftingRecipe> getRecipes() {
        return recipes;
    }

    public void addRecipe(CraftingRecipe recipe) {
        this.recipes.add(recipe);
    }

    public ItemStack toItemStack() {
        try {
            Identifier itemId = Identifier.tryParse(material);
            if (itemId != null && Registries.ITEM.containsId(itemId)) {
                ItemStack stack = new ItemStack(Registries.ITEM.get(itemId));
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
                return stack;
            }
        } catch (Exception e) {
            ItemList.LOGGER.error("Failed to create ItemStack for: " + material, e);
        }
        return new ItemStack(Items.BARRIER);
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();

        if (name.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        for (String loreLine : lore) {
            if (loreLine.toLowerCase().contains(lowerQuery)) {
                return true;
            }
        }

        return false;
    }

    public static class CraftingRecipe {
        private String[][] pattern;
        private ItemStack result;

        public CraftingRecipe(String[][] pattern, ItemStack result) {
            this.pattern = pattern;
            this.result = result;
        }

        public String[][] getPattern() {
            return pattern;
        }

        public ItemStack getResult() {
            return result;
        }
    }
}