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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerStepsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player steps on block (in <area>)
    // player steps on <material> (in <area>)
    //
    // @Regex ^on player steps on [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a player steps onto a material.
    //
    // @Context
    // <context.location> returns a dLocation of the block the player is stepping on.
    // <context.cuboids> DEPRECATED.
    // <context.previous_location> returns a dLocation of where the player was before stepping onto the block.
    // <context.new_location> returns a dLocation of where the player is now.
    //
    // -->

    public PlayerStepsOnScriptEvent() {
        instance = this;
    }

    public static PlayerStepsOnScriptEvent instance;
    public dLocation location;
    public dLocation previous_location;
    public dLocation new_location;
    public dList cuboids;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player steps on");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        String mat = CoreUtilities.getXthArg(3, lower);
        dMaterial material = new dMaterial(location.getBlock());
        if (!tryMaterial(material, mat)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerStepsOn";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("previous_location")) {
            return previous_location;
        }
        else if (name.equals("new_location")) {
            return new_location;
        }
        else if (name.equals("cuboids")) { // NOTE: Deprecated in favor of context.location.cuboids
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
    public void onPlayerStepsOn(PlayerMoveEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        if (event.getTo().getBlock().getLocation().equals(event.getFrom().getBlock().getLocation())) {
            return;
        }
        location = new dLocation(event.getTo().clone().subtract(0, 1, 0));
        previous_location = new dLocation(event.getFrom());
        new_location = new dLocation(event.getTo());
        cuboids = null;
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        onPlayerStepsOn(evt);
        event.setCancelled(evt.isCancelled());
    }

}
