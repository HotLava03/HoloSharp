package io.github.hotlava03.collectibles.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hotlava03.collectibles.Collectibles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

import net.minecraft.server.v1_12_R1.PacketPlayInUpdateSign;

public class SignGui
{
    private ProtocolManager protocolManager;
    private PacketAdapter packetListener;
    private Map<String, SignGUIListener> listeners;

    public SignGui()
    {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.packetListener = getListener();
        this.protocolManager.addPacketListener(packetListener);
        this.listeners = new ConcurrentHashMap<>();
    }

     // TODO: update this to get a BlockData as 2nd param in sendBlockChange()
    public void open(Player player, String[] lines, SignGUIListener response)
    {
        int x = player.getLocation().getBlockX();
        int y = 0;
        int z = player.getLocation().getBlockZ();

        Location signLoc = new Location(player.getLocation().getWorld(), x, y, z);
        //player.sendBlockChange(signLoc, Material.SIGN_POST, (byte) 0);
        player.sendSignChange(signLoc, lines);

        PacketContainer packetSignEditor = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        packetSignEditor.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
        try
        {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetSignEditor);
            listeners.put(player.getName(), response);
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }

        //player.sendBlockChange(signLoc, Material.BEDROCK, (byte) 0);

    }

    public void destroy()
    {
        protocolManager.removePacketListener(packetListener);
        listeners.clear();
    }

    public interface SignGUIListener
    {
        public void onSignDone(Player player, String[] lines);
    }

    public PacketAdapter getListener()
    {
        return new PacketAdapter(Collectibles.getPlugin(Collectibles.class), ListenerPriority.NORMAL,
                PacketType.Play.Client.UPDATE_SIGN)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                final Player player = event.getPlayer();
                String[] lines = ((PacketPlayInUpdateSign) event.getPacket().getHandle()).b();
                final SignGUIListener response = listeners.remove(event.getPlayer().getName());

                if (response != null)
                {
                    event.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> response.onSignDone(player, lines));
                }
            }
        };
    }
}
