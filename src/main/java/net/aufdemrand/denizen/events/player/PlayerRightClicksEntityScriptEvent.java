package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

// <--[event]
// @Events
// player right clicks entity
// player right clicks entity in <area>
// player right clicks entity in notable cuboid
// player right clicks <entity>
// player right clicks <entity> in <area>
// player right clicks <entity> in notable cuboid
//
// @Regex ^on player right clicks [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
//
// @Switch with <item>
//
// @Cancellable true
//
// @Triggers when a player right clicks on an entity.
//
// @Context
// <context.entity> returns the dEntity the player is clicking on.
// <context.item> returns the dItem the player is clicking with.
// <context.cuboids> NOTE: DEPRECATED IN FAVOUR OF <context.location.cuboids>
// <context.location> returns a dLocation of the clicked entity. NOTE: DEPRECATED IN FAVOUR OF <context.entity.location>
//
// -->

public class PlayerRightClicksEntityScriptEvent extends BukkitScriptEvent implements Listener {

    PlayerRightClicksEntityScriptEvent instance;
    PlayerInteractEntityEvent event;
    dEntity entity;
    dItem item;
    dLocation location;
    dList cuboids;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player right clicks") && !CoreUtilities.getXthArg(3, lower).equals("at");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!tryEntity(entity, CoreUtilities.getXthArg(3, lower))) {
            return false;
        }
        if (!runInCheck(scriptContainer, s, lower, event.getPlayer().getLocation())) {
            return false;
        }
        if (!runWithCheck(scriptContainer, s, lower, new dItem(event.getPlayer().getItemInHand()))) {
            return false;
        }
        // Deprecated in favor of with: format
        if (CoreUtilities.xthArgEquals(4, lower, "with")) {
            if (!tryItem(new dItem(event.getPlayer().getItemInHand()), CoreUtilities.getXthArg(5, lower))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerRightClicksEntity";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
    }


    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("cuboids")) {
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerRightClicksEntity(PlayerInteractEntityEvent event) {
        entity = new dEntity(event.getRightClicked());
        item = new dItem(event.getPlayer().getItemInHand());
        location = new dLocation(event.getRightClicked().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identify());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
