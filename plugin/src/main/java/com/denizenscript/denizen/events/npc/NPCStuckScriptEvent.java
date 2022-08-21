package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCStuckScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // npc stuck
    //
    // @Group NPC
    //
    // @Location true
    //
    // @Triggers when an NPC's navigator is stuck.
    //
    // @Context
    // <context.action> returns 'teleport' or 'none'
    //
    // @Determine
    // "NONE" to do nothing.
    // "TELEPORT" to teleport.
    //
    // @NPC Always.
    //
    // -->

    public NPCStuckScriptEvent() {
        instance = this;
        registerCouldMatcher("npc stuck");
    }

    public static NPCStuckScriptEvent instance;
    public NavigationStuckEvent event;
    public NPCTag npc;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, npc.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, npc);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String lowVal = CoreUtilities.toLowerCase(determinationObj.toString());
        if (lowVal.equals("none")) {
            event.setAction(null);
            return true;
        }
        else if (lowVal.equals("teleport")) {
            event.setAction(TeleportStuckAction.INSTANCE);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "action": return new ElementTag(event.getAction() == TeleportStuckAction.INSTANCE ? "teleport" : "none");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void navStuck(NavigationStuckEvent event) {
        this.npc = new NPCTag(event.getNPC());
        this.event = event;
        fire(event);
    }
}
