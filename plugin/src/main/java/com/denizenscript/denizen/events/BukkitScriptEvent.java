package com.denizenscript.denizen.events;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.NotedAreaTracker;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.JavaReflectedObjectTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public abstract class BukkitScriptEvent extends ScriptEvent {

    // <--[language]
    // @name Advanced Object Matchables
    // @group Object System
    // @description
    // Script events have a variety of matchable object inputs, and the range of inputs they accept may not always be obvious.
    // For example, an event might be "player clicks <block>"... what can "<block>" be filled with?
    //
    // "<block>" usually indicates that a LocationTag and/or MaterialTag will be matched against.
    // This means you can specify any valid block material name, like "stone" or "air", like "on player clicks stone:" (will only run the event if the player is clicking stone)
    // You can also use a catch-all such as "block", like "on player clicks block:" (will always run the event when the player clicks anything/anywhere)
    // You can also use some more complicated matchables such as "vanilla_tagged:", like "on player clicks vanilla_tagged:mineable/axe:" (will run if the block is mineable with axes)
    // (For more block-related options, refer to the <@link objecttype LocationTag> and <@link objecttype MaterialTag> matchers lists.)
    //
    // Many object types can be used for matchables, the valid inputs are unique depending on the object type involved.
    //
    // Some inputs don't refer to any object at all - they're just advanced matchers for some generic plaintext,
    // for example "<cause>" implies an enumeration of causes will be matched against.
    //
    // Many inputs support advanced matchers. For details on that, see <@link language Advanced Object Matching>.
    //
    // A common matchable type found among different objects is a Flag Matchable. This usually looks like "item_flagged:<flag>"
    // This matches if the object has the specified flag, and fails to match if the object doesn't have that flag.
    // You can specify multiple required flags with '|', like "item_flagged:a|b|c", which will match if-and-only-if the item has ALL the flags named.
    // They can also be used to require the object does NOT have the flag with a "!" like "item_flagged:!<flag>".
    // When using multiple flags with "|", the "!" is per-entry, so "item_flagged:!a|b" requires the item DOES have 'b' but does NOT have 'a'.
    //
    // Note also that in addition to events, tags often also have matchables as input params,
    // usually documented like ".types[<matcher>]", with tag documentation specifying what matcher is used,
    // or like "<material_matcher>" to indicate in this example specifically MaterialTag matchables are allowed.
    //
    // Not all object types have defined matchable options, and those that do list them in their ObjectType meta. For an example of this, check <@link objecttype ItemTag>.
    //
    // As a special case, "in:<area>" style matchable listings in event conform to the following option set:
    // "biome:<name>": matches if the location is in a given biome, using advanced matchers.
    // "cuboid" plaintext: matches if the location is in any noted cuboid.
    // "ellipsoid" plaintext: matches if the location is in any noted ellipsoid.
    // "polygon" plaintext: matches if the location is in any noted polygon.
    // "chunk_flagged:<flag>": a Flag Matchable for ChunkTag flags.
    // "area_flagged:<flag>": a Flag Matchable for AreaObject flags.
    // Area note name: matches if an AreaObject note that matches the given advanced matcher contains the location.
    // If none of the above are used, uses WorldTag matchers.
    //
    // -->


    public static boolean couldMatchLegacyInArea(String lower) {
        int index = CoreUtilities.split(lower, ' ').indexOf("in");
        if (index == -1) {
            return true;
        }
        String in = CoreUtilities.getXthArg(index + 1, lower);
        if (couldMatchInventory(in)) {
            return false;
        }
        if (in.equals("notable") || in.equals("noted")) {
            String next = CoreUtilities.getXthArg(index + 2, lower);
            if (!next.equals("cuboid") && !next.equals("ellipsoid")) {
                return false;
            }
        }
        return true;
    }

    public static HashSet<String> areaCouldMatchableText = new HashSet<>(Arrays.asList("area", "cuboid", "polygon", "ellipsoid"));
    public static HashSet<String> areaCouldMatchPrefixes = new HashSet<>(Arrays.asList("area_flagged", "biome"));

    public static boolean couldMatchArea(String text) {
        if (areaCouldMatchableText.contains(text)) {
            return true;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (areaCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (NoteManager.getSavedObject(text) instanceof AreaContainmentObject) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (Notable obj : NoteManager.nameToObject.values()) {
                if (obj instanceof AreaContainmentObject && matcher.doesMatch(((AreaContainmentObject) obj).getNoteName())) {
                    return true;
                }
            }
            addPossibleCouldMatchFailReason("Imperfect area label (allowed due to advanced-matcher usage)", text);
            return true;
        }
        else {
            addPossibleCouldMatchFailReason("Not a valid area label", text);
            return false;
        }
    }

    public static boolean exactMatchesEnum(String text, final Enum<?>[] enumVals) {
        for (Enum<?> val : enumVals) {
            if (CoreUtilities.equalsIgnoreCase(val.name(), text)) {
                return true;
            }
        }
        addPossibleCouldMatchFailReason("Does not match required enumeration", text);
        return false;
    }

    public static boolean couldMatchEnum(String text, final Enum<?>[] enumVals) {
        if (exactMatchesEnum(text, enumVals)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (Enum<?> val : enumVals) {
                if (matcher.doesMatch(val.name())) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Does not match required enumeration", text);
        return false;
    }

    public static HashSet<String> inventoryCouldMatchableText = new HashSet<>(Arrays.asList("inventory", "notable", "note"));
    public static HashSet<String> inventoryCouldMatchPrefixes = new HashSet<>(Arrays.asList("inventory_flagged"));

    public static boolean couldMatchInventory(String text) {
        if (inventoryCouldMatchableText.contains(text)) {
            return true;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (inventoryCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (InventoryTag.matches(text)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (InventoryType type : InventoryType.values()) {
                if (matcher.doesMatch(type.name())) {
                    return true;
                }
            }
            for (String type : InventoryTag.idTypes) {
                if (matcher.doesMatch(type)) {
                    return true;
                }
            }
            for (String type : InventoryScriptHelper.inventoryScripts.keySet()) {
                if (matcher.doesMatch(type)) {
                    return true;
                }
            }
            for (InventoryTag note : InventoryScriptHelper.notedInventories.values()) {
                if (matcher.doesMatch(note.noteName)) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid inventory label", text);
        return false;
    }

    public static boolean couldMatchEntity(String text) {
        if (exactMatchEntity(text)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (EntityType entity : EntityType.values()) {
                if (matcher.doesMatch(entity.name())) {
                    return true;
                }
            }
            for (String script : EntityScriptHelper.scripts.keySet()) {
                if (matcher.doesMatch(script)) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid entity label", text);
        return false;
    }

    public static HashSet<String> entityCouldMatchPrefixes = new HashSet<>(Arrays.asList("entity_flagged", "player_flagged", "npc_flagged"));

    public static boolean exactMatchEntity(String text) {
        if (EntityTag.specialEntityMatchables.contains(text)) {
            return true;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (entityCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (EntityTag.matches(text)) {
            return true;
        }
        addPossibleCouldMatchFailReason("Not a valid entity label", text);
        return false;
    }

    public static HashSet<String> vehicleCouldMatchPrefixes = new HashSet<>(Arrays.asList("entity_flagged"));

    public static boolean exactMatchesVehicle(String text) {
        if (text.equals("vehicle")) {
            return true;
        }
        if (EntityTag.specialEntityMatchables.contains(text)) {
            return false;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (vehicleCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (text.startsWith("player_flagged:") || text.startsWith("npc_flagged:")) {
            return false;
        }
        if (EntityTag.matches(text)) {
            EntityTag entity = EntityTag.valueOf(text, CoreUtilities.noDebugContext);
            if (entity == null) {
                addPossibleCouldMatchFailReason("Broken entity/vehicle reference", text);
                return false;
            }
            if (!Vehicle.class.isAssignableFrom(entity.getEntityType().getBukkitEntityType().getEntityClass())) {
                addPossibleCouldMatchFailReason("Entity type is not a vehicle", text);
                return false;
            }
            return true;
        }
        addPossibleCouldMatchFailReason("Not a valid vehicle label", text);
        return false;
    }

    public static boolean couldMatchVehicle(String text) {
        if (exactMatchesVehicle(text)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (EntityType entity : EntityType.values()) {
                if (matcher.doesMatch(entity.name())) {
                    return true;
                }
            }
            for (String script : EntityScriptHelper.scripts.keySet()) {
                if (matcher.doesMatch(script)) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid vehicle label", text);
        return false;
    }

    public static boolean couldMatchBlockOrItem(String text) {
        if (itemCouldMatchableText.contains(text) || materialCouldMatchableText.contains(text)) {
            return true;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (itemCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
            if (materialCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (MaterialTag.matches(text)) {
            MaterialTag mat = MaterialTag.valueOf(text, CoreUtilities.noDebugContext);
            if (mat == null) {
                return false;
            }
            return true;
        }
        if (ItemTag.matches(text)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (Material material : Material.values()) {
                if (matcher.doesMatch(material.name())) {
                    return true;
                }
            }
            for (String item : ItemScriptHelper.item_scripts.keySet()) {
                if (matcher.doesMatch(item)) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid block or item label", text);
        return false;
    }

    public static boolean couldMatchBlock(String text) {
        return couldMatchBlock(text, null);
    }

    public static HashSet<String> materialCouldMatchableText = new HashSet<>(Arrays.asList("block", "material"));
    public static HashSet<String> materialCouldMatchPrefixes = new HashSet<>(Arrays.asList("vanilla_tagged", "material_flagged"));

    public static boolean couldMatchBlock(String text, Function<Material, Boolean> requirement) {
        if (materialCouldMatchableText.contains(text)) {
            return true;
        }
        if (text.equals("item")) {
            return false;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (materialCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        if (MaterialTag.matches(text)) {
            MaterialTag mat = MaterialTag.valueOf(text, CoreUtilities.noDebugContext);
            if (mat == null || !mat.getMaterial().isBlock()) {
                return false;
            }
            if (mat.getMaterial().isBlock() && (requirement == null || requirement.apply(mat.getMaterial()))) {
                return true;
            }
            addPossibleCouldMatchFailReason("Material is an item not a block", text);
            return false;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (Material material : Material.values()) {
                if (material.isBlock() && matcher.doesMatch(material.name()) && (requirement == null || requirement.apply(material))) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid block label", text);
        return false;
    }

    public static HashSet<String> itemCouldMatchableText = new HashSet<>(Arrays.asList("item", "potion"));
    public static HashSet<String> itemCouldMatchPrefixes = new HashSet<>(Arrays.asList("item_flagged", "vanilla_tagged", "item_enchanted", "material_flagged", "raw_exact"));

    public static boolean couldMatchItem(String text) {
        if (itemCouldMatchableText.contains(text)) {
            return true;
        }
        int colon = text.indexOf(':');
        if (colon != -1) {
            if (itemCouldMatchPrefixes.contains(text.substring(0, colon))) {
                return true;
            }
        }
        int bracketIndex = text.indexOf('[');
        if (bracketIndex != -1) {
            return ItemTag.matches(text);
        }
        if (MaterialTag.matches(text)) {
            MaterialTag mat = MaterialTag.valueOf(text, CoreUtilities.noDebugContext);
            if (mat == null || !mat.getMaterial().isItem()) {
                addPossibleCouldMatchFailReason("Material is not an item", text);
                return false;
            }
            return true;
        }
        if (ItemTag.matches(text)) {
            return true;
        }
        if (isAdvancedMatchable(text)) {
            MatchHelper matcher = createMatcher(text);
            for (Material material : Material.values()) {
                if (material.isItem() && matcher.doesMatch(material.name())) {
                    return true;
                }
            }
            for (String item : ItemScriptHelper.item_scripts.keySet()) {
                if (matcher.doesMatch(item)) {
                    return true;
                }
            }
        }
        addPossibleCouldMatchFailReason("Not a valid item label", text);
        return false;
    }

    public static boolean nonSwitchWithCheck(ScriptPath path, ItemTag held) {
        int index;
        for (index = 0; index < path.eventArgsLower.length; index++) {
            if (path.eventArgsLower[index].equals("with")) {
                break;
            }
        }
        if (index >= path.eventArgsLower.length) {
            // No 'with ...' specified
            return true;
        }

        String with = path.eventArgLowerAt(index + 1);
        if (with != null && (held == null || !held.tryAdvancedMatcher(with))) {
            return false;
        }
        return true;
    }

    @Override
    public BukkitTagContext getTagContext(ScriptPath path) {
        return (BukkitTagContext) super.getTagContext(path);
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

    @Override
    public ScriptEvent fire() {
        if (!Bukkit.isPrimaryThread()) {
            if (CoreConfiguration.debugVerbose) {
                Debug.log("Event is firing async: " + getName());
            }
            BukkitScriptEvent altEvent = (BukkitScriptEvent) clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    altEvent.fire();
                }
            }.runTask(Denizen.getInstance());
            return altEvent;
        }
        return super.fire();
    }

    @Override
    public void cancellationChanged() {
        if (currentEvent instanceof Cancellable) {
            ((Cancellable) currentEvent).setCancelled(cancelled);
        }
        super.cancellationChanged();
    }

    public Event currentEvent = null;

    public void fire(Event event) {
        currentEvent = event;
        if (event instanceof Cancellable) {
            cancelled = ((Cancellable) event).isCancelled();
        }
        else {
            cancelled = false;
        }
        fire();
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "reflect_event": return currentEvent == null ? null : new JavaReflectedObjectTag(currentEvent);
        }
        return super.getContext(name);
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
                    EventPriority priority = EventPriority.valueOf(CoreUtilities.toUpperCase(bukkitPriority));
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
        Plugin plugin = Denizen.getInstance();
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

    public static boolean runFlaggedCheck(ScriptPath path, String switchName, AbstractFlagTracker tracker) {
        String flagged = path.switches.get(switchName);
        return coreFlaggedCheck(flagged, tracker);
    }

    public boolean runLocationFlaggedCheck(ScriptPath path, String switchName, Location location) {
        if (!path.switches.containsKey(switchName)) { // NOTE: opti to avoid 'getFlagTracker' call, also prevents pre-1.16 borks
            return true;
        }
        return runFlaggedCheck(path, switchName, location == null ? null : new LocationTag(location).getFlagTracker());
    }

    public static class BoolHolder {
        public boolean bool;
    }

    public boolean runInCheck(ScriptPath path, Location location, String innote) {
        if (!runLocationFlaggedCheck(path, "location_flagged", location)) {
            return false;
        }
        String inputText = path.switches.get(innote);
        if (inputText == null) {
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
            inputText = path.eventArgLowerAt(index + 1);
            if (inputText.equals("notable") || inputText.equals("noted")) {
                String subit = path.eventArgLowerAt(index + 2);
                if (subit.equals("cuboid")) {
                    BoolHolder bool = new BoolHolder();
                    NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (a instanceof CuboidTag) { bool.bool = true; } });
                    return bool.bool;
                }
                else if (subit.equals("ellipsoid")) {
                    BoolHolder bool = new BoolHolder();
                    NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (a instanceof EllipsoidTag) { bool.bool = true; } });
                    return bool.bool;
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
        if (inputText.startsWith("!")) {
            return !inCheckInternal(getTagContext(path), getName(), location, inputText.substring(1), path.event, path.container.getName());
        }
        return inCheckInternal(getTagContext(path), getName(), location, inputText, path.event, path.container.getName());
    }

    public static boolean inCheckInternal(TagContext context, String name, Location location, String inputText, String evtLine, String containerName) {
        String lower = CoreUtilities.toLowerCase(inputText);
        if (lower.contains(":")) {
            if (lower.startsWith("world_flagged:")) {
                return coreFlaggedCheck(inputText.substring("world_flagged:".length()), new WorldTag(location.getWorld()).getFlagTracker());
            }
            else if (lower.startsWith("chunk_flagged:")) {
                return coreFlaggedCheck(inputText.substring("chunk_flagged:".length()), new ChunkTag(location).getFlagTracker());
            }
            else if (lower.startsWith("area_flagged:")) {
                String flagName = inputText.substring("area_flagged:".length());
                BoolHolder bool = new BoolHolder();
                NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> {
                    if (a instanceof FlaggableObject && coreFlaggedCheck(flagName, ((FlaggableObject) a).getFlagTracker())) {
                        bool.bool = true;
                    }
                });
                return bool.bool;
            }
            else if (lower.startsWith("biome:")) {
                String biome = inputText.substring("biome:".length());
                return runGenericCheck(biome, new LocationTag(location).getBiome().name);
            }
        }
        if (lower.equals("cuboid")) {
            BoolHolder bool = new BoolHolder();
            NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (a instanceof CuboidTag) { bool.bool = true; } });
            return bool.bool;
        }
        else if (lower.equals("ellipsoid")) {
            BoolHolder bool = new BoolHolder();
            NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (a instanceof EllipsoidTag) { bool.bool = true; } });
            return bool.bool;
        }
        else if (lower.equals("polygon")) {
            BoolHolder bool = new BoolHolder();
            NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (a instanceof PolygonTag) { bool.bool = true; } });
            return bool.bool;
        }
        else if (WorldTag.matches(inputText)) {
            return CoreUtilities.equalsIgnoreCase(location.getWorld().getName(), lower);
        }
        else if (CuboidTag.matches(inputText)) {
            CuboidTag cuboid = CuboidTag.valueOf(inputText, context);
            if (cuboid == null || !cuboid.isUnique()) {
                if (context.showErrors()) {
                    Debug.echoError("Invalid event 'in:<area>' switch [" + name + "] (invalid cuboid): '" + evtLine + "' for " + containerName);
                }
                return false;
            }
            return cuboid.isInsideCuboid(location);
        }
        else if (EllipsoidTag.matches(inputText)) {
            EllipsoidTag ellipsoid = EllipsoidTag.valueOf(inputText, context);
            if (ellipsoid == null || !ellipsoid.isUnique()) {
                if (context.showErrors()) {
                    Debug.echoError("Invalid event 'in:<area>' switch [" + name + "] (invalid ellipsoid): '" + evtLine + "' for " + containerName);
                }
                return false;
            }
            return ellipsoid.contains(location);
        }
        else if (PolygonTag.matches(inputText)) {
            PolygonTag polygon = PolygonTag.valueOf(inputText, context);
            if (polygon == null || !polygon.isUnique()) {
                if (context.showErrors()) {
                    Debug.echoError("Invalid event 'in:<area>' switch [" + name + "] (invalid polygon): '" + evtLine + "' for " + containerName);
                }
                return false;
            }
            return polygon.doesContainLocation(location);
        }
        else if (isAdvancedMatchable(lower)) {
            MatchHelper matcher = createMatcher(lower);
            BoolHolder bool = new BoolHolder();
            NotedAreaTracker.forEachAreaThatContains(new LocationTag(location), (a) -> { if (matcher.doesMatch(a.getNoteName())) { bool.bool = true; } });
            if (bool.bool) {
                return true;
            }
            if (matcher.doesMatch(CoreUtilities.toLowerCase(location.getWorld().getName()))) {
                return true;
            }
            return false;
        }
        else {
            if (context.showErrors()) {
                Debug.echoError("Invalid event 'in:<area>' switch [" + name + "] ('in:???') (did you make a typo, or forget to 'note' an object with that name?): '" + evtLine + "' for " + containerName);
            }
            return false;
        }
    }

    public static boolean trySlot(ScriptPath path, String switchName, Entity entity, int slot) {
        String slotMatch = path.switches.get(switchName);
        if (slotMatch != null) {
            return SlotHelper.doesMatch(slotMatch, entity, slot);
        }
        return true;
    }

    public static boolean runWithCheck(ScriptPath path, ItemTag held) {
        return runWithCheck(path, held, "with");
    }

    public static boolean runWithCheck(ScriptPath path, ItemTag held, String key) {
        String with = path.switches.get(key);
        if (with != null) {
            if (CoreUtilities.equalsIgnoreCase(with, "item")) {
                return true;
            }
            if (held == null || !held.tryAdvancedMatcher(with)) {
                return false;
            }
        }
        return true;
    }

    public static boolean runFlaggedCheck(ScriptPath path, PlayerTag player) {
        return runFlaggedCheck(path, "flagged", player);
    }

    public static boolean runFlaggedCheck(ScriptPath path, String switchName, PlayerTag player) {
        String flagged = path.switches.get(switchName);
        if (flagged == null) {
            return true;
        }
        if (player == null) {
            return false;
        }
        return coreFlaggedCheck(flagged, player.getFlagTracker());
    }

    public static boolean runPermissionCheck(ScriptPath path, PlayerTag player) {
        return runPermissionCheck(path, "permission", player);
    }

    public static boolean runPermissionCheck(ScriptPath path, String switchName, PlayerTag player) {
        String perm = path.switches.get(switchName);
        if (perm == null) {
            return true;
        }
        if (player == null || !player.isOnline()) {
            return false;
        }
        for (String permName : CoreUtilities.split(perm, '|')) {
            boolean expect = true;
            if (permName.startsWith("!")) {
                permName = permName.substring(1);
                expect = false;
            }
            if (player.getPlayerEntity().hasPermission(permName) != expect) {
                return false;
            }
        }
        return true;
    }

    public static boolean runAutomaticPlayerSwitches(ScriptEvent event, ScriptPath path) {
        if (!path.switches.containsKey("flagged") && !path.switches.containsKey("permission")) {
            return true;
        }
        BukkitScriptEntryData data = (BukkitScriptEntryData) event.getScriptEntryData();
        if (!data.hasPlayer()) {
            return false;
        }
        if (!runFlaggedCheck(path, data.getPlayer())) {
            return false;
        }
        if (!runPermissionCheck(path, data.getPlayer())) {
            return false;
        }
        return true;
    }

    public static boolean runAssignedCheck(ScriptPath path, NPCTag npc) {
        String matcher = path.switches.get("assigned");
        if (matcher == null) {
            return true;
        }
        if (npc == null) {
            return false;
        }
        AssignmentTrait trait = npc.getCitizen().getTraitNullable(AssignmentTrait.class);
        if (trait == null) {
            return false;
        }
        for (String script : trait.assignments) {
            if (runGenericCheck(matcher, script)) {
                return true;
            }
        }
        return false;
    }

    public static boolean runAutomaticNPCSwitches(ScriptEvent event, ScriptPath path) {
        if (!path.switches.containsKey("assigned")) {
            return true;
        }
        BukkitScriptEntryData data = (BukkitScriptEntryData) event.getScriptEntryData();
        if (!data.hasNPC()) {
            return false;
        }
        if (!runAssignedCheck(path, data.getNPC())) {
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
    // within the firing of that event.
    // Take the following examples:
    // <code>
    // on player clicks in inventory:
    // - take iteminhand
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
    // The solution to this problem is simple: Use "after" instead of "on".
    // <code>
    // after player clicks in inventory:
    // - take iteminhand
    // after entity damaged:
    // - if <context.entity.is_spawned||false>:
    //   - remove <context.entity>
    // </code>
    // This will delay the script until *after* the event is complete, and thus outside of the problem area.
    // And thus should be fine. One limitation you should note is demonstrated in the second example event:
    // The normal guarantees of the event are no longer present (eg that the entity is still valid) and as such
    // you should validate these expectations remain true after the event (as seen with the 'if is_spawned' check).
    //
    // If you need determine changes to the event, you can instead use 'on' but add a 'wait 1t' after the determine but before other script logic.
    // This allows the risky parts to be after the event and outside the problem area, but still determine changes to the event.
    // Be sure to use 'passively' to allow the script to run in full.
    // <code>
    // on player clicks in inventory:
    // - determine passively cancelled
    // - wait 1t
    // - take iteminhand
    // on entity damaged:
    // - determine passively cancelled
    // - wait 1t
    // - if <context.entity.is_spawned||false>:
    //   - remove <context.entity>
    // </code>
    //
    // -->
}
