package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_4_R1.EntityHuman;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SittingTrait extends Trait implements Listener  {
	
	@Persist("sitting")
    private boolean sitting = false;
	
	@Persist("chair location")
	private Location location = null;
	
	EntityHuman eh = null;
	
	@Override
	public void run() {
		if (eh == null || location == null) return;
		
		if (!Utilities.checkLocation(npc.getBukkitEntity(), location, 1)) {
            stand();
        }
	}
	
	@Override
	public void onSpawn() {
		eh = ((CraftPlayer) npc.getBukkitEntity()).getHandle();
	}
	
	/**
	 * Makes the NPC sit
	 */
	public void sit() {
		if (sitting == true) {
			dB.log("...npc is sitting");
			return;
		}
		
		eh.mount(eh);
		dB.log("...sit() called");
		
		sitting = true;
		location = npc.getBukkitEntity().getLocation();
		return;
	}
	
	/**
	 * Makes the NPC sit a the specified location
	 * 
	 * @param location
	 */
	public void sit(Location location) {
		if (sitting == true) {
			dB.log("...npc is sitting");
			return;
		}
		
		/*
		 * Teleport NPC to the location before
		 * sending the sit packet to the clients.
		 */
		npc.getBukkitEntity().teleport(location.add(0.5, 0, 0.5));
		dB.echoDebug("...NPC moved to chair");
		
		eh.mount(eh);
		dB.log("...sit(Location location) called");

		sitting = true;
		this.location = location;
	}
	
	/**
	 * Makes the NPC stand
	 */
	public void stand() {
		if (sitting == false) {
			return;
		}
		
		eh.mount(null);
		dB.log("...stand() called");
		
		location = null;
		sitting = false;
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
		return location;
	}
	
    
    /**
     * If someone tries to break the poor
     * NPC's chair, we need to stop them!
     *
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
    	if (location == null) return;
        if (event.getBlock().getLocation().equals(location)) {
            event.setCancelled(true);
        }
    }
	
	public SittingTrait() {
		super("sitting");
	}

}
