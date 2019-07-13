package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public class EntityCombustsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity combusts
    // <entity> combusts
    //
    // @Regex ^on [^\s]+ combusts$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity catches fire.
    //
    // @Context
    // <context.entity> returns the entity that caught fire.
    // <context.duration> returns the length of the burn.
    // <context.source> returns the dEntity or dLocation that caused the fire, if any. NOTE: Currently, if the source is a dLocation, the tag will return a null. It is expected that this will be fixed by Spigot in the future.
    // <context.source_type> returns the type of the source, which can be: ENTITY, LOCATION, NONE.
    //
    // @Determine
    // Element(Number) set the length of duration.
    //
    // @Player when the entity that catches fire is a player.
    //
    // @NPC when the entity that catches fire is an NPC.
    //
    // -->

    public EntityCombustsScriptEvent() {
        instance = this;
    }

    public static EntityCombustsScriptEvent instance;
    public dEntity entity;
    private int burntime;
    public EntityCombustEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("combusts");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityCombusts";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            burntime = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("duration")) {
            return new Duration(burntime);
        }
        else if (name.equals("source")) {
            if (event instanceof EntityCombustByEntityEvent) {
                return new dEntity(((EntityCombustByEntityEvent) event).getCombuster());
            }
            else if (event instanceof EntityCombustByBlockEvent) {
                Block combuster = ((EntityCombustByBlockEvent) event).getCombuster();
                if (combuster != null) {
                    return new dLocation(combuster.getLocation());
                }
            }
        }
        else if (name.equals("source_type")) {
            if (event instanceof EntityCombustByEntityEvent) {
                return new Element("ENTITY");
            }
            else if (event instanceof EntityCombustByBlockEvent) {
                return new Element("LOCATION");
            }
            return new Element("NONE");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCombusts(EntityCombustEvent event) {
        entity = new dEntity(event.getEntity());
        burntime = event.getDuration();
        this.event = event;
        fire(event);
        event.setDuration(burntime);
    }
}
