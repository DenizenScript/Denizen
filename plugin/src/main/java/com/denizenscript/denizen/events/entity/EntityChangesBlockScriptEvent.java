package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> changes <block> (into <block>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity changes the material of a block.
    //
    // @Context
    // <context.entity> returns the EntityTag that changed the block.
    // <context.location> returns the LocationTag of the changed block.
    // <context.old_material> returns the old material of the block.
    // <context.new_material> returns the new material of the block.
    //
    // @Player when the entity that changed the block is a player.
    //
    // -->

    public EntityChangesBlockScriptEvent() {
        registerCouldMatcher("<entity> changes <block> (into <block>)");
    }

    public EntityTag entity;
    public LocationTag location;
    public MaterialTag oldMaterial;
    public MaterialTag newMaterial;
    public EntityChangeBlockEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String entName = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(entName, path.context)) {
            return false;
        }
        if (!path.tryArgObject(2, oldMaterial)) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("into")) {
            String mat2 = path.eventArgLowerAt(4);
            if (mat2.isEmpty()) {
                Debug.echoError("Invalid event material [" + getName() + "]: '" + path.event + "' for " + path.container.getName());
                return false;
            }
            else if (!newMaterial.tryAdvancedMatcher(mat2, path.context)) {
                return false;
            }
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
        return switch (name) {
            case "entity" -> entity.getDenizenObject();
            case "location" -> location;
            case "new_material" -> newMaterial;
            case "old_material" -> oldMaterial;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityChangesBlock(EntityChangeBlockEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getBlock().getLocation());
        oldMaterial = new MaterialTag(location.getBlock());
        newMaterial = new MaterialTag(event.getTo());
        this.event = event;
        fire(event);
    }
}
