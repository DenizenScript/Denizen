package net.aufdemrand.denizen.npc.activities.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;


public class TaskGoal implements Goal {

    private dNPC npc;
    private TaskActivity activity;
    private final int delay;
    private final int duration;
    private final int repeats;
    private final String scriptName;

    private int counter = 0;

    public TaskGoal(dNPC npc, int delay, int duration, String scriptName, int repeats, TaskActivity activityInstance) {
        this.activity = activityInstance;
        this.npc = npc;
        this.delay = delay;
        this.duration = duration;
        this.repeats = repeats;
        this.scriptName = scriptName;
    }

    @Override
    public void reset() {   }

    private long dur = 0;

    @Override
    public void run(GoalSelector goalSelecter) {
        if (dur > 0) 
            if (dur < System.currentTimeMillis()) {
                counter++;
                cooldown();
                dur = 0;
                goalSelecter.finish();

            } else {
                dur = System.currentTimeMillis() + (this.duration * 1000);
                TaskScriptContainer taskScript = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
                taskScript.runTaskScript(null, npc, null);
            }
    }

    @Override
    public boolean shouldExecute(GoalSelector arg0) {
        if (counter <= repeats || repeats == -1) return (isCool());
        return false;
    }

    private long cooldown = 0;

    public void cooldown() {
        cooldown = System.currentTimeMillis() + (this.delay * 1000);
    }

    public boolean isCool() {
        if (cooldown < System.currentTimeMillis()) return true;
        else return false;
    }

}