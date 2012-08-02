package net.aufdemrand.denizen.activities.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;


import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

public class WanderGoal implements Goal {

	DenizenNPC denizenNPC;
	Location wanderLocation = null;
	final Location startingLocation;
	Boolean distracted = false;
	WanderActivity wA;

	WanderGoal(DenizenNPC npc, WanderActivity wA) {
		this.denizenNPC = npc;
		this.wA = wA;
		this.startingLocation = new Location(denizenNPC.getWorld(), denizenNPC.getLocation().getX(), denizenNPC.getLocation().getY(), denizenNPC.getLocation().getZ());
		this.wanderLocation = wA.getNewLocation(startingLocation);
	}

	@Override
	public void reset() {
	}

	@Override
	public void run() {
		if (wanderLocation != null) {
			if (denizenNPC.getNavigator().isNavigating()) {
				return; }
			else {
				wanderLocation = startingLocation;
				wanderLocation = wA.getNewLocation(startingLocation);
				denizenNPC.getNavigator().setTarget(wanderLocation);
				Bukkit.getLogger().info("New Navigation! " + wanderLocation.getBlock().getType().name() + "  X " + wanderLocation.getBlockX() + "  Y " + wanderLocation.getBlockY() + "  Z " + wanderLocation.getBlockZ() );
				}
		} else 
			Bukkit.getLogger().info("wanderLocation went null!");
	}

	@Override
	public boolean shouldExecute(GoalSelector arg0) {
		return true;
	}

}
