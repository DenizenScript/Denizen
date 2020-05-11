package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FakeEntity {

    public static class FakeEntityMap {

        public Map<Integer, FakeEntity> byId = new HashMap<>();

        public FakeEntity getOrAdd(PlayerTag player, LocationTag location, int id) {
            FakeEntity entity = byId.get(id);
            if (entity != null) {
                return entity;
            }
            entity = new FakeEntity(player, location, id);
            byId.put(id, entity);
            return entity;
        }

        public void remove(FakeEntity entity) {
            byId.remove(entity.id);
        }
    }

    public final static Map<UUID, FakeEntityMap> entityMap = new HashMap<>();

    public static FakeEntity getFakeEntityFor(UUID uuid, int id) {
        FakeEntityMap map = entityMap.get(uuid);
        if (map == null) {
            return null;
        }
        return map.byId.get(id);
    }

    public PlayerTag player;
    public int id;
    public EntityTag entity;
    public LocationTag location;
    public BukkitTask currentTask = null;

    private FakeEntity(PlayerTag player, LocationTag location, int id) {
        this.player = player;
        this.location = location;
        this.id = id;
    }

    public static void showFakeEntityTo(List<PlayerTag> players, EntityTag entityTag, LocationTag location, DurationTag duration) {
        for (PlayerTag player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            FakeEntity.FakeEntityMap playerEntities = entityMap.get(uuid);
            if (playerEntities == null) {
                playerEntities = new FakeEntity.FakeEntityMap();
                entityMap.put(uuid, playerEntities);
            }
            Entity entity = NMSHandler.getPlayerHelper().sendEntitySpawn(player.getPlayerEntity(), entityTag.getBukkitEntityType(), location, entityTag.getWaitingMechanisms());
            FakeEntity fakeEntity = playerEntities.getOrAdd(player, location, entity.getEntityId());
            fakeEntity.updateEntity(new EntityTag(entity), duration);
        }
    }

    public void cancelEntity() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        if (player.isOnline()) {
            NMSHandler.getPlayerHelper().sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
        }
        FakeEntity.FakeEntityMap mapping = entityMap.get(player.getOfflinePlayer().getUniqueId());
        mapping.remove(this);
    }

    private void updateEntity(EntityTag entity, DurationTag duration) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        this.entity = entity;
        if (duration != null && duration.getTicks() > 0) {
            currentTask = new BukkitRunnable() {
                @Override
                public void run() {
                    currentTask = null;
                    cancelEntity();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), duration.getTicks());
        }
    }
}
