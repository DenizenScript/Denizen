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

public class EntityTamesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity tamed (in <area>)
    // <entity> tamed (in <area>)
    // player tames entity (in <area>)
    // player tames <entity> (in <area>)
    //
    // @Regex ^on [^\s]+ (tames [^\s]+|tamed)( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity is tamed.
    //
    // @Context
    // <context.entity> returns a dEntity of the tamed entity.
    // <context.owner> returns a dEntity of the owner.
    //
    // @Player when a player tames an entity and using the 'players tames entity' event.
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
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String cmd = CoreUtilities.getXthArg(1, lower);
        String ownerTest = cmd.equals("tames") ? CoreUtilities.getXthArg(0, lower) : CoreUtilities.getXthArg(2, lower);
        String tamed = cmd.equals("tamed") ? CoreUtilities.getXthArg(0, lower) : CoreUtilities.getXthArg(2, lower);

        if (!tryEntity(owner, ownerTest) || !tryEntity(entity, tamed)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
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
                owner.isCitizensNPC() ? owner.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("owner")) {
            return owner;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityTames(EntityTameEvent event) {
        entity = new dEntity(event.getEntity());
        owner = new dEntity((Entity) event.getOwner());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
