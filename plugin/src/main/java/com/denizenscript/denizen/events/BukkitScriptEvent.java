package com.denizenscript.denizen.events;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
    // @name Script Event Object Matchables
    // @group Script Events
    // @description
    // Script events have a variety of matchable object inputs, and the range of inputs they accept may not always be obvious.
    // For example, an event might be "player clicks <block>"... what can "<block>" be filled with?
    //
    // "<block>" usually indicates that a MaterialTag will be matched against.
    // This means you can specify any valid block material name, like "stone" or "air".
    // You can also use "vanilla_tagged:<vanilla_tag_name>" or "material_flagged:<flag_name>".
    // You can also use "block" or "material" as catch-alls.
    //
    // "<item>" or similar expects of course an ItemTag.
    // You can use any valid item material type like "stick", or the name of an item script, or "item" as a catch-all, or "potion" for any potion item.
    // Items can also be used with an "item_flagged" secondary prefix, so for an event that has "with:<item>", you can also do "with:item_flagged:<flag name>".
    // For item matchers that aren't switches, this works similarly, like "on player consumes item_flagged:myflag:" (note that this is not a switch).
    // You can also use "vanilla_tagged:<vanilla_tag_name>" to check vanilla tags, or "material_flagged:<flag_name>" to check material flags (not item flags),
    // or "item_enchanted:<enchantment>" to check enchantments.
    // You can also use "raw_exact:<item>" to do an exact raw item data comparison (almost always a bad idea to use).
    //
    // "<entity>", "<projectile>", "<vehicle>", etc. are examples of where an EntityTag will be expected.
    // You can generally specify any potentially relevant entity type, such as "creeper" under "<entity>", or "arrow" for "<projectile>",
    // but you can also specify "entity" (catch-all), "player" (real players, NOT player-type NPCs), "npc" (Citizens NPC),
    // "vehicle" (minecarts, boats, horses, etc), "fish" (cod, pufferfish, etc), "projectile" (arrow, trident, fish hook, snowball, etc), "hanging" (painting, item_frame, etc),
    // "monster" (creepers, zombies, etc), "animals" (pigs, cows, etc), "mob" (creepers, pigs, etc), "living" (players, pigs, creepers, etc),
    // "entity_flagged:<flag_name>", "npc_flagged:<flag_name>", "player_flagged:<flag_name>" (all three work similar to 'item_flagged').
    //
    // "<inventory>" or similar expects of course an InventoryTag.
    // You can use "inventory" as a catch-all, "note" to mean any noted inventory, the name of an inventory script,
    // the name of an inventory note, the name of an inventory type (like "chest"),
    // or "inventory_flagged:<flag_name>" for noted inventories.
    //
    // "<world>" or similar expects of course a WorldTag.
    // You can use "world" as a catch-all, a world name, or "world_flagged:<flag_name>" (works similar to 'item_flagged').
    //
    // "<area>" or similar refers to any area-defining tag type, including WorldTag, CuboidTag, EllipsoidTag, and PolygonTag.
    // You can specify the name of any world, the name of any noted area, "world_flagged:<flag_name>", "chunk_flagged:<flag_name>", "area_flagged:<flag_name>" (all work similar to 'item_flagged'),
    // "biome:<name>" to match based on the location's biome,
    // "cuboid" for any noted cuboid, "ellipsoid" for any noted ellipsoid, or "polygon" for any noted polygon.
    //
    // You will also often see match inputs like "<cause>" or "<reason>" or similar,
    // which will expect the name from an enumeration documented elsewhere in the event meta (usually alongside a "<context.cause>" or similar).
    //
    // You can also do more advanced multi-matchers in any of these inputs.
    // For details on that, see <@link language Advanced Script Event Matching>.
    // -->


    public boolean couldMatchLegacyInArea(String lower) {
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

    public boolean couldMatchArea(String text) {
        if (text.equals("area") || text.equals("cuboid") || text.equals("polygon") || text.equals("ellipsoid")) {
            return true;
        }
        if (text.startsWith("area_flagged:") || text.startsWith("biome:")) {
            return true;
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
        }
        addPossibleCouldMatchFailReason("Not a valid area label", text);
        return false;
    }

    public boolean exactMatchesEnum(String text, final Enum<?>[] enumVals) {
        for (Enum<?> val : enumVals) {
            if (CoreUtilities.equalsIgnoreCase(val.name(), text)) {
                return true;
            }
        }
        addPossibleCouldMatchFailReason("Does not match required enumeration", text);
        return false;
    }

    public boolean couldMatchEnum(String text, final Enum<?>[] enumVals) {
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

    public boolean couldMatchInventory(String text) {
        if (text.equals("inventory") || text.equals("notable") || text.equals("note")) {
            return true;
        }
        if (text.startsWith("inventory_flagged:")) {
            return true;
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

    public boolean couldMatchEntity(String text) {
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

    public boolean exactMatchEntity(String text) {
        if (specialEntityMatchables.contains(text)) {
            return true;
        }
        if (text.startsWith("entity_flagged:") || text.startsWith("player_flagged:") || text.startsWith("npc_flagged:")) {
            return true;
        }
        if (EntityTag.matches(text)) {
            return true;
        }
        addPossibleCouldMatchFailReason("Not a valid entity label", text);
        return false;
    }

    public boolean exactMatchesVehicle(String text) {
        if (text.equals("vehicle")) {
            return true;
        }
        if (specialEntityMatchables.contains(text)) {
            return false;
        }
        if (text.startsWith("entity_flagged:")) {
            return true;
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

    public boolean couldMatchVehicle(String text) {
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

    public boolean couldMatchBlockOrItem(String text) {
        if (text.equals("block") || text.equals("material") || text.equals("item") || text.equals("potion") || text.startsWith("item_flagged:") || text.startsWith("vanilla_tagged:")) {
            return true;
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

    public boolean couldMatchBlock(String text) {
        return couldMatchBlock(text, null);
    }

    public boolean couldMatchBlock(String text, Function<Material, Boolean> requirement) {
        if (text.equals("block") || text.equals("material") || text.startsWith("vanilla_tagged:")) {
            return true;
        }
        if (text.equals("item")) {
            return false;
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

    public boolean couldMatchItem(String text) {
        if (text.equals("item") || text.equals("potion")) {
            return true;
        }
        if (text.startsWith("item_flagged:") || text.startsWith("vanilla_tagged:")) {
            return true;
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

    public boolean nonSwitchWithCheck(ScriptPath path, ItemTag held) {
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
        if (with != null && (held == null || !tryItem(held, with))) {
            return false;
        }
        return true;
    }

    public BukkitTagContext getTagContext(ScriptPath path) {
        BukkitTagContext context = (BukkitTagContext) getScriptEntryData().getTagContext().clone();
        context.script = new ScriptTag(path.container);
        context.debug = path.container.shouldDebug();
        return context;
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
            if (Debug.verbose) {
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

    public boolean runFlaggedCheck(ScriptPath path, String switchName, AbstractFlagTracker tracker) {
        String flagged = path.switches.get(switchName);
        if (flagged == null) {
            return true;
        }
        if (tracker == null) {
            return false;
        }
        for (String flag : CoreUtilities.split(flagged, '|')) {
            if (!tracker.hasFlag(flag)) {
                return false;
            }
        }
        return true;
    }

    public boolean runLocationFlaggedCheck(ScriptPath path, String switchName, Location location) {
        return runFlaggedCheck(path, switchName, location == null ? null : new LocationTag(location).getFlagTracker());
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
        if (inputText.startsWith("!")) {
            return !inCheckInternal(getTagContext(path), getName(), location, inputText.substring(1), path.event, path.container.getName());
        }
        return inCheckInternal(getTagContext(path), getName(), location, inputText, path.event, path.container.getName());
    }

    public static boolean inCheckInternal(TagContext context, String name, Location location, String inputText, String evtLine, String containerName) {
        String lower = CoreUtilities.toLowerCase(inputText);
        if (lower.contains(":")) {
            if (lower.startsWith("world_flagged:")) {
                return new WorldTag(location.getWorld()).getFlagTracker().hasFlag(inputText.substring("world_flagged:".length()));
            }
            else if (lower.startsWith("chunk_flagged:")) {
                return new ChunkTag(location).getFlagTracker().hasFlag(inputText.substring("chunk_flagged:".length()));
            }
            else if (lower.startsWith("area_flagged:")) {
                String flagName = inputText.substring("area_flagged:".length());
                for (CuboidTag cuboid : NoteManager.getAllType(CuboidTag.class)) {
                    if (cuboid.isInsideCuboid(location) && cuboid.flagTracker.hasFlag(flagName)) {
                        return true;
                    }
                }
                for (EllipsoidTag ellipsoid : NoteManager.getAllType(EllipsoidTag.class)) {
                    if (ellipsoid.contains(location) && ellipsoid.flagTracker.hasFlag(flagName)) {
                        return true;
                    }
                }
                for (PolygonTag polygon : NoteManager.getAllType(PolygonTag.class)) {
                    if (polygon.doesContainLocation(location) && polygon.flagTracker.hasFlag(flagName)) {
                        return true;
                    }
                }
                return false;
            }
            else if (lower.startsWith("biome:")) {
                String biome = inputText.substring("biome:".length());
                return runGenericCheck(biome, new LocationTag(location).getBiome().name);
            }
        }
        if (lower.equals("cuboid")) {
            for (CuboidTag cuboid : NoteManager.getAllType(CuboidTag.class)) {
                if (cuboid.isInsideCuboid(location)) {
                    return true;
                }
            }
            return false;
        }
        else if (lower.equals("ellipsoid")) {
            for (EllipsoidTag ellipsoid : NoteManager.getAllType(EllipsoidTag.class)) {
                if (ellipsoid.contains(location)) {
                    return true;
                }
            }
            return false;
        }
        else if (lower.equals("polygon")) {
            for (PolygonTag polygon : NoteManager.getAllType(PolygonTag.class)) {
                if (polygon.doesContainLocation(location)) {
                    return true;
                }
            }
            return false;
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
            for (CuboidTag cuboid : NoteManager.getAllType(CuboidTag.class)) {
                if (cuboid.isInsideCuboid(location) && matcher.doesMatch(cuboid.noteName)) {
                    return true;
                }
            }
            for (EllipsoidTag ellipsoid : NoteManager.getAllType(EllipsoidTag.class)) {
                if (ellipsoid.contains(location) && matcher.doesMatch(ellipsoid.noteName)) {
                    return true;
                }
            }
            for (PolygonTag polygon : NoteManager.getAllType(PolygonTag.class)) {
                if (polygon.doesContainLocation(location) && matcher.doesMatch(polygon.noteName)) {
                    return true;
                }
            }
            if (matcher.doesMatch(CoreUtilities.toLowerCase(location.getWorld().getName()))) {
                return true;
            }
            return false;
        }
        else {
            Debug.echoError("Invalid event 'in:<area>' switch [" + name + "] ('in:???') (did you make a typo, or forget to make a notable by that name?): '" + evtLine + "' for " + containerName);
            return false;
        }
    }

    public boolean runWithCheck(ScriptPath path, ItemTag held) {
        String with = path.switches.get("with");
        if (with != null) {
            if (CoreUtilities.equalsIgnoreCase(with, "item")) {
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
        for (String flag : CoreUtilities.split(flagged, '|')) {
            if (!player.getFlagTracker().hasFlag(flag)) {
                return false;
            }
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
        for (String permName : CoreUtilities.split(perm, '|')) {
            if (!player.getPlayerEntity().hasPermission(permName)) {
                return false;
            }
        }
        return true;
    }

    public boolean runAutomaticPlayerSwitches(ScriptPath path) {
        if (!path.switches.containsKey("flagged") && !path.switches.containsKey("permission")) {
            return true;
        }
        BukkitScriptEntryData data = (BukkitScriptEntryData) getScriptEntryData();
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

    @Override
    public boolean matches(ScriptPath path) {
        if (!runAutomaticPlayerSwitches(path)) {
            return false;
        }
        return super.matches(path);
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

    public static boolean tryWorld(WorldTag world, String comparedto) {
        if (comparedto.equals("world")) {
            return true;
        }
        if (comparedto.startsWith("world_flagged:")) {
            return world.getFlagTracker().hasFlag(comparedto.substring("world_flagged:".length()));
        }
        return runGenericCheck(comparedto, world.getName());
    }

    public static boolean compareInventoryToMatch(InventoryTag inv, MatchHelper matcher) {
        if (matcher instanceof InverseMatchHelper) {
            return !compareInventoryToMatch(inv, ((InverseMatchHelper) matcher).matcher);
        }
        if (matcher.doesMatch(inv.getInventoryType().name())) {
            return true;
        }
        if (matcher.doesMatch(inv.getIdType())) {
            return true;
        }
        if (matcher.doesMatch(inv.getIdHolder().toString())) {
            return true;
        }
        if (inv.getIdHolder() instanceof ScriptTag && matcher.doesMatch(((ScriptTag) inv.getIdHolder()).getName())) {
            return true;
        }
        String notedId = NoteManager.getSavedId(inv);
        if (notedId != null && matcher.doesMatch(notedId)) {
            return true;
        }
        return false;
    }

    public static boolean tryInventory(InventoryTag inv, String comparedto) {
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("inventory")) {
            return true;
        }
        if (comparedto.equals("notable") || comparedto.equals("note")) {
            return NoteManager.isSaved(inv);
        }
        if (comparedto.startsWith("inventory_flagged:")) {
            return inv.flagTracker != null && inv.flagTracker.hasFlag(comparedto.substring("inventory_flagged:".length()));
        }
        MatchHelper matcher = createMatcher(comparedto);
        return compareInventoryToMatch(inv, matcher);
    }

    public static boolean tryItem(ItemTag item, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || item == null) {
            return false;
        }
        String rawComparedTo = comparedto;
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.contains(":")) {
            if (comparedto.startsWith("item_flagged:")) {
                for (String flag : CoreUtilities.split(rawComparedTo.substring("item_flagged:".length()), '|')) {
                    if (!item.getFlagTracker().hasFlag(flag)) {
                        return false;
                    }
                }
                return true;
            }
            else if (comparedto.startsWith("item_enchanted:")) {
                String enchMatcher = comparedto.substring("item_enchanted:".length());
                if (!item.getItemMeta().hasEnchants()) {
                    return false;
                }
                for (Enchantment enchant : item.getItemMeta().getEnchants().keySet()) {
                    if (runGenericCheck(enchMatcher, enchant.getKey().getKey())) {
                        return true;
                    }
                }
                return false;
            }
            else if (comparedto.startsWith("raw_exact:")) {
                ItemTag compareItem = ItemTag.valueOf(rawComparedTo.substring("raw_exact:".length()), CoreUtilities.errorButNoDebugContext);
                return compareItem != null && compareItem.matchesRawExact(item);
            }
            else if (comparedto.startsWith("vanilla_tagged:")) {
                String tagCheck = comparedto.substring("vanilla_tagged:".length());
                HashSet<String> tags = VanillaTagHelper.tagsByMaterial.get(item.getItemStack().getType());
                if (tags == null) {
                    return false;
                }
                MatchHelper matcher = createMatcher(tagCheck);
                for (String tag : tags) {
                    if (matcher.doesMatch(tag)) {
                        return true;
                    }
                }
                return false;
            }
            else if (comparedto.startsWith("material_flagged:")) {
                return item.getMaterial().getFlagTracker().hasFlag(comparedto.substring("material_flagged:".length()));
            }
        }
        if (comparedto.equals("item")) {
            return true;
        }
        if (comparedto.equals("potion") && CoreUtilities.toLowerCase(item.getBukkitMaterial().name()).contains("potion")) {
            return true;
        }
        MatchHelper matcher = createMatcher(comparedto);
        if (item.isItemscript()) {
            if (matcher.doesMatch(item.getScriptName())) {
                return true;
            }
        }
        else {
            if (matcher.doesMatch(item.getMaterialName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean tryMaterial(MaterialTag mat, String comparedto) {
        return tryMaterial(mat.getMaterial(), comparedto);
    }

    public static boolean tryMaterial(Material mat, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || mat == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("block") || comparedto.equals("material")) {
            return true;
        }
        if (comparedto.contains(":")) {
            if (comparedto.startsWith("vanilla_tagged:")) {
                String tagCheck = comparedto.substring("vanilla_tagged:".length());
                HashSet<String> tags = VanillaTagHelper.tagsByMaterial.get(mat);
                if (tags == null) {
                    return false;
                }
                MatchHelper matcher = createMatcher(tagCheck);
                for (String tag : tags) {
                    if (matcher.doesMatch(tag)) {
                        return true;
                    }
                }
                return false;
            }
            else if (comparedto.startsWith("material_flagged:")) {
                return new MaterialTag(mat).getFlagTracker().hasFlag(comparedto.substring("material_flagged:".length()));
            }
        }
        MaterialTag quickOf = MaterialTag.quickOfNamed(comparedto);
        if (quickOf != null) {
            if (quickOf.getMaterial() != mat) {
                return false;
            }
            if (quickOf.getMaterial().equals(mat)) {
                return true;
            }
        }
        MatchHelper matcher = createMatcher(comparedto);
        if (matcher.doesMatch(mat.name())) {
            return true;
        }
        return false;
    }

    public static HashSet<String> specialEntityMatchables = new HashSet<>(Arrays.asList("entity", "npc", "player", "living", "vehicle", "fish", "projectile", "hanging", "monster", "mob", "animal"));

    public static boolean tryEntity(EntityTag entity, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || entity == null) {
            return false;
        }
        Entity bEntity = entity.getBukkitEntity();
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (specialEntityMatchables.contains(comparedto)) {
            switch (comparedto) {
                case "entity":
                    return true;
                case "npc":
                    return entity.isCitizensNPC();
                case "player":
                    return entity.isPlayer();
                case "living":
                    return bEntity instanceof LivingEntity;
                case "vehicle":
                    return bEntity instanceof Vehicle;
                case "fish":
                    return bEntity instanceof Fish;
                case "projectile":
                    return bEntity instanceof Projectile;
                case "hanging":
                    return bEntity instanceof Hanging;
                case "monster":
                    return bEntity instanceof Monster;
                case "mob":
                    return bEntity instanceof Mob;
                case "animal":
                    return bEntity instanceof Animals;
            }
        }
        if (comparedto.contains(":")) {
            if (comparedto.startsWith("entity_flagged:")) {
                return entity.getFlagTracker().hasFlag(comparedto.substring("entity_flagged:".length()));
            }
            else if (comparedto.startsWith("player_flagged:")) {
                return entity.isPlayer() && entity.getFlagTracker().hasFlag(comparedto.substring("player_flagged:".length()));
            }
            else if (comparedto.startsWith("npc_flagged:")) {
                return entity.isCitizensNPC() && entity.getFlagTracker().hasFlag(comparedto.substring("npc_flagged:".length()));
            }
        }
        MatchHelper matcher = createMatcher(comparedto);
        if (matcher instanceof InverseMatchHelper) {
            matcher = ((InverseMatchHelper) matcher).matcher;
            if (entity.getEntityScript() != null && matcher.doesMatch(entity.getEntityScript())) {
                return false;
            }
            else if (matcher.doesMatch(entity.getEntityType().getLowercaseName())) {
                return false;
            }
            return true;
        }
        else {
            if (entity.getEntityScript() != null && matcher.doesMatch(entity.getEntityScript())) {
                return true;
            }
            else if (matcher.doesMatch(entity.getEntityType().getLowercaseName())) {
                return true;
            }
            return false;
        }
    }

}
