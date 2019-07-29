package io.github.hotlava03.collectibles.commands;

import io.github.hotlava03.collectibles.Collectibles;
import io.github.hotlava03.collectibles.gui.GUIHandler;
import io.github.hotlava03.collectibles.util.Colors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CollectiblesCmd implements CommandExecutor {

    private Collectibles plugin;

    public CollectiblesCmd(Collectibles instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Console can't open GUIs...");
            return true;
        }

        Player player = (Player) sender;

        if (!(player.hasPermission("collectibles.view") || player.hasPermission("collectibles.admin"))) {
            player.sendMessage(Colors.fixCodes("&cError &8&l\u00BB &7You do not have permission to do this."));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && player.hasPermission("collectibles.admin")) {
                plugin.reload();
                player.sendMessage(Colors.fixCodes("&aSuccess &8&l\u00BB &7Reloaded the configuration files."));
            }else {
                player.sendMessage(Colors.fixCodes("&6Collectibles &8&l\u00BB &7Collectibles &f1.0-SNAPSHOT &7by &fHotLava03&7."));
            }
            return true;
        }

        GUIHandler handler = new GUIHandler(plugin, player);
        Inventory inv = handler.open();
        if (!(inv == null))
            player.openInventory(inv);

        return true;
    }
}
