package com.self.itemlist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class ItemListClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register screen open/close events
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ItemListScreen.onScreenOpened(screen);
        });

        ItemList.LOGGER.info("ItemList client initialized!");
    }
}