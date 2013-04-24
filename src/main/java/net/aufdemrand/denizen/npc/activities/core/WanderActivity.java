package net.aufdemrand.denizen.npc.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.activities.AbstractActivity;
import net.aufdemrand.denizen.utilities.RandomGenerator;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WanderActivity extends AbstractActivity implements Listener {

	private Map<NPC, WanderGoal> wanderMap = new HashMap<NPC, WanderGoal>();
	
	public boolean addGoal(dNPC npc, String[] arguments, int priority) {
		dB.echoDebug("Adding WANDER Activity.");

		// Defaults
		int delay = 10;
		int radius = 5;
		int depth = 2;
		float speed = npc.getNavigator().getDefaultParameters().speedModifier();

		Location specifiedLocation = null;
		ArrayList<Material> materialList = new ArrayList<Material>();
		ArrayList<Integer> materialIdList = new ArrayList<Integer>();

		npc.getNavigator().getDefaultParameters().avoidWater(true);

		for (String arg : arguments) {

			if (aH.matchesValueArg("DELAY", arg, ArgumentType.Integer)) {
				delay = aH.getIntegerFrom(arg);
			}

			else if (aH.matchesValueArg("RADIUS", arg, ArgumentType.Integer)) {
				radius = aH.getIntegerFrom(arg);
			}

			else if (aH.matchesValueArg("DEPTH", arg, ArgumentType.Integer)) {
				depth = aH.getIntegerFrom(arg);
			}

			else if (aH.matchesValueArg("SPEED", arg, ArgumentType.Float)) {
				speed = aH.getFloatFrom(arg);
			}

			else if (aH.matchesValueArg("FILTER", arg, ArgumentType.Custom)) 
				for (String materialString : arg.split(":")[1].split(",")) 
					try { if (materialString.matches("//d+"))	materialIdList.add(Integer.valueOf(materialString));
						  else materialList.add(Material.valueOf(materialString.toUpperCase().trim()));
					} catch(Exception e) { dB.echoError("...bad argument '%s'! Check to be sure this is a valid Material.", arg); }

			else if (aH.matchesLocation(arg)) {
				if (aH.getLocationFrom(arg) != null)					
					specifiedLocation = aH.getLocationFrom(arg);
			}
			
			else dB.echoError("Could not match argument '%s'.", arg);
		}

		if (wanderMap.containsKey(npc)) {
			dB.echoDebug("Already found a WANDER instance for this NPC! Removing existing and adding new.");
			removeGoal(npc, true);
		}

		wanderMap.put(npc.getCitizen(), new WanderGoal(npc, radius, depth, delay, speed, materialList, materialIdList, specifiedLocation, this));
		npc.getCitizen().getDefaultGoalController().addGoal(wanderMap.get(npc.getCitizen()), priority);
		return true;
	}

	public boolean removeGoal(dNPC npc, boolean verbose) {
	    WanderGoal goal = wanderMap.remove(npc.getCitizen());
		if (goal != null) {
			npc.getCitizen().getDefaultGoalController().removeGoal(goal);
			if (verbose) dB.echoDebug("Removed Wander Activities from NPC.");
			return true;
		} 

		else if (verbose) dB.echoError("NPC does not have this activity...");
		return false;
	}

	public Location getNewLocation(double X, double Y, double Z, World world, int radius, int depth) {
		Location newLocation = new Location(world, X, Y, Z);
		
		int randomX = RandomGenerator.nextInt(radius * 2) - radius;
		int randomZ = RandomGenerator.nextInt(radius * 2) - radius;
		int randomY = 0;

		if (depth > 1) randomY = RandomGenerator.nextInt(depth * 2) - depth;
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

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

}
