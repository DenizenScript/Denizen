package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public class EntityCombustsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> combusts
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity catches fire.
    //
    // @Context
    // <context.entity> returns the entity that caught fire.
    // <context.duration> returns the length of the burn.
    // <context.source> returns the EntityTag or LocationTag that caused the fire, if any. NOTE: Currently, if the source is a LocationTag, the tag will return a null. It is expected that this will be fixed by Spigot in the future.
    // <context.source_type> returns the type of the source, which can be: ENTITY, LOCATION, NONE.
    //
    // @Determine
    // DurationTag set the burn duration.
    //
    // @Player when the entity that catches fire is a player.
    //
    // @NPC when the entity that catches fire is an NPC.
    //
    // -->

    public EntityCombustsScriptEvent() {
        instance = this;
        registerCouldMatcher("<entity> combusts");
    }

    public static EntityCombustsScriptEvent instance;
    public EntityTag entity;
    public EntityCombustEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityCombusts";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setDuration(((ElementTag) determinationObj).asInt());
            return true;
        }
        else if (DurationTag.matches(determinationObj.toString())) {
            event.setDuration(DurationTag.valueOf(determinationObj.toString(), getTagContext(path)).getTicksAsInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "duration":
                return new DurationTag(event.getDuration());
            case "source":
                if (event instanceof EntityCombustByEntityEvent) {
                    return new EntityTag(((EntityCombustByEntityEvent) event).getCombuster()).getDenizenObject();
                }
                else if (event instanceof EntityCombustByBlockEvent) {
                    Block combuster = ((EntityCombustByBlockEvent) event).getCombuster();
                    if (combuster != null) {
                        return new LocationTag(combuster.getLocation());
                    }
                }
                break;
            case "source_type":
                if (event instanceof EntityCombustByEntityEvent) {
                    return new ElementTag("ENTITY");
                }
                else if (event instanceof EntityCombustByBlockEvent) {
                    return new ElementTag("LOCATION");
                }
                return new ElementTag("NONE");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCombusts(EntityCombustEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
