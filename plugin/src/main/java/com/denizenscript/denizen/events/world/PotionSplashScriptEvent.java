package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // potion splash|splashes
    // <item> splash|splashes
    //
    // @Group World
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a splash potion breaks open
    //
    // @Context
    // <context.potion> returns an ItemTag of the potion that broke open.
    // <context.entities> returns a ListTag of affected entities.
    // <context.location> returns the LocationTag the splash potion broke open at.
    // <context.entity> returns an EntityTag of the splash potion.
    //
    // -->

    public PotionSplashScriptEvent() {
        instance = this;
        registerCouldMatcher("<item> splash|splashes");
    }

    public static PotionSplashScriptEvent instance;
    public ItemTag potion;
    public LocationTag location;
    public PotionSplashEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(0);
        if (!iTest.equals("potion") && !potion.tryAdvancedMatcher(iTest)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PotionSplash";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return new EntityTag(event.getEntity());
            case "entities":
                ListTag entities = new ListTag();
                for (Entity e : event.getAffectedEntities()) {
                    entities.addObject(new EntityTag(e));
                }
                return entities;
            case "location":
                return location;
            case "potion":
                return potion;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        potion = new ItemTag(event.getPotion().getItem());
        location = new LocationTag(event.getEntity().getLocation());
        this.event = event;
        fire(event);
    }
}
