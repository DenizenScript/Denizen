package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityPicksUpItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> picks up <item>
    // <entity> takes <item>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity picks up an item.
    //
    // @Context
    // <context.item> returns the ItemTag.
    // <context.entity> returns the EntityTag of the item being picked up.
    // <context.pickup_entity> returns the EntityTag of the entity picking up the item.
    // <context.location> returns a LocationTag of the item's location.
    //
    // @Determine
    // "ITEM:" + ItemTag to changed the item being picked up.
    //
    // @Player when the entity picking up the item is a player.
    //
    // @NPC when the entity picking up the item is an npc.
    //
    // -->

    public EntityPicksUpItemScriptEvent() {
        registerCouldMatcher("<entity> picks up <item>");
        registerCouldMatcher("<entity> takes <item>");
    }

    public ItemTag item;
    public EntityTag entity;
    public LocationTag location;
    public EntityPickupItemEvent event;

    private static final Set<UUID> editedItems = new HashSet<>();

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!item.tryAdvancedMatcher(path.eventArgLowerAt(path.eventArgLowerAt(1).equals("picks") ? 3 : 2))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("item:")) {
            item = ItemTag.valueOf(determination.substring("item:".length()), path.container);
            editedItems.add(event.getItem().getUniqueId());
            event.getItem().setItemStack(item.getItemStack());
            event.setCancelled(true);
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
            case "item":
                return item;
            case "entity":
                return new EntityTag(event.getItem());
            case "pickup_entity":
                return entity;
            case "location":
                return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityPicksUpItem(EntityPickupItemEvent event) {
        entity = new EntityTag(event.getEntity());
        Item itemEntity = event.getItem();
        UUID itemUUID = itemEntity.getUniqueId();
        if (editedItems.contains(itemUUID)) {
            editedItems.remove(itemUUID);
            return;
        }
        location = new LocationTag(itemEntity.getLocation());
        item = new ItemTag(itemEntity.getItemStack());
        this.event = event;
        fire(event);
    }
}
