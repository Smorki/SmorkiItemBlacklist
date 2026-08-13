package com.smorki.itemblacklist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class SmorkiItemBlacklist extends JavaPlugin {

    private static SmorkiItemBlacklist instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Set<Material> blacklistedMaterials = new HashSet<>();
    private final Map<UUID, Map<Material, Integer>> warningCounts = new HashMap<>();
    private final Map<UUID, Long> lastWarningMessageTimes = new HashMap<>();

    private String messagePrefix;
    private String messageReloadSuccess;
    private String messageReloadNoPermission;
    private String messageItemBlocked;
    private String messageItemRemoved;
    private String bypassPermission;

    private boolean enabled;
    private boolean blockInventoryClick;
    private boolean blockDrop;
    private boolean blockPickup;
    private boolean blockPlace;
    private boolean blockShulkerWithBlacklistedItems;
    private int removeAfterWarnings;
    private int warningCooldownSeconds;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadBlacklistEngine();

        printBootLogo();

        Bukkit.getPluginManager().registerEvents(new BlacklistListener(this), this);

        getCommand("smorkiblacklist").setExecutor(this);

        getLogger().info("SmorkiItemBlacklist has been enabled with " + blacklistedMaterials.size() + " blacklisted items.");
    }

    @Override
    public void onDisable() {
        blacklistedMaterials.clear();
        warningCounts.clear();
        lastWarningMessageTimes.clear();
        getLogger().info("SmorkiItemBlacklist has been disabled.");
    }

    public static SmorkiItemBlacklist getInstance() {
        return instance;
    }

    public void loadBlacklistEngine() {
        reloadConfig();
        FileConfiguration config = getConfig();

        blacklistedMaterials.clear();

        enabled = config.getBoolean("settings.enabled", true);
        bypassPermission = config.getString("settings.bypass-permission", "smorki.blacklist.bypass");
        blockInventoryClick = config.getBoolean("settings.block-inventory-click", true);
        blockDrop = config.getBoolean("settings.block-drop", true);
        blockPickup = config.getBoolean("settings.block-pickup", true);
        blockPlace = config.getBoolean("settings.block-place", true);
        blockShulkerWithBlacklistedItems = config.getBoolean("settings.block-shulker-with-blacklisted-items", true);
        removeAfterWarnings = config.getInt("settings.remove-after-warnings", 5);
        warningCooldownSeconds = config.getInt("settings.warning-cooldown-seconds", 1);

        messagePrefix = config.getString("messages.prefix", "");
        messageReloadSuccess = config.getString("messages.reload-success", "<green>Configuration reloaded successfully.");
        messageReloadNoPermission = config.getString("messages.reload-no-permission", "<red>You do not have permission to reload this plugin.");
        messageItemBlocked = config.getString("messages.item-blocked", "<red>That item is blacklisted and cannot be used.");
        messageItemRemoved = config.getString("messages.item-removed", "<yellow>Blacklisted items were removed from your inventory.");

        List<String> rawList = config.getStringList("blacklist");

        for (String rawEntry : rawList) {
            if (rawEntry == null || rawEntry.isBlank()) {
                continue;
            }

            String cleaned = rawEntry.replace("minecraft:", "").replace("MINECRAFT:", "");
            cleaned = cleaned.toUpperCase();

            Material material = Material.getMaterial(cleaned);

            if (material != null) {
                blacklistedMaterials.add(material);
            } else {
                getLogger().log(Level.WARNING, "Skipped unknown material in blacklist: " + rawEntry);
            }
        }
    }

    private void printBootLogo() {
        String[] logoLines = new String[] {
                "<gradient:#00A3E0:#00FFFF>███████╗███╗   ███╗ ██████╗ ██████╗ ██╗  ██╗██╗</gradient>",
                "<gradient:#00A3E0:#00FFFF>██╔════╝████╗ ████║██╔═══██╗██╔══██╗██║  ██║██║</gradient>",
                "<gradient:#00A3E0:#00FFFF>╚█████╗ ██╔████╔██║██║   ██║██████╔╝█████╔╝██║</gradient>",
                "<gradient:#00A3E0:#00FFFF> ╚═══██╗██║╚██╔╝██║██║   ██║██╔══██╗██╔═██╗ ██║</gradient>",
                "<gradient:#00A3E0:#00FFFF>███████║██║ ╚═╝ ██║╚██████╔╝██║  ██║██║  ██╗██║</gradient>",
                "<gradient:#00A3E0:#00FFFF>╚══════╝╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝</gradient>",
                "",
                " [Item Blacklist] v" + getDescription().getVersion() + " (Target: Minecraft 1.21.11 Safety Core)",
                " [SmorkiItemBlacklist] System Online - Developed by Smorki"
        };

        for (String line : logoLines) {
            Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize(line));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("smorkiblacklist")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("smorki.blacklist.admin")) {
                    sender.sendMessage(deserializeWithPrefix(messageReloadNoPermission));
                    return true;
                }

                loadBlacklistEngine();
                sender.sendMessage(deserializeWithPrefix(messageReloadSuccess));
                return true;
            }

            sender.sendMessage(miniMessage.deserialize("<gray>Usage: /" + label + " reload"));
            return true;
        }

        return false;
    }

    public Component deserializeWithPrefix(String message) {
        return miniMessage.deserialize(messagePrefix + message);
    }

    public Component getItemBlockedMessage() {
        return deserializeWithPrefix(messageItemBlocked);
    }

    public void handleBlockedItem(Player player, Material material) {
        sendWarningMessage(player);

        if (material == null || removeAfterWarnings <= 0 || !blacklistedMaterials.contains(material)) {
            return;
        }

        Map<Material, Integer> counts = warningCounts.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        int count = counts.getOrDefault(material, 0) + 1;

        if (count < removeAfterWarnings) {
            counts.put(material, count);
            return;
        }

        counts.put(material, 0);
        if (removeMaterialFromInventory(player, material)) {
            player.sendMessage(deserializeWithPrefix(messageItemRemoved));
        }
    }

    private void sendWarningMessage(Player player) {
        long now = System.currentTimeMillis();
        long cooldownMs = Math.max(0, warningCooldownSeconds) * 1000L;

        if (cooldownMs > 0) {
            Long last = lastWarningMessageTimes.get(player.getUniqueId());
            if (last != null && now - last < cooldownMs) {
                return;
            }
            lastWarningMessageTimes.put(player.getUniqueId(), now);
        }

        player.sendMessage(getItemBlockedMessage());
    }

    private boolean removeMaterialFromInventory(Player player, Material material) {
        boolean removed = false;

        for (ItemStack item : player.getInventory().all(material).values()) {
            player.getInventory().remove(item);
            removed = true;
        }

        return removed;
    }

    public void clearWarnings(UUID playerId) {
        warningCounts.remove(playerId);
        lastWarningMessageTimes.remove(playerId);
    }

    public Set<Material> getBlacklistedMaterials() {
        return blacklistedMaterials;
    }

    public boolean isEngineEnabled() {
        return enabled;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public boolean isBlockInventoryClick() {
        return blockInventoryClick;
    }

    public boolean isBlockDrop() {
        return blockDrop;
    }

    public boolean isBlockPickup() {
        return blockPickup;
    }

    public boolean isBlockPlace() {
        return blockPlace;
    }

    public boolean isBlockShulkerWithBlacklistedItems() {
        return blockShulkerWithBlacklistedItems;
    }
}
