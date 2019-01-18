package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class PlayerShearsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player shears entity (in <area>)
    // player shears <entity> (in <area>)
    // player shears <color> sheep (in <area>)
    //
    // @Regex ^on player shears [^\s]+( sheep)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a player shears an entity.
    //
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    //
    // -->

    public PlayerShearsScriptEvent() {
        instance = this;
    }

    public static PlayerShearsScriptEvent instance;
    public dEntity entity;
    public PlayerShearEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player shears");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String ent = CoreUtilities.xthArgEquals(3, lower, "sheep") ? "sheep" : CoreUtilities.getXthArg(2, lower);

        if (!ent.equals("sheep") && !tryEntity(entity, ent)) {
            return false;
        }

        String color = CoreUtilities.xthArgEquals(3, lower, "sheep") ? CoreUtilities.getXthArg(2, lower) : "";
        if (color.length() > 0 && !color.equals(CoreUtilities.toLowerCase(((Sheep) entity.getBukkitEntity()).getColor().name()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerShears";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerShearEntityEvent.getHandlerList().unregister(this);
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
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("state")) { // NOTE: Deprecated
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerShears(PlayerShearEntityEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        entity = new dEntity(event.getEntity());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
