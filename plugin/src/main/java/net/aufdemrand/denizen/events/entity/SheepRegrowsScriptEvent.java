package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

public class SheepRegrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sheep regrows wool (in <area>)
    //
    // @Regex ^on sheep regrows wool( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a sheep regrows wool.
    //
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    //
    // -->

    public SheepRegrowsScriptEvent() {
        instance = this;
    }

    public static SheepRegrowsScriptEvent instance;
    public dEntity entity;
    private dLocation location;
    public SheepRegrowWoolEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("sheep regrows wool");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "SheepRegrows";
    }

    @Override
    public void destroy() {
        SheepRegrowWoolEvent.getHandlerList().unregister(this);
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
        return super.getContext(name);
    }

    @EventHandler
    public void onSheepRegrows(SheepRegrowWoolEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(entity.getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
