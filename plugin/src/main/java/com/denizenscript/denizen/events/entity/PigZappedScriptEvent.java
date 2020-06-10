package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PigZapEvent;

public class PigZappedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // pig zapped
    //
    // @Regex ^on pig zapped$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a pig is zapped by lightning and turned into a pig zombie.
    //
    // @Context
    // <context.pig> returns the EntityTag of the pig.
    // <context.pig_zombie> returns the EntityTag of the pig zombie.
    // <context.lightning> returns the EntityTag of the lightning.
    //
    // -->

    public PigZappedScriptEvent() {
        instance = this;
    }

    public static PigZappedScriptEvent instance;
    public EntityTag pig;
    public EntityTag pig_zombie;
    private EntityTag lightning;
    public PigZapEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.equals("pig zapped")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, pig.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PigZapped";
    }

    @Override
    public ObjectTag getContext(String name) {
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
        pig = new EntityTag(event.getEntity());
        Entity pigZombie = event.getPigZombie();
        EntityTag.rememberEntity(pigZombie);
        pig_zombie = new EntityTag(pigZombie);
        lightning = new EntityTag(event.getLightning());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(pigZombie);
    }
}
