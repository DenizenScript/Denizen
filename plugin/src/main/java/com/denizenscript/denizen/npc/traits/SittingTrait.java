package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.ArrayList;

public class SittingTrait extends Trait implements Listener {

    @Persist("sitting")
    private boolean sitting = false;

    @Persist("chair location")
    private Location chairLocation = null;

    @Override
    public void run() {
        if (!npc.isSpawned() || chairLocation == null) {
            return;
        }
        if (!Utilities.checkLocation((LivingEntity) npc.getEntity(), chairLocation, 1)) {
            stand();
        }
    }

    @Override
    public void onSpawn() {
        if (sitting) {
            if (chairLocation == null) {
                sit();
            }
            else {
                sit(chairLocation);
            }
        }
    }

    @Override
    public void onDespawn() {
        if (npc == null || npc.getEntity() == null) {
            // Wat.
            return;
        }
        if (npc.getEntity().getVehicle() != null) {
            npc.getEntity().getVehicle().eject();
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

        sit(npc.getEntity().getLocation());
    }

    private void sitInternal() {
        if (npc.getEntity() instanceof Player) {
            PlayerAnimation.SIT.play((Player) npc.getEntity());
        }
        else {
            DenizenEntityType.getByName("FAKE_ARROW").spawnNewEntity(npc.getEntity().getLocation(),
                    new ArrayList<>(), null).setPassenger(npc.getEntity());
        }
        //eh.getDataWatcher().watch(0, (byte) 0x04);
        sitting = true;
    }

    private void standInternal() {
        if (npc.getEntity() instanceof Player) {
            PlayerAnimation.STOP_SITTING.play((Player) npc.getEntity());
        }
        else {
            Entity vehicle = npc.getEntity().getVehicle();
            npc.despawn();
            npc.spawn(npc.getStoredLocation().clone().add(0, 0.5, 0));
            if (vehicle != null && vehicle.isValid()) {
                vehicle.eject();
                vehicle.remove();
            }
        }
        //eh.getDataWatcher().watch(0, (byte) 0x00);
        sitting = false;
    }

    public void sitInternal(Location location) {
        new NPCTag(npc).action("sit", null);

        /*
         * Teleport NPC to the location before
         * sending the sit packet to the clients.
         */
        npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);

        sitInternal();
    }

    /**
     * Makes the NPC sit at the specified location
     *
     * @param location where to sit
     */
    public void sit(Location location) {
        sitInternal(location.clone().add(0, 0.5, 0));
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
     * Gets the chair the NPC is sitting on
     * Returns null if the NPC isnt sitting
     *
     * @return Location
     */
    public Location getChair() {
        return chairLocation;
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

    @EventHandler
    public void arrowDismount(final VehicleExitEvent event) {
        // TODO: Move elsewhere so not multi-firing?
        if (event.getVehicle() instanceof FakeArrow) {
            Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    if (event.getVehicle().isValid()) {
                        event.getVehicle().remove();
                    }
                }
            }, 1);
        }
    }

    public SittingTrait() {
        super("sitting");
    }
}
