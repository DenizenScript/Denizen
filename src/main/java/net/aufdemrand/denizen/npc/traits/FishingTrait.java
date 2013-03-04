package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_4_R1.EntityFishingHook;
import net.minecraft.server.v1_4_R1.EntityHuman;
import net.minecraft.server.v1_4_R1.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class FishingTrait extends Trait {

	@Persist("fishing")
    private boolean fishing = false;
	
	@Persist("fishing hole")
	private Location fishingLocation = null;
	
	EntityHuman eh = null;
	WorldServer nmsworld = null;
	EntityFishingHook fishHook = null;
	
	@Override
	public void run() {

	}
	
	@Override
	public void onSpawn() {
		eh = ((CraftPlayer) npc.getBukkitEntity()).getHandle();
		nmsworld = ((CraftWorld) npc.getBukkitEntity().getWorld()).getHandle();
        fishHook = new EntityFishingHook(nmsworld, eh);
	}
	
	/**
	 * Makes the NPC fish in the nearest water
	 * 
	 * TODO Needs logic for handling that.
	 */
	public void startFishing() {
		fishing = true;
		fishingLocation = npc.getBukkitEntity().getLocation();
		return;
	}
	
	/**
	 * Makes the NPC fish at the specified location
	 * 
	 * TODO Add variance, so each cast doesnt land in the exact same spot.
	 * 
	 * @param location
	 */
	public void startFishing(Location location) {
		dB.log("...adding hook to world");
		nmsworld.addEntity(fishHook);
		
		dB.log("...setting up vectors");
		Vector v1 = fishHook.getBukkitEntity().getLocation().toVector();
		Vector v2 = location.toVector();
		Vector v3 = v2.subtract(v1).normalize().multiply(1.5);
		
		dB.log("...set npc as shooting");
		((Projectile) fishHook.getBukkitEntity()).setShooter(npc.getBukkitEntity());
		dB.log("...set hook velocity");
		fishHook.getBukkitEntity().setVelocity(v3);
		
		dB.log("...npc should be fishing!");
		fishing = true;
		fishingLocation = location;
	}
	
	/**
	 * Makes the stop fishing.
	 */
	public void stopFishing() {
		
		dB.log("...removing fish hook.");
		fishHook.getBukkitEntity().remove();
		
		fishingLocation = null;
		fishing = false;
	}
	
	/**
	 * Checks if the NPC is currently fishing
	 * 
	 * @return boolean
	 */
	public boolean isFishing() {
		return fishing;
	}
	
	/**
	 * Gets the location the NPC is casting to
	 * Returns null if the NPC isnt fishing
	 * 
	 * @return Location
	 */
	public Location getFishingLocation() {
		return fishingLocation;
	}
	
	public FishingTrait() {
		super("fishing");
	}

}
