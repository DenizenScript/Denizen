package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

public class EntityUnleashedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity unleashed (because <reason>)
    // <entity> unleashed (because <reason>)
    //
    // @Regex ^on [^\s]+ unleashed( because [^\s]+)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity is unleashed.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.reason> returns an Element of the reason for the unleashing.
    // Reasons include DISTANCE, HOLDER_GONE, PLAYER_UNLEASH, and UNKNOWN
    //
    // @NPC when the entity being unleashed is an NPC.
    //
    // -->

    public EntityUnleashedScriptEvent() {
        instance = this;
    }

    public static EntityUnleashedScriptEvent instance;
    public dEntity entity;
    public Element reason;
    public EntityUnleashEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "unleashed");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (path.eventArgAt(2).equals("because") && !path.eventArgLowerAt(3).equals(CoreUtilities.toLowerCase(reason.asString()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityUnleashed";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityUnleashed(EntityUnleashEvent event) {
        entity = new dEntity(event.getEntity());
        reason = new Element(event.getReason().toString());
        this.event = event;
        fire(event);
    }

}
