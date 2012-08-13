package net.aufdemrand.denizen.activities.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;


public class WanderGoal implements Goal {

	DenizenNPC denizenNPC;
	Location wanderLocation = null;
	final double X;
	final double Y;
	final double Z;
	final int radius;
	final int delay;
	final float speed;
	final World world;
	Boolean distracted = false;
	WanderActivity wA;
	private GoalSelector goalSelecter;

	WanderGoal(DenizenNPC npc, Integer radius, Integer delay, float speed, WanderActivity wA) {
		this.denizenNPC = npc;
		this.radius = radius;
		this.delay = delay;
		this.wA = wA;
		this.speed = speed;
		this.X = npc.getLocation().getX();
		this.Y = npc.getLocation().getY();
		this.Z = npc.getLocation().getZ();
		this.world = npc.getWorld();
		this.wanderLocation = wA.getNewLocation(X, Y, Z, world, radius);
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
				denizenNPC.getNavigator().setSpeed(speed);
				wanderLocation = wA.getNewLocation(X, Y, Z, world, radius);
				wA.cooldown(denizenNPC, delay);
				if (wanderLocation.getBlock().getType() != Material.WATER
						&& wanderLocation.getBlock().getType() != Material.LAVA)
					denizenNPC.getNavigator().setTarget(wanderLocation);
				goalSelecter.finish();
			}
		} else 
			Bukkit.getLogger().info("Oh no! wanderLocation went null! Report this to aufdemrand.");
	}

	@Override
	public boolean shouldExecute(GoalSelector arg0) {
		this.goalSelecter = arg0;
		return (wA.isCool(denizenNPC));
	}

}
