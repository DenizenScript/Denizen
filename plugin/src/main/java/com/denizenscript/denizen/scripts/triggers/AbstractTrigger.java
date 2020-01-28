package com.denizenscript.denizen.scripts.triggers;

import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.scripts.queues.core.TimedQueue;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;

import java.util.*;

public abstract class AbstractTrigger {

    protected String name;

    public AbstractTrigger as(String triggerName) {
        this.name = triggerName.toUpperCase();
        // Register command with Registry
        DenizenAPI.getCurrentInstance().getTriggerRegistry().register(triggerName, this);
        onEnable();
        return this;
    }

    public String getName() {
        // Return the name of the trigger specified upon registration.
        return name;
    }

    /**
     * Part of the Plugin disable sequence.
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     */
    public void onDisable() {
        // Nothing to do here on this level of abstraction.
    }

    /**
     * Part of the Plugin enable sequence.
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when bukkit sends a
     * onEnable() to Denizen. (ie. Server shuts down or restarts)
     */
    public void onEnable() {
        // Nothing to do here on this level of abstraction.
    }

    /**
     * Part of the Plugin enable sequence.
     * <p/>
     * Can be '@Override'n by a Trigger which requires a method when being enabled via
     * the Trigger Registry, usually upon startup.
     */
    public AbstractTrigger activate() {
        // Nothing to do here on this level of abstraction.
        return this;
    }

    public boolean parse(NPCTag npc, PlayerTag player, InteractScriptContainer script, String id) {
        return parse(npc, player, script, id, null);
    }

    public boolean parse(NPCTag npc, PlayerTag player, InteractScriptContainer script, String id, Map<String, ObjectTag> context) {
        if (npc == null || player == null || script == null) {
            return false;
        }

        List<ScriptEntry> entries = script.getEntriesFor(this.getClass(), player, npc, id, true);
        if (entries.isEmpty()) {
            return false;
        }

        Debug.echoDebug(script, DebugElement.Header, "Parsing " + name + " trigger: n@" + npc.getName() + "/p@" + player.getName());
        // Create Queue
        long speedTicks;
        if (script.contains("SPEED")) {
            speedTicks = DurationTag.valueOf(script.getString("SPEED", "0")).getTicks();
        }
        else {
            speedTicks = DurationTag.valueOf(Settings.interactQueueSpeed()).getTicks();
        }
        ScriptQueue queue;
        if (speedTicks > 0) {
            queue = new TimedQueue(script.getName()).setSpeed(speedTicks);
        }
        else {
            queue = new InstantQueue(script.getName());
        }
        // Add all entries to set it up
        queue.addEntries(entries);
        // Add context
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        // Start it
        queue.start();

        return true;
    }
}
