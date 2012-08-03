package net.aufdemrand.denizen.activities.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.event.NPCPushEvent;


public class WanderGoal implements Goal {

	public void pushable(NPCPushEvent event) {
		// if sentry ...
		event.setCancelled(false);
	}
	
	
	DenizenNPC denizenNPC;
	Location wanderLocation = null;
	final double X;
	final double Y;
	final double Z;
	final World world;
	Boolean distracted = false;
	WanderActivity wA;
	private GoalSelector goalSelecter;

	WanderGoal(DenizenNPC npc, WanderActivity wA) {
		this.denizenNPC = npc;
		this.wA = wA;
		this.X = npc.getLocation().getX();
		this.Y = npc.getLocation().getY();
		this.Z = npc.getLocation().getZ();
		this.world = npc.getWorld();
		this.wanderLocation = wA.getNewLocation(X, Y, Z, world);
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
				wanderLocation = wA.getNewLocation(X, Y, Z, world);
				wA.cooldown(denizenNPC);
				denizenNPC.getNavigator().setTarget(wanderLocation);
				goalSelecter.finish();
				}
		} else 
			Bukkit.getLogger().info("wanderLocation went null!");
	}

	@Override
	public boolean shouldExecute(GoalSelector arg0) {
		this.goalSelecter = arg0;
		return (wA.isCool(denizenNPC));
	}

}
