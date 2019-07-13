package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashesEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player leashes entity
    // player leashes <entity>
    //
    // @Regex ^on player leashes [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player leashes an entity.
    //
    // @Context
    // <context.entity> returns the dEntity of the leashed entity.
    // <context.holder> returns the dEntity that is holding the leash.
    //
    // -->

    public PlayerLeashesEntityScriptEvent() {
        instance = this;
    }

    public static PlayerLeashesEntityScriptEvent instance;
    public dEntity entity;
    public dPlayer holder;
    public PlayerLeashEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player leashes");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(2))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerLeashesEntity";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(holder, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("holder")) {
            return holder;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLeashes(PlayerLeashEntityEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        holder = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}
