package net.aufdemrand.denizen.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.aufdemrand.denizen.activities.AbstractActivity;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.Goal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class WanderActivity extends AbstractActivity {



	private Map<DenizenNPC, List<WanderGoal>> wanderMap = new HashMap<DenizenNPC, List<WanderGoal>>();





	public void addGoal(DenizenNPC npc, String[] arguments, int priority) {

		aH.echoDebug("Adding WANDER Activity.");

		// Check Arguments

		int delay = 10;
		int radius = 5;
		int depth = 2;
		float speed = npc.getNavigator().getDefaultParameters().speed();
		Location specifiedLocation = null;
		ArrayList<Material> materialList = new ArrayList<Material>();

		npc.getNavigator().getDefaultParameters().avoidWater(true);

		for (String thisArgument : arguments) {
			if (thisArgument.toUpperCase().contains("DELAY:")) {
				try { delay = Integer.valueOf(thisArgument.split(":", 2)[1]); }
				catch (NumberFormatException e) { aH.echoError("...bad argument '%s'!", thisArgument); }
			}

			else if (thisArgument.toUpperCase().contains("RADIUS:")) {
				try { radius = Integer.valueOf(thisArgument.split(":", 2)[1]); }
				catch (NumberFormatException e) { aH.echoError("...bad argument '%s'!", thisArgument); }
			}

			else if (thisArgument.toUpperCase().contains("DEPTH:")) {
				try { depth = Integer.valueOf(thisArgument.split(":", 2)[1]); }
				catch (NumberFormatException e) { aH.echoError("...bad argument '%s'!", thisArgument); }
			}

			else if (thisArgument.toUpperCase().contains("SPEED:")) {
				try { speed = Float.valueOf(thisArgument.split(":", 2)[1]); }
				catch (NumberFormatException e) { aH.echoError("...bad argument '%s'!", thisArgument); }
			}

			else if (thisArgument.toUpperCase().contains("FILTER:")) {
				for (String materialString : thisArgument.split(":")[1].split(",")) {
					try { materialList.add(Material.valueOf(materialString.toUpperCase().trim())); }
					catch(Exception e) { aH.echoError("...bad argument '%s'! Check to be sure this is a valid Material.", thisArgument); }
				}
			}

			/* If argument is a BOOKMARK modifier */
			else if (aH.matchesBookmark(thisArgument)) {
				if (aH.getBookmarkModifier(thisArgument, npc) != null)					
					specifiedLocation = aH.getBookmarkModifier(thisArgument, npc);
				aH.echoDebug("...will wander '%s'.", thisArgument);
			}
		}
		
		List<WanderGoal> wanderGoals = new ArrayList<WanderGoal>();

		if (wanderMap.containsKey(npc)) 
			wanderGoals = wanderMap.get(npc);

		wanderGoals.add(0, new WanderGoal(npc, radius, depth, delay, speed, materialList, specifiedLocation, this));

		wanderMap.put(npc, wanderGoals);
		npc.getCitizensEntity().getDefaultGoalController().addGoal(wanderMap.get(npc).get(0), priority);	

	}



	public void removeGoal(DenizenNPC npc, boolean verbose) {

		if (wanderMap.containsKey(npc)) {
			for (Goal goal : wanderMap.get(npc))
				npc.getCitizensEntity().getDefaultGoalController().removeGoal(goal);
			wanderMap.remove(npc);
			if (verbose) plugin.getLogger().info("Removed Wander Activity from NPC.");
		} 

		else if (verbose) plugin.getLogger().info("NPC does not have this activity...");

	}

	public Location getNewLocation(double X, double Y, double Z, World world, int radius, int depth) {

		Location newLocation = new Location(world, X, Y, Z);

		Random intRandom = new Random();
		int randomX = intRandom.nextInt(radius * 2) - radius;
		int randomZ = intRandom.nextInt(radius * 2) - radius;
		int randomY = 0;
		if (depth > 1) randomY = intRandom.nextInt(depth * 2) - depth;
		else randomY = 2;

		newLocation.setX(newLocation.getX() + randomX);
		newLocation.setZ(newLocation.getZ() + randomZ);
		newLocation.setY(newLocation.getY() + randomY);

		if (newLocation != null) Bukkit.getServer().getLogger().info("Location not null...");
		return newLocation;
	}


}
