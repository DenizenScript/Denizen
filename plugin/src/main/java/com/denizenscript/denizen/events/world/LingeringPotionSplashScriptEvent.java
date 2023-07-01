package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

public class LingeringPotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lingering potion splash|splashes
    // lingering <item> splash|splashes
    //
    // @Group World
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a lingering splash potion breaks open
    //
    // @Context
    // <context.potion> returns an ItemTag of the potion that broke open.
    // <context.location> returns the LocationTag the splash potion broke open at.
    // <context.entity> returns an EntityTag of the splash potion.
    // <context.cloud> returns the EntityTag of the area of effect cloud.
    // <context.radius> returns the radius of the effect cloud.
    // <context.duration> returns the lingering duration of the effect cloud.
    //
    // -->

    public LingeringPotionSplashScriptEvent() {
        registerCouldMatcher("lingering <item> splash|splashes");
    }

    public LingeringPotionSplashEvent event;
    public LocationTag location;
    public ItemTag item;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(1, item)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "radius" -> new ElementTag(event.getAreaEffectCloud().getRadius());
            case "duration" -> new DurationTag((long) event.getAreaEffectCloud().getDuration());
            case "potion" -> item;
            case "entity" -> new EntityTag(event.getEntity());
            case "cloud" -> new EntityTag(event.getAreaEffectCloud());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        item = new ItemTag(event.getEntity().getItem());
        location = new LocationTag(event.getAreaEffectCloud().getLocation());
        this.event = event;
        fire(event);
    }
}
