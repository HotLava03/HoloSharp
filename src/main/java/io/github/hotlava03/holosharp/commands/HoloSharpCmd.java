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

package io.github.hotlava03.holosharp.commands;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import io.github.hotlava03.holosharp.HoloSharp;
import io.github.hotlava03.holosharp.config.Messages;
import io.github.hotlava03.holosharp.util.HologramIdentification;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class HoloSharpCmd implements CommandExecutor {

    private HoloSharp plugin;

    public HoloSharpCmd(HoloSharp instance) {
        plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command Command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[HoloSharp] Sorry, this command can only be ran by in-game players.");
            return true;
        }
        Player player = (Player) sender;
        // If this permission isn't added to the user, they have no permission to use the plugin at all
        if(!player.hasPermission("holosharp.user.hs")){
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_PERMS);
            return true;
        }
        Location coords = player.getLocation();
        coords.setY(player.getLocation().getBlockY() + 2);
        if (args.length == 0 && !player.hasPermission("holosharp.staff.help")) {
            player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE);
            return true;
        } else if (args.length == 0) {
            player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE_ADMIN);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create":
            case "buy":
                buyHologram(player,coords,args);
                break;
            case "delete":
            case "remove":
            case "del":
                deleteHologram(player,args);
                break;
            case "addline":
                addLine(player,args);
                break;
            case "list":
            case "hololist":
                list(player,args);
                break;
            case "movehere":
                moveHere(player,coords,args);
                break;
            case "reload":
                reloadPlugin(player);
                break;
            case "help":
                sendDetailedHelp(player,args);
                break;
            case "transfer":
                transferHologram(player,args);
                break;
            case "transferall":
                transferAll(player,args);
                break;
            default:
                if (!player.hasPermission("holosharp.staff.help"))
                    player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE);
                else
                    player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE_ADMIN);

        }
        return true;
    }

    /*
        The sub-command you start with.
        Takes a certain amount of money edited in config.yml.
        Spawns a new hologram.
        Adds first line only.
        To add more lines user runs /hs addline <params>
    */
    private void buyHologram(Player player, Location coords, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Messages.PREFIX + Messages.BUY_HELP);
            return;
        }
        if (!MPlayer.get(player).hasFaction()) { // Even if player is in Wilderness, adding holograms there won't be allowed
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_FACTION);
            return;
        }
        if (!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk()))) && !player.hasPermission("holosharp.staff.bypass")) { // When a player is trying to add a hologram outside their faction
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_ADD_HOLOGRAM_HERE);
            return;
        }else if(!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk()))) && player.hasPermission("holosharp.staff.bypass"))
            player.sendMessage(Messages.STAFF_PREFIX+Messages.BYPASSED_CHUNKS);
        if (HoloSharp.econ.getBalance(player) <= plugin.getConfig().getDouble("costPerHologram") && !player.hasPermission("holosharp.staff.bypass")) { // If a player doesn't have enough money
            player.sendMessage(Messages.ERROR_PREFIX+Messages.BAD_BAL+plugin.getConfig().getDouble("costPerHologram"));
            return;
        }else if(HoloSharp.econ.getBalance(player) <= plugin.getConfig().getDouble("costPerHologram") && player.hasPermission("holosharp.staff.bypass"))
            player.sendMessage(Messages.STAFF_PREFIX+Messages.BYPASSED_BAL);
        if (!(HoloSharp.holograms.get("holograms." + player.getName() + "." + args[1] + ".coordinates") == null)) {
            player.sendMessage(Messages.ERROR_PREFIX+Messages.EXISTS_ALREADY);
            return;
        }
        Hologram hologram = HologramsAPI.createHologram(plugin, coords);
        TextLine line = hologram.insertTextLine(0, getTxt(1,args).replaceAll("&", "\u00a7"));
        List<String> lineList = new ArrayList<>();
        lineList.add(line.getText());
        try {
            if (!HologramIdentification.saveHologram(player, hologram, args[1], lineList)) {
                player.sendMessage(Messages.ERROR_PREFIX+Messages.EXISTS_ALREADY);
                return;
            }
        } catch (IOException e) {
            player.sendMessage(Messages.ERROR_PREFIX+Messages.FATAL);
            e.printStackTrace();
            return;
        }
        HoloSharp.econ.withdrawPlayer(player, plugin.getConfig().getDouble("costPerHologram")); // take money, varies in the config.yml
        player.sendMessage(Messages.PREFIX+Messages.BUY_SUCCESS+plugin.getConfig().getDouble("costPerHologram"));
    }

    /*
        Called on sub-command.
        Deletes the hologram with the name args[1] from player player.
        If the user is staff, then args[1] MAY be a user name, but that will
        be verified in the beginning through the args length.
    */
    private void deleteHologram(Player player, String[] args) {
        String name;
        String owner;
        if (args.length > 2 && player.hasPermission("holosharp.staff.deleteOther")){
            name = args[2];
            owner = args[1];
        } else if(args.length > 1){
            name = args[1];
            owner = player.getName();
        }
        else{
            if(!player.hasPermission("holosharp.staff.deleteOther"))
                player.sendMessage(Messages.PREFIX+Messages.REMOVE_HELP);
            else
                player.sendMessage(Messages.PREFIX+Messages.REMOVE_OTHER_HELP);
            return;
        }
        Location location;
        try {
            location = HologramIdentification.deleteHologram(owner, name);
        } catch (IOException e) {
            player.sendMessage(Messages.ERROR_PREFIX+Messages.FATAL);
            e.printStackTrace();
            return;
        }
        if (location == null) {
            player.sendMessage(Messages.ERROR_PREFIX + Messages.NOT_FOUND);
            return;
        }
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                holo.clearLines();
                holo.delete();
                player.sendMessage(Messages.PREFIX + Messages.DELETED_SUCCESS);
                return;
            }
        }
        player.sendMessage(Messages.ERROR_PREFIX + Messages.NOT_FOUND);
    }

    /*
        Admin/Staff only.
        Reloads plugin config.
        reload() is a method in the instance.
    */
    private void reloadPlugin(Player player) {
        if (!player.hasPermission("holosharp.staff.reload")) {
            player.sendMessage(Messages.ERROR_PREFIX + Messages.NO_PERMS);
            return;
        }
        plugin.reload();
        player.sendMessage(Messages.PREFIX + Messages.RELOAD_SUCCESS);
    }

    /*
        As simple as adding a line.
        Called on sub-command.
    */
    private void addLine(Player player, String[] args) {
        String playerName = player.getName();
        if (args.length < 2) {
            player.sendMessage(Messages.PREFIX + Messages.ADDLINE_HELP);
            return;
        }
        Location location = HologramIdentification.getLocation(playerName, args[1]);
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        boolean success = false;
        String newLine = "";
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                newLine = holo.appendTextLine(getTxt(1,args).replace("&", "\u00a7")).getText();
                success = true;
            }
        }
        List<String> lines = HoloSharp.holograms.getStringList("holograms." + playerName + "." + args[1] + ".lines");
        lines.add(newLine);
        if (!success) {
            player.sendMessage(Messages.PREFIX + Messages.NOT_FOUND);
            return;
        }
        HologramIdentification.addLine(player, args[1], lines);
        player.sendMessage(Messages.PREFIX + Messages.ADDLINE_SUCCESS + args[1]);
    }

    /*
        Called on sub-command execution.
        Mostly verifies if the user is staff or not... And if the args are greater than 1.
    */
    private void list(Player player, String[] args) {
        if (player.hasPermission("holosharp.staff.listOther") && args.length > 1) {
            sendList(args[1],player);
            return;
        }
        sendList(player.getName(),player);
    }

    /*
        Called for the /hs list command.
        Universal for both staff and players, called method is above for the command.
    */
    private void sendList(String playerName, Player player) {
        Set<String> names;
        try {
            names = HoloSharp.holograms.getConfigurationSection("holograms." + playerName).getKeys(false);
        } catch (NullPointerException e) {
            player.sendMessage(Messages.ERROR_PREFIX + Messages.PLAYER_NOT_FOUND);
            return;
        }
        List<String> elements = new ArrayList<>();
        for (String name : names) {
            Location location = (Location) HoloSharp.holograms.get(("holograms." + playerName + "." + name + ".coordinates"));
            if(location == null)
                continue;
            elements.add(Messages.LIST_PREFIX + name + Messages.LIST_ELEMENT_SEP + Math.round(location.getX()) + " " + Math.round(location.getY()) + " " + Math.round(location.getZ()) + Messages.LIST_ELEMENT_SEP2 + location.getWorld().getName());
        }
        StringBuilder sb = new StringBuilder();
        for (String element : elements)
            sb.append(element).append("\n");
        player.sendMessage(Messages.LIST_HEADER_1 + playerName + Messages.LIST_HEADER_2 + "\n" + sb + Messages.LIST_END);
    }

    /*
        Moves the hologram to the user's location.
        If the requirements aren't met, then the method will return (void).
    */
    private void moveHere(Player player, Location coords, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Messages.PREFIX + Messages.MOVE_HERE_HELP);
            return;
        }
        Location location = HologramIdentification.getLocation(player.getName(), args[1]);
        if (location == null) {
            player.sendMessage(Messages.ERROR_PREFIX + Messages.NOT_FOUND);
            return;
        }
        if (!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk())))) { // When a player is trying to add a hologram outside their faction
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_ADD_HOLOGRAM_HERE);
            return;
        }
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                List<String> lines = HoloSharp.holograms.getStringList("holograms."+player.getName()+"."+args[1]+".lines");
                try {
                    HologramIdentification.deleteHologram(player.getName(), args[1]);
                } catch (IOException e) {
                    player.sendMessage(Messages.ERROR_PREFIX+Messages.FATAL);
                    e.printStackTrace();
                    return;
                }
                holo.clearLines();
                holo.delete();
                // Now that the old hologram is deleted, we create the new one
                Hologram newHologram = HologramsAPI.createHologram(plugin, coords);
                for(String line : lines)
                    newHologram.appendTextLine(line);
                try {
                    HologramIdentification.saveHologram(player,newHologram,args[1],lines);
                } catch (IOException e) {
                    player.sendMessage(Messages.ERROR_PREFIX+Messages.FATAL);
                    e.printStackTrace();
                    return;
                }
                player.sendMessage(Messages.PREFIX+Messages.MOVE_SUCCESS);
                return;
            }
        }
        player.sendMessage(Messages.PREFIX + Messages.NOT_FOUND);
    }

    /*
        Transfers a given hologram name to another player.
        Use transferAll() for a full transfer.
    */
    private void transferHologram(Player player, String[] args){
        if(!player.hasPermission("holosharp.staff.transfer")){
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_PERMS);
            return;
        }
        if(args.length < 4) {
            player.sendMessage(Messages.PREFIX+Messages.TRANSFER_HELP);
            return;
        }
        if(!HologramIdentification.transfer(args[1],args[2],args[3])){
            player.sendMessage(Messages.ERROR_PREFIX+Messages.NOT_FOUND);
            return;
        }
        player.sendMessage(Messages.STAFF_PREFIX+Messages.TRANSFER_SUCCESS+args[3]);
    }

    /*
        Transfers a player's holograms to another.
        Can be used in an account transfer, I assume?
    */
    private void transferAll(Player player, String[] args){
        if(args.length < 3){
            player.sendMessage(Messages.ERROR_PREFIX+Messages.TRANSFER_ALL_HELP);
            return;
        }
        Set<String> keys = HoloSharp.holograms.getConfigurationSection("holograms."+args[1]).getKeys(false);
        for(String key : keys)
            HologramIdentification.transfer(key,args[1],args[2]);
        player.sendMessage(Messages.PREFIX+Messages.TRANSFER_ALL_SUCCESS+args[2]);
    }

    /*
        This method sends the user information about each sub-command.
        In case of having no command found, the default message will be sent.
    */
    private void sendDetailedHelp(Player player, String[] args) {
        if(args.length == 1) {
            if (player.hasPermission("holosharp.staff.help"))
                player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE_ADMIN);
            else
                player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "buy":
                player.sendMessage(Messages.PREFIX + Messages.BUY_HELP);
                break;
            case "remove":
            case "delete":
            case "del":
                if (!player.hasPermission("holosharp.staff.help")) {
                    player.sendMessage(Messages.PREFIX + Messages.REMOVE_HELP);
                    break;
                }
                player.sendMessage(Messages.PREFIX + Messages.REMOVE_OTHER_HELP);
                break;
            case "addline":
                player.sendMessage(Messages.PREFIX + Messages.ADDLINE_HELP);
                break;
            case "movehere":
                player.sendMessage(Messages.PREFIX + Messages.MOVE_HERE_HELP);
                break;
            case "about":
                player.sendMessage(Messages.PREFIX + Messages.ABOUT_HELP);
                break;
            case "hololist":
                if (!player.hasPermission("holosharp.listOther")) {
                    player.sendMessage(Messages.PREFIX + Messages.HOLOLIST_HELP);
                    break;
                }
                player.sendMessage(Messages.PREFIX + Messages.HOLOLISTOTHER_HELP);
                break;
            case "transfer":
                if(!player.hasPermission("holosharp.staff.transfer")){
                    player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_PERMS);
                    break;
                }
                player.sendMessage(Messages.PREFIX+Messages.TRANSFER_HELP);
                break;
            case "transferall":
                if(!player.hasPermission("holosharp.staff.transferAll")){
                    player.sendMessage(Messages.ERROR_PREFIX+Messages.NO_PERMS);
                    break;
                }
                player.sendMessage(Messages.PREFIX+Messages.TRANSFER_ALL_HELP);
                break;
            default:
                if (!player.hasPermission("holosharp.staff.help"))
                    player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE);
                else
                    player.sendMessage(Messages.PREFIX + Messages.CMD_USAGE_ADMIN);
        }
    }

    /*
        EXTRAS (UTILITY METHODS AND GAME-UNRELATED)
    */

    /*
        Used when necessary to get text that contains spaces in command arguments.
        Basically a utility method in the middle of nowhere :P
    */
    private String getTxt(int from, String[] args) {
        String txt = "";
        int i = 0;
        for (String arg : args) {
            if (i <= from) {
                i++;
                continue;
            }
            if (i++ == args.length - 1) { // prevents from adding a space in the last sentence
                txt = txt.concat(arg);
                continue;
            }
            txt = txt.concat(arg).concat(" ");
        }
        return txt;
    }
}