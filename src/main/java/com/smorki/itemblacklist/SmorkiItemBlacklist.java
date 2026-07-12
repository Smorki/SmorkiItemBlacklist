package com.smorki.itemblacklist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public final class SmorkiItemBlacklist extends JavaPlugin {

    private static SmorkiItemBlacklist instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Set<Material> blacklistedMaterials = new HashSet<>();

    private String messagePrefix;
    private String messageReloadSuccess;
    private String messageReloadNoPermission;
    private String messageItemBlocked;
    private String bypassPermission;

    private boolean enabled;
    private boolean blockInventoryClick;
    private boolean blockDrop;
    private boolean blockPickup;
    private boolean blockPlace;

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

        messagePrefix = config.getString("messages.prefix", "");
        messageReloadSuccess = config.getString("messages.reload-success", "<green>Configuration reloaded successfully.");
        messageReloadNoPermission = config.getString("messages.reload-no-permission", "<red>You do not have permission to reload this plugin.");
        messageItemBlocked = config.getString("messages.item-blocked", "<red>That item is blacklisted and cannot be used.");

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
}
