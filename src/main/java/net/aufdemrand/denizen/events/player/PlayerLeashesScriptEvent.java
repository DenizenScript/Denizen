package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

import java.util.HashMap;

public class PlayerLeashesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player leashes entity (in <area>)
    // player leashes <entity> (in <area>)
    //
    // @Regex ^on player leashes [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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

    public PlayerLeashesScriptEvent() {
        instance = this;
    }

    public static PlayerLeashesScriptEvent instance;
    public dEntity entity;
    public dEntity holder;
    public PlayerLeashEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player leashes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(2, lower);
        if (target.length() > 0) {
            if (!target.equals(CoreUtilities.toLowerCase(entity.getName()))) {
                return false;
            }
        }
        return runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "PlayerLeashes";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerLeashEntityEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(holder.isPlayer() ? holder.getDenizenPlayer() : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("holder", holder);
        context.put("entity", entity);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeashes(PlayerLeashEntityEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        holder = new dEntity(event.getPlayer());
        entity = new dEntity(event.getEntity());
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
