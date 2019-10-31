package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;

public class CreeperPoweredScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // creeper powered (because <cause>)
    //
    // @Regex ^on creeper powered( because [^\s]+)?$
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a creeper is struck by lightning and turned into a powered creeper.
    //
    // @Context
    // <context.entity> returns the EntityTag of the creeper.
    // <context.lightning> returns the EntityTag of the lightning.
    // <context.cause> returns an ElementTag of the cause for the creeper being powered.
    //
    // -->

    public CreeperPoweredScriptEvent() {
        instance = this;
    }

    public static CreeperPoweredScriptEvent instance;
    public EntityTag lightning;
    public EntityTag entity;
    public ElementTag cause;
    public CreeperPowerEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("creeper powered");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (path.eventArgLowerAt(2).equals("because")
                && !path.eventArgLowerAt(3).equals(CoreUtilities.toLowerCase(cause.toString()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "CreeperPowered";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("lightning") && lightning != null) {
            return lightning;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCreeperPowered(CreeperPowerEvent event) {
        lightning = new EntityTag(event.getLightning());
        entity = new EntityTag(event.getEntity());
        cause = new ElementTag(event.getCause().name());
        this.event = event;
        fire(event);
    }
}
