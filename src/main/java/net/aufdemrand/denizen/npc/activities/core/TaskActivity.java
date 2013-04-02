package net.aufdemrand.denizen.npc.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.activities.AbstractActivity;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.Goal;

public class TaskActivity extends AbstractActivity {

	private Map<dNPC, List<TaskGoal>> taskMap = new HashMap<dNPC, List<TaskGoal>>();

	public boolean addGoal(dNPC dNPC, String[] arguments, int priority) {
		
		dB.echoDebug("Adding TASK Activity.");

		// Defaults
		int delay = 60;
		int repeats = -1;
		String script = null;
		int duration = 50;

		for (String arg : arguments) {
			
			if (aH.matchesValueArg("DELAY", arg, ArgumentType.Integer)) {
				delay = aH.getIntegerFrom(arg);
			
			} 	else if (aH.matchesValueArg("REPEATS", arg, ArgumentType.Integer)) {
				repeats = aH.getIntegerFrom(arg);
			
			} 	else if (aH.matchesValueArg("DURATION", arg, ArgumentType.Integer)) {
				duration = aH.getIntegerFrom(arg);
			
			} 	else if (aH.matchesScript(arg)) {
				script = aH.getStringFrom(arg);
			
			}	else dB.echoError("Could not match argument '%s'.", arg);
		}

		if (script == null) {
			dB.echoError("No script defined!");
			return false;
		}
		
		List<TaskGoal> taskGoals = taskMap.get(dNPC);
		if (taskGoals == null) {
			taskGoals = new ArrayList<TaskGoal>();
			taskMap.put(dNPC, taskGoals);
		}
		taskGoals.add(0, new TaskGoal(dNPC, delay, duration, script, repeats, this));
		dNPC.getCitizen().getDefaultGoalController().addGoal(taskGoals.get(0), priority);
		return true;
	}

	public boolean removeGoal(dNPC dNPC, boolean verbose) {
		if (taskMap.containsKey(dNPC)) {
			for (Goal goal : taskMap.get(dNPC))
				dNPC.getCitizen().getDefaultGoalController().removeGoal(goal);
			taskMap.remove(dNPC);
			if (verbose) dB.log("Removed Task Activity from NPC.");
			return true;
		} 
		
		if (verbose) dB.log("NPC does not have this activity!");
		return false;
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}
