package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerWalkScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks
    //
    // @Regex ^on player walks$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Warning This event fires very very rapidly!
    //
    // @Cancellable true
    //
    // @Triggers when a player moves in the slightest.
    //
    // @Context
    // <context.old_location> returns the location of where the player was.
    // <context.new_location> returns the location of where the player is.
    //
    // @Player Always.
    //
    // -->

    public PlayerWalkScriptEvent() {
    }


    public LocationTag old_location;
    public LocationTag new_location;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player walks") && !path.eventArgLowerAt(2).equals("over");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, old_location)) {
            return false;
        }
        if (!runInCheck(path, new_location)) {
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
        if (name.equals("old_location")) {
            return old_location;
        }
        else if (name.equals("new_location")) {
            return new_location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerMoves(PlayerMoveEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        old_location = new LocationTag(event.getFrom());
        new_location = new LocationTag(event.getTo());
        this.event = event;
        fire(event);
    }
}
