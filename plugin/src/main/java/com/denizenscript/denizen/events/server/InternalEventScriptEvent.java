package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class InternalEventScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // internal bukkit event
    //
    // @Switch event:<path> (required) to specify the Bukkit event path to use (like "event:org.bukkit.event.block.BlockBreakEvent")
    //
    // @Warning This exists primarily for testing/debugging, and is almost never a good idea to include in a real script.
    //
    // @Group Server
    //
    // @Cancellable true
    //
    // @Triggers when the specified internal Bukkit event fires. Useful for testing/debugging, or for interoperation with external plugins that have their own Bukkit events. Get the raw event via 'context.reflect_event'.
    //
    // -->

    public InternalEventScriptEvent() {
        registerCouldMatcher("internal bukkit event");
        registerSwitches("event");
    }


    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (!path.switches.containsKey("event")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!currentEvent.getClass().getCanonicalName().equals(path.switches.get("event"))) {
            return false;
        }
        return true;
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "fields":
                if (!CoreConfiguration.allowReflectionFieldReads) {
                    return null;
                }
                BukkitImplDeprecations.internalEventReflectionContext.warn();
                ListTag result = new ListTag();
                Class c = currentEvent.getClass();
                while (c != null && c != Object.class) {
                    for (Field field : ReflectionHelper.getFields(c).getAllFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            result.addObject(new ElementTag(field.getName(), true));
                        }
                    }
                    c = c.getSuperclass();
                }
                return result;
        }
        if (name.startsWith("field_")) {
            if (!CoreConfiguration.allowReflectionFieldReads) {
                return null;
            }
            BukkitImplDeprecations.internalEventReflectionContext.warn();
            String fName = CoreUtilities.toLowerCase(name.substring("field_".length()));
            Class c = currentEvent.getClass();
            while (c != null && c != Object.class) {
                ReflectionHelper.FieldCache fields = ReflectionHelper.getFields(c);
                for (Field field : fields.getAllFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && CoreUtilities.toLowerCase(field.getName()).equals(fName)) {
                        Object val = null;
                        try {
                            val = field.get(currentEvent);
                        }
                        catch (Throwable ex) {
                            Debug.echoError(ex);
                        }
                        if (val != null) {
                            return CoreUtilities.objectToTagForm(val, CoreUtilities.errorButNoDebugContext, false, false, false);
                        }
                    }
                }
                c = c.getSuperclass();
            }
        }
        return super.getContext(name);
    }


    @Override
    public void destroy() {
        if (registeredHandlers != null) {
            for (Map.Entry<RegisteredListener, HandlerList> handler : registeredHandlers) {
                handler.getValue().unregister(handler.getKey());
            }
            registeredHandlers = null;
        }
    }

    @Override
    public void init() {
        registeredHandlers = new ArrayList<>();
        HashSet<String> eventsGrabbed = new HashSet<>();
        for (ScriptPath path : new ArrayList<>(eventPaths)) {
            String eventName = path.switches.get("event");
            if (!eventsGrabbed.add(eventName)) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(eventName);
                if (!Event.class.isAssignableFrom(clazz)) {
                    Debug.echoError("Cannot initialize Internal Bukkit Event for event '" + eventName + "': that class is not an event class.");
                    return;
                }
                EventPriority priority = EventPriority.NORMAL;
                String bukkitPriority = path.switches.get("bukkit_priority");
                if (bukkitPriority != null) {
                    try {
                        priority = EventPriority.valueOf(bukkitPriority.toUpperCase());
                    }
                    catch (IllegalArgumentException ex) {
                        Debug.echoError("Invalid 'bukkit_priority' switch for event '" + path.event + "' in script '" + path.container.getName() + "'.");
                        Debug.echoError(ex);
                    }
                }
                InternalEventScriptEvent handler = (InternalEventScriptEvent) clone();
                handler.eventPaths = new ArrayList<>();
                handler.eventPaths.add(path);
                handler.registeredHandlers = null;
                handler.initForPriority(priority, this, (Class<? extends Event>) clazz);
                eventPaths.remove(path);
            }
            catch (ClassNotFoundException ex) {
                Debug.echoError("Cannot initialize Internal Bukkit Event for event '" + eventName + "': that event class does not exist.");
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
    }

    public void initForPriority(EventPriority priority, InternalEventScriptEvent baseEvent, Class<? extends Event> clazz) {
        Plugin plugin = Denizen.getInstance();
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(this, plugin).entrySet()) {
            for (RegisteredListener registeredListener : entry.getValue()) {
                RegisteredListener newListener = new RegisteredListener(this, getExecutor(registeredListener), priority, plugin, false);
                HandlerList handlers = getEventListeners(clazz);
                handlers.register(newListener);
                baseEvent.registeredHandlers.add(new HashMap.SimpleEntry<>(newListener, handlers));
            }
        }
    }

    @EventHandler
    public void onEventHappens(Event event) {
        fire(event);
    }
}
