package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.citizensnpcs.api.event.NPCOpenDoorEvent;
import net.citizensnpcs.api.event.NPCOpenGateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCOpensScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // npc opens <block>
    //
    // @Group NPC
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an NPC opens a door or gate.
    //
    // @Context
    // <context.location> returns the location of the door or gate opened.
    //
    // @NPC Always.
    //
    // -->

    public NPCOpensScriptEvent() {
        instance = this;
        registerCouldMatcher("npc opens <block>");
    }

    public static NPCOpensScriptEvent instance;
    public NPCTag npc;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!new MaterialTag(location.getBlock()).tryAdvancedMatcher(path.eventArgLowerAt(2))) {
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
        switch (name) {
            case "location": return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void NPCOpenDoor(NPCOpenDoorEvent event) {
        npc = new NPCTag(event.getNPC());
        location = new LocationTag(event.getDoor().getLocation());
        fire(event);
    }

    @EventHandler
    public void NPCOpenGate(NPCOpenGateEvent event) {
        npc = new NPCTag(event.getNPC());
        location = new LocationTag(event.getGate().getLocation());
        fire(event);
    }
}
