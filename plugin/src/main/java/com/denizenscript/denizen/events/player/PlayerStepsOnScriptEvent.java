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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerStepsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player steps on block
    // player steps on <material>
    //
    // @Regex ^on player steps on [^\s]+$
    // @Switch in <area>
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a player steps onto a material.
    //
    // @Context
    // <context.location> returns a dLocation of the block the player is stepping on.
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
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player steps on");
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(3);
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
        else if (name.equals("cuboids")) {
            dB.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            dList cuboids = new dList();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identifySimple());
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
        this.event = event;
        fire(event);
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
