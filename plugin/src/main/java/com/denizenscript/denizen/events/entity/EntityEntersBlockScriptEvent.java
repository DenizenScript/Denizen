package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;

public class EntityEntersBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> enters block
    // <entity> enters <material>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Triggers when an entity enters, and is stored in a block. Does not fire when a silverfish "enters" a stone block. Prefer <@link event <entity> changes <block> (into <block>)> for that.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.location> returns the LocationTag of the block entered by the entity.
    // <context.material> returns the MaterialTag of the block entered by the entity.
    //
    // @Player when the entity that entered the block is a player
    //
    // @NPC when the entity that entered the block is an NPC.
    //
    // -->

    public EntityEntersBlockScriptEvent() {
        registerCouldMatcher("<entity> enters <block>");
    }

    public EntityTag entity;
    public MaterialTag material;
    public LocationTag location;
    public EntityEnterBlockEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(target)) {
            return false;
        }
        if (!path.tryArgObject(2, material)) {
            return false;
        }
        if (!runInCheck(path, location)) {
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
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityEntersBlock(EntityEnterBlockEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
