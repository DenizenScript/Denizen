package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
    // @Switch with <item>
    //
    // @Cancellable true
    //
    // @Triggers when a player right clicks on an entity.
    //
    // @Context
    // <context.entity> returns the EntityTag the player is clicking on.
    // <context.item> returns the ItemTag the player is clicking with.
    // <context.click_position> returns a LocationTag of the click position (as a world-less vector, relative to the entity's center). This is only available when clicking armor stands.
    //
    // -->

    public static PlayerRightClicksEntityScriptEvent instance;
    PlayerInteractEntityEvent event;
    EntityTag entity;
    ItemTag item;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player right clicks");
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
        return true;
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
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        entity = new EntityTag(event.getRightClicked());
        item = new ItemTag(event.getPlayer().getItemInHand());
        this.event = event;
        fire(event);
    }

}
