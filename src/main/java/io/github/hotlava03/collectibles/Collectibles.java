package io.github.hotlava03.collectibles;

import io.github.hotlava03.collectibles.commands.CollectiblesCmd;
import io.github.hotlava03.collectibles.gui.GUIHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Collectibles extends JavaPlugin {

    public static FileConfiguration collectibles;
    public static File file;
    public static FileConfiguration config;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        reloadConfig();
        config = getConfig();


        file = new File(getDataFolder(), "collectibles.yml");
        if(!(file.exists()))
            saveResource("collectibles.yml",true);

        saveResource("ids.txt",true);

        collectibles = YamlConfiguration.loadConfiguration(file);

        this.getServer().getPluginManager().registerEvents(new GUIHandler(this, null), this);
        this.getCommand("collectibles").setExecutor(new CollectiblesCmd(this));
    }

    public void reload() {
        reloadConfig();
        config = getConfig();

        file = new File(getDataFolder(), "collectibles.yml");
        if(!(file.exists()))
            saveResource("collectibles.yml",true);

        collectibles = YamlConfiguration.loadConfiguration(file);

        saveResource("ids.txt", true);

        getLogger().info("Reloaded Collectibles' config.");
    }
}
