package net.aufdemrand.denizen.activities.core;

import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;


public class TaskGoal implements Goal {

	DenizenNPC denizenNPC;
	TaskActivity tA;
	final int delay;
	final int duration;
	final int repeats;
	final String scriptName;

	int counter;

	public TaskGoal(DenizenNPC npc, int delay, int duration, String scriptName, int repeats, TaskActivity taskActivity) {
		this.tA = taskActivity;
		this.denizenNPC = npc;
		this.delay = delay;
		this.duration = duration;
		this.repeats = repeats;
		this.counter = 0;
		this.scriptName = scriptName;
	}


	@Override
	public void reset() {
	}


	@Override
	public void run(GoalSelector goalSelecter) {

		if (hasDuration()) {
			if (getDurationTimeout() < System.currentTimeMillis()) {
				counter++;
				cooldown();
				removeDuration();
				goalSelecter.finish();
			}
		} 

		else {
			setDuration();
			ScriptHelper sE = tA.plugin.getScriptEngine().helper;
			List<String> theScript = sE.getScript(scriptName + ".Script");
			if (theScript.isEmpty()) return;
			sE.queueScriptEntries(denizenNPC, sE.buildScriptEntries(denizenNPC, theScript, scriptName), QueueType.ACTIVITY);	
		}
	}


	@Override
	public boolean shouldExecute(GoalSelector arg0) {
		if (counter <= repeats || repeats == -1) {
			return (isCool());
		}
		return false;
	}




	private Long cooldownMap = 0L;

	public void cooldown() {
		cooldownMap = System.currentTimeMillis() + (this.delay * 1000);
	}

	public boolean isCool() {
		if (cooldownMap < System.currentTimeMillis()) return true;
		else return false;
	}


	private Long durationMap = 0L;

	public void setDuration() {
		durationMap = System.currentTimeMillis() + (this.duration * 1000);
	}

	public void removeDuration() {
		durationMap = 0L;
	}

	public boolean hasDuration() {
		if (durationMap == 0L) return false;
		else return true;
	}

	public long getDurationTimeout() {
		return durationMap;
	}



}