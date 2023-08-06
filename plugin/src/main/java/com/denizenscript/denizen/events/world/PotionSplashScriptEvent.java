package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

import java.util.ArrayList;
import java.util.List;

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
    // @Triggers when a splash potion breaks open.
    //
    // @Context
    // <context.potion> returns an ItemTag of the potion that broke open.
    // <context.entities> returns a ListTag of affected entities.
    // <context.location> returns the LocationTag the splash potion broke open at.
    // <context.entity> returns an EntityTag of the splash potion.
    // <context.intensity> returns an ListTag(MapTag) of the intensity for all affected entities.
    //
    // @Determine
    // INTENSITY:<ListTag(MapTag)>" to set the intensity of specified entities.
    //
    // @Example
    // # This example sets the intensity of the first affected entity to 0.6.
    // on potion splashes:
    // - if <context.entities.any>:
    //      - determine INTENSITY:[entity=<context.entities.first>;intensity=0.6]
    //
    // -->

    public PotionSplashScriptEvent() {
        registerCouldMatcher("<item> splash|splashes");
    }

    public ItemTag potion;
    public LocationTag location;
    public PotionSplashEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String item = path.eventArgLowerAt(0);
        if (!potion.tryAdvancedMatcher(item)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return new EntityTag(event.getEntity());
            case "location": return location;
            case "potion": return potion;
            case "entities":
                ListTag entities = new ListTag();
                for (Entity e : event.getAffectedEntities()) {
                    entities.addObject(new EntityTag(e).getDenizenObject());
                }
                return entities;
            case "intensity":
                ListTag intensity = new ListTag();
                for (Entity e : event.getAffectedEntities()) {
                    if (e instanceof LivingEntity) {
                        intensity.addObject(intensityToMap((LivingEntity) e));
                    }
                }
                return intensity;
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("intensity:")) {
                TagContext context = getTagContext(path);
                ObjectTag obj = ListTag.valueOf(lower.substring("intensity:".length()), context);
                List<ObjectTag> data = new ArrayList<>(CoreUtilities.objectToList(obj, context));
                for (ObjectTag result : data) {
                    if (result.canBeType(MapTag.class)) {
                        EntityTag entity = result.asType(MapTag.class, context).getObjectAs("entity", EntityTag.class, context);
                        ElementTag intensity = result.asType(MapTag.class, context).getObjectAs("intensity", ElementTag.class, context);
                        if (entity == null || intensity == null) {
                            Debug.echoError("Cannot return values from map. Are you sure your MapTag input matches [entity=<EntityTag>;intensity=<ElementTag(Number)>]?");
                            continue;
                        }
                        if (intensity.isDouble()) {
                            event.setIntensity(entity.getLivingEntity(), intensity.asDouble());
                        }
                    }
                    else {
                        Debug.echoError("MapTag input invalid. Are you sure you provided a valid MapTag?");
                    }
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        potion = new ItemTag(event.getPotion().getItem());
        location = new LocationTag(event.getEntity().getLocation());
        this.event = event;
        fire(event);
    }

    public MapTag intensityToMap(LivingEntity entity) {
        MapTag result = new MapTag();
        result.putObject("entity", new EntityTag(entity));
        result.putObject("intensity", new ElementTag(event.getIntensity(entity)));
        return result;
    }
}
