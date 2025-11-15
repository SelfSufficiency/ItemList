package com.self.itemlist;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemList implements ModInitializer {
    public static final String MOD_ID = "itemlist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("ItemList mod initialized!");
        ItemRegistry.loadItems();
    }
}