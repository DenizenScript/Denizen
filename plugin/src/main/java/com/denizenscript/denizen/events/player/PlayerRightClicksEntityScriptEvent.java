package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerRightClicksEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player right clicks entity
    // player right clicks <entity>
    //
    // @Regex ^on player right clicks [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    // @Switch with:<item> to only process the event when the player is holding a specified item.
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

    public static PlayerRightClicksEntityScriptEvent instance;
    PlayerInteractEntityEvent event;
    EntityTag entity;
    ItemTag item;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player right clicks")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(3))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        boolean isAt = path.eventArgLowerAt(3).equals("at");
        if (!tryEntity(entity, path.eventArgLowerAt(isAt ? 4 : 3))) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (!runWithCheck(path, item)) {
            return false;
        }
        // Deprecated in favor of with: format
        if (path.eventArgLowerAt(isAt ? 5 : 4).equals("with") && !tryItem(item, path.eventArgLowerAt(isAt ? 6 : 5))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerRightClicksEntity";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), entity.isNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("hand")) {
            return new ElementTag(event.getHand() == EquipmentSlot.OFF_HAND ? "offhand" : "mainhand");
        }
        else if (name.equals("location")) {
            Deprecations.playerRightClicksEntityContext.warn();
            return entity.getLocation();
        }
        else if (name.equals("click_position") && event instanceof PlayerInteractAtEntityEvent) {
            return new LocationTag(((PlayerInteractAtEntityEvent) event).getClickedPosition());
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
