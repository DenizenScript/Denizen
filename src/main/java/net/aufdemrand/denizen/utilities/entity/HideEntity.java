package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class HideEntity {

    public static Map<UUID, Set<UUID>> hiddenEntities = new HashMap<UUID, Set<UUID>>();

    public static void hideEntity(Player player, Entity entity) {
        if (dEntity.isPlayer(entity)) {
            player.hidePlayer((Player)entity);
        }
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            if (!hiddenEntities.containsKey(playerUUID)) {
                hiddenEntities.put(playerUUID, new HashSet<UUID>());
            }
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (!hidden.contains(entityUUID)) {
                hidden.add(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_8_R3.Entity other = ((CraftEntity)entity).getHandle();
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if (entry != null) {
                    entry.clear(entityPlayer);
                }
            }
        }
    }

    public static void showEntity(Player player, Entity entity) {
        if (dEntity.isPlayer(entity)) {
            player.showPlayer((Player)entity);
        }
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity) && hiddenEntities.containsKey(playerUUID)) {
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (hidden.contains(entityUUID)) {
                hidden.remove(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_8_R3.Entity other = ((CraftEntity)entity).getHandle();
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if(entry != null && !entry.trackedPlayers.contains(entityPlayer)) {
                    entry.updatePlayer(entityPlayer);
                }
            }
        }
    }
}
