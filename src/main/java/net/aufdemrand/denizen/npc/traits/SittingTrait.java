package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.entity.CraftFakeArrow;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
            sit();
        }
    }

    @Override
    public void onDespawn() {
        if (npc == null || npc.getEntity() == null) {
            // Wat.
            return;
        }
        if (npc.getEntity().getVehicle() != null) {
            npc.getEntity().getVehicle().setPassenger(null);
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
        DenizenAPI.getDenizenNPC(npc).action("sit", null);

        if (npc.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        sitInternal();
        chairLocation = npc.getEntity().getLocation().clone();
    }

    private void sitInternal() {
        CraftFakeArrow.createArrow(npc.getEntity().getLocation(), new ArrayList<Mechanism>()).setPassenger(npc.getEntity());
        //PlayerAnimation.SIT.play((Player)npc.getEntity());
        //eh.getDataWatcher().watch(0, (byte) 0x04);
        sitting = true;
    }

    private void standInternal() {
        Entity vehicle = npc.getEntity().getVehicle();
        npc.despawn();
        npc.spawn(npc.getStoredLocation().clone().add(0, 0.5, 0));
        if (vehicle != null && vehicle.isValid()) {
            vehicle.setPassenger(null);
            vehicle.remove();
        }
        //PlayerAnimation.STOP_SITTING.play((Player)npc.getEntity());
        //eh.getDataWatcher().watch(0, (byte) 0x00);
        sitting = false;
    }

    /**
     * Makes the NPC sit at the specified location
     *
     * @param location where to sit
     */
    public void sit(Location location) {
        DenizenAPI.getDenizenNPC(npc).action("sit", null);

        if (npc.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        /*
         * Teleport NPC to the location before
         * sending the sit packet to the clients.
         */
        // TODO: Make this work better.
        npc.teleport(location.clone().add(0, 0.5, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);

        sitInternal();
        chairLocation = location;
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
        DenizenAPI.getDenizenNPC(npc).action("stand", null);

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
        return true; // If the trait is attached, let's assume the NPC is sitting
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
        if (event.getVehicle() instanceof CraftFakeArrow) {
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
