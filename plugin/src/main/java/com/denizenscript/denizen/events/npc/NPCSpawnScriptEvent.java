package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCSpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // npc spawns
    //
    // @Group NPC
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an NPC spawns.
    //
    // @Switch npc:<npc> to only process the event if the spawned NPC matches.
    // @Switch reason:<reason> to only process the event if the NPC's spawn reason matches. See <@link url https://jd.citizensnpcs.co/net/citizensnpcs/api/event/SpawnReason.html> for a list of reasons.
    //
    // @Context
    // <context.location> returns the location the entity will spawn at.
    // <context.reason> returns the reason of the spawn.
    //
    // @NPC Always.
    //
    // -->

    public NPCSpawnScriptEvent() {
        registerCouldMatcher("npc spawns");
        registerSwitches("npc", "reason");
    }

    public NPCTag npc;
    public LocationTag location;
    public NPCSpawnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("npc", npc)) {
            return false;
        }
        if (!path.tryObjectSwitch("reason", new ElementTag(event.getReason()))) {
            return false;
        }
        if (!runInCheck(path, location)) {
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
            case "reason" -> new ElementTag(event.getReason());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent event) {
        this.npc = new NPCTag(event.getNPC());
        location = new LocationTag(event.getLocation());
        this.event = event;
        fire(event);
    }
}
