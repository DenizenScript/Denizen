package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class EntityDespawnScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // entity despawns
    // <entity> despawns
    //
    // @Regex ^on [^\s]+ despawns$
    // @Switch in <area>
    //
    // @Warning this event fires very rapidly.
    //
    // @Switch cause death|chunk_unload|other
    //
    // @Triggers when an entity despawns permanently from the world. May fire repeatedly for one entity.
    //
    // @Context
    // <context.entity> returns the entity that despawned.
    // <context.cause> returns the reason the entity despawned. Can be: DEATH, CHUNK_UNLOAD, or OTHER
    //
    // @NPC when the entity that despawned is an NPC.
    //
    // -->


    public EntityDespawnScriptEvent() {
        instance = this;
    }

    public static EntityDespawnScriptEvent instance;
    public dEntity entity;
    public Element cause;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.xthArgEquals(1, CoreUtilities.toLowerCase(s), "despawns");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String target = path.eventArgLowerAt(0);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!checkSwitch(lower, "cause", CoreUtilities.toLowerCase(cause.asString()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityDespawn";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(entity.getBukkitEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(entity.getBukkitEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        return super.getContext(name);
    }
}
