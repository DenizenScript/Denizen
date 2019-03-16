package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class EntityTamesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity tamed
    // <entity> tamed
    // player tames entity
    // player tames <entity>
    //
    // @Regex ^on [^\s]+ (tames [^\s]+|tamed)$
    // @Switch in <area>
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
        String cmd = path.eventArgLowerAt(1);
        String ownerTest = cmd.equals("tames") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        String tamed = cmd.equals("tamed") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);

        if (!tryEntity(owner, ownerTest) || !tryEntity(entity, tamed)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityTames";
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
        this.event = event;
        fire(event);
    }

}
