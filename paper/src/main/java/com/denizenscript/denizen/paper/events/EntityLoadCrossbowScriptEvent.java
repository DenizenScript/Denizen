package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityLoadCrossbowScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> loads crossbow
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Switch crossbow:<item> to only process the event if the crossbow is a specified item.
    //
    // @Triggers when a living entity loads a crossbow with a projectile.
    //
    // @Context
    // <context.entity> returns the EntityTag that is loading the crossbow.
    // <context.crossbow> returns the ItemTag of the crossbow.
    // <context.consumes> returns true if the loading will consume a projectile item, otherwise false.
    // <context.hand> returns "HAND" or "OFF_HAND" depending on which hand is holding the crossbow item.
    //
    // @Determine
    // "KEEP_ITEM" to keep the projectile item in the shooter's inventory.
    //
    // @Player when the entity is a player.
    //
    // @NPC when the entity is an NPC.
    //
    // -->

    public EntityLoadCrossbowScriptEvent() {
        registerCouldMatcher("<entity> loads crossbow");
        registerSwitches("crossbow");
        this.<EntityLoadCrossbowScriptEvent>registerTextDetermination("keep_item", (evt) -> {
            evt.event.setConsumeItem(false);
        });
    }

    public EntityLoadCrossbowEvent event;
    public EntityTag entity;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(event.getCrossbow()), "crossbow")) {
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
            case "crossbow" -> new ItemTag(event.getCrossbow());
            case "hand" -> new ElementTag(event.getHand());
            case "consumes" -> new ElementTag(event.shouldConsumeItem());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onLoadCrossbow(EntityLoadCrossbowEvent event) {
        this.event = event;
        this.entity = new EntityTag(event.getEntity());
        fire(event);
    }
}
