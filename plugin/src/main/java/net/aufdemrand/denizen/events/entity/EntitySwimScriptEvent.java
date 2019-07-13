package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;

public class EntitySwimScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity toggles swimming
    // entity starts swimming
    // entity stops swimming
    //
    // @Regex ^on player (toggles|starts|stops) swimming
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity starts or stops swimming.
    //
    // @Context
    // <context.entity> returns the dEntity of this event.
    // <context.state> returns an Element(Boolean) with a value of "true" if the entity is now swimming and "false" otherwise.
    //
    // @Player when the entity is a player.
    //
    // @NPC when the entity is an NPC.
    //
    // -->

    public EntitySwimScriptEvent() {
        instance = this;
    }

    public static EntitySwimScriptEvent instance;
    public dEntity entity;
    public Boolean state;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).equals("swimming");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntitySwim";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("state")) {
            return new Element(state);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityToggleSwim(EntityToggleSwimEvent event) {
        entity = new dEntity(event.getEntity());
        state = event.isSwimming();
        fire(event);
    }
}
