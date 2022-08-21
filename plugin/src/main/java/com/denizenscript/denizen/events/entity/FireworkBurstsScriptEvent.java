package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Material;
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
    //
    // @Group Entity
    //
    // @Location true
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
    }

    public FireworkExplodeEvent event;
    public EntityTag entity;
    public LocationTag location;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("firework bursts")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "location":
                return location;
            case "item":
                ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
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
