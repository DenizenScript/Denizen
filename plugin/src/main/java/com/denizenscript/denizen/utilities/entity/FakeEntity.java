package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FakeEntity {

    public static class FakeEntityMap {

        public Map<Integer, FakeEntity> byId = new HashMap<>();

        public FakeEntity getOrAdd(List<PlayerTag> players, LocationTag location, int id) {
            FakeEntity entity = byId.get(id);
            if (entity != null) {
                return entity;
            }
            entity = new FakeEntity(players, location, id);
            byId.put(id, entity);
            return entity;
        }

        public void remove(FakeEntity entity) {
            byId.remove(entity.id);
        }
    }

    public final static Map<UUID, FakeEntityMap> playersToEntities = new HashMap<>();
    public final static Map<UUID, FakeEntity> idsToEntities = new HashMap<>();

    public static FakeEntity getFakeEntityFor(UUID uuid, int id) {
        FakeEntityMap map = playersToEntities.get(uuid);
        if (map == null) {
            return null;
        }
        return map.byId.get(id);
    }

    public List<PlayerTag> players;
    public int id;
    public EntityTag entity;
    public LocationTag location;
    public BukkitTask currentTask = null;

    private FakeEntity(List<PlayerTag> player, LocationTag location, int id) {
        this.players = player;
        this.location = location;
        this.id = id;
    }

    public static ListTag showFakeEntityTo(List<PlayerTag> players, EntityTag typeToSpawn, LocationTag location, DurationTag duration) {
        ListTag result = new ListTag();
        for (PlayerTag player : players) {
            if (!player.isOnline() || !player.isValid()) {
                continue;
            }
            UUID uuid = player.getPlayerEntity().getUniqueId();
            FakeEntity.FakeEntityMap playerEntities = playersToEntities.get(uuid);
            if (playerEntities == null) {
                playerEntities = new FakeEntity.FakeEntityMap();
                playersToEntities.put(uuid, playerEntities);
            }
            EntityTag entTag = NMSHandler.getPlayerHelper().sendEntitySpawn(players, typeToSpawn.getBukkitEntityType(), location, typeToSpawn.getWaitingMechanisms(), -1, null, true);
            FakeEntity fakeEntity = playerEntities.getOrAdd(players, location, entTag.getBukkitEntity().getEntityId());
            entTag.isFake = true;
            entTag.isFakeValid = true;
            fakeEntity.updateEntity(entTag, duration);
            idsToEntities.put(entTag.getUUID(), fakeEntity);
            result.addObject(entTag);
        }
        return result;
    }

    public void cancelEntity() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        idsToEntities.remove(entity.getUUID());
        for (PlayerTag player : players) {
            if (player.isOnline()) {
                NMSHandler.getPlayerHelper().sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
            }
            FakeEntity.FakeEntityMap mapping = playersToEntities.get(player.getOfflinePlayer().getUniqueId());
            mapping.remove(this);
        }
        entity.isFakeValid = false;
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
            }.runTaskLater(Denizen.getInstance(), duration.getTicks());
        }
    }
}
