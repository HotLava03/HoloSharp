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
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.massivecore.ps.PS;
import io.github.hotlava03.holosharp.commands.HoloSharpCmd;
import io.github.hotlava03.holosharp.commands.HoloSharpTabComplete;
import io.github.hotlava03.holosharp.config.Messages;
import io.github.hotlava03.holosharp.util.HologramIdentification;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.github.hotlava03.holosharp.config.Messages.*;

public final class HoloSharp extends JavaPlugin implements Listener {

    public static FileConfiguration holograms;
    public static FileConfiguration messages;
    public static Economy econ;
    public static File file;
    public static File file2;

    @Override
    @SuppressWarnings("Duplicates")
    public void onEnable(){
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
        if(!file.exists()) saveResource("holograms.yml",false);
        if(!file2.exists() || messages.get("prefix") == null){
            getLogger().info("Setting up plugin messages...");
            saveResource("messages.yml",true);
            getLogger().info("Plugin messages set.");
        }
        messages = YamlConfiguration.loadConfiguration(file2);
        holograms = YamlConfiguration.loadConfiguration(file);
        setupHolograms();
        this.getCommand("holosharp").setExecutor(new HoloSharpCmd(this));
        this.getCommand("holosharp").setTabCompleter(new HoloSharpTabComplete());
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info(ChatColor.GREEN + "Plugin loaded successfully.");
    }

    @SuppressWarnings("Duplicates")
    public void reload(){
        for(Hologram hologram : HologramsAPI.getHolograms(this)){
            hologram.clearLines();
            hologram.delete();
        }
        reloadConfig();
        if(!file.exists()) saveResource("holograms.yml",false);
        if(!file2.exists() || messages.get("prefix") == null){
            getLogger().info("Setting up plugin messages...");
            saveResource("messages.yml",true);
            getLogger().info("Plugin messages set.");
        }
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
    @EventHandler
    public void onDisbandFac(EventFactionsDisband event) {

        Faction faction = event.getFaction();
        List<Chunk> chunks = new ArrayList<>();
        for(PS ps : BoardColl.get().getChunks(faction))
            chunks.add(ps.asBukkitChunk());
        if(chunks.isEmpty()) return;
        int amt = HologramIdentification.deleteAll(chunks);
        if(amt == 0)
            return;
        else
            event.getMPlayer().getPlayer().sendMessage(PREFIX + DISBANDED + amt);
        getLogger().info("As the faction "+faction.getName()+" was disbanded, all holograms from it have been deleted.");

    }

    @EventHandler
    public void onClaimChange(EventFactionsChunksChange event) {
        Set<PS> psChunks = event.getChunks();
        List<Chunk> chunks = new ArrayList<>();
        for(PS ps : psChunks)
            chunks.add(ps.asBukkitChunk());
        int amt = HologramIdentification.deleteAll(chunks);
        if(amt == 0)
            return;
        else
            event.getMPlayer().getPlayer().sendMessage(PREFIX + CLAIM_UNCLAIMED + amt);
        getLogger().info("As the chunk " + chunks.get(0).getX() + " " + chunks.get(0).getZ() + " was claimed/unclaimed, all holograms from it have been deleted");
    }

    @EventHandler
    public void onMembershipChange(EventFactionsMembershipChange event) {
        int i = 0;
        String playerName;
        try {
            playerName = event.getMPlayer().getPlayer().getName();
        }catch(NullPointerException e) {
            return;
        }
        Set<String> ownedHolograms = holograms.getConfigurationSection("holograms."+playerName).getKeys(false);
        if(ownedHolograms.isEmpty()) return;
        for(String ownedHologram : ownedHolograms) {
            Location location;
            try {
                location = HologramIdentification.deleteHologram(playerName, ownedHologram);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            Collection<Hologram> list = HologramsAPI.getHolograms(this);
            for (Hologram holo : list) {
                if (holo.getLocation().equals(location)) {
                    holo.clearLines();
                    holo.delete();
                    i++;
                }
            }
        }
        event.getMPlayer().getPlayer().sendMessage(PREFIX + LEFT_JOINED + i);
        getLogger().info("As the user "+playerName+" joined/left a faction, all their holograms have been deleted.");
    }

}
