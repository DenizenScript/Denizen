package com.denizenscript.denizen.events;

import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.*;
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

    public boolean couldMatchItem(String text) {
        if (text.equals("item")) {
            return true;
        }
        if (MaterialTag.matches(text) || ItemTag.matches(text)) {
            return true;
        }
        if (text.contains("*")) {
            return true;
        }
        if (text.startsWith("regex:")) {
            return true;
        }
        // This one must be last.
        if (text.contains("|")) {
            for (String subMatch : text.split("\\|")) {
                if (!couldMatchItem(subMatch)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        }
        catch (NoSuchMethodException var3) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Event.class) && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            }
            else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    public static HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        }
        catch (Exception var3) {
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
            Debug.echoError(ex);
        }
    }

    public static EventExecutor getExecutor(RegisteredListener listener) {
        try {
            return (EventExecutor) REGISTERED_LISTENER_EXECUTOR_FIELD.get(listener);
        }
        catch (IllegalAccessException ex) {
            Debug.echoError(ex);
        }
        return null;
    }

    public HashMap<EventPriority, BukkitScriptEvent> priorityHandlers;

    public List<Map.Entry<RegisteredListener, HandlerList>> registeredHandlers;

    // <--[language]
    // @name Bukkit Event Priority
    // @group Script Events
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

    public void fire(Event event) {
        if (event instanceof Cancellable) {
            Cancellable cancellable = (Cancellable) event;
            cancelled = cancellable.isCancelled();
            boolean wasCancelled = cancelled;
            fire();
            if (cancelled != wasCancelled) {
                cancellable.setCancelled(cancelled);
            }
        }
        else {
            cancelled = false;
            fire();
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
                    Debug.echoError("Invalid 'bukkit_priority' switch for event '" + path.event + "' in script '" + path.container.getName() + "'.");
                    Debug.echoError(ex);
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
            if (location == null) {
                return false;
            }
            Deprecations.inAreaSwitchFormat.warn();
            it = path.eventArgLowerAt(index + 1);
            if (it.equals("notable")) {
                String subit = path.eventArgLowerAt(index + 2);
                if (subit.equals("cuboid")) {
                    return CuboidTag.getNotableCuboidsContaining(location).size() > 0;
                }
                else if (subit.equals("ellipsoid")) {
                    return EllipsoidTag.getNotableEllipsoidsContaining(location).size() > 0;
                }
                else {
                    Debug.echoError("Invalid event 'IN ...' check [" + getName() + "] ('in notable ???'): '" + path.event + "' for " + path.container.getName());
                    return false;
                }
            }
        }
        if (location == null) {
            return false;
        }
        String lower = CoreUtilities.toLowerCase(it);
        if (lower.equals("cuboid")) {
            return CuboidTag.getNotableCuboidsContaining(location).size() > 0;
        }
        else if (lower.equals("ellipsoid")) {
            return EllipsoidTag.getNotableEllipsoidsContaining(location).size() > 0;
        }
        else if (WorldTag.matches(it)) {
            return CoreUtilities.toLowerCase(location.getWorld().getName()).equals(lower);
        }
        else if (CuboidTag.matches(it)) {
            CuboidTag cuboid = CuboidTag.valueOf(it);
            if (cuboid == null || !cuboid.isUnique()) {
                Debug.echoError("Invalid event 'in:<area>' switch [" + getName() + "] (invalid cuboid): '" + path.event + "' for " + path.container.getName());
                return false;
            }
            return cuboid.isInsideCuboid(location);
        }
        else if (EllipsoidTag.matches(it)) {
            EllipsoidTag ellipsoid = EllipsoidTag.valueOf(it);
            if (ellipsoid == null || !ellipsoid.isUnique()) {
                Debug.echoError("Invalid event 'in:<area>' switch [" + getName() + "] (invalid ellipsoid): '" + path.event + "' for " + path.container.getName());
                return false;
            }
            return ellipsoid.contains(location);
        }
        else {
            Debug.echoError("Invalid event 'in:<area>' switch [" + getName() + "] ('in:???'): '" + path.event + "' for " + path.container.getName());
            return false;
        }
    }

    public boolean tryLocation(LocationTag location, String comparedto) {
        if (comparedto == null || comparedto.length() == 0) {
            Debug.echoError("Null or empty location string to compare");
            return false;
        }
        if (comparedto.equals("notable")) {
            return true;
        }
        comparedto = "l@" + comparedto;
        LocationTag loc = LocationTag.valueOf(comparedto);
        if (loc == null) {
            Debug.echoError("Invalid location in location comparison string: " + comparedto);
            return false;
        }
        return loc.getBlock().equals(location.getBlock());
    }

    @Deprecated
    public boolean runWithCheck(ScriptContainer scriptContainer, String s, String lower, ItemTag held) {
        return runWithCheck(new ScriptPath(scriptContainer, s), held);
    }

    public boolean runGenericCheck(String inputValue, String trueValue) {
        if (inputValue != null) {
            trueValue = CoreUtilities.toLowerCase(trueValue);
            inputValue = CoreUtilities.toLowerCase(inputValue);
            if (inputValue.equals(trueValue)) {
                return true;
            }
            Pattern regexd = regexHandle(inputValue);
            if (!equalityCheck(trueValue, inputValue, regexd)) {
                return false;
            }
        }
        return true;
    }

    public boolean runGenericSwitchCheck(ScriptPath path, String switchName, String value) {
        String with = path.switches.get(switchName);
        if (with != null) {
            value = CoreUtilities.toLowerCase(value);
            with = CoreUtilities.toLowerCase(with);
            if (with.equals(value)) {
                return true;
            }
            Pattern regexd = regexHandle(with);
            if (!equalityCheck(value, with, regexd)) {
                return false;
            }
        }
        return true;
    }

    public boolean runWithCheck(ScriptPath path, ItemTag held) {
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

    public boolean runFlaggedCheck(ScriptPath path, PlayerTag player) {
        return runFlaggedCheck(path, "flagged", player);
    }

    public boolean runFlaggedCheck(ScriptPath path, String switchName, PlayerTag player) {
        String flagged = path.switches.get(switchName);
        if (flagged == null) {
            return true;
        }
        if (player == null) {
            return false;
        }
        if (!FlagManager.playerHasFlag(player, flagged)) {
            return false;
        }
        return true;
    }

    public boolean runPermissionCheck(ScriptPath path, PlayerTag player) {
        return runPermissionCheck(path, "permission", player);
    }

    public boolean runPermissionCheck(ScriptPath path, String switchName, PlayerTag player) {
        String perm = path.switches.get(switchName);
        if (perm == null) {
            return true;
        }
        if (player == null || !player.isOnline()) {
            return false;
        }
        if (!player.getPlayerEntity().hasPermission(perm)) {
            return false;
        }
        return true;
    }

    // <--[language]
    // @name Player Event Switches
    // @group Script Events
    // @description
    // There are a few special switches available to any script event with a linked event.
    //
    // The "flagged:<flag name>" will limit the event to only fire when the player has the flag with the specified name.
    // It can be used like "on player breaks block flagged:nobreak:" (that would be used alongside "- flag player nobreak").
    //
    // The "permission:<perm key>" will limit the event to only fire when the player has the specified permission key.
    // It can be used like "on player breaks block permission:denizen.my.perm:"
    //
    // Note that these switches will be ignored for events that do not have a linked player.
    // Be cautious with events that will only sometimes have a linked player.
    // -->

    public boolean runAutomaticPlayerSwitches(ScriptPath path) {
        BukkitScriptEntryData data = (BukkitScriptEntryData) getScriptEntryData();
        if (!data.hasPlayer()) {
            return true;
        }
        if (!runFlaggedCheck(path, data.getPlayer())) {
            return false;
        }
        if (!runPermissionCheck(path, data.getPlayer())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runAutomaticPlayerSwitches(path)) {
            return false;
        }
        return true;
    }

    // <--[language]
    // @name Safety In Events
    // @group Script Events
    // @description
    // One of the more common issues in Denizen scripts (particularly ones relating to inventories) is
    // *event safety*. That is, using commands inside an event that don't get along with the event.
    //
    // The most common example of this is editing a player's inventory, within an inventory-related event.
    // Generally speaking, this problem becomes relevant any time an edit is made to something involved with an event,
    // within the firing of an example.
    // Take the following examples:
    // <code>
    // on player clicks in inventory:
    // - take <player.item_in_hand>
    // on entity damaged:
    // - remove <context.entity>
    // </code>
    //
    // In both examples above, something related to the event (the player's inventory, and the entity being damaged)
    // is being modified within the event itself.
    // These break due a rather important reason: The event is firing before and/or during the change to the object.
    // Most events operate this way. A series of changes *to the object* are pending, and will run immediately after
    // your script does... the problems resultant can range from your changes being lost to situational issues
    // (eg an inventory suddenly being emptied entirely) to even server crashes!
    // The second example event also is a good example of another way this can go wrong:
    // Many scripts and plugins will listen to the entity damage event, in ways that are simply unable to handle
    // the damaged entity just being gone now (when the event fires, it's *guaranteed* the entity is still present
    // but that remove command breaks the guarantee!).
    //
    // The solution to this problem is simple: Just wait a tick. Literally.
    // <code>
    // on player clicks in inventory:
    // - wait 1t
    // - take <player.item_in_hand>
    // on entity damaged:
    // - wait 1t
    // - if <context.entity.is_spawned||false>:
    //   - remove <context.entity>
    // </code>
    //
    // By waiting a tick, you cause your script to run *after* the event, and thus outside of the problem area.
    // And thus should be fine. One limitation you should note is demonstrated in the second example event:
    // The normal guarantees of the event are no longer present (eg that the entity is still valid) and as such
    // you should validate these expectations remain true after the event (as seen with the 'if is_spawned' check).
    //
    // -->

    public boolean tryInventory(InventoryTag inv, String comparedto) {
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("inventory")) {
            return true;
        }
        if (comparedto.equals("notable")) {
            return NotableManager.isSaved(inv);
        }
        Pattern regexd = regexHandle(comparedto);
        if (equalityCheck(inv.getInventoryType().name(), comparedto, regexd)) {
            return true;
        }
        if (equalityCheck(inv.getIdType(), comparedto, regexd)) {
            return true;
        }
        if (equalityCheck(inv.getIdHolder(), comparedto, regexd)) {
            return true;
        }
        if (inv.scriptName != null && equalityCheck(inv.scriptName, comparedto, regexd)) {
            return true;
        }
        if (NotableManager.isSaved(inv)) {
            if (equalityCheck(NotableManager.getSavedId(inv), comparedto, regexd)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryItem(ItemTag item, String comparedto) {
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
        MaterialTag quickOf = MaterialTag.quickOfNamed(comparedto);
        if (quickOf != null) {
            MaterialTag mat = item.getMaterial();
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        Pattern regexd = regexHandle(comparedto);
        item = new ItemTag(item.getItemStack().clone());
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

    public boolean tryMaterial(MaterialTag mat, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || mat == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("block") || comparedto.equals("material")) {
            return true;
        }
        MaterialTag quickOf = MaterialTag.quickOfNamed(comparedto);
        if (quickOf != null) {
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        Pattern regexd = regexHandle(comparedto);
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

    public boolean tryEntity(EntityTag entity, String comparedto) {
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
        else if (comparedto.equals("fish")) {
            return bEntity instanceof Fish;
        }
        else if (comparedto.equals("projectile")) {
            return bEntity instanceof Projectile;
        }
        else if (comparedto.equals("hanging")) {
            return bEntity instanceof Hanging;
        }
        Pattern regexd = regexHandle(comparedto);
        if (entity.getEntityScript() != null && equalityCheck(entity.getEntityScript(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(entity.getEntityType().getLowercaseName(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

}
