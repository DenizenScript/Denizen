package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;

public class BlockEquipsItemOnEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> equips <item> (on <entity>)
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block dispenses armor and is equipped to an entity.
    //
    // @Context
    // <context.block> returns the MaterialTag of the dispenser.
    // <context.item> returns the ItemTag of the armor being dispensed.
    // <context.entity> returns the EntityTag of the entity having the armor equipped.
    // <context.location> returns the LocationTag of the dispenser.
    //
    // @Player when the equipped entity is a player.
    //
    // @NPC when the equipped entity is an NPC.
    // -->

    public BlockEquipsItemOnEntityScriptEvent() {
        registerCouldMatcher("<block> equips <item> (on <entity>)");
    }

    MaterialTag block;
    ItemTag item;
    EntityTag entity;
    LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, block)) {
            return false;
        }
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("on") && !path.tryArgObject(4, entity)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "block" -> block;
            case "item" -> item;
            case "entity" -> entity;
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @EventHandler
    public void onBlockEquipsItemOntoEntity(BlockDispenseArmorEvent event) {
        block = new MaterialTag(event.getBlock());
        item = new ItemTag(event.getItem());
        entity = new EntityTag(event.getTargetEntity());
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
