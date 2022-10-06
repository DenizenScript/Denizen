package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.ClickRedirectTrait;
import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SittingTrait extends Trait implements Listener {

    @Persist("sitting")
    private boolean sitting = false;

    @Persist("chair location")
    private Location chairLocation = null;

    private boolean hasSpawned = false;

    @Override
    public void run() {
        if (!npc.isSpawned() || chairLocation == null || !hasSpawned) {
            return;
        }
        Location curLoc = npc.getEntity().getLocation();
        if (curLoc.getWorld() != chairLocation.getWorld()) {
            stand();
            Messaging.debug("(Denizen/SittingTrait) NPC", npc.getId(), "stood up because it change world.");
            return;
        }
        double xoff = chairLocation.getX() - curLoc.getX(), zoff = chairLocation.getZ() - curLoc.getZ();
        double dist = xoff * xoff + zoff * zoff;
        if (dist > 4) {
            stand();
            Messaging.debug("(Denizen/SittingTrait) NPC", npc.getId(), "stood up because it moved away:", xoff, "on X and", zoff, "on Z");
            return;
        }
    }

    @Override
    public void onSpawn() {
        if (!sitting) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.instance, () -> {
                if (!sitting && npc != null) {
                    npc.removeTrait(SittingTrait.class);
                }
            }, 1);
            return;
        }
        hasSpawned = true;
        if (chairLocation == null) {
            sit();
        }
        else {
            chairLocation = chairLocation.clone();
            chairLocation.setYaw(npc.getStoredLocation().getYaw());
            chairLocation.setPitch(npc.getStoredLocation().getPitch());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> sit(chairLocation), 1);
        }
    }

    @Override
    public void onDespawn() {
        hasSpawned = false;
        if (npc == null || npc.getEntity() == null) {
            return;
        }
        Entity vehicle = npc.getEntity().getVehicle();
        if (vehicle != null) {
            vehicle.eject();
            NPC vehicleNPC = CitizensAPI.getNPCRegistry().getNPC(vehicle);
            if (vehicleNPC != null && vehicleNPC.data().get("is-denizen-seat", false)) {
                vehicleNPC.destroy();
            }
        }
    }

    // <--[action]
    // @Actions
    // sit
    //
    // @Triggers when the NPC sits down.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Makes the NPC sit
     */
    public void sit() {
        if (!npc.isSpawned()) {
            return;
        }
        new NPCTag(npc).action("sit", null);
        sit(npc.getStoredLocation());
    }

    private void standInternal() {
        sitting = false;
        safetyCleanup(chairLocation.clone());
        if (!npc.isSpawned()) {
            return;
        }
        forceUnsit(npc.getEntity());
        if (chairLocation == null) {
            return;
        }
        npc.teleport(chairLocation.clone().add(0, 0.3, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void sitInternal(Location location) {
        sitting = true;
        safetyCleanup(location.clone());
        if (!npc.isSpawned()) {
            return;
        }
        new NPCTag(npc).action("sit", null);
        npc.getEntity().teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        forceEntitySit(npc.getEntity(), location.clone(), 0);
    }

    public void safetyCleanup(Location loc) {
        if (loc.getWorld() == null) {
            return;
        }
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity.getType() == EntityType.ARMOR_STAND && entity.getCustomName() != null && entity.getCustomName().equals(SIT_STAND_NAME) && entity.getPassengers().isEmpty()) {
                ArmorStand stand = (ArmorStand) entity;
                if (stand.isMarker() && stand.isSmall() && !stand.isVisible() && stand.getPassengers().isEmpty()) {
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(stand);
                    if (npc != null) {
                        npc.destroy();
                    }
                    else {
                        stand.remove();
                    }
                }
            }
        }
    }

    /**
     * Makes the NPC sit at the specified location
     *
     * @param location where to sit
     */
    public void sit(Location location) {
        sitInternal(location.clone());
        chairLocation = location.clone();
    }

    // <--[action]
    // @Actions
    // stand
    //
    // @Triggers when the NPC stands up.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Makes the NPC stand
     */
    public void stand() {
        new NPCTag(npc).action("stand", null);
        standInternal();
        standInternal();
        chairLocation = null;
    }

    /**
     * Checks if the NPC is currently sitting
     *
     * @return boolean
     */
    public boolean isSitting() {
        return sitting;
    }

    /**
     * If someone tries to break the poor
     * NPC's chair, we need to stop them!
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (chairLocation == null) {
            return;
        }
        if (event.getBlock().getLocation().equals(chairLocation)) {
            event.setCancelled(true);
        }
    }

    public SittingTrait() {
        super("sitting");
    }

    public static String SIT_STAND_NAME = ChatColor.BLACK + "Deniz" + ChatColor.DARK_GRAY + "NPCSit";

    public void forceUnsit(Entity entity) {
        entity.removeMetadata("denizen.sitting", Denizen.getInstance());
        if (entity.isInsideVehicle()) {
            entity.leaveVehicle();
        }
        if (sitStandNPC != null) {
            sitStandNPC.destroy();
            sitStandNPC = null;
        }
    }

    public NPC sitStandNPC = null;

    public void forceEntitySit(Entity entity, Location location, int retryCount) {
        if (sitStandNPC != null) {
            sitStandNPC.destroy();
        }
        entity.setMetadata("denizen.sitting", new FixedMetadataValue(Denizen.getInstance(), true));
        NPCRegistry registry = CitizensAPI.getNamedNPCRegistry("DenizenSitRegistry");
        if (registry == null) {
            registry = CitizensAPI.createNamedNPCRegistry("DenizenSitRegistry", new MemoryNPCDataStore());
        }
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        final NPC holder = registry.createNPC(EntityType.ARMOR_STAND, SIT_STAND_NAME);
        sitStandNPC = holder;
        if (npc != null) {
            holder.addTrait(new ClickRedirectTrait(npc));
            Messaging.debug("(Denizen/SittingTrait) Spawning chair for", npc.getId(), "as id", holder.getId());
        }
        ArmorStandTrait trait = holder.getOrAddTrait(ArmorStandTrait.class);
        trait.setGravity(false);
        trait.setHasArms(false);
        trait.setHasBaseplate(false);
        trait.setSmall(true);
        trait.setMarker(true);
        trait.setVisible(false);
        holder.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, false);
        holder.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);
        boolean spawned = holder.spawn(location);
        if (!spawned || !holder.isSpawned()) {
            if (retryCount >= 4) {
                Debug.echoError("NPC " + (npc == null ? "null" : npc.getId()) + " sit failed (" + spawned + "," + holder.isSpawned() + "): cannot spawn chair id "
                        + holder.getId() + " at " + new LocationTag(location).identifySimple() + " ChunkIsLoaded=" + new ChunkTag(location).isLoaded());
                holder.destroy();
                sitStandNPC = null;
            }
            else {
                Messaging.debug("(Denizen/SittingTrait) retrying failed sit for", npc.getId());
                Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> { if (npc.isSpawned()) { forceEntitySit(entity, location, retryCount + 1); } }, 5);
            }
            return;
        }
        holder.data().set("is-denizen-seat", true);
        new BukkitRunnable() {
            @Override
            public void cancel() {
                super.cancel();
                if (entity.isValid() && entity.hasMetadata("denizen.sitting")) {
                    entity.removeMetadata("denizen.sitting", Denizen.getInstance());
                }
                if (holder.getTraits().iterator().hasNext()) { // Hacky NPC-already-removed test
                    holder.destroy();
                }
                if (sitStandNPC == holder) {
                    sitStandNPC = null;
                }
            }

            @Override
            public void run() {
                if (holder != sitStandNPC) {
                    cancel();
                }
                else if (!holder.getTraits().iterator().hasNext()) { // Hacky NPC-already-removed test
                    cancel();
                }
                else if (!entity.isValid() || !entity.hasMetadata("denizen.sitting") || !entity.getMetadata("denizen.sitting").get(0).asBoolean()) {
                    cancel();
                }
                else if (npc != null && !npc.isSpawned()) {
                    cancel();
                }
                else if (!holder.isSpawned()) {
                    cancel();
                }
                else if (!NMS.getPassengers(holder.getEntity()).contains(entity)) {
                    holder.getEntity().addPassenger(entity);
                }
            }
        }.runTaskTimer(Denizen.getInstance(), 0, 1);
    }
}
