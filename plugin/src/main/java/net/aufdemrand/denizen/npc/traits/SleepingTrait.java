package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.Utilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class SleepingTrait extends Trait {

    @Persist("sleeping")
    private boolean sleeping = false;

    @Persist("bed location")
    private Location bedLocation = null;

    @Override
    public void run() {
        if (npc == null || bedLocation == null) {
            return;
        }

        //if (npc.getEntity().getPassenger() == null && sitting) eh.mount(eh);

        if (!Utilities.checkLocation((LivingEntity) npc.getEntity(), bedLocation, 1)) {
            wakeUp();
        }
    }

    /**
     * Makes the NPC sleep
     */
    public void toSleep() {
        if (sleeping) {
            return;
        }

        PlayerAnimation.SLEEP.play((Player) npc.getEntity());

        sleeping = true;
        bedLocation = npc.getEntity().getLocation();
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

        /*
         * Teleport NPC to the location before
         * playing sleep animation.
         */
        //TODO Adjust the .add()
        npc.getEntity().teleport(location.add(0.5, 0, 0.5));

        PlayerAnimation.SLEEP.play((Player) npc.getEntity());

        sleeping = true;
        bedLocation = location;
    }

    /**
     * Makes the NPC wake up
     */
    public void wakeUp() {
        if (!sleeping) {
            return;
        }

        PlayerAnimation.STOP_SLEEPING.play((Player) npc.getEntity());

        bedLocation = null;
        sleeping = false;
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
