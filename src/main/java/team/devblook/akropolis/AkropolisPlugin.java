package team.devblook.akropolis;

import net.megavex.scoreboardlibrary.ScoreboardLibraryImplementation;
import net.megavex.scoreboardlibrary.api.ScoreboardManager;
import net.megavex.scoreboardlibrary.exception.ScoreboardLibraryLoadException;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import team.devblook.akropolis.action.ActionManager;
import team.devblook.akropolis.command.CommandManager;
import team.devblook.akropolis.config.ConfigManager;
import team.devblook.akropolis.cooldown.CooldownManager;
import team.devblook.akropolis.hook.HooksManager;
import team.devblook.akropolis.inventory.InventoryManager;
import team.devblook.akropolis.module.ModuleManager;
import team.devblook.akropolis.module.ModuleType;
import team.devblook.akropolis.module.modules.hologram.HologramManager;

import java.util.logging.Level;

public class AkropolisPlugin extends JavaPlugin {
    private static AkropolisPlugin plugin;
    private static final int BSTATS_ID = 3151;
    private ConfigManager configManager;
    private ActionManager actionManager;
    private HooksManager hooksManager;
    private CommandManager commandManager;
    private CooldownManager cooldownManager;
    private ModuleManager moduleManager;
    private InventoryManager inventoryManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        // Set the unique plugin instance
        setInstance(this);

        long start = System.currentTimeMillis();

        getLogger().log(Level.INFO, "     _    _                          _ _     ");
        getLogger().log(Level.INFO, "    / \\  | | ___ __ ___  _ __   ___ | (_)___ ");
        getLogger().log(Level.INFO, "   / _ \\ | |/ / '__/ _ \\| '_ \\ / _ \\| | / __|");
        getLogger().log(Level.INFO, "  / ___ \\|   <| | | (_) | |_) | (_) | | \\__ \\");
        getLogger().log(Level.INFO, " /_/   \\_\\_|\\_\\_|  \\___/| .__/ \\___/|_|_|___/");
        getLogger().log(Level.INFO, "                        |_|                  ");
        getLogger().log(Level.INFO, "Author: ZetaStormy");
        getLogger().log(Level.INFO, "Based on DeluxeHub by ItsLewizz.");
        getLogger().log(Level.INFO, "--------");

        // Check if using Paper
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
        } catch (ClassNotFoundException ex) {
            getLogger().severe("Akropolis requires Paper 1.19+ to run, you can download");
            getLogger().severe("Paper here: https://papermc.io/downloads.");
            getLogger().severe("The plugin will now disable.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Enable bStats metrics
        new MetricsLite(this, BSTATS_ID);

        // Check plugin hooks
        hooksManager = new HooksManager(this);

        // Load config files
        configManager = new ConfigManager();
        configManager.loadFiles(this);

        // If there were any configuration errors we should not continue
        if (!getServer().getPluginManager().isPluginEnabled(this)) return;

        // Command manager
        commandManager = new CommandManager(this);
        commandManager.reload();

        // Cooldown manager
        cooldownManager = new CooldownManager();

        // Inventory (GUI) manager
        inventoryManager = new InventoryManager();
        if (!hooksManager.isHookEnabled("HEAD_DATABASE")) inventoryManager.onEnable(this);

        //Scoreboard library
        try {
            ScoreboardLibraryImplementation.init();
        } catch (ScoreboardLibraryLoadException e) {
            e.printStackTrace();
            return;
        }

        scoreboardManager = ScoreboardManager.scoreboardManager(this);

        // Core plugin modules
        moduleManager = new ModuleManager();
        moduleManager.loadModules(this);

        // Action system
        actionManager = new ActionManager(this);

        // Register BungeeCord channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().log(Level.INFO, "Successfully loaded in {0} ms.", (System.currentTimeMillis() - start));
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        if (moduleManager != null) moduleManager.unloadModules();

        if (scoreboardManager != null) {
            scoreboardManager.close();
            ScoreboardLibraryImplementation.close();
        }

        if (inventoryManager != null) inventoryManager.onDisable();

        if (configManager != null) configManager.saveData();
    }

    public void reload() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        configManager.reloadFiles();

        inventoryManager.onDisable();
        inventoryManager.onEnable(this);

        getCommandManager().reload();

        moduleManager.loadModules(this);
    }

    public static synchronized void setInstance(AkropolisPlugin instance) {
        if (plugin == null) {
            plugin = instance;
        }
    }

    public static synchronized AkropolisPlugin getInstance() {
        return plugin;
    }


    public HologramManager getHologramManager() {
        return (HologramManager) moduleManager.getModule(ModuleType.HOLOGRAMS);
    }

    public HooksManager getHookManager() {
        return hooksManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}