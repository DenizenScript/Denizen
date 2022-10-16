package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;

public class CauldronLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // cauldron level changes|raises|lowers
    //
    // @Group Block
    //
    // @Location true
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
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
    // ElementTag(Number) to set the new level.
    //
    // -->

    public CauldronLevelChangeScriptEvent() {
        registerCouldMatcher("cauldron level changes|raises|lowers");
        registerSwitches("cause");
    }

    public LocationTag location;
    public CauldronLevelChangeEvent event;

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
        else if (changeType.equals("lowers")) {
            if (event.getNewLevel() >= event.getOldLevel()) {
                return false;
            }
        }
        else if (!changeType.equals("changes")) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setNewLevel(((ElementTag) determinationObj).asInt());
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "cause": return new ElementTag(event.getReason());
            case "old_level": return new ElementTag(event.getOldLevel());
            case "new_level": return new ElementTag(event.getNewLevel());
            case "entity":
                if (event.getEntity() != null) {
                    return new EntityTag(event.getEntity()).getDenizenObject();
                }
                break;
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
