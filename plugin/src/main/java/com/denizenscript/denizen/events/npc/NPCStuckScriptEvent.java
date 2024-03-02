package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // @Switch npc:<npc> to only process the event if the spawned NPC matches.
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
        registerCouldMatcher("npc stuck");
        registerSwitches("npc");
        this.<NPCStuckScriptEvent, ElementTag>registerDetermination(null, ElementTag.class, (evt, context, action) -> {
            evt.event.setAction(action.asLowerString().equals("none") ? null : TeleportStuckAction.INSTANCE);
        });
    }

    public NavigationStuckEvent event;
    public NPCTag npc;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("npc", npc)) {
            return false;
        }
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
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "action" -> new ElementTag(event.getAction() == TeleportStuckAction.INSTANCE ? "teleport" : "none");
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void navStuck(NavigationStuckEvent event) {
        this.npc = new NPCTag(event.getNPC());
        this.event = event;
        fire(event);
    }
}
