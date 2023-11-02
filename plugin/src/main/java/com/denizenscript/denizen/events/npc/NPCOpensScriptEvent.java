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
    // @Switch npc:<npc> to only process the event if the spawned NPC matches.
    //
    // @Context
    // <context.location> returns the location of the door or gate opened.
    //
    // @NPC Always.
    //
    // -->

    public NPCOpensScriptEvent() {
        registerCouldMatcher("npc opens <block>");
        registerSwitches("npc");
    }

    public NPCTag npc;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("npc", npc)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(2, new MaterialTag(location.getBlock()))) {
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
            case "location" -> location;
            default -> super.getContext(name);
        };
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
