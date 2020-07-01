package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
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

    public final static Map<UUID, FakeEntityMap> playersToEntities = new HashMap<>();
    public final static Map<UUID, FakeEntity> idsToEntities = new HashMap<>();

    public static FakeEntity getFakeEntityFor(UUID uuid, int id) {
        FakeEntityMap map = playersToEntities.get(uuid);
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
            Entity entity = NMSHandler.getPlayerHelper().sendEntitySpawn(player.getPlayerEntity(), typeToSpawn.getBukkitEntityType(), location, typeToSpawn.getWaitingMechanisms(), -1, null);
            FakeEntity fakeEntity = playerEntities.getOrAdd(player, location, entity.getEntityId());
            EntityTag entTag = new EntityTag(entity);
            entTag.isFake = true;
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
        if (player.isOnline()) {
            NMSHandler.getPlayerHelper().sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
        }
        FakeEntity.FakeEntityMap mapping = playersToEntities.get(player.getOfflinePlayer().getUniqueId());
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
