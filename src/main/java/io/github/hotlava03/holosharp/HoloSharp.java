/*
 * Copyright (c) 2019 HotLava03.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.hotlava03.holosharp;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.hotlava03.holosharp.commands.HoloSharpCmd;
import io.github.hotlava03.holosharp.config.Messages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HoloSharp extends JavaPlugin implements Listener {

    public static FileConfiguration holograms;
    public static FileConfiguration messages;
    public static Economy econ;
    public static File file;
    public static File file2;

    @Override
    public void onEnable(){
        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") || !Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            getLogger().warning("HolographicDisplays/Factions is/are not enabled or installed. Please install this/these plugin(s) in order for HoloSharp to enable.");
            getLogger().warning("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
        if (!setupEconomy()) {
            getLogger().warning("Vault not found. Please add Vault in order for HoloSharp to load.");
            getLogger().warning("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
        saveDefaultConfig();
        reloadConfig();
        file = new File(getDataFolder(),"holograms.yml");
        file2 = new File(getDataFolder(),"messages.yml");
        messages = YamlConfiguration.loadConfiguration(file2);
        holograms = YamlConfiguration.loadConfiguration(file);
        if(!file.exists()) saveResource("holograms.yml",true);
        if(!file2.exists() || messages.get("prefix") == null){
            getLogger().info("Setting up plugin messages...");
            saveResource("messages.yml",true);
            getLogger().info("Plugin messages set.");
        }
        messages = YamlConfiguration.loadConfiguration(file2);
        holograms = YamlConfiguration.loadConfiguration(file);
        setupHolograms();
        this.getCommand("holosharp").setExecutor(new HoloSharpCmd(this));
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info(ChatColor.GREEN + "Plugin loaded successfully.");
    }

    public void reload(){
        for(Hologram hologram : HologramsAPI.getHolograms(this)){
            hologram.clearLines();
            hologram.delete();
        }
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(file2);
        holograms = YamlConfiguration.loadConfiguration(file);
        Messages.reload();
        setupHolograms();
        getLogger().info("Successfully reloaded HoloSharp.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void setupHolograms() {
        if (holograms.getConfigurationSection("holograms") == null) {
            saveResource("holograms.yml",true);
            return;
        }
        // Get players
        Set<String> players = holograms.getConfigurationSection("holograms").getKeys(false);
        // Initialize the set of names of the holograms from each player
        List<Set<String>> names = new ArrayList<>();
        // Complete
        for(String str : players)
            names.add(holograms.getConfigurationSection("holograms."+str).getKeys(false));
        // If there are no holograms just return
        if(names.isEmpty()) return;
        for(String player : players) {
            for (Set<String> nameList : names) {
                for (String name : nameList) {
                    Location location = (Location) holograms.get(("holograms." + player + "." + name + ".coordinates"));
                    if (location == null) continue;
                    List<String> list = holograms.getStringList("holograms." + player + "." + name + ".lines");
                    Hologram currentHologram = HologramsAPI.createHologram(this, location);
                    for (String line : list)
                        currentHologram.appendTextLine(line);
                }
            }
        }
        getLogger().info("Process of adding holograms finished. Check above for errors.");
    }
    /*@EventHandler
    public void onDisbandFac(EventFactionsDisband event) throws IOException {
        // TODO fix on future version
        Faction faction = event.getFaction();
        List<Player> players = new ArrayList<>();
        for (MPlayer player : faction.getMPlayers())
            players.add(player.getPlayer());
        for (Player player : players) {
            Set<String> names;
            try {
                names = HoloSharp.holograms.getConfigurationSection("holograms." + player).getKeys(false);
            }catch(NullPointerException e){
                continue;
            }
            for (String name : names) {
                List<Location> allLocations = (List<Location>) HoloSharp.holograms.getList("holograms." + player + "." + name + "coordinates");
                for (Location location : allLocations) {
                    Set<PS> psSet = BoardColl.get().getChunks(faction);
                    for (PS ps : psSet)
                        if (location.getChunk().equals(ps.getChunk().asBukkitChunk()))
                            HologramIdentification.deleteHologram(player, name);
                }
            }
        }
    }*/

}
