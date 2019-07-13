package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangesWorldScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes world (from <world>) (to <world>)
    //
    // @Regex ^on player world( from [^\s]+)?( to [^\s]+)?$
    // @Switch in <area>
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
    public boolean matches(ScriptPath path) {

        String[] data = path.eventArgsLower;
        for (int index = 3; index < data.length; index++) {
            if (data[index].equals("from")) {
                if (!data[index + 1].equals(CoreUtilities.toLowerCase(origin_world.getName()))) {
                    return false;
                }
            }
            else if (data[index].equals("to")) {
                if (!data[index + 1].equals(CoreUtilities.toLowerCase(destination_world.getName()))) {
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("origin_world")) {
            return origin_world;
        }
        else if (name.equals("destination_world")) {
            return destination_world;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesWorld(PlayerChangedWorldEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        origin_world = new dWorld(event.getFrom());
        destination_world = new dWorld(event.getPlayer().getWorld());
        this.event = event;
        fire(event);
    }
}
