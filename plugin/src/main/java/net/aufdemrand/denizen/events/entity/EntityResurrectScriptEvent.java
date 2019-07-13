package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class EntityResurrectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity resurrected
    // <entity> resurrected
    //
    // @Regex ^on [^\s]+ resurrected$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity dies and is resurrected by a totem.
    //
    // @Context
    // <context.entity> returns the dEntity being resurrected.
    //
    // @Player when the entity being resurrected is a player.
    //
    // -->

    public EntityResurrectScriptEvent() {
        instance = this;
    }

    public static EntityResurrectScriptEvent instance;
    public dEntity entity;
    public EntityResurrectEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "resurrected");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityResurrected";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}
