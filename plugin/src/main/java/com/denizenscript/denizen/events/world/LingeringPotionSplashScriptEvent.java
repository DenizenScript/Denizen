package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

public class LingeringPotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lingering potion splash
    // lingering <item> splashes
    //
    // @Regex ^on lingering [^\s]+ splashes$
    //
    // @Group World
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a lingering splash potion breaks open
    //
    // @Context
    // <context.potion> returns a ItemTag of the potion that broke open.
    // <context.location> returns the LocationTag the splash potion broke open at.
    // <context.entity> returns a EntityTag of the splash potion.
    // <context.radius> returns the radius of the effect cloud.
    // <context.duration> returns the lingering duration of the effect cloud.
    //
    // -->

    public LingeringPotionSplashScriptEvent() {
        instance = this;
    }

    public static LingeringPotionSplashScriptEvent instance;
    public LingeringPotionSplashEvent event;
    public LocationTag location;
    public ItemTag item;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(0).equals("lingering")) {
            return false;
        }
        String cmd = path.eventArgLowerAt(2);
        if (!cmd.equals("splash") && !cmd.equals("splashes")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = CoreUtilities.getXthArg(1, path.event);
        if (!tryItem(item, iTest)) {
            return false;
        }
        if (runInCheck(path, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "LingeringPotionSplash";
    }


    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("radius")) {
            return new ElementTag(event.getAreaEffectCloud().getRadius());
        }
        else if (name.equals("duration")) {
            return new DurationTag((long) event.getAreaEffectCloud().getDuration());
        }
        else if (name.equals("potion")) {
            return item;
        }
        else if (name.equals("entity")) {
            return new EntityTag(event.getEntity());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        item = new ItemTag(event.getEntity().getItem());
        location = new LocationTag(event.getAreaEffectCloud().getLocation());
        this.event = event;
        fire(event);
    }
}
