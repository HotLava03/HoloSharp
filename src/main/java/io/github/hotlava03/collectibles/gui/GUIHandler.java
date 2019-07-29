package io.github.hotlava03.collectibles.gui;

import io.github.hotlava03.collectibles.Collectibles;
import io.github.hotlava03.collectibles.util.Colors;
import io.github.hotlava03.collectibles.util.UserInputUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GUIHandler implements Listener {

    private Player player;
    private Plugin plugin;
    private FileConfiguration config = Collectibles.config;

    public GUIHandler(Plugin plugin, Player player) {
        this.player = player;
        this.plugin = plugin;
    }

    private Inventory menu = Bukkit.createInventory(
            null,
            54,
            config.getString("server") + "'s collectibles"
    );

    @SuppressWarnings("Duplicates")
    private ItemStack getPageNextItemStack(int i) {
        ItemStack pageNext = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageNext.getItemMeta();
        pageMeta.setDisplayName("\u00a7aNext page");
        List<String> lore = new ArrayList<>();
        lore.add("\u00a77Current page: " + i);
        lore.add("\u00a77Click this to display");
        lore.add("\u00a77the next page.");
        pageMeta.setLore(lore);
        pageMeta.addEnchant(Enchantment.MENDING, 1, false);
        pageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pageNext.setItemMeta(pageMeta);

        return pageNext;
    }

    @SuppressWarnings("Duplicates")
    private ItemStack getPageBackItemStack(int i) {
        ItemStack pagePrev = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pagePrev.getItemMeta();
        pageMeta.setDisplayName("\u00a7cPrevious page");
        List<String> lore = new ArrayList<>();
        lore.add("\u00a77Current page: " + i);
        lore.add("\u00a77Click this to display");
        lore.add("\u00a77the previous page.");
        pageMeta.setLore(lore);
        pageMeta.addEnchant(Enchantment.MENDING, 1, false);
        pageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pagePrev.setItemMeta(pageMeta);

        return pagePrev;
    }

    private ItemStack getDecoGlass() {
        ItemStack deco = new ItemStack(Material.STAINED_GLASS_PANE);
        ItemMeta meta = deco.getItemMeta();
        meta.setDisplayName(" ");
        deco.setItemMeta(meta);
        return deco;
    }

    private ItemStack getCloseBarrier() {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("\u00a7cClose");
        close.setItemMeta(closeMeta);
        return close;
    }

    public Inventory open() {

        Set<String> collectibles = getCollectibles();

        if (collectibles == null) {
            player.sendMessage(Colors.fixCodes("&cError&8 &l\u00BB &7The server has no collectibles."));
            return null;
        }


        List<ItemStack> collectibleList = getAllCollectibles();
        if (collectibleList == null) return null;
        List<ItemStack> pageCollectibles = new ArrayList<>();

        for (int i = 45; i < 54; i++) {
            if (i == 49) {
                menu.setItem(i, getCloseBarrier());
                continue;
            }
            menu.setItem(i, getDecoGlass());
        }

        if (collectibles.size() > 45) {
            menu.setItem(53, getPageNextItemStack(1));
        }

        int i = 0;
        for (ItemStack stack : collectibleList) {
            if (i++ == 45) break;
            pageCollectibles.add(stack);
        }

        for (ItemStack itemStack : pageCollectibles)
            menu.addItem(itemStack);

        return menu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getSlot() < 0) return;
        if (!event.getInventory().getName().equals(menu.getName()) || event.getInventory().getItem(event.getSlot()) == null)
            return;
        Player player = (Player) event.getWhoClicked();
        ItemStack slot = event.getInventory().getItem(event.getSlot());
        event.setCancelled(true);
        UserInputUtil util = new UserInputUtil(plugin);
        if (slot.getItemMeta().getDisplayName().equals("\u00a7cClose")) {
            player.closeInventory();
            return;
        }

        if (slot.getItemMeta().getLore() == null
                && (slot.getItemMeta().getDisplayName().equals("\u00a7aNext page")
                        || (slot.getItemMeta().getDisplayName().equals("\u00a7cPrevious page")))
        )
            return;

        if (slot.getItemMeta().getDisplayName().equals("\u00a7aNext page")) {
            if(slot.getItemMeta().getLore().isEmpty()) return;
            openPage(Integer.parseInt(slot.getItemMeta()
                            .getLore()
                            .get(0)
                            .replace("\u00a77Current page: ", "")
                    ) + 1,
                    player
            );
            return;
        } else if (slot.getItemMeta().getDisplayName().equals("\u00a7cPrevious page")) {
            if(slot.getItemMeta().getLore().isEmpty()) return;
            openPage(Integer.parseInt(slot.getItemMeta()
                            .getLore()
                            .get(0)
                            .replace("\u00a77Current page: ", "")
                    ) - 1,
                    player
            );
            return;
        }


        if (!player.hasPermission("collectibles.admin")) return;

        if (!slot.getItemMeta().getDisplayName().equals(" "))
            util.openSignGui(player, event.getInventory().getItem(event.getSlot()));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().getName().equals(menu.getName()) || event.getCursor() == null)
            return;
        event.setCancelled(true);
    }

    @SuppressWarnings("ConstantConditions")
    private List<ItemStack> getAllCollectibles() {
        List<ItemStack> collectibleList = new ArrayList<>();
        boolean returned = false;
        for (String key : getCollectibles()) {

            // Executes at the beginning of each collectible found
            HashMap<String, String> meta = new HashMap<>();
            HashMap<String, List<String>> metaLists = new HashMap<>();

            meta.put("item", getString(key, "item"));

            // If item is null in the collectible, consider the collectible doesn't exist
            if (meta.get("item") == null || Material.getMaterial(meta.get("item")) == null) {
                returned = true;
                continue;
            }

            meta.put("displayName", getString(key, "displayName"));
            if (meta.get("displayName") == null)
                meta.put(
                        "displayName",
                        ChatColor.WHITE + Material.getMaterial(meta.get("item")).name()
                );

            List<Integer> levels = getIntList(key, "levels");

            // Not verifying if lists are null yet
            metaLists.put("lore", getStringList(key, "lore"));
            int i = 0;
            if (!metaLists.get("lore").isEmpty())
                for (String line : metaLists.get("lore"))
                    metaLists.get("lore").set(i++, line.replace("&", "\u00a7"));


            metaLists.put("enchants", getStringList(key, "enchants"));
            metaLists.put("flags", getStringList(key, "flags"));

            meta.put("unbreakable", getString(key, "unbreakable"));
            meta.putIfAbsent("unbreakable", "false");

            ItemStack itemStack = new ItemStack(Material.getMaterial(meta.get("item")));
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(meta.get("displayName"));
            itemMeta.setUnbreakable(Boolean.parseBoolean(meta.get("unbreakable")));
            if (metaLists.get("lore") == null)
                itemMeta.setLore(metaLists.get("lore"));

            for (String flag : metaLists.get("flags")) {
                try {
                    itemMeta.addItemFlags(ItemFlag.valueOf(flag));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            i = 0;
            for (String flag : metaLists.get("enchants")) {
                try {
                    itemMeta.addEnchant(Enchantment.getByName(flag), levels.get(i++), false);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            itemStack.setItemMeta(itemMeta);

            collectibleList.add(itemStack);
            returned = false;
        }

        if (returned) {
            player.sendMessage(Colors.fixCodes("&cError&8 &l\u00BB &7All collectibles are invalid. They may exist, but the item ID is wrong."));
            plugin.getLogger().warning("There are invalid collectible item IDs in the collectibles file. Please fix this.");
            return null;
        }

        return collectibleList;
    }

    private void openPage(int num, Player player) {

        double amountPages = Math.ceil(getCollectibles().size() / 45);
        if (!(getCollectibles().size() % 45 == 0)) amountPages++;
        if (num > amountPages || num < 1) return;
        if (getAllCollectibles() == null)
            return;

        menu.clear();

        for (int i = 1; i <= amountPages; i++) {
            int j = 0;
            if (i == num)
                for (ItemStack collectible : getAllCollectibles())
                    if (j <= i * 45 && j++ >= i * 45 - 45)
                        menu.addItem(collectible);
        }

        menu.setItem(53, getPageNextItemStack(num));
        if (num != 1)
            menu.setItem(45, getPageBackItemStack(num));
        else
            menu.setItem(45, new ItemStack(Material.STAINED_GLASS_PANE));
        for (int i = 45; i < 54; i++) {
            if (i == 49) {
                menu.setItem(i, getCloseBarrier());
                continue;
            }
            if (i == 45) {
                if (num == 1)
                    menu.setItem(i, getDecoGlass());
                else
                    menu.setItem(i, getPageBackItemStack(num));
                continue;
            }

            if (i == 53) {
                menu.setItem(i, getPageNextItemStack(num));
                continue;
            }

            menu.setItem(i, getDecoGlass());
        }
        player.closeInventory();
        player.openInventory(menu);
    }

    private Set<String> getCollectibles() {
        final FileConfiguration coll = Collectibles.collectibles;
        return coll.getConfigurationSection("collectibles").getKeys(false);
    }

    private String getString(String key, String path) {
        try {
            return Collectibles.collectibles.getString("collectibles." + key + "." + path).replace("&", "\u00a7");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private List<String> getStringList(String key, String path) {
        try {
            return Collectibles.collectibles.getStringList("collectibles." + key + "." + path);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private List<Integer> getIntList(String key, String path) {
        try {
            return Collectibles.collectibles.getIntegerList("collectibles." + key + "." + path);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
