package com.smorki.itemblacklist;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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

    private boolean isShulkerWithBlacklistedItems(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (!(itemStack.getItemMeta() instanceof BlockStateMeta meta)) {
            return false;
        }

        if (!(meta.getBlockState() instanceof org.bukkit.block.ShulkerBox shulkerBox)) {
            return false;
        }

        for (ItemStack content : shulkerBox.getInventory().getContents()) {
            if (isBlacklisted(content)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlockedItem(ItemStack itemStack) {
        if (isBlacklisted(itemStack)) {
            return true;
        }

        return plugin.isBlockShulkerWithBlacklistedItems() && isShulkerWithBlacklistedItems(itemStack);
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
            plugin.handleBlockedItem(player, placedMaterial);
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

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !plugin.isBlockPlace()) {
            return;
        }

        ItemStack item = event.getItem();

        if (isBlacklisted(item)) {
            event.setCancelled(true);
            plugin.handleBlockedItem(player, item.getType());
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

        boolean currentBlocked = isBlockedItem(currentItem);
        boolean cursorBlocked = isBlockedItem(cursorItem);

        if (!currentBlocked && !cursorBlocked) {
            return;
        }

        event.setCancelled(true);

        if (currentBlocked) {
            plugin.handleBlockedItem(player, currentItem.getType());
        }

        if (cursorBlocked) {
            plugin.handleBlockedItem(player, cursorItem.getType());
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

        if (isBlockedItem(droppedItem.getItemStack())) {
            event.setCancelled(true);
            plugin.handleBlockedItem(player, droppedItem.getItemStack().getType());
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

        if (isBlockedItem(itemStack)) {
            event.setCancelled(true);
            plugin.handleBlockedItem(player, itemStack.getType());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearWarnings(event.getPlayer().getUniqueId());
    }
}
