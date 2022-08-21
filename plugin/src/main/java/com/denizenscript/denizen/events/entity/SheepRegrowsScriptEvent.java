package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

public class SheepRegrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sheep regrows wool
    //
    // @Regex ^on sheep regrows wool$
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a sheep regrows wool.
    //
    // @Context
    // <context.entity> returns the EntityTag of the sheep.
    //
    // -->

    public SheepRegrowsScriptEvent() {
    }

    public EntityTag entity;
    private LocationTag location;
    public SheepRegrowWoolEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("sheep regrows wool")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!runInCheck(path, location)) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSheepRegrows(SheepRegrowWoolEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(entity.getLocation());
        this.event = event;
        fire(event);
    }
}
