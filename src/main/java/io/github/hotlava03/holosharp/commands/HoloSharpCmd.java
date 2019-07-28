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
import io.github.hotlava03.holosharp.util.HologramIdentification;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.github.hotlava03.holosharp.config.Messages.*;


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
        if (!player.hasPermission("holosharp.user.hs")) {
            player.sendMessage(ERROR_PREFIX + NO_PERMS);
            return true;
        }
        Location coords = player.getLocation();
        coords.setY(player.getLocation().getBlockY() + 2);
        if (args.length == 0 && !player.hasPermission("holosharp.staff.help")) {
            player.sendMessage(PREFIX + CMD_USAGE);
            return true;
        } else if (args.length == 0) {
            player.sendMessage(PREFIX + CMD_USAGE_ADMIN);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "buy":
                buyHologram(player, coords, args);
                break;
            case "delete":
            case "remove":
            case "del":
                deleteHologram(player, args);
                break;
            case "addline":
                addLine(player, args);
                break;
            case "list":
            case "hololist":
                list(player, args);
                break;
            case "movehere":
                moveHere(player, coords, args);
                break;
            case "reload":
                reloadPlugin(player);
                break;
            case "help":
                sendDetailedHelp(player, args);
                break;
            case "transfer":
                transferHologram(player, args);
                break;
            case "transferall":
                transferAll(player, args);
                break;
            case "removeline":
            case "deleteline":
                deleteLine(player, args);
                break;
            case "insertline":
                insertLine(player, args);
                break;
            case "create":
                createHologram(player, args);
                break;
            case "about":
                player.sendMessage("\u00a75Holo# \u00a78\u00a7l\u00bb \u00a77Holo# by \u00a7fHotLava03\u00a77. Version \u00a7f1.0\u00a77.");
                break;
            default:
                if (!player.hasPermission("holosharp.staff.help"))
                    player.sendMessage(PREFIX + CMD_USAGE);
                else
                    player.sendMessage(PREFIX + CMD_USAGE_ADMIN);

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
            player.sendMessage(PREFIX + BUY_HELP);
            return;
        }
        if (!MPlayer.get(player).hasFaction()) { // Even if player is in Wilderness, adding holograms there won't be allowed
            player.sendMessage(ERROR_PREFIX + NO_FACTION);
            return;
        }
        if (!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk()))) && !player.hasPermission("holosharp.staff.bypass")) { // When a player is trying to add a hologram outside their faction
            player.sendMessage(ERROR_PREFIX + NO_ADD_HOLOGRAM_HERE);
            return;
        } else if (!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk()))) && player.hasPermission("holosharp.staff.bypass"))
            player.sendMessage(STAFF_PREFIX + BYPASSED_CHUNKS);
        if (HoloSharp.econ.getBalance(player) <= plugin.getConfig().getDouble("costPerHologram") && !player.hasPermission("holosharp.staff.bypass")) { // If a player doesn't have enough money
            player.sendMessage(ERROR_PREFIX + BAD_BAL + plugin.getConfig().getDouble("costPerHologram"));
            return;
        } else if (HoloSharp.econ.getBalance(player) <= plugin.getConfig().getDouble("costPerHologram") && player.hasPermission("holosharp.staff.bypass"))
            player.sendMessage(STAFF_PREFIX + BYPASSED_BAL);
        if (!(HoloSharp.holograms.get("holograms." + player.getName() + "." + args[1] + ".coordinates") == null)) {
            player.sendMessage(ERROR_PREFIX + EXISTS_ALREADY);
            return;
        }
        if (tooManyHolograms(player) && !player.hasPermission("holosharp.staff.bypass")) {
            player.sendMessage(ERROR_PREFIX + TOO_MANY_HOLOGRAMS);
            return;
        } else if (tooManyHolograms(player) && player.hasPermission("holosharp.staff.bypass"))
            player.sendMessage(STAFF_PREFIX + BYPASSED_LIMIT);
        if (!createHologram(coords, player, getTxt(1, args), args[1], player.getName())) return;
        HoloSharp.econ.withdrawPlayer(player, plugin.getConfig().getDouble("costPerHologram")); // take money, varies in the config.yml
        player.sendMessage(PREFIX + BUY_SUCCESS + plugin.getConfig().getDouble("costPerHologram"));
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
        if (args.length > 2 && player.hasPermission("holosharp.staff.deleteOther")) {
            name = args[2];
            owner = args[1];
        } else if (args.length > 1) {
            name = args[1];
            owner = player.getName();
        } else {
            if (!player.hasPermission("holosharp.staff.deleteOther"))
                player.sendMessage(PREFIX + REMOVE_HELP);
            else
                player.sendMessage(PREFIX + REMOVE_OTHER_HELP);
            return;
        }
        Location location;
        try {
            location = HologramIdentification.deleteHologram(owner, name);
        } catch (IOException e) {
            player.sendMessage(ERROR_PREFIX + FATAL);
            e.printStackTrace();
            return;
        }
        HoloSharp.messages = YamlConfiguration.loadConfiguration(HoloSharp.file);
        if (location == null) {
            player.sendMessage(ERROR_PREFIX + NOT_FOUND);
            return;
        }
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                holo.clearLines();
                holo.delete();
                player.sendMessage(PREFIX + DELETED_SUCCESS);
                return;
            }
        }
        player.sendMessage(ERROR_PREFIX + NOT_FOUND);
    }

    /*
        Admin/Staff only.
        Reloads plugin config.
        reload() is a method in the instance.
    */
    private void reloadPlugin(Player player) {
        if (!player.hasPermission("holosharp.staff.reload")) {
            player.sendMessage(ERROR_PREFIX + NO_PERMS);
            return;
        }
        plugin.reload();
        player.sendMessage(PREFIX + RELOAD_SUCCESS);
    }

    /*
        As simple as adding a line.
        Called on sub-command.
    */
    private void addLine(Player player, String[] args) {
        String playerName = player.getName();
        if (args.length < 2) {
            player.sendMessage(PREFIX + ADDLINE_HELP);
            return;
        }
        Location location = HologramIdentification.getLocation(playerName, args[1]);
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        boolean success = false;
        String newLine = "";
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                newLine = holo.appendTextLine(getTxt(1, args).replace("&", "\u00a7")).getText();
                success = true;
            }
        }
        List<String> lines = HoloSharp.holograms.getStringList("holograms." + playerName + "." + args[1] + ".lines");
        lines.add(newLine);
        if (!success) {
            player.sendMessage(PREFIX + NOT_FOUND);
            return;
        }
        try {
            HologramIdentification.addLine(player, args[1], lines);
        } catch (IOException e) {
            player.sendMessage(ERROR_PREFIX + FATAL);
            return;
        }
        HoloSharp.messages = YamlConfiguration.loadConfiguration(HoloSharp.file);
        player.sendMessage(PREFIX + ADDLINE_SUCCESS + args[1]);
    }

    /*
        Called on sub-command execution.
        Mostly verifies if the user is staff or not... And if the args are greater than 1.
    */
    private void list(Player player, String[] args) {
        if (player.hasPermission("holosharp.staff.listOther") && args.length > 1) {
            sendList(args[1], player);
            return;
        }
        sendList(player.getName(), player);
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
            player.sendMessage(ERROR_PREFIX + PLAYER_NOT_FOUND);
            return;
        }
        List<String> elements = new ArrayList<>();
        for (String name : names) {
            Location location = (Location) HoloSharp.holograms.get(("holograms." + playerName + "." + name + ".coordinates"));
            if (location == null)
                continue;
            elements.add(LIST_PREFIX + name + LIST_ELEMENT_SEP + Math.round(location.getX()) + " " + Math.round(location.getY()) + " " + Math.round(location.getZ()) + LIST_ELEMENT_SEP2 + location.getWorld().getName());
        }
        if (!elements.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String element : elements)
                sb.append(element).append("\n");
            player.sendMessage(LIST_HEADER_1 + playerName + LIST_HEADER_2 + "\n" + sb + LIST_END);
        } else
            player.sendMessage(LIST_HEADER_1 + playerName + LIST_HEADER_2 + "\n" + "\u00a77\u00a7o(no holograms for this user)" + LIST_END);

    }

    /*
        Moves the hologram to the user's location.
        If the requirements aren't met, then the method will return (void).
    */
    private void moveHere(Player player, Location coords, String[] args) {
        if (args.length < 2) {
            player.sendMessage(PREFIX + MOVE_HERE_HELP);
            return;
        }
        Location location = HologramIdentification.getLocation(player.getName(), args[1]);
        if (location == null) {
            player.sendMessage(ERROR_PREFIX + NOT_FOUND);
            return;
        }
        if (!MPlayer.get(player).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(coords.getChunk())))) { // When a player is trying to add a hologram outside their faction
            player.sendMessage(ERROR_PREFIX + NO_ADD_HOLOGRAM_HERE);
            return;
        }
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                List<String> lines = HoloSharp.holograms.getStringList("holograms." + player.getName() + "." + args[1] + ".lines");
                try {
                    HologramIdentification.deleteHologram(player.getName(), args[1]);
                } catch (IOException e) {
                    player.sendMessage(ERROR_PREFIX + FATAL);
                    e.printStackTrace();
                    return;
                }
                holo.clearLines();
                holo.delete();
                // Now that the old hologram is deleted, we create the new one
                Hologram newHologram = HologramsAPI.createHologram(plugin, coords);
                for (String line : lines)
                    newHologram.appendTextLine(line);
                try {
                    HologramIdentification.saveHologram(player.getName(), newHologram.getLocation(), args[1], lines);
                } catch (IOException e) {
                    player.sendMessage(ERROR_PREFIX + FATAL);
                    e.printStackTrace();
                    return;
                }
                player.sendMessage(PREFIX + MOVE_SUCCESS);
                return;
            }
        }
        player.sendMessage(PREFIX + NOT_FOUND);
    }

    /*
        Inserts text into a line of the hologram.
        Example: the hologram has 3 lines, if the user types
        /hs insertline <name> 0 <text>, it will add the line as
        the first one and all others go down.
    */
    private void insertLine(Player player, String[] args) {
        if (!(args.length > 3)) {
            player.sendMessage(PREFIX + INSERT_LINE_HELP);
            return;
        }
        int lineNum;
        try {
            lineNum = Integer.valueOf(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ERROR_PREFIX + INVALID_NUM);
            return;
        }

        Location location = HologramIdentification.getLocation(player.getName(), args[1]);
        List<String> oldLines = HoloSharp.holograms.getStringList("holograms." + player.getName() + "." + args[1] + ".lines");
        List<String> newLines = new ArrayList<>();

        if(lineNum > oldLines.size()-1) {
            player.sendMessage(ERROR_PREFIX + NUM_TOO_BIG);
            return;
        }

        int i = 0;
        for (String line : oldLines) {
            if (i++ == lineNum) {
                newLines.add(getTxt(2, args).replace("&", "\u00a7"));
                newLines.add(line);
            } else
                newLines.add(line);
        }
        boolean success = false;


        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list) {
            if (holo.getLocation().equals(location)) {
                holo.clearLines();
                for(String line : newLines)
                    holo.appendTextLine(line);
                success = true;
                break;
            }
        }

        if(!success) {
            player.sendMessage(PREFIX + NOT_FOUND);
            return;
        }

        try {
            HologramIdentification.addLine(player, args[1], newLines);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ERROR_PREFIX + FATAL);
            return;
        }

        HoloSharp.messages = YamlConfiguration.loadConfiguration(HoloSharp.file);

        player.sendMessage(PREFIX + INSERT_LINE_SUCCESS);

    }

    /*
        Transfers a given hologram name to another player.
        Use transferAll() for a full transfer.
    */
    private void transferHologram(Player player, String[] args) {
        if (!player.hasPermission("holosharp.staff.transfer")) {
            player.sendMessage(ERROR_PREFIX + NO_PERMS);
            return;
        }
        if (args.length < 4) {
            player.sendMessage(PREFIX + TRANSFER_HELP);
            return;
        }
        try {
            if (!HologramIdentification.transfer(args[1], args[2], args[3])) {
                player.sendMessage(ERROR_PREFIX + NOT_FOUND);
                return;
            }
        } catch (IOException e) {
            player.sendMessage(ERROR_PREFIX + FATAL);
            return;
        }
        player.sendMessage(STAFF_PREFIX + TRANSFER_SUCCESS + args[3]);
    }

    /*
        Transfers a player's holograms to another.
        Can be used in an account transfer, I assume?
    */
    private void transferAll(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ERROR_PREFIX + TRANSFER_ALL_HELP);
            return;
        }
        Set<String> keys = HoloSharp.holograms.getConfigurationSection("holograms." + args[1]).getKeys(false);
        for (String key : keys) {
            try {
                HologramIdentification.transfer(key, args[1], args[2]);
            } catch (IOException e) {
                player.sendMessage(ERROR_PREFIX + FATAL);
                return;
            }
        }
        player.sendMessage(PREFIX + TRANSFER_ALL_SUCCESS + args[2]);
    }

    /*
        Delete line from a hologram, specified with an integer.
    */
    private void deleteLine(Player player, String[] args) {
        if (args.length < 3) {
            if (player.hasPermission("holosharp.staff.deleteLineOther")) {
                player.sendMessage(PREFIX + DELETE_LINE_OTHER_HELP);
                return;
            }
            player.sendMessage(PREFIX + DELETE_LINE_HELP);
            return;
        }
        String playerName = player.getName();
        String name = args[1];
        int lineNumber;
        try {
            lineNumber = Integer.valueOf(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ERROR_PREFIX + INVALID_NUM);
            return;
        }
        if (player.hasPermission("holosharp.staff.deleteLineOther") && args.length > 3) {
            playerName = args[1];
            name = args[2];
            try {
                lineNumber = Integer.valueOf(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(ERROR_PREFIX + INVALID_NUM);
                return;
            }
        }
        Location location = HologramIdentification.getLocation(playerName, name);
        Collection<Hologram> list = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : list)
            if (holo.getLocation().equals(location)) {
                if (holo.size() == 1 && lineNumber == 0) {
                    player.sendMessage(ERROR_PREFIX + LAST_LINE);
                    return;
                } else if (holo.size() == 1) {
                    player.sendMessage(ERROR_PREFIX + INVALID_LINE);
                    return;
                }
                try {
                    holo.removeLine(lineNumber);
                } catch (IndexOutOfBoundsException e) {
                    player.sendMessage(ERROR_PREFIX + INVALID_LINE);
                    return;
                }
                break;
            }
        try {
            HologramIdentification.removeLine(playerName, name, lineNumber);
        } catch (IOException e) {
            player.sendMessage(ERROR_PREFIX + FATAL);
            return;
        }
        player.sendMessage(PREFIX + DELETE_LINE_SUCCESS + args[1]);
    }

    private void createHologram(Player player, String[] args) {
        if (!player.hasPermission("holosharp.staff.create")) {
            player.sendMessage(ERROR_PREFIX + NO_PERMS);
            return;
        }
        if (args.length < 4) {
            player.sendMessage(PREFIX + CREATE_HELP);
            return;
        }
        Location coords = player.getLocation();
        coords.setY(player.getLocation().getBlockY() + 2);
        if (!createHologram(coords, player, getTxt(2, args), args[2], args[1])) return;
        player.sendMessage(STAFF_PREFIX + CREATE_SUCCESS);
    }

    /*
        This method sends the user information about each sub-command.
        In case of having no command found, the default message will be sent.
    */
    private void sendDetailedHelp(Player player, String[] args) {
        if (args.length == 1) {
            if (player.hasPermission("holosharp.staff.help"))
                player.sendMessage(PREFIX + CMD_USAGE_ADMIN);
            else
                player.sendMessage(PREFIX + CMD_USAGE);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "buy":
                player.sendMessage(PREFIX + BUY_HELP);
                break;
            case "remove":
            case "delete":
            case "del":
                if (!player.hasPermission("holosharp.staff.help")) {
                    player.sendMessage(PREFIX + REMOVE_HELP);
                    break;
                }
                player.sendMessage(PREFIX + REMOVE_OTHER_HELP);
                break;
            case "addline":
                player.sendMessage(PREFIX + ADDLINE_HELP);
                break;
            case "movehere":
                player.sendMessage(PREFIX + MOVE_HERE_HELP);
                break;
            case "about":
                player.sendMessage(PREFIX + ABOUT_HELP);
                break;
            case "hololist":
                if (!player.hasPermission("holosharp.listOther")) {
                    player.sendMessage(PREFIX + HOLOLIST_HELP);
                    break;
                }
                player.sendMessage(PREFIX + HOLOLISTOTHER_HELP);
                break;
            case "transfer":
                if (!player.hasPermission("holosharp.staff.transfer")) {
                    player.sendMessage(ERROR_PREFIX + NO_PERMS);
                    break;
                }
                player.sendMessage(PREFIX + TRANSFER_HELP);
                break;
            case "insertline":
                player.sendMessage(PREFIX + INSERT_LINE_HELP);
                break;
            case "transferall":
                if (!player.hasPermission("holosharp.staff.transferAll")) {
                    player.sendMessage(ERROR_PREFIX + NO_PERMS);
                    break;
                }
                player.sendMessage(PREFIX + TRANSFER_ALL_HELP);
                break;

            case "deleteline":
            case "removeline":
                if (!player.hasPermission("holosharp.staff.deleteLineOther")) {
                    player.sendMessage(PREFIX + DELETE_LINE_HELP);
                    break;
                }
                player.sendMessage(PREFIX + DELETE_LINE_OTHER_HELP);
                break;
            case "create":
                if (!player.hasPermission("holosharp.staff.create")) {
                    player.sendMessage(ERROR_PREFIX + NO_PERMS);
                    break;
                }
                player.sendMessage(PREFIX + CREATE_HELP);
                break;
            default:
                if (!player.hasPermission("holosharp.staff.help"))
                    player.sendMessage(PREFIX + CMD_USAGE);
                else
                    player.sendMessage(PREFIX + CMD_USAGE_ADMIN);
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

    /*
        Checks if the user cannot add any more holograms due to
        the limit given in config.yml.
        If the limit in config.yml is -1, the user can add unlimited
        holograms.
    */
    private boolean tooManyHolograms(Player player) {
        if (plugin.getConfig().getInt("userLimit") == -1) return false;
        if (HoloSharp.holograms.getConfigurationSection("holograms." + player.getName()) == null) return false;
        Set<String> hologramKeys = HoloSharp.holograms.getConfigurationSection("holograms." + player.getName()).getKeys(false);
        return hologramKeys.size() >= plugin.getConfig().getInt("userLimit");
    }

    private boolean createHologram(Location coords, Player player, String text, String name, String playerName) {
        if (!HologramIdentification.hologramExists(player, name)) {
            player.sendMessage(ERROR_PREFIX + EXISTS_ALREADY);
            return false;
        }
        Hologram hologram = HologramsAPI.createHologram(plugin, coords);
        TextLine line = hologram.insertTextLine(0, text.replaceAll("&", "\u00a7"));
        List<String> lineList = new ArrayList<>();
        lineList.add(line.getText());
        try {
            HologramIdentification.saveHologram(playerName, hologram.getLocation(), name, lineList);
        } catch (IOException e) {
            player.sendMessage(ERROR_PREFIX + FATAL);
            e.printStackTrace();
            return false;
        }
        HoloSharp.messages = YamlConfiguration.loadConfiguration(HoloSharp.file);
        return true;
    }
}