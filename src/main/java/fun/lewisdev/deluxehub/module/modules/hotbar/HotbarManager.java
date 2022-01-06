package fun.lewisdev.deluxehub.module.modules.hotbar;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.config.ConfigType;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.hotbar.items.CustomItem;
import fun.lewisdev.deluxehub.module.modules.hotbar.items.PlayerHider;
import fun.lewisdev.deluxehub.util.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HotbarManager extends Module {
    private List<HotbarItem> hotbarItems;

    public HotbarManager(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.HOTBAR_ITEMS);
    }

    @Override
    public void onEnable() {
        hotbarItems = new ArrayList<>();
        FileConfiguration config = getConfig(ConfigType.SETTINGS);
        ConfigurationSection customItemsSections = config.getConfigurationSection("custom_join_items");

        if (customItemsSections == null) {
            getPlugin().getLogger().severe("Custom join items configuration section is missing!");
            return;
        }

        if (customItemsSections.getBoolean("enabled")) {
            registerCustomItems(customItemsSections);
        }

        ConfigurationSection hiderSection = config.getConfigurationSection("player_hider");

        if (hiderSection == null) {
            getPlugin().getLogger().severe("Player hider item configuration section is missing!");
            return;
        }

        if (hiderSection.getBoolean("enabled")) {
            ItemStack item = ItemStackBuilder.getItemStack(hiderSection.getConfigurationSection("not_hidden")).build();
            PlayerHider playerHider = new PlayerHider(this, item, hiderSection.getInt("slot"), "PLAYER_HIDER");

            playerHider.setAllowMovement(hiderSection.getBoolean("disable_inventory_movement"));
            registerHotbarItem(playerHider);
        }

        giveItems();
    }

    @Override
    public void onDisable() {
        removeItems();
    }

    private void registerCustomItems(ConfigurationSection customItemsSection) {
        ConfigurationSection itemsSection = customItemsSection.getConfigurationSection("items");

        if (itemsSection == null) {
            getPlugin().getLogger().severe("Items of custom join items configuration section is missing!");
            return;
        }

        for (String itemEntry : itemsSection.getKeys(false)) {
            ItemStack item = ItemStackBuilder.getItemStack(itemsSection.getConfigurationSection(itemEntry)).build();
            CustomItem customItem = new CustomItem(this, item, itemsSection.getInt(itemEntry + ".slot"), itemEntry);

            if (itemsSection.contains(itemEntry + ".permission")) {
                customItem.setPermission(itemsSection.getString(itemEntry + ".permission"));
            }

            customItem.setConfigurationSection(itemsSection.getConfigurationSection(itemEntry));
            customItem.setAllowMovement(customItemsSection.getBoolean("disable_inventory_movement"));
            registerHotbarItem(customItem);
        }
    }

    public void registerHotbarItem(HotbarItem hotbarItem) {
        getPlugin().getServer().getPluginManager().registerEvents(hotbarItem, getPlugin());
        hotbarItems.add(hotbarItem);
    }

    private void giveItems() {
        Bukkit.getOnlinePlayers().stream().filter(player -> !inDisabledWorld(player.getLocation()))
                .forEach(player -> hotbarItems.forEach(hotbarItem -> hotbarItem.giveItem(player)));
    }

    private void removeItems() {
        Bukkit.getOnlinePlayers().stream().filter(player -> !inDisabledWorld(player.getLocation()))
                .forEach(player -> hotbarItems.forEach(hotbarItem -> hotbarItem.removeItem(player)));
    }

    public List<HotbarItem> getHotbarItems() {
        return hotbarItems;
    }
}
