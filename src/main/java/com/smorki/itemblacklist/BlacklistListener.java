package com.smorki.itemblacklist;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class BlacklistListener implements Listener {

    private final SmorkiItemBlacklist plugin;

    public BlacklistListener(SmorkiItemBlacklist plugin) {
        this.plugin = plugin;
    }

    private boolean isBlacklisted(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        Material material = itemStack.getType();
        return plugin.getBlacklistedMaterials().contains(material);
    }

    private boolean hasBypass(HumanEntity humanEntity) {
        return humanEntity.hasPermission(plugin.getBypassPermission());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isEngineEnabled() || !plugin.isBlockPlace()) {
            return;
        }

        Player player = event.getPlayer();

        if (hasBypass(player)) {
            return;
        }

        Material placedMaterial = event.getBlockPlaced().getType();

        if (plugin.getBlacklistedMaterials().contains(placedMaterial)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getItemBlockedMessage());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isEngineEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        if (hasBypass(player)) {
            return;
        }

        ItemStack item = event.getItem();

        if (isBlacklisted(item)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getItemBlockedMessage());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isEngineEnabled() || !plugin.isBlockInventoryClick()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (hasBypass(player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (isBlacklisted(currentItem) || isBlacklisted(cursorItem)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getItemBlockedMessage());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.isEngineEnabled() || !plugin.isBlockDrop()) {
            return;
        }

        Player player = event.getPlayer();

        if (hasBypass(player)) {
            return;
        }

        Item droppedItem = event.getItemDrop();

        if (isBlacklisted(droppedItem.getItemStack())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getItemBlockedMessage());
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.isEngineEnabled() || !plugin.isBlockPickup()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (hasBypass(player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();

        if (isBlacklisted(itemStack)) {
            event.setCancelled(true);
        }
    }
}
