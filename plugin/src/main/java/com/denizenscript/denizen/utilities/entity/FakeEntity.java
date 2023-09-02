package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

public class FakeEntity {

    public static class FakeEntityMap {

        public Map<Integer, FakeEntity> byId = new HashMap<>();

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
    public Consumer<PlayerTag> triggerSpawnPacket;
    public Runnable triggerUpdatePacket;
    public Runnable triggerDestroyPacket;
    public UUID overrideUUID;

    public FakeEntity(List<PlayerTag> player, LocationTag location, int id) {
        this.players = player;
        this.location = location;
        this.id = id;
    }

    public static FakeEntity showFakeEntityTo(List<PlayerTag> players, EntityTag typeToSpawn, LocationTag location, DurationTag duration, EntityTag vehicle) {
        NetworkInterceptHelper.enable();
        FakeEntity fakeEntity = NMSHandler.playerHelper.sendEntitySpawn(players, typeToSpawn.getEntityType(), location, typeToSpawn.mechanisms == null ? null : new ArrayList<>(typeToSpawn.mechanisms), -1, null, true);
        if (vehicle != null) {
            NMSHandler.playerHelper.addFakePassenger(players, vehicle.getBukkitEntity(), fakeEntity);
        }
        idsToEntities.put(fakeEntity.overrideUUID == null ? fakeEntity.entity.getUUID() : fakeEntity.overrideUUID, fakeEntity);
        for (PlayerTag player : players) {
            UUID uuid = player.getPlayerEntity().getUniqueId();
            FakeEntity.FakeEntityMap playerEntities = playersToEntities.get(uuid);
            if (playerEntities == null) {
                playerEntities = new FakeEntity.FakeEntityMap();
                playersToEntities.put(uuid, playerEntities);
            }
            playerEntities.byId.put(fakeEntity.id, fakeEntity);
        }
        fakeEntity.updateEntity(fakeEntity.entity, duration);
        return fakeEntity;
    }

    public void cancelEntity() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        idsToEntities.remove(overrideUUID == null ? entity.getUUID() : overrideUUID);
        if (triggerDestroyPacket != null) {
            triggerDestroyPacket.run();
        }
        else {
            for (PlayerTag player : players) {
                if (player.isOnline()) {
                    NMSHandler.playerHelper.sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
                }
            }
        }
        for (PlayerTag player : players) {
            FakeEntity.FakeEntityMap mapping = playersToEntities.get(player.getUUID());
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
