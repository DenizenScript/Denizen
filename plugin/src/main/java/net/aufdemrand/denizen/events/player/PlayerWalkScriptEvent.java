package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerWalkScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks (in <area>)
    //
    // @Regex ^on player walks( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    // -->

    public PlayerWalkScriptEvent() {
        instance = this;
    }

    public static PlayerWalkScriptEvent instance;

    public dLocation old_location;
    public dLocation new_location;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player walks") && !CoreUtilities.xthArgEquals(2, lower, "over");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        return runInCheck(path, old_location) || runInCheck(path, new_location);
    }

    @Override
    public String getName() {
        return "PlayerWalks";
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
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        old_location = new dLocation(event.getFrom());
        new_location = new dLocation(event.getTo());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
