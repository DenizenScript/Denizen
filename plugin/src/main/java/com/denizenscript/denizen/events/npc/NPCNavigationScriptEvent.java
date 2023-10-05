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
    // npc begins|completes|cancels navigation
    //
    // @Group NPC
    //
    // @Location true
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an NPC begins, finishes, or cancels navigating.
    //
    // @Switch npc:<npc> to only process the event if the spawned NPC matches.
    //
    // @Context
    // None
    //
    // @NPC Always.
    //
    // -->

    public NPCNavigationScriptEvent() {
        registerCouldMatcher("npc begins|completes|cancels navigation");
        registerSwitches("npc");
    }

    public NPCTag npc;
    public String type;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("npc", npc)) {
            return false;
        }
        if (!runInCheck(path, npc.getLocation())) {
            return false;
        }
        if (!path.eventArgLowerAt(1).equals(type)) {
            return false;
        }
        return super.matches(path);
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
