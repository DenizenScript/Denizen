package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class PlayerPlacesHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: de-collide with places block
    // <--[event]
    // @Events
    // player places hanging
    // player places <hanging>
    //
    // @Regex ^on player places [^\s]+$
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    //
    // @Context
    // <context.hanging> returns the EntityTag of the hanging.
    // <context.location> returns the LocationTag of the block the hanging was placed on.
    //
    // @Player Always.
    //
    // -->

    public PlayerPlacesHangingScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesHangingScriptEvent instance;
    public EntityTag hanging;
    public LocationTag location;
    public HangingPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player places");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(2);
        if (!tryEntity(hanging, hangCheck)) {
            return false;
        }

        return runInCheck(path, location);

    }

    @Override
    public String getName() {
        return "PlayerPlacesHanging";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void pnPlayerPlacesHanging(HangingPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity hangingEntity = event.getEntity();
        EntityTag.rememberEntity(hangingEntity);
        hanging = new EntityTag(hangingEntity);
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(hangingEntity);
    }
}
