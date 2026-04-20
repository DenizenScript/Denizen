package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EntityFormsBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> forms <block>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block is formed by an entity.
    // For example, when a snowman forms snow.
    //
    // @Context
    // <context.location> returns the LocationTag the block.
    // <context.old_material> returns the MaterialTag of the original block.
    // <context.new_material> returns the MaterialTag of the block that will be formed.
    // <context.entity> returns the EntityTag that formed the block.
    //
    // -->

    public EntityFormsBlockScriptEvent() {
        registerCouldMatcher("<entity> forms <block>");
    }

    public MaterialTag old_material;
    public MaterialTag new_material;
    public LocationTag location;
    public EntityTag entity;
    public EntityBlockFormEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!path.tryArgObject(2, new_material)) {
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
        return switch (name) {
            case "location" -> location;
            case "material" -> {
                BukkitImplDeprecations.entityFormsBlockMaterialContext.warn();
                yield old_material;
            }
            case "old_material" -> old_material;
            case "new_material" -> new_material;
            case "entity" -> entity;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityFormsBlock(EntityBlockFormEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        old_material = new MaterialTag(event.getBlock());
        new_material = new MaterialTag(event.getNewState());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
