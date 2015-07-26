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

import java.util.HashMap;

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
        return s.toLowerCase().startsWith("player steps on");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String mat = CoreUtilities.getXthArg(3, lower);
        dMaterial material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        if (!tryMaterial(material, mat)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerStepsOn";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerMoveEvent.getHandlerList().unregister(this);
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("previous_location", previous_location);
        context.put("new_location", new_location);
        context.put("cuboids", cuboids);
        return context;
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
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
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
