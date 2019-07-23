package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class FireworkBurstsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // firework bursts
    //
    // @Regex ^on firework bursts$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a firework bursts (explodes).
    //
    // @Context
    // <context.entity> returns the firework that exploded.
    // <context.item>  returns the firework item.
    // <context.location> returns the LocationTag the firework exploded at.
    //
    // -->

    public FireworkBurstsScriptEvent() {
        instance = this;
    }

    public static FireworkBurstsScriptEvent instance;
    public FireworkExplodeEvent event;
    public EntityTag entity;
    public LocationTag location;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("firework bursts");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "FireworkBursts";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            ItemStack itemStack = new ItemStack(MaterialCompat.FIREWORK_ROCKET);
            itemStack.setItemMeta(event.getEntity().getFireworkMeta());
            return new ItemTag(itemStack);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onFireworkBursts(FireworkExplodeEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(entity.getLocation());
        this.event = event;
        fire(event);
    }
}
