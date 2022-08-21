package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;

public class EntityDespawnScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // <entity> despawns
    //
    // @Group Entity
    //
    // @Warning this event fires very rapidly.
    //
    // @Location true
    //
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
    //
    // @Triggers when an entity despawns permanently from the world. May fire repeatedly for one entity.
    //
    // @Context
    // <context.entity> returns the entity that despawned.
    // <context.cause> returns the reason the entity despawned. Can be: DEATH, CHUNK_UNLOAD, CITIZENS, or OTHER
    //
    // @NPC when the entity that despawned is an NPC.
    //
    // -->

    public EntityDespawnScriptEvent() {
        instance = this;
        registerCouldMatcher("<entity> despawns");
        registerSwitches("cause");
    }

    public static EntityDespawnScriptEvent instance;
    public EntityTag entity;
    public ElementTag cause;

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(target)) {
            return false;
        }
        if (!path.checkSwitch("cause", cause.asLowerString())) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        return super.getContext(name);
    }
}
