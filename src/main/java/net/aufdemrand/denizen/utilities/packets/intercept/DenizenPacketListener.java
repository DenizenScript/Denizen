package net.aufdemrand.denizen.utilities.packets.intercept;

import net.aufdemrand.denizen.events.player.ResourcePackStatusScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.PacketPlayInResourcePackStatus.EnumResourcePackStatus;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class DenizenPacketListener extends AbstractListenerPlayIn {

    public DenizenPacketListener(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.playerConnection);
    }

    public static void enable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(new DenizenPacketListener.PlayerEventListener(), DenizenAPI.getCurrentInstance());
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {
        ItemStack itemStack = packet.getItemStack();
        if (itemStack != null && itemStack.hasTag() && !itemStack.getTag().isEmpty()) {
            NBTTagCompound tag = itemStack.getTag();
            if (tag.hasKey("Denizen Item Script")) {
                NBTTagCompound display = tag.getCompound("display");
                NBTTagList nbtLore = display.hasKey("Lore") ? (NBTTagList) display.get("Lore") : new NBTTagList();
                try {
                    if (nbtLore.size() == 0) {
                        nbtLore.add(new NBTTagString(tag.getString("Denizen Item Script")));
                    }
                    else {
                        List<NBTBase> list = (List<NBTBase>) nbttaglist_list.get(nbtLore);
                        list.add(0, new NBTTagString(tag.getString("Denizen Item Script")));
                    }
                    display.set("Lore", nbtLore);
                }
                catch (IllegalAccessException e) {
                    dB.echoError(e);
                }
                tag.set("display", display);
                tag.remove("Denizen Item Script");
                itemStack.setTag(tag);
            }
        }
        super.a(packet);
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
                    event.player = dPlayer.mirrorBukkitPlayer(player.getBukkitEntity());
                    event.fire();
                }
            });
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        super.a(packet);
    }

    // For compatibility with other plugins using Reflection weirdly...
    @Override
    public void sendPacket(Packet packet) {
        super.sendPacket(packet);
    }

    // IMPORTANT NOTE WHEN ADDING MORE HANDLERS:
    // Packets are handled asynchronously. Remember to use Bukkit's Scheduler!

    public static class PlayerEventListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            DenizenNetworkManager.setNetworkManager(event.getPlayer());
        }
    }

    //////////////////////////////////
    //// Packet Fields
    ///////////

    private static final Field resource_pack_hash, resource_pack_status;
    private static final Field nbttaglist_list;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayInResourcePackStatus.class);
        resource_pack_hash = fields.get("a");
        resource_pack_status = fields.get("status");
        fields = PacketHelper.registerFields(NBTTagList.class);
        nbttaglist_list = fields.get("list");
    }
}
