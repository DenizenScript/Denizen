package net.aufdemrand.denizen.utilities.packets.intercept;

import net.aufdemrand.denizen.events.scriptevents.ResourcePackStatusScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketListenerPlayIn;
import net.minecraft.server.v1_8_R3.PacketPlayInResourcePackStatus;
import net.minecraft.server.v1_8_R3.PacketPlayInResourcePackStatus.EnumResourcePackStatus;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.util.Map;

public class DenizenPacketListener extends AbstractListenerPlayIn {

    public DenizenPacketListener(EntityPlayer entityPlayer, PacketListenerPlayIn oldListener) {
        super(entityPlayer, oldListener);
    }

    public static void enable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(new DenizenPacketListener.PlayerEventListener(), DenizenAPI.getCurrentInstance());
    }

    //////////////////////////////////
    //// Resource Pack Status
    ///////////

    private static final Field resource_pack_hash, resource_pack_status;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayInResourcePackStatus.class);
        resource_pack_hash = fields.get("a");
        resource_pack_status = fields.get("b");
    }

    @Override
    public void a(PacketPlayInResourcePackStatus packet) {
        try {
            final String hash = (String) resource_pack_hash.get(packet);
            final EnumResourcePackStatus status = (EnumResourcePackStatus) resource_pack_status.get(packet);
            Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    ResourcePackStatusScriptEvent event = ResourcePackStatusScriptEvent.instance;
                    event.hash = new Element(hash);
                    event.status = new Element(status.name());
                    event.player = dPlayer.mirrorBukkitPlayer(entityPlayer.getBukkitEntity());
                    event.fire();
                }
            });
        } catch (Exception e) {
            dB.echoError(e);
        }
        oldListener.a(packet);
    }

    // IMPORTANT NOTE WHEN ADDING MORE HANDLERS:
    // Packets are handled asynchronously. Remember to use Bukkit's Scheduler!

    public static class PlayerEventListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            DenizenNetworkManager.setNetworkManager(event.getPlayer());
        }
    }
}
