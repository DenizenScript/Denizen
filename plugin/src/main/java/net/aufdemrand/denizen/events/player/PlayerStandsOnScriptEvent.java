package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerStandsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player stands on material (in <area>)
    // player stands on (<material>) (in <area>)
    //
    // @Regex ^on player stands on [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player stands on a pressure plate, tripwire, or redstone ore.
    // @Context
    // <context.location> returns the dLocation the player is interacting with.
    // <context.material> returns the dMaterial the player is interacting with.
    //
    // @Determine
    //
    // -->

    public PlayerStandsOnScriptEvent() {
        instance = this;
    }

    PlayerStandsOnScriptEvent instance;
    PlayerInteractEvent event;
    dLocation location;
    dMaterial material;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player stands on");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String mat = CoreUtilities.getXthArg(3, lower);
        if (mat.length() > 0
                && !mat.equals("in")
                && !tryMaterial(material, mat)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, event.getPlayer().getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerStandsOn";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.getPlayerFrom(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerStandsOn(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        material = dMaterial.getMaterialFrom(event.getClickedBlock().getType(), event.getClickedBlock().getData());
        location = new dLocation(event.getClickedBlock().getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
