package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EntityDropsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> drops <item>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity drops an item.
    //
    // @Context
    // <context.item> returns the ItemTag.
    // <context.entity> returns a EntityTag of the item.
    // <context.dropped_by> returns the EntityTag that dropped the item.
    // <context.location> returns a LocationTag of the item's location.
    //
    // @Player When the entity dropping an item is a player.
    //
    // -->

    public EntityDropsItemScriptEvent() {
        registerCouldMatcher("<entity> drops <item>");
    }

    public ItemTag item;
    public LocationTag location;
    public EntityTag itemEntity;
    public EntityTag dropper;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, dropper)) {
            return false;
        }
        String iCheck = path.eventArgLowerAt(2);
        if (!item.tryAdvancedMatcher(iCheck)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dropper);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return item;
            case "entity":
                return itemEntity;
            case "dropped_by":
                return dropper.getDenizenObject();
            case "location":
                return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerDropsItem(PlayerDropItemEvent event) {
        dropper = new EntityTag(event.getPlayer());
        location = dropper.getLocation();
        itemEntity = new EntityTag(event.getItemDrop());
        EntityTag.rememberEntity(itemEntity.getBukkitEntity());
        item = new ItemTag(((Item) itemEntity.getBukkitEntity()).getItemStack());
        fire(event);
    }

    @EventHandler
    public void onEntityDropsItem(EntityDropItemEvent event) {
        dropper = new EntityTag(event.getEntity());
        location = dropper.getLocation();
        itemEntity = new EntityTag(event.getItemDrop());
        EntityTag.rememberEntity(itemEntity.getBukkitEntity());
        item = new ItemTag(((Item) itemEntity.getBukkitEntity()).getItemStack());
        fire(event);
    }
}
