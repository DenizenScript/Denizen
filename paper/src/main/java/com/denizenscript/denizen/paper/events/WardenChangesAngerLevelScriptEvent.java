package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // @Triggers when a warden changes its anger level. (In practice, only fires when increasing).
    //
    // @Player when the entity who triggered the change is a player.
    //
    // @NPC when the entity who triggered the change is an NPC.
    //
    // @Context
    // <context.entity> returns the EntityTag of the warden which changed its anger level.
    // <context.new_anger> returns an ElementTag(Number) of the new anger level.
    // <context.old_anger> returns an ElementTag(Number) of the old anger level.
    // <context.target> returns the EntityTag who triggered the change (if any). (In practice, always present).
    //
    // @Determine
    // "ANGER:<ElementTag(Number)>" to set the value of the anger level. Value must be between 0 and 150.
    //
    // @Example
    // on warden changes anger level:
    // - if <context.new_anger> >= 40 && <context.new_anger> < 80:
    //     - announce "Careful, the warden is agitated!"
    //
    // -->

    public WardenChangesAngerLevelScriptEvent() {
        registerCouldMatcher("warden changes anger level");
        this.<WardenChangesAngerLevelScriptEvent, ElementTag>registerOptionalDetermination("anger", ElementTag.class, (evt, context, anger) -> {
            if (anger.isInt()) {
                evt.event.setNewAnger(anger.asInt());
                return true;
            }
            return false;
        });
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
        return switch (name) {
            case "entity" -> new EntityTag(event.getEntity());
            case "new_anger" -> new ElementTag(event.getNewAnger());
            case "old_anger" -> new ElementTag(event.getOldAnger());
            case "target" -> event.getTarget() != null ? new EntityTag(event.getTarget()).getDenizenObject() : null;
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getTarget());
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
