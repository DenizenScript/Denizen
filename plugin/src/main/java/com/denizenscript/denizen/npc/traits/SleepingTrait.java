package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class SleepingTrait extends Trait {

    @Persist("sleeping")
    private boolean sleeping = false;

    @Persist("bed location")
    private Location bedLocation = null;

    @Override
    public void run() {
        if (npc == null || bedLocation == null || !npc.isSpawned()) {
            return;
        }
        if (!Utilities.checkLocation((LivingEntity) npc.getEntity(), bedLocation, 2)) {
            wakeUp();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
                if (npc.hasTrait(SleepingTrait.class) && !npc.getOrAddTrait(SleepingTrait.class).isSleeping()) {
                    npc.removeTrait(SleepingTrait.class);
                }
            });
        }
    }

    @Override
    public void onSpawn() {
        if (sleeping) {
            internalSleepNow();
        }
    }

    public void internalSleepNow() {
        if (npc.getEntity() instanceof Villager) {
            if (!((Villager) npc.getEntity()).sleep(bedLocation.clone())) {
                return;
            }
        }
        else if (npc.getEntity() instanceof Player) {
            if (bedLocation.getBlock().getBlockData() instanceof Bed) {
                ((Player) npc.getEntity()).sleep(bedLocation.clone(), true);
            }
            else {
                NMSHandler.entityHelper.setPose(npc.getEntity(), Pose.SLEEPING);
            }
        }
        else {
            return;
        }
        sleeping = true;
    }

    /**
     * Makes the NPC sleep
     */
    public void toSleep() {
        if (sleeping) {
            return;
        }
        if (!npc.isSpawned()) {
            Debug.echoError("NPC " + npc.getId() + " cannot sleep: not spawned.");
            return;
        }
        bedLocation = npc.getStoredLocation().clone();
        if (!bedLocation.isWorldLoaded()) {
            Debug.echoError("NPC " + npc.getId() + " cannot sleep: invalid bed location.");
            return;
        }
        internalSleepNow();
    }

    /**
     * Makes the NPC sleep at the specified location
     *
     * @param location where to sleep at
     */
    public void toSleep(Location location) {
        if (sleeping) {
            return;
        }
        if (!npc.isSpawned()) {
            Debug.echoError("NPC " + npc.getId() + " cannot sleep: not spawned.");
            return;
        }
        if (!location.isWorldLoaded()) {
            Debug.echoError("NPC " + npc.getId() + " cannot sleep: invalid bed location.");
            return;
        }
        npc.getEntity().teleport(location.clone());
        bedLocation = location.clone();
        internalSleepNow();
    }

    /**
     * Makes the NPC wake up
     */
    public void wakeUp() {
        if (!sleeping) {
            return;
        }
        sleeping = false;
        if (npc.getEntity() instanceof Villager) {
            ((Villager) npc.getEntity()).wakeup();
        }
        else {
            if (((Player) npc.getEntity()).isSleeping()) {
                ((Player) npc.getEntity()).wakeup(false);
            }
            NMSHandler.entityHelper.setPose(npc.getEntity(), Pose.STANDING);
        }
        bedLocation = null;
    }

    /**
     * Checks if the NPC is currently sleeping
     *
     * @return boolean
     */
    public boolean isSleeping() {
        return sleeping;
    }

    /**
     * Gets the bed the NPC is sleeping on
     * Returns null if the NPC isnt sleeping
     *
     * @return Location
     */
    public Location getBed() {
        return bedLocation;
    }

    /**
     * If someone tries to break the poor
     * NPC's bed, we need to stop them!
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (bedLocation == null) {
            return;
        }
        if (event.getBlock().getLocation().equals(bedLocation)) {
            event.setCancelled(true);
        }
    }

    public SleepingTrait() {
        super("sleeping");
    }
}
