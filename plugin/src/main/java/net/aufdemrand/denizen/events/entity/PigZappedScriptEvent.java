package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PigZapEvent;

public class PigZappedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // pig zapped (in <area>)
    //
    // @Regex ^on pig zapped( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a pig is zapped by lightning and turned into a pig zombie.
    //
    // @Context
    // <context.pig> returns the dEntity of the pig.
    // <context.pig_zombie> returns the dEntity of the pig zombie.
    // <context.lightning> returns the dEntity of the lightning.
    //
    // -->

    public PigZappedScriptEvent() {
        instance = this;
    }

    public static PigZappedScriptEvent instance;
    public dEntity pig;
    public dEntity pig_zombie;
    private dEntity lightning;
    public PigZapEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (lower.equals("pig zapped"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, pig.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PigZapped";
    }

    @Override
    public void destroy() {
        PigZapEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("pig")) {
            return pig;
        }
        else if (name.equals("pig_zombie")) {
            return pig_zombie;
        }
        else if (name.equals("lightning")) {
            return lightning;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPigZapped(PigZapEvent event) {
        pig = new dEntity(event.getEntity());
        Entity pigZombie = event.getPigZombie();
        dEntity.rememberEntity(pigZombie);
        pig_zombie = new dEntity(pigZombie);
        lightning = new dEntity(event.getLightning());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        dEntity.forgetEntity(pigZombie);
        event.setCancelled(cancelled);
    }
}
