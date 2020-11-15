package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCNavigationScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // npc begins navigation
    // npc completes navigation
    // npc cancels navigation
    //
    // @Group NPC
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Regex ^on npc (begins|completes|cancels) navigation$
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an NPC begins, finishes, or cancels navigating.
    //
    // @Context
    // None
    //
    // @NPC Always.
    //
    // -->

    public NPCNavigationScriptEvent() {
        instance = this;
    }

    public static NPCNavigationScriptEvent instance;
    public NPCTag npc;
    public String type;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("npc")) {
            return false;
        }
        if (!path.eventArgLowerAt(2).equals("navigation")) {
            return false;
        }
        String arg1 = path.eventArgLowerAt(1);
        if (!arg1.equals("begins") && !arg1.equals("completes") && !arg1.equals("cancels")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, npc.getLocation())) {
            return false;
        }
        if (!path.eventArgLowerAt(1).equals(type)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "NPCNavigation";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, npc);
    }

    @EventHandler
    public void navComplete(NavigationCompleteEvent event) {
        npc = new NPCTag(event.getNPC());
        type = "completes";
        fire(event);
    }

    @EventHandler
    public void navBegin(NavigationBeginEvent event) {
        npc = new NPCTag(event.getNPC());
        type = "begins";
        fire(event);
    }

    @EventHandler
    public void navCancel(NavigationCancelEvent event) {
        npc = new NPCTag(event.getNPC());
        type = "cancels";
        fire(event);
    }
}
