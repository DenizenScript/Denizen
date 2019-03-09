package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    //
    // @Context
    // <context.hanging> returns the dEntity of the hanging.
    // <context.location> returns the dLocation of the block the hanging was placed on.
    // <context.cuboids> DEPRECATED.
    //
    // -->

    public PlayerPlacesHangingScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesHangingScriptEvent instance;
    public dEntity hanging;
    public dList cuboids;
    public dLocation location;
    public HangingPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player places");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String hangCheck = CoreUtilities.getXthArg(2, lower);
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("cuboids")) { // NOTE: Deprecated in favor of context.location.cuboids
            if (cuboids == null) {
                cuboids = new dList();
                for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                    cuboids.add(cuboid.identifySimple());
                }
            }
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void pnPlayerPlacesHanging(HangingPlaceEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        Entity hangingEntity = event.getEntity();
        dEntity.rememberEntity(hangingEntity);
        hanging = new dEntity(hangingEntity);
        location = new dLocation(event.getBlock().getLocation());
        cuboids = null;
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        dEntity.forgetEntity(hangingEntity);
        event.setCancelled(cancelled);
    }
}
