package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangesWorldScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes world
    //
    // @Group Player
    //
    // @Location true
    //
    // @Switch from:<world> to only run if the player came from the specified world.
    // @Switch to:<world> to only run if the player is going to the specified world.
    //
    // @Triggers when a player moves to a different world.
    //
    // @Context
    // <context.origin_world> returns the WorldTag that the player was previously on.
    // <context.destination_world> returns the WorldTag that the player is now in.
    //
    // @Player Always.
    //
    // -->

    public PlayerChangesWorldScriptEvent() {
        registerCouldMatcher("player changes world (from <world>) (to <world>)");
        registerSwitches("from", "to");
    }

    public WorldTag origin_world;
    public WorldTag destination_world;
    public PlayerChangedWorldEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String[] data = path.eventArgsLower;
        for (int index = 3; index < data.length; index++) {
            if (data[index].equals("from")) {
                BukkitImplDeprecations.playerChangesWorldSwitches.warn(getTagContext(path));
                if (!origin_world.tryAdvancedMatcher(data[index + 1], path.context)) {
                    return false;
                }
            }
            else if (data[index].equals("to")) {
                BukkitImplDeprecations.playerChangesWorldSwitches.warn(getTagContext(path));
                if (!destination_world.tryAdvancedMatcher(data[index + 1], path.context)) {
                    return false;
                }
            }
        }
        if (!path.tryObjectSwitch("from", origin_world)) {
            return false;
        }
        if (!path.tryObjectSwitch("to", destination_world)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "origin_world" -> origin_world;
            case "destination_world" -> destination_world;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerChangesWorld(PlayerChangedWorldEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        origin_world = new WorldTag(event.getFrom());
        destination_world = new WorldTag(event.getPlayer().getWorld());
        this.event = event;
        fire(event);
    }
}
