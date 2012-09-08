package net.aufdemrand.denizen.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;


public class WanderGoal implements Goal {

	DenizenNPC denizenNPC;
	Location wanderLocation = null;
	final double X;
	final double Y;
	final double Z;
	final int radius;
	final int depth;
	final int delay;
	final float speed;
	final World world;
	final ArrayList<Material> materials;
	WanderActivity wA;

	private Long cooldownMap;

	WanderGoal(DenizenNPC npc, Integer radius, Integer depth, Integer delay, float speed, ArrayList<Material> materials, Location bookmark, WanderActivity wA) {
		this.materials = materials;
		cooldownMap = 12345L;
		this.denizenNPC = npc;
		this.radius = radius;
		this.depth = depth;
		this.delay = delay;
		this.wA = wA;
		this.speed = speed;

		if (bookmark == null) {
			this.X = npc.getLocation().getX();
			this.Y = npc.getLocation().getY();
			this.Z = npc.getLocation().getZ();
			this.world = npc.getWorld();
		} else {
			this.X = bookmark.getX();
			this.Y = bookmark.getY();
			this.Z = bookmark.getZ();
			this.world = bookmark.getWorld();
		}

		this.wanderLocation = wA.getNewLocation(X, Y, Z, world, radius, depth);
	}


	@Override
	public void reset() {
	}

//	@EventHandler
    public void navFail(NavigationCancelEvent event) {
		if (event.getNavigator().getLocalParameters().hashCode() == denizenNPC.getNavigator().getLocalParameters().hashCode())
			if (event.getCancelReason().equals(CancelReason.STUCK))
				if (event.getNavigator().getLocalParameters().avoidWater() && event.getNavigator().getTargetAsLocation().getBlock().isLiquid()) {
					denizenNPC.getEntity().teleport(new Location(world,X,Y,Z));
				}
	}

	@EventHandler
	public void navComplete(NavigationCompleteEvent event) {
		if (event.getNavigator().getTargetAsLocation() == wanderLocation)
			cooldown();
	}


	@Override
	public void run(GoalSelector goalSelecter) {
//		Bukkit.getServer().getLogger().info("Wandering...");
		if (wanderLocation != null) {
	//		Bukkit.getServer().getLogger().info("Wandering not null...");
			// If already navigating, nothing to do here...
			if (denizenNPC.getNavigator().isNavigating()) {
				return; }

			// If not already navigating.. let's find a new block to navigate to.
			else {
				denizenNPC.getNavigator().getDefaultParameters().speed(speed);
				wanderLocation = wA.getNewLocation(X, Y, Z, world, radius, depth);

				if (!materials.isEmpty()) {
					Boolean move = false;
					for (Material acceptableMaterial : materials) {
						if (wanderLocation.getBlock().getType() == acceptableMaterial) move = true;
					}

					if (move) denizenNPC.getNavigator().setTarget(wanderLocation);
					goalSelecter.finish();

				} else {
					denizenNPC.getNavigator().setTarget(wanderLocation);
					goalSelecter.finish();
				}
			}
		 } else wanderLocation = wA.getNewLocation(X, Y, Z, world, radius, depth);
	}


	@Override
	public boolean shouldExecute(GoalSelector arg0) {
		// Bukkit.getServer().getLogger().info("Should wander? " + isCool());
		return (isCool());
	}
	
	public void cooldown() {
		cooldownMap = System.currentTimeMillis() + (this.delay * 1000);
	}

	public boolean isCool() {
		if (cooldownMap < System.currentTimeMillis()) return true;
			else return false;
	}

}