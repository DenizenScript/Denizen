package net.aufdemrand.denizen.npc.traits;

import java.util.ArrayList;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_4_R1.EntityFishingHook;
import net.minecraft.server.v1_4_R1.EntityHuman;
import net.minecraft.server.v1_4_R1.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FishingTrait extends Trait {

	@Persist("fishing")
    private boolean fishing = false;
	
	@Persist("fishing hole")
	private Location fishingLocation = null;
	
	ArrayList<Location> available = new ArrayList<Location>();
	EntityHuman eh = null;
	WorldServer nmsworld = null;
	Location fishingSpot = null;
	EntityFishingHook fishHook = null;
	Entity fish = null;
	int reelCount = 100;
	int castCount = 0;
	
	@Override
	public void run() {
	    reelCount++;
	    castCount++;
	    
	    if(reelCount == 500) {
	        reel();
	        reelCount = 0;
	        castCount = 425;
	    }
	    
	    if(castCount == 500) {
	        try{
	            fish.remove();
	        } catch(Exception e) { }
	        cast();
	        castCount = 0;
	    }
	}
	
	@Override
	public void onSpawn() {
		eh = ((CraftPlayer) npc.getBukkitEntity()).getHandle();
		nmsworld = ((CraftWorld) npc.getBukkitEntity().getWorld()).getHandle();
        fishHook = new EntityFishingHook(nmsworld, eh);
        fishingSpot = fishingLocation.clone();
	}
	
	/**
	 * Makes the NPC fish in the nearest water
	 * 
	 * TODO Needs logic for handling that.
	 */
	public void startFishing() {
		fishing = true;
		fishingLocation = npc.getBukkitEntity().getLocation();
		fishingSpot = fishingLocation.clone();
		return;
	}
	
	private void findAvailable() {
	    available.clear();
	    Location loc = fishingLocation.clone();
	    Location xLeft = loc.add(-1, 0, 0);
	    loc = fishingLocation.clone();
	    Location xRight = loc.add(1, 0, 0);
        loc = fishingLocation.clone();
	    Location zBack = loc.add(0, 0, -1);
        loc = fishingLocation.clone();
	    Location zForward = loc.add(0, 0, 1);
	    
	    available.add(fishingLocation);

	    if(xLeft.getBlock().getTypeId() == 9 || xLeft.getBlock().getTypeId() == 8)
	        available.add(xLeft);
        if(xRight.getBlock().getTypeId() == 9 || xRight.getBlock().getTypeId() == 8)
            available.add(xRight);
        if(zBack.getBlock().getTypeId() == 9 || zBack.getBlock().getTypeId() == 8)
            available.add(zBack);
        if(zForward.getBlock().getTypeId() == 9 || zForward.getBlock().getTypeId() == 8)
            available.add(zForward);
	}
	
	private void cast() {
	    findAvailable();
        fishHook = new EntityFishingHook(nmsworld, eh);
        nmsworld.addEntity(fishHook);
        
        int x = (int)(Math.random()*available.size());
        
        fishingSpot = available.get(x);
        Vector v1 = fishHook.getBukkitEntity().getLocation().toVector();
        Vector v2 = fishingSpot.toVector();
        Vector v3 = v2.subtract(v1).normalize().multiply(1.5);
        
        ((Projectile) fishHook.getBukkitEntity()).setShooter(npc.getBukkitEntity());
        PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());
        fishHook.getBukkitEntity().setVelocity(v3);
        
	}
	
	private void reel() {
	    
	    int chance = (int)(Math.random()*100);

	    if(chance > 65) {
	        
	        fish = fishingSpot.getWorld().dropItem(fishingSpot, (new ItemStack(Material.RAW_FISH)));
	        Vector v1 = fish.getLocation().toVector();
	        Vector v2 = npc.getBukkitEntity().getLocation().add(0,14,0).toVector();
	        Vector v3 = v2.subtract(v1).normalize().multiply(0.975);
	        fish.setVelocity(v3);
	    }
	    PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());
        fishHook.getBukkitEntity().remove();
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
        PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());
		dB.log("...set hook velocity");
		fishHook.getBukkitEntity().setVelocity(v3);
		
        dB.log("..." + npc.getName() + " should be fishing!");
		fishing = true;
		fishingLocation = location;
		fishingSpot = fishingLocation.clone();
	    findAvailable();
	}
	
	/**
	 * Makes the stop fishing.
	 */
	public void stopFishing() {
        PlayerAnimation.ARM_SWING.play((Player) npc.getBukkitEntity());

		dB.log("...removing fish hook.");
		fishHook.getBukkitEntity().remove();
		
		fishingLocation = null;
		fishingSpot = null;
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
