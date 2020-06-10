package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    //
    // @Group Player
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
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
        instance = this;
    }

    public static PlayerChangesWorldScriptEvent instance;
    public WorldTag origin_world;
    public WorldTag destination_world;
    public PlayerChangedWorldEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player changes world");
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

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerChangesWorld";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        origin_world = new WorldTag(event.getFrom());
        destination_world = new WorldTag(event.getPlayer().getWorld());
        this.event = event;
        fire(event);
    }
}
