package exsistory.dev.hubpvp;

import exsistory.dev.hubpvp.api.HubPVPAPIImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class HubPVP extends JavaPlugin {

    private static HubPVP instance;
    private PVPManager pvpManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        pvpManager = new PVPManager(this);

        new HubPVPAPIImpl(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PVPListener(this), this);

        getCommand("hubpvp").setExecutor(new PVPCommand(this));
        getCommand("pvp").setExecutor(new PVPCommand(this));

        if (pm.getPlugin("PlaceholderAPI") != null) {
            new PVPPlaceholders(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        getLogger().info("HubPVP v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (pvpManager != null) {
            pvpManager.disableAllPVP();
        }

        HubPVPAPIImpl.cleanup();

        getLogger().info("HubPVP has been disabled!");
    }

    public static HubPVP getInstance() {
        return instance;
    }

    public PVPManager getPVPManager() {
        return pvpManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}