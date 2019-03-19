package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSneakScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles sneaking
    // player starts sneaking
    // player stops sneaking
    //
    // @Regex ^on player (toggles|starts|stops) sneaking$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops sneaking.
    //
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now sneaking and "false" otherwise.
    //
    // -->

    public PlayerSneakScriptEvent() {
        instance = this;
    }

    public static PlayerSneakScriptEvent instance;
    public Boolean state;
    public PlayerToggleSneakEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).equals("sneaking");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }

        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerSneak";
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
        if (name.equals("state")) {
            return new Element(state);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        state = event.isSneaking();
        this.event = event;
        fire(event);
    }
}
