package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PigZapEvent;

public class PigZappedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // pig zapped
    //
    // @Synonyms pig struck by lightning, pig electrocuted, pig lightning strike, pig turns into pig zombie, pig turns into zombie pigman
    //
    // @Group Entity
    //
    // @Location true
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
        registerCouldMatcher("pig zapped");
    }

    public EntityTag pig;
    public EntityTag pig_zombie;
    private EntityTag lightning;
    public PigZapEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, pig.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return  switch (name) {
            case "pig" -> pig;
            case "pig_zombie" -> pig_zombie;
            case "lightning" -> lightning;
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(pig);
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
