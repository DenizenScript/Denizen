package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerLeavesBedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player leaves bed
    //
    // @Regex ^on player leaves bed$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player leaves a bed.
    //
    // @Context
    // <context.location> returns the dLocation of the bed.
    //
    // -->

    public PlayerLeavesBedScriptEvent() {
        instance = this;
    }

    public static PlayerLeavesBedScriptEvent instance;
    public dLocation location;
    public PlayerBedLeaveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player leaves bed");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerLeavesBed";
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
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLeavesBed(PlayerBedLeaveEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        location = new dLocation(event.getBed().getLocation());
        this.event = event;
        fire();
    }
}
