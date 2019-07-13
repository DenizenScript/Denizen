package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    // <context.potion> returns a ItemTag of the potion that broke open.
    // <context.entities> returns a ListTag of effected entities.
    // <context.location> returns the LocationTag the splash potion broke open at.
    // <context.entity> returns a EntityTag of the splash potion.
    //
    // -->

    public PotionSplashScriptEvent() {
        instance = this;
    }

    public static PotionSplashScriptEvent instance;
    public ItemTag potion;
    public ListTag entities;
    public LocationTag location;
    public EntityTag entity;
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
    public ObjectTag getContext(String name) {
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
        entity = new EntityTag(event.getEntity());
        potion = new ItemTag(event.getPotion().getItem());
        location = new LocationTag(entity.getLocation());
        entities = new ListTag();
        for (Entity e : event.getAffectedEntities()) {
            entities.add(new EntityTag(e).identify());
        }
        this.event = event;
        fire(event);
    }
}
