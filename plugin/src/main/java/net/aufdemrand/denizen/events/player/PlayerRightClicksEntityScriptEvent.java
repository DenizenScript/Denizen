package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
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
import org.bukkit.inventory.EquipmentSlot;

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
// <context.cuboids> NOTE: DEPRECATED IN FAVOR OF <context.entity.location.cuboids>
// <context.location> returns a dLocation of the clicked entity. NOTE: DEPRECATED IN FAVOR OF <context.entity.location>
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
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        if (!tryEntity(entity, CoreUtilities.getXthArg(3, lower))) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (!runWithCheck(path, new dItem(event.getPlayer().getItemInHand()))) {
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
    public void destroy() {
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
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
        else if (name.equals("cuboids")) {
            if (cuboids == null) {
                cuboids = new dList();
                for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                    cuboids.add(cuboid.identifySimple());
                }
            }
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerRightClicksEntity(PlayerInteractEntityEvent event) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2) && event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        entity = new dEntity(event.getRightClicked());
        item = new dItem(event.getPlayer().getItemInHand());
        location = new dLocation(event.getRightClicked().getLocation());
        cuboids = null;
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
