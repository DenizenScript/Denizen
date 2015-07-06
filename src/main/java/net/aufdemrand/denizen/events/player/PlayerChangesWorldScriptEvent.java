package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.HashMap;
import java.util.List;

public class PlayerChangesWorldScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes world (from <world>) (to <world>)
    //
    // @Cancellable false
    //
    // @Triggers when a player moves to a different world.
    //
    // @Context
    // <context.origin_world> returns the dWorld that the player was previously on.
    // <context.destination_world> returns the dWorld that the player is now in.
    //
    // -->

    public PlayerChangesWorldScriptEvent() {
        instance = this;
    }

    public static PlayerChangesWorldScriptEvent instance;
    public dWorld origin_world;
    public dWorld destination_world;
    public PlayerChangedWorldEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes world");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        List<String> data = CoreUtilities.split(lower, ' ');
        for (int index = 3; index < data.size(); index++) {
            if (data.get(index).equals("from")) {
                if (!data.get(index+1).equals(origin_world.getName().toLowerCase())){
                    return false;
                }
            }
            else if (data.get(index).equals("to")) {
                if (!data.get(index+1).equals(destination_world.getName().toLowerCase())){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesWorld";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("origin_world", origin_world);
        context.put("destination_world", destination_world);
        return context;
    }

    @EventHandler
    public void onPlayerChangesWorld(PlayerChangedWorldEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        origin_world = new dWorld(event.getFrom());
        destination_world = new dWorld(event.getPlayer().getWorld());
        this.event = event;
        fire();
    }
}
