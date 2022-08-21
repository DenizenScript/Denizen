package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.GenericGameEvent;

public class GenericGameEventScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // generic game event
    //
    // @Group World
    //
    // @Location true
    //
    // @Switch type:<game_event_name> to only process the event when a specific game event is fired.
    //
    // @Cancellable true
    //
    // @Triggers when the minecraft world experiences a generic minecraft game event. This is normally used for sculk sensors.
    //
    // @Context
    // <context.location> returns the location of the event.
    // <context.entity> returns the entity that triggered the event, if any.
    // <context.game_event> returns the name of the Minecraft game event, for example "minecraft:block_change". See <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameEvent.html>.
    // <context.radius> returns the radius, in blocks, that the event is broadcast to.
    //
    // -->

    public GenericGameEventScriptEvent() {
        registerCouldMatcher("generic game event");
        registerSwitches("type");
    }

    public LocationTag location;
    public GenericGameEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        String typeSwitch = path.switches.get("type");
        if (typeSwitch != null) {
            if (!runGenericCheck(typeSwitch, event.getEvent().getKey().toString()) && !runGenericCheck(typeSwitch, event.getEvent().getKey().getKey())) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getEntity());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "entity":
                if (event.getEntity() != null) {
                    return new EntityTag(event.getEntity()).getDenizenObject();
                }
                break;
            case "game_event": return new ElementTag(event.getEvent().getKey().toString());
            case "radius": return new ElementTag(event.getRadius());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onGenericGameEvent(GenericGameEvent event) {
        location = new LocationTag(event.getLocation());
        this.event = event;
        fire(event);
    }
}
