package net.aufdemrand.denizen.npc.activities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.activities.AbstractActivity;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.citizensnpcs.api.ai.Goal;

public class TaskActivity extends AbstractActivity {

	private Map<DenizenNPC, List<TaskGoal>> taskMap = new HashMap<DenizenNPC, List<TaskGoal>>();

	public boolean addGoal(DenizenNPC denizenNPC, String[] arguments, int priority) {
		
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
		
		List<TaskGoal> taskGoals = new ArrayList<TaskGoal>();
		if (taskMap.containsKey(denizenNPC)) 
			taskGoals = taskMap.get(denizenNPC);
		taskGoals.add(0, new TaskGoal(denizenNPC, delay, duration, script, repeats, this));
		taskMap.put(denizenNPC, taskGoals);
		denizenNPC.getCitizen().getDefaultGoalController().addGoal(taskMap.get(denizenNPC).get(0), priority);
		return true;
	}

	public boolean removeGoal(DenizenNPC denizenNPC, boolean verbose) {
		if (taskMap.containsKey(denizenNPC)) {
			for (Goal goal : taskMap.get(denizenNPC))
				denizenNPC.getCitizen().getDefaultGoalController().removeGoal(goal);
			taskMap.remove(denizenNPC);
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
