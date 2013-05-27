package net.aufdemrand.denizen.npc.activities.core;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.aufdemrand.denizen.objects.dNPC;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

public class WanderGoal implements Goal {

	final dNPC dNPC;
	final double X;
	final double Y;
	final double Z;
	final int radius;
	final int depth;
	final int delay;
	final float speed;
	final World world;
	final ArrayList<Material> materials;
	final ArrayList<Integer> materialIds;
	final WanderActivity wA;
	Location wanderLocation = null;
	Long cooldownTimer;

	WanderGoal(dNPC npc, Integer radius, Integer depth, Integer delay, float speed, ArrayList<Material> materials, ArrayList<Integer> materialIds, Location bookmark, WanderActivity wA) {
		this.materialIds = materialIds;
		this.materials = materials;
		cooldownTimer = 0L;
		this.dNPC = npc;
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

	@Override
	public void run(GoalSelector goalSelecter) {

		// If already navigating, nothing to do here...
		if (dNPC.getNavigator().isNavigating()) return;

		// If not already navigating.. let's find a new block to navigate to.
		dNPC.getNavigator().getDefaultParameters().speedModifier(speed);
		wanderLocation = wA.getNewLocation(X, Y, Z, world, radius, depth);
		if (wanderLocation.getWorld().getBlockAt(wanderLocation.getBlockX(), wanderLocation.getBlockY() + 2, wanderLocation.getBlockZ()).getType() != Material.AIR) { 
			wanderLocation = wA.getNewLocation(X, Y, Z, world, radius, depth);
			goalSelecter.finish();
			return;
		}

		if (!materials.isEmpty()) {
			boolean move = false;
			for (Material acceptableMaterial : materials)
				if (wanderLocation.getBlock().getType() == acceptableMaterial) move = true;
			if (materialIds.contains(Integer.valueOf(wanderLocation.getBlock().getTypeId()))) move = true;

			if (move) dNPC.getNavigator().setTarget(wanderLocation);

			goalSelecter.finish();
		} else {
			dNPC.getNavigator().setTarget(wanderLocation);
			goalSelecter.finish();
		}
	}

@Override
public boolean shouldExecute(GoalSelector arg0) {
	if (!dNPC.getNavigator().isNavigating()) return (isCool());
	else return false;
}

public void cooldown() {
	cooldownTimer = System.currentTimeMillis() + (this.delay * 1000);
}

public boolean isCool() {
	if (cooldownTimer < System.currentTimeMillis()) return true;
	else return false;
}

}