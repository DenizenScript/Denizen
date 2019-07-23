package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;

public class CauldronLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // cauldron level changes
    // cauldron level raises
    // cauldron level lowers
    //
    // @Regex ^on cauldron level (changes|raises|lowers)$
    //
    // @Group Block
    //
    // @Switch in <area>
    // @Switch cause <cause>
    //
    // @Cancellable true
    //
    // @Triggers when a cauldron's level changes.
    //
    // @Context
    // <context.location> returns the LocationTag of the cauldron that changed.
    // <context.entity> returns the LocationTag of the entity that caused the cauldron level to change (if any).
    // <context.cause> returns the reason that the cauldron level changed, from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/CauldronLevelChangeEvent.ChangeReason.html>
    // <context.old_level> returns the previous cauldron level.
    // <context.new_level> returns the new cauldron level.
    //
    // @Determine
    // Element(Number) to set the new level.
    //
    // -->

    public CauldronLevelChangeScriptEvent() {
        instance = this;
    }

    public static CauldronLevelChangeScriptEvent instance;
    public LocationTag location;
    public CauldronLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("cauldron level ");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "cause", event.getReason().name())) {
            return false;
        }
        String changeType = path.eventArgLowerAt(2);
        if (changeType.equals("raises")) {
            if (event.getNewLevel() <= event.getOldLevel()) {
                return false;
            }
        }
        if (changeType.equals("lowers")) {
            if (event.getNewLevel() >= event.getOldLevel()) {
                return false;
            }
        }
        else if (!changeType.equals("changes")) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "CauldronLevelChange";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (!isDefaultDetermination(determinationObj)) {
            if (ArgumentHelper.matchesInteger(determination)) {
                event.setNewLevel(ArgumentHelper.getIntegerFrom(determination));
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("cause")) {
            return new ElementTag(event.getReason().name());
        }
        else if (name.equals("old_level")) {
            return new ElementTag(event.getOldLevel());
        }
        else if (name.equals("new_level")) {
            return new ElementTag(event.getNewLevel());
        }
        else if (name.equals("entity") && event.getEntity() != null) {
            return new EntityTag(event.getEntity());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}
