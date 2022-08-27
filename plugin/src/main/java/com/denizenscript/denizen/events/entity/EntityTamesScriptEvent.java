package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity is tamed.
    //
    // @Context
    // <context.entity> returns a EntityTag of the tamed entity.
    // <context.owner> returns a EntityTag of the owner.
    //
    // @Player when a player tames an entity and using the 'players tames entity' event.
    //
    // -->

    public EntityTamesScriptEvent() {
    }

    public EntityTag entity;
    public EntityTag owner;
    public EntityTameEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("tames") && !path.eventArgLowerAt(1).equals("tamed")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String ownerTest = cmd.equals("tames") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        String tamed = cmd.equals("tamed") ? path.eventArgLowerAt(0) : path.eventArgLowerAt(2);
        if (!owner.tryAdvancedMatcher(ownerTest) || !entity.tryAdvancedMatcher(tamed)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(owner);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        entity = new EntityTag(event.getEntity());
        owner = new EntityTag((Entity) event.getOwner());
        this.event = event;
        fire(event);
    }
}
