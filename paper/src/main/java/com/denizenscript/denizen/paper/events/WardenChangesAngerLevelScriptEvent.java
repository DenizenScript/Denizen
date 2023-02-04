package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WardenChangesAngerLevelScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // warden changes anger level
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Triggers when a warden changes its anger level.
    //
    // @Player when the entity who triggered the change is a player.
    //
    // @NPC when the entity who triggered the change is an npc.
    //
    // @Context
    // <context.entity> returns the EntityTag of the warden which changed its anger level.
    // <context.new_anger> returns an ElementTag of the new anger level.
    // <context.old_anger> returns an ElementTag of the old anger level.
    // <context.target> returns the EntityTag who triggered the change (if any).
    //
    // @Determine
    // "ANGER:" + ElementTag(Number) to set the value of the anger level. Value must not exceed 150.
    //
    // @Example
    // on warden changes anger level:
    // - if <context.new_anger> >= 40 && <context.new_anger> < 80:
    //     - announce "Careful, the warden is agitated!"
    //
    // -->

    public WardenChangesAngerLevelScriptEvent() {
        registerCouldMatcher("warden changes anger level");
    }

    public WardenAngerChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return new EntityTag(event.getEntity());
            case "new_anger": return new ElementTag(event.getNewAnger());
            case "old_anger": return new ElementTag(event.getOldAnger());
            case "target": return event.getTarget() != null ? new EntityTag(event.getTarget()) : null;
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase((determinationObj.toString()));
            if (lower.startsWith("anger:")) {
                ElementTag value = new ElementTag(lower.substring("anger:".length()));
                if (value.isInt()) {
                    event.setNewAnger(value.asInt());
                    return true;
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getTarget() != null ? new EntityTag(event.getTarget()) : null);
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
