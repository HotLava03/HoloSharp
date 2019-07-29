package io.github.hotlava03.collectibles.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;


public class UserInputUtil {

    private Plugin plugin;

    public UserInputUtil(Plugin instance) {
        plugin = instance;
    }

    private final String PLAYER_NOT_FOUND = Colors.fixCodes("&cError &8&l\u00BB &7Player not found.");
    private final String FULL_INVENTORY = Colors.fixCodes("&cError &8&l\u00BB &7Inventory is full.");

    public void openSignGui(Player player, ItemStack collectible) {
        SignGui signGui = new SignGui();
        signGui.open(player, new String[4], (player1, lines) -> {

            // line text removed to allow using larger names

            Player toGiveColl = null;
            for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
                if (currentPlayer.getName().equalsIgnoreCase(String.join("",lines))) {
                    toGiveColl = currentPlayer;
                    break;
                }
            }

            signGui.destroy();

            if (toGiveColl == null) {
                player1.sendMessage(PLAYER_NOT_FOUND);
                return;
            }

            if (toGiveColl.getInventory().firstEmpty() == -1) {
                player.sendMessage(FULL_INVENTORY);
                return;
            }

            // Send the title to the user
            Title title = new Title();
            title.setTitle(Colors.fixCodes("&9Congratulations, &7" + toGiveColl.getName() + "&9!"));
            title.setSubtitle(
                    "&9You got &7" + collectible.getItemMeta().getDisplayName()
            );
            title.send(toGiveColl);

            toGiveColl.getInventory().addItem(collectible);
        });
    }
}
