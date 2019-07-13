package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // potion splash
    // <item> splashes
    //
    // @Regex ^on [^\s]+ splashes$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a splash potion breaks open
    //
    // @Context
    // <context.potion> returns a dItem of the potion that broke open.
    // <context.entities> returns a dList of effected entities.
    // <context.location> returns the dLocation the splash potion broke open at.
    // <context.entity> returns a dEntity of the splash potion.
    //
    // -->

    public PotionSplashScriptEvent() {
        instance = this;
    }

    public static PotionSplashScriptEvent instance;
    public dItem potion;
    public dList entities;
    public dLocation location;
    public dEntity entity;
    public PotionSplashEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("splash") || cmd.equals("splashes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(0);
        return tryItem(potion, iTest) && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PotionSplash";
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
        else if (name.equals("entities")) {
            return entities;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("potion")) {
            return potion;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        entity = new dEntity(event.getEntity());
        potion = new dItem(event.getPotion().getItem());
        location = new dLocation(entity.getLocation());
        entities = new dList();
        for (Entity e : event.getAffectedEntities()) {
            entities.add(new dEntity(e).identify());
        }
        this.event = event;
        fire(event);
    }
}
