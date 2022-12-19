package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HideEntitiesHelper {

    public static class PlayerHideMap {

        public UUID player;

        public HashSet<UUID> entitiesHidden = new HashSet<>();

        public HashSet<UUID> overridinglyShow = new HashSet<>();

        public HashSet<String> matchersHidden = new HashSet<>();

        public boolean shouldHideViaMatcher(Entity entity) {
            if (entity == null) {
                return false;
            }
            if (!matchersHidden.isEmpty()) {
                if (overridinglyShow.contains(entity.getUniqueId())) {
                    return false;
                }
                EntityTag entityTag = new EntityTag(entity);
                for (String matchable : matchersHidden) {
                    if (entityTag.tryAdvancedMatcher(matchable)) {
                        if (entity instanceof Player) {
                            Player thisPlayer = Bukkit.getPlayer(player);
                            if (thisPlayer != null && thisPlayer.canSee((Player) entity)) {
                                thisPlayer.hidePlayer(Denizen.getInstance(), (Player) entity);
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean shouldHide(Entity entity) {
            if (entity == null) {
                return false;
            }
            if (overridinglyShow.contains(entity.getUniqueId())) {
                return false;
            }
            if (entitiesHidden.contains(entity.getUniqueId())) {
                return true;
            }
            if (defaultHidden.contains(entity.getUniqueId())) {
                return true;
            }
            return shouldHideViaMatcher(entity);
        }
    }

    public static HashMap<UUID, PlayerHideMap> playerHides = new HashMap<>();

    public static HashSet<UUID> defaultHidden = new HashSet<>();

    public static boolean hasAnyHides() {
        return !playerHides.isEmpty() || !defaultHidden.isEmpty();
    }

    public static PlayerHideMap getPlayerMapFor(UUID player) {
        PlayerHideMap map = playerHides.get(player);
        if (map == null) {
            map = new PlayerHideMap();
            map.player = player;
            playerHides.put(player, map);
        }
        return map;
    }

    public static boolean playerShouldHide(UUID player, Entity ent) {
        PlayerHideMap map = playerHides.get(player);
        if (map == null) {
            return defaultHidden.contains(ent.getUniqueId()) && !player.equals(ent.getUniqueId());
        }
        return map.shouldHide(ent);
    }

    public static boolean addHide(UUID player, UUID entity) {
        NetworkInterceptHelper.enable();
        ensurePlayerHiding();
        if (player == null) {
            return defaultHidden.add(entity);
        }
        PlayerHideMap map = getPlayerMapFor(player);
        map.overridinglyShow.remove(entity);
        return map.entitiesHidden.add(entity);
    }

    public static void hideEntity(Player player, Entity entity) {
        if (addHide(player == null ? null : player.getUniqueId(), entity.getUniqueId())) {
            if (player == null) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    NMSHandler.entityHelper.sendHidePacket(pl, entity);
                }
            }
            else {
                NMSHandler.entityHelper.sendHidePacket(player, entity);
            }
        }
    }

    public static boolean removeHide(UUID player, UUID entity) {
        NetworkInterceptHelper.enable();
        if (player == null) {
            return defaultHidden.remove(entity);
        }
        PlayerHideMap map = playerHides.get(player);
        if (defaultHidden.contains(entity) || (map != null && map.shouldHideViaMatcher(Bukkit.getEntity(entity)))) {
            if (map == null) {
                map = new PlayerHideMap();
                map.player = player;
                playerHides.put(player, map);
            }
            map.entitiesHidden.remove(entity);
            return map.overridinglyShow.add(entity);
        }
        if (map == null) {
            return false;
        }
        boolean result = map.entitiesHidden.remove(entity);
        if (result && map.entitiesHidden.isEmpty() && map.overridinglyShow.isEmpty() && map.matchersHidden.isEmpty()) {
            playerHides.remove(player);
        }
        return result;
    }

    public static void unhideEntity(Player player, Entity entity) {
        if (removeHide(player == null ? null : player.getUniqueId(), entity.getUniqueId())) {
            if (player == null) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    NMSHandler.entityHelper.sendShowPacket(pl, entity);
                }
            }
            else {
                NMSHandler.entityHelper.sendShowPacket(player, entity);
            }
        }
    }

    public static class EnforcePlayerHides implements Listener {

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            for (UUID id : defaultHidden) {
                Player pTarget = Bukkit.getPlayer(id);
                if (pTarget != null) {
                    event.getPlayer().hidePlayer(Denizen.getInstance(), pTarget);
                }
            }
            final Player pl = event.getPlayer();
            PlayerHideMap map = playerHides.get(pl.getUniqueId());
            if (map == null) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (pl.isOnline()) {
                        if (map.matchersHidden != null) {
                            for (Entity entity : pl.getWorld().getEntities()) {
                                if (map.shouldHide(entity)) {
                                    NMSHandler.entityHelper.sendHidePacket(pl, entity);
                                }
                            }
                        }
                        else {
                            for (UUID id : map.entitiesHidden) {
                                Entity ent = Bukkit.getEntity(id);
                                if (ent != null) {
                                    NMSHandler.entityHelper.sendHidePacket(pl, ent);
                                }
                            }
                        }
                    }
                }
            }.runTaskLater(Denizen.getInstance(), 5);
        }
    }

    public static EnforcePlayerHides EPH = null;

    public static void ensurePlayerHiding() {
        if (EPH == null) {
            EPH = new EnforcePlayerHides();
            Bukkit.getPluginManager().registerEvents(EPH, Denizen.getInstance());
        }
    }
}
