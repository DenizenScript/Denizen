package net.aufdemrand.denizen.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.aufdemrand.denizen.activities.AbstractActivity;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WanderActivity extends AbstractActivity implements Listener {

	private Map<NPC, WanderGoal> wanderMap = new HashMap<NPC, WanderGoal>();

	public void addGoal(DenizenNPC npc, String[] arguments, int priority) {

		aH.echoDebug("Adding WANDER Activity for " + npc.getName() + "/" + npc.getId());

		// Check Arguments
		int delay = 10;
		int radius = 5;
		int depth = 2;
		float speed = npc.getNavigator().getDefaultParameters().speed();
		Location specifiedLocation = null;
		ArrayList<Material> materialList = new ArrayList<Material>();
		ArrayList<Integer> materialIdList = new ArrayList<Integer>();

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
					try { 
						if (materialString.matches("//d+"))
							materialIdList.add(Integer.valueOf(materialString));
						else 
							materialList.add(Material.valueOf(materialString.toUpperCase().trim())); }
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

		if (wanderMap.containsKey(npc)) {
			aH.echoDebug("Already found a WANDER instance for this NPC! Removing first...");
			removeGoal(npc, true);
		}

		wanderMap.put(npc.getCitizensEntity(), new WanderGoal(npc, radius, depth, delay, speed, materialList, materialIdList, specifiedLocation, this));
		npc.getCitizensEntity().getDefaultGoalController().addGoal(wanderMap.get(npc.getCitizensEntity()), priority);
	}


	public void removeGoal(DenizenNPC npc, boolean verbose) {

		if (wanderMap.containsKey(npc.getCitizensEntity())) {
			npc.getCitizensEntity().getDefaultGoalController().removeGoal(wanderMap.get(npc.getCitizensEntity()));
			wanderMap.remove(npc.getCitizensEntity());
			if (verbose) plugin.getLogger().info("Removed Wander Activities from NPC.");
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

		return newLocation;
	}

	
	@EventHandler
	public void navComplete(NavigationCompleteEvent event) {
		if (wanderMap.containsKey(event.getNPC())) {
			wanderMap.get(event.getNPC()).cooldown();
		}
	}

}
