package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public abstract class BukkitScriptEvent extends ScriptEvent {

    public static Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException var3) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Event.class) && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    public static HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList)method.invoke(null);
        } catch (Exception var3) {
            throw new IllegalPluginAccessException(var3.toString());
        }
    }

    private static Field REGISTERED_LISTENER_EXECUTOR_FIELD;

    static {
        try {
            REGISTERED_LISTENER_EXECUTOR_FIELD = RegisteredListener.class.getDeclaredField("executor");
            REGISTERED_LISTENER_EXECUTOR_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException ex) {
            dB.echoError(ex);
        }
    }

    public static EventExecutor getExecutor(RegisteredListener listener) {
        try {
            return (EventExecutor) REGISTERED_LISTENER_EXECUTOR_FIELD.get(listener);
        }
        catch (IllegalAccessException ex) {
            dB.echoError(ex);
        }
        return null;
    }

    public HashMap<EventPriority, BukkitScriptEvent> priorityHandlers;

    public List<Map.Entry<RegisteredListener, HandlerList>> registeredHandlers;

    // <--[language]
    // @name bukkit event priority
    // @description
    // Script events that are backed by standard Bukkit events are able to control what underlying Bukkit event priority
    // they register as.
    // This can be useful, for example, if a different plugin is cancelling the event at a later priority,
    // and you're writing a script that needs to un-cancel the event.
    // This can be done using the "bukkit_priority" switch.
    // Valid priorities, in order of execution, are: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR.
    // Monitor is executed last, and is intended to only be used when reading the results of an event but not changing it.
    // The default priority is "normal".
    // -->

    public void fire(Cancellable cancellable) {
        cancelled = cancellable.isCancelled();
        boolean wasCancelled = cancelled;
        fire();
        if (cancelled != wasCancelled) {
            cancellable.setCancelled(cancelled);
        }
    }

    @Override
    public void destroy() {
        if (priorityHandlers != null) {
            for (BukkitScriptEvent event : priorityHandlers.values()) {
                event.destroy();
            }
            priorityHandlers = null;
        }
        if (registeredHandlers != null) {
            for (Map.Entry<RegisteredListener, HandlerList> handler : registeredHandlers) {
                handler.getValue().unregister(handler.getKey());
            }
            registeredHandlers = null;
        }
    }

    @Override
    public void init() {
        if (this instanceof Listener) {
            initListener((Listener) this);
        }
    }

    public void initListener(Listener listener) {
        if (priorityHandlers == null) {
            priorityHandlers = new HashMap<>();
        }
        for (ScriptPath path : new ArrayList<>(eventPaths)) {
            String bukkitPriority = path.switches.get("bukkit_priority");
            if (bukkitPriority != null) {
                try {
                    EventPriority priority = EventPriority.valueOf(bukkitPriority.toUpperCase());
                    BukkitScriptEvent handler = priorityHandlers.get(priority);
                    if (handler == null) {
                        handler = (BukkitScriptEvent) clone();
                        handler.eventPaths = new ArrayList<>();
                        handler.priorityHandlers = null;
                        handler.registeredHandlers = null;
                        priorityHandlers.put(priority, handler);
                        handler.initForPriority(priority, (Listener) handler);
                    }
                    handler.eventPaths.add(path);
                    eventPaths.remove(path);
                }
                catch (IllegalArgumentException ex) {
                    dB.echoError("Invalid 'bukkit_priority' switch for event '" + path.event + "' in script '" + path.container.getName() + "'.");
                    dB.echoError(ex);
                }
            }
        }
        if (!eventPaths.isEmpty()) {
            initForPriority(EventPriority.NORMAL, listener);
        }
    }

    public void initForPriority(EventPriority priority, Listener listener) {
        if (registeredHandlers == null) {
            registeredHandlers = new ArrayList<>();
        }
        Plugin plugin = DenizenAPI.getCurrentInstance();
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry :
                plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            for (RegisteredListener registeredListener : entry.getValue()) {
                RegisteredListener newListener = new RegisteredListener(listener, getExecutor(registeredListener), priority, plugin, false);
                HandlerList handlers = getEventListeners(getRegistrationClass(entry.getKey()));
                handlers.register(newListener);
                registeredHandlers.add(new HashMap.SimpleEntry<>(newListener, handlers));
            }
        }
    }

    @Deprecated
    public boolean runInCheck(ScriptContainer scriptContainer, String s, String lower, Location location) {
        return runInCheck(scriptContainer, s, lower, location, "in");
    }

    @Deprecated
    public boolean runInCheck(ScriptContainer scriptContainer, String s, String lower, Location location, String innote) {
        return runInCheck(new ScriptPath(scriptContainer, s), location, innote);
    }

    public boolean runInCheck(ScriptPath path, Location location) {
        return runInCheck(path, location, "in");
    }

    public boolean runInCheck(ScriptPath path, Location location, String innote) {
        String it = path.switches.get(innote);
        if (it == null) {
            int index;
            for (index = 0; index < path.eventArgsLower.length; index++) {
                if (path.eventArgsLower[index].equals(innote)) {
                    break;
                }
            }
            if (index >= path.eventArgsLower.length) {
                // No 'in ...' specified
                return true;
            }
            it = path.eventArgLowerAt(index + 1);
            if (it.equals("notable")) {
                String subit = path.eventArgLowerAt(index + 2);
                if (subit.equals("cuboid")) {
                    return dCuboid.getNotableCuboidsContaining(location).size() > 0;
                }
                else if (subit.equals("ellipsoid")) {
                    return dEllipsoid.getNotableEllipsoidsContaining(location).size() > 0;
                }
                else {
                    dB.echoError("Invalid event 'IN ...' check [" + getName() + "] ('in notable ???'): '" + path.event + "' for " + path.container.getName());
                    return false;
                }
            }
        }
        String lower = CoreUtilities.toLowerCase(it);
        if (lower.equals("cuboid")) {
            return dCuboid.getNotableCuboidsContaining(location).size() > 0;
        }
        else if (lower.equals("ellipsoid")) {
            return dEllipsoid.getNotableEllipsoidsContaining(location).size() > 0;
        }
        else if (dWorld.matches(it)) {
            return CoreUtilities.toLowerCase(location.getWorld().getName()).equals(lower);
        }
        else if (dCuboid.matches(it)) {
            dCuboid cuboid = dCuboid.valueOf(it);
            return cuboid.isInsideCuboid(location);
        }
        else if (dEllipsoid.matches(it)) {
            dEllipsoid ellipsoid = dEllipsoid.valueOf(it);
            return ellipsoid.contains(location);
        }
        else {
            dB.echoError("Invalid event 'in:<area>' switch [" + getName() + "] ('in:???'): '" + path.event + "' for " + path.container.getName());
            return false;
        }
    }

    public boolean tryLocation(dLocation location, String comparedto) {
        if (comparedto == null || comparedto.length() == 0) {
            dB.echoError("Null or empty location string to compare");
            return false;
        }
        if (comparedto.equals("notable")) {
            return true;
        }
        comparedto = "l@" + comparedto;
        dLocation loc = dLocation.valueOf(comparedto);
        if (loc == null) {
            dB.echoError("Invalid location in location comparison string: " + comparedto);
            return false;
        }
        return loc.getBlock().equals(location.getBlock());
    }

    @Deprecated
    public boolean runWithCheck(ScriptContainer scriptContainer, String s, String lower, dItem held) {
        return runWithCheck(new ScriptPath(scriptContainer, s), held);
    }

    public static TagContext noDebugTagContext = new BukkitTagContext(null, null, false, null, false, null);

    public boolean runWithCheck(ScriptPath path, dItem held) {
        String with = path.switches.get("with");
        if (with != null) {
            if (with.equalsIgnoreCase("item")) {
                return true;
            }
            if (held == null || !tryItem(held, with)) {
                return false;
            }
        }
        return true;
    }

    private static final String ASTERISK_QUOTED = Pattern.quote("*");

    public String regexHandle(String input) {
        if (input.startsWith("regex:")) {
            return input.substring("regex:".length());
        }
        if (input.contains("*")) {
            return Pattern.quote(input).replace(ASTERISK_QUOTED, "(.*)");
        }
        return null;
    }

    public boolean equalityCheck(String input, String compared, String regexed) {
        input = CoreUtilities.toLowerCase(input);
        return input.equals(compared) || (regexed != null && input.matches(regexed));
    }

    public boolean tryInventory(dInventory inv, String comparedto) {
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("inventory")) {
            return true;
        }
        if (comparedto.equals("notable")) {
            return NotableManager.isSaved(inv);
        }
        String regexd = regexHandle(comparedto);
        if (equalityCheck(inv.getInventoryType().name(), comparedto, regexd)) {
            return true;
        }
        if (equalityCheck(inv.getIdType(), comparedto, regexd)) {
            return true;
        }
        if (equalityCheck(inv.getIdHolder(), comparedto, regexd)) {
            return true;
        }
        if (NotableManager.isSaved(inv)) {
            if (equalityCheck(NotableManager.getSavedId(inv), comparedto, regexd)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryItem(dItem item, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || item == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("item")) {
            return true;
        }
        if (comparedto.equals("potion") && CoreUtilities.toLowerCase(item.getItemStack().getType().name()).contains("potion")) {
            return true;
        }
        dMaterial quickOf = dMaterial.quickOfNamed(comparedto);
        if (quickOf != null) {
            dMaterial mat = item.getMaterial();
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        String regexd = regexHandle(comparedto);
        item = new dItem(item.getItemStack().clone());
        item.setAmount(1);
        if (equalityCheck(item.identify().substring("i@".length()), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(item.identifyMaterialNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(item.identifySimple().substring("i@".length()), comparedto, regexd)) {
            return true;
        }
        item.setDurability((short) 0);
        if (equalityCheck(item.identifyMaterialNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

    public boolean tryMaterial(dMaterial mat, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || mat == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("block") || comparedto.equals("material")) {
            return true;
        }
        dMaterial quickOf = dMaterial.quickOfNamed(comparedto);
        if (quickOf != null) {
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        String regexd = regexHandle(comparedto);
        if (equalityCheck(mat.realName(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifyNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifySimpleNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifyFullNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

    public boolean tryEntity(dEntity entity, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || entity == null) {
            return false;
        }
        Entity bEntity = entity.getBukkitEntity();
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("entity")) {
            return true;
        }
        else if (comparedto.equals("npc")) {
            return entity.isCitizensNPC();
        }
        else if (comparedto.equals("player")) {
            return entity.isPlayer();
        }
        else if (comparedto.equals("vehicle")) {
            return bEntity instanceof Vehicle;
        }
        else if (comparedto.equals("projectile")) {
            return bEntity instanceof Projectile;
        }
        else if (comparedto.equals("hanging")) {
            return bEntity instanceof Hanging;
        }
        String regexd = regexHandle(comparedto);
        if (entity.getEntityScript() != null && equalityCheck(entity.getEntityScript(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(entity.getEntityType().getLowercaseName(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

}
