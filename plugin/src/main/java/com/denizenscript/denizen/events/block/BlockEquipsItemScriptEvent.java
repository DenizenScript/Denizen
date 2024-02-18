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

public class BlockEquipsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block equips <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block dispenses armor and is equipped to an entity.
    //
    // @Switch on:<entity> to only process the event if the entity having the armor equipped matches the entity input.
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
    //
    // @Example
    // # Will cause leather armor to be dispensed like a normal item and not be equipped on an armor stand.
    // on block equips leather* on:armor_stand:
    // - determined cancelled
    // -->

    public BlockEquipsItemScriptEvent() {
        registerCouldMatcher("block equips <item>");
        registerSwitches("on");
    }

    ItemTag item;
    EntityTag entity;
    LocationTag location;
    BlockDispenseArmorEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        if (!path.tryObjectSwitch("on", entity)) {
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
            case "block" -> new MaterialTag(event.getBlock());
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
        item = new ItemTag(event.getItem());
        entity = new EntityTag(event.getTargetEntity());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
