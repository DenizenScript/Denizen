package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // <context.item> returns an ItemTag of the item being picked up.
    // <context.entity> returns an EntityTag of the item entity being picked up.
    // <context.pickup_entity> returns an EntityTag of the entity picking up the item.
    // <context.location> returns a LocationTag of the item's location.
    //
    // @Determine
    // "ITEM:<ItemTag>" to change the item being picked up.
    //
    // @Player when the entity picking up the item is a player.
    //
    // @NPC when the entity picking up the item is an npc.
    //
    // -->

    public EntityPicksUpItemScriptEvent() {
        registerCouldMatcher("<entity> picks up <item>");
        registerCouldMatcher("<entity> takes <item>");
        this.<EntityPicksUpItemScriptEvent, ItemTag>registerDetermination("item", ItemTag.class, (evt, context, item) -> {
            editedItems.add(event.getItem().getUniqueId());
            evt.event.getItem().setItemStack(item.getItemStack());
            evt.event.setCancelled(true);
        });
    }

    public ItemTag item;
    public EntityTag entity;
    public EntityPickupItemEvent event;

    private static final Set<UUID> editedItems = new HashSet<>();

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!path.tryArgObject(path.eventArgLowerAt(1).equals("picks") ? 3 : 2, item)) {
            return false;
        }
        if (!runInCheck(path, event.getItem().getLocation())) {
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
            case "item" -> item;
            case "entity" -> new EntityTag(event.getItem());
            case "pickup_entity" -> entity.getDenizenObject();
            case "location" -> new LocationTag(event.getItem().getLocation());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityPicksUpItem(EntityPickupItemEvent event) {
        if (editedItems.remove(event.getItem().getUniqueId())) {
            return;
        }
        entity = new EntityTag(event.getEntity());
        item = new ItemTag(event.getItem().getItemStack());
        this.event = event;
        fire(event);
    }
}
