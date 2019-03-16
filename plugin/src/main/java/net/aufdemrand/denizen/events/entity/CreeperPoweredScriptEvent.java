package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;

public class CreeperPoweredScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // creeper powered (because <cause>)
    //
    // @Regex ^on creeper powered( because [^\s]+)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a creeper is struck by lightning and turned into a powered creeper.
    //
    // @Context
    // <context.entity> returns the dEntity of the creeper.
    // <context.lightning> returns the dEntity of the lightning.
    // <context.cause> returns an Element of the cause for the creeper being powered.
    //
    // -->

    public CreeperPoweredScriptEvent() {
        instance = this;
    }

    public static CreeperPoweredScriptEvent instance;
    public dEntity lightning;
    public dEntity entity;
    public Element cause;
    public CreeperPowerEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("creeper powered");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

        if (path.eventArgLowerAt(2).equals("because")
                && !CoreUtilities.xthArgEquals(3, lower, CoreUtilities.toLowerCase(cause.toString()))) {
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
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
        lightning = new dEntity(event.getLightning());
        entity = new dEntity(event.getEntity());
        cause = new Element(event.getCause().name());
        this.event = event;
        fire(event);
    }
}
