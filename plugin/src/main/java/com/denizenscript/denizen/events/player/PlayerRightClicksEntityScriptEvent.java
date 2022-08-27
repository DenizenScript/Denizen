package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerRightClicksEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player right clicks <entity>
    //
    // @Group Player
    //
    // @Location true
    // @Switch with:<item> to only process the event when the player is holding a specified item.
    // @Switch type:<entity> to only run if the entity clicked matches the entity input.
    //
    // @Warning this event may in some cases double-fire, requiring usage of the 'ratelimit' command (like 'ratelimit <player> 1t') to prevent doubling actions.
    //
    // @Cancellable true
    //
    // @Triggers when a player right clicks on an entity.
    //
    // @Context
    // <context.entity> returns the EntityTag the player is clicking on.
    // <context.item> returns the ItemTag the player is clicking with.
    // <context.hand> returns "offhand" or "mainhand" to indicate which hand was used to fire the event. Some events fire twice - once for each hand.
    // <context.click_position> returns a LocationTag of the click position (as a world-less vector, relative to the entity's center). This is only available when clicking armor stands.
    //
    // @Player Always.
    //
    // -->

    PlayerInteractEntityEvent event;
    EntityTag entity;
    ItemTag item;

    public PlayerRightClicksEntityScriptEvent() {
        registerCouldMatcher("player right clicks <entity>");
        registerSwitches("with", "type");
    }

    @Override
    public boolean matches(ScriptPath path) {
        boolean isAt = path.eventArgLowerAt(3).equals("at");
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(isAt ? 4 : 3))) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!runWithCheck(path, item)) {
            return false;
        }
        // Deprecated in favor of with: format
        if (path.eventArgLowerAt(isAt ? 5 : 4).equals("with") && !item.tryAdvancedMatcher(path.eventArgLowerAt(isAt ? 6 : 5))) {
            return false;
        }
        if (!path.tryObjectSwitch("type", entity)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), entity.isNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity.getDenizenObject();
            case "item": return item;
            case "hand": return new ElementTag(event.getHand() == EquipmentSlot.OFF_HAND ? "offhand" : "mainhand");
            case "location":
                BukkitImplDeprecations.playerRightClicksEntityContext.warn();
                return entity.getLocation();
            case "click_position":
                if (event instanceof PlayerInteractAtEntityEvent) {
                    return new LocationTag(((PlayerInteractAtEntityEvent) event).getClickedPosition());
                }
                break;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerRightClicksAtEntity(PlayerInteractAtEntityEvent event) {
        playerRightClicksEntityHandler(event);
    }

    @EventHandler
    public void playerRightClicksEntity(PlayerInteractEntityEvent event) {
        if (event instanceof PlayerInteractAtEntityEvent) {
            return;
        }
        playerRightClicksEntityHandler(event);
    }

    public void playerRightClicksEntityHandler(PlayerInteractEntityEvent event) {
        entity = new EntityTag(event.getRightClicked());
        item = new ItemTag(event.getPlayer().getEquipment().getItem(event.getHand()));
        this.event = event;
        fire(event);
    }
}
