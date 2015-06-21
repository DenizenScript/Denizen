package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.HashMap;

public class EntityTamesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity tamed
    // <entity> tamed
    // player tames entity
    // player tames <entity>
    //
    // @Cancellable true
    //
    // @Triggers when an entity is tamed.
    //
    // @Context
    // <context.entity> returns a dEntity of the tamed entity.
    // <context.owner> returns a dEntity of the owner.
    //
    // -->

    public EntityTamesScriptEvent() {
        instance = this;
    }

    public static EntityTamesScriptEvent instance;
    public dEntity entity;
    public dEntity owner;
    public EntityTameEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "tames");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String ownerTest = cmd.equals("tames") ? CoreUtilities.getXthArg(0, lower): CoreUtilities.getXthArg(2, lower);
        String tamed = cmd.equals("tamed") ? CoreUtilities.getXthArg(0, lower): CoreUtilities.getXthArg(2, lower);
        if (ownerTest.length() > 0) {
            if (owner != null) {
                if(!owner.matchesEntity(ownerTest)) {
                    return false;
                }
            }
        }
        if (tamed.length() > 0) {
            if (!entity.matchesEntity(tamed)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "EntityTames";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityTameEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(owner.isPlayer() ? owner.getDenizenPlayer() : null,
                owner.isCitizensNPC() ? owner.getDenizenNPC(): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("owner", owner);
        return context;
    }

    @EventHandler
    public void onEntityTames(EntityTameEvent event) {
        entity = new dEntity(event.getEntity());
        owner = new dEntity((Entity)event.getOwner());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
