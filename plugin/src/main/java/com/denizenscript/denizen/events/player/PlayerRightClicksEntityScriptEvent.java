package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns a dLocation of the clicked entity. NOTE: DEPRECATED IN FAVOR OF <context.entity.location>
    // <context.click_position> returns a dLocation of the click position (as a world-less vector, relative to the entity's center). This is only available when clicking armor stands.
    //
    // -->

    PlayerRightClicksEntityScriptEvent instance;
    PlayerInteractEntityEvent event;
    dEntity entity;
    dItem item;
    dLocation location;

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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), entity.isNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("click_position") && event instanceof PlayerInteractAtEntityEvent) {
            return new dLocation(((PlayerInteractAtEntityEvent) event).getClickedPosition());
        }
        else if (name.equals("cuboids")) {
            dB.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            dList cuboids = new dList();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.addObject(cuboid);
            }
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerRightClicksAtEntity(PlayerInteractAtEntityEvent event) {
        playerRightClicksEntity(event);
    }

    @EventHandler
    public void playerRightClicksEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        entity = new dEntity(event.getRightClicked());
        item = new dItem(event.getPlayer().getItemInHand());
        location = new dLocation(event.getRightClicked().getLocation());
        this.event = event;
        fire(event);
    }

}
