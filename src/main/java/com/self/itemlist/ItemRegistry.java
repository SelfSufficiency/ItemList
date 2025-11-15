package com.self.itemlist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static final List<CustomItem> ITEMS = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void loadItems() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("itemlist");
        File itemsFile = configDir.resolve("items.json").toFile();

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!itemsFile.exists()) {
                createDefaultItemsFile(itemsFile);
            }

            loadItemsFromFile(itemsFile);
            ItemList.LOGGER.info("Loaded {} items", ITEMS.size());
        } catch (IOException e) {
            ItemList.LOGGER.error("Failed to load items", e);
        }
    }

    private static void createDefaultItemsFile(File file) throws IOException {
        JsonArray items = new JsonArray();

        // Create some example items
        for (int i = 1; i <= 150; i++) {
            JsonObject item = new JsonObject();
            item.addProperty("id", "custom_item_" + i);
            item.addProperty("name", "§6Custom Item #" + i);
            item.addProperty("material", "minecraft:diamond");

            JsonArray lore = new JsonArray();
            lore.add("§7This is item number " + i);
            lore.add("§aRarity: LEGENDARY");
            item.add("lore", lore);

            item.addProperty("description", "This is a custom item #" + i + " for testing purposes.");

            items.add(item);
        }

        JsonObject root = new JsonObject();
        root.add("items", items);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        }

        ItemList.LOGGER.info("Created default items.json with {} items", items.size());
    }

    private static void loadItemsFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            JsonArray itemsArray = root.getAsJsonArray("items");

            ITEMS.clear();

            for (JsonElement element : itemsArray) {
                JsonObject obj = element.getAsJsonObject();

                String id = obj.get("id").getAsString();
                String name = obj.get("name").getAsString();
                String material = obj.get("material").getAsString();

                List<String> lore = new ArrayList<>();
                if (obj.has("lore")) {
                    JsonArray loreArray = obj.getAsJsonArray("lore");
                    for (JsonElement loreElement : loreArray) {
                        lore.add(loreElement.getAsString());
                    }
                }

                String description = obj.has("description") ? obj.get("description").getAsString() : "";

                CustomItem item = new CustomItem(id, name, material, lore, description);
                ITEMS.add(item);
            }
        }
    }

    public static List<CustomItem> getAllItems() {
        return new ArrayList<>(ITEMS);
    }

    public static List<CustomItem> searchItems(String query) {
        if (query == null || query.isEmpty()) {
            return getAllItems();
        }

        List<CustomItem> results = new ArrayList<>();
        for (CustomItem item : ITEMS) {
            if (item.matchesSearch(query)) {
                results.add(item);
            }
        }
        return results;
    }
}