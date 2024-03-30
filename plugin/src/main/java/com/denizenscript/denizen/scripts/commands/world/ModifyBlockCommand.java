package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ScriptUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ModifyBlockCommand extends AbstractCommand implements Listener, Holdable {

    public ModifyBlockCommand() {
        setName("modifyblock");
        setSyntax("modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (no_physics/naturally:<tool>) (delayed) (<script>) (<percent chance>|...) (source:<player>) (max_delay_ms:<#>)");
        setRequiredArguments(2, 8);
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
        // Keep the list empty automatically - we don't want to still block physics so much later that something else edited the block!
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Denizen.getInstance(), () -> {
            tick++;
            if (physitick < tick - 1) {
                block_physics.clear();
            }
        }, 2, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name ModifyBlock
    // @Syntax modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (no_physics/naturally:<tool>) (delayed) (<script>) (<percent chance>|...) (source:<player>) (max_delay_ms:<#>)
    // @Required 2
    // @Maximum 8
    // @Short Modifies blocks.
    // @Synonyms SetBlock,ChangeBlock,PlaceBlock,BreakBlock
    // @Group world
    //
    // @Description
    // Changes blocks in the world based on the criteria given.
    //
    // Use 'no_physics' to place the blocks without physics taking over the modified blocks.
    // This is useful for block types such as portals or water. This does NOT control physics for an extended period of time.
    //
    // Specify (<percent chance>|...) to give a chance of each material being placed (in any material at all).
    //
    // Use 'naturally:' when setting a block to air to break it naturally, meaning that it will drop items. Specify the tool item that should be used for calculating drops.
    //
    // Use 'delayed' to make the modifyblock slowly edit blocks at a time pace roughly equivalent to the server's limits.
    // Optionally, specify 'max_delay_ms' to control how many milliseconds the 'delayed' set can run for in any given tick (defaults to 50).
    //
    // Note that specifying a list of locations will take more time in parsing than in the actual block modification.
    //
    // Optionally, specify a script to be ran after the delayed edits finish. (Doesn't fire if delayed is not set.)
    //
    // Optionally, specify a source player. When set, Bukkit events will fire that identify that player as the source of a change, and potentially cancel the change.
    // The source argument might cause weird interoperation with other plugins, use with caution.
    //
    // The modifyblock command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <LocationTag.material>
    //
    // @Usage
    // Use to change the block a player is looking at to stone.
    // - modifyblock <player.cursor_on> stone
    //
    // @Usage
    // Use to modify an entire cuboid to half stone, half dirt.
    // - modifyblock <player.location.to_cuboid[<player.cursor_on>]> stone|dirt
    //
    // @Usage
    // Use to modify an entire cuboid to some stone, some dirt, and some left as it is.
    // - modifyblock <player.location.to_cuboid[<player.cursor_on>]> stone|dirt 25|25
    //
    // @Usage
    // Use to modify the ground beneath the player's feet.
    // - modifyblock <player.location.add[2,-1,2].to_cuboid[<player.location.add[-2,-1,-2]>]> RED_WOOL
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteBlockMaterials(tab);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(CuboidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && (arg.startsWith("cu@") || !arg.getRawValue().contains("|"))) {
                scriptEntry.addObject("locations", arg.asType(CuboidTag.class).getBlockLocationsUnfiltered(false));
            }
            else if (arg.matchesArgumentType(EllipsoidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && (arg.startsWith("ellipsoid@") || !arg.getRawValue().contains("|"))) {
                scriptEntry.addObject("locations", arg.asType(EllipsoidTag.class).getBlockLocationsUnfiltered(false));
            }
            else if (arg.matchesArgumentList(LocationTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")) {
                scriptEntry.addObject("location_list", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("materials")
                    && arg.matchesArgumentList(MaterialTag.class)) {
                scriptEntry.addObject("materials", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius", "r")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("radius", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height", "h")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("height", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth", "d")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("depth", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("source")
                    && arg.matchesPrefix("source")
                    && arg.matchesArgumentType(PlayerTag.class)) {
                scriptEntry.addObject("source", arg.asType(PlayerTag.class));
            }
            else if (!scriptEntry.hasObject("physics")
                    && arg.matches("no_physics")) {
                scriptEntry.addObject("physics", new ElementTag(false));
            }
            else if (!scriptEntry.hasObject("natural")
                    && arg.matches("naturally")) {
                scriptEntry.addObject("natural", new ItemTag(new ItemStack(Material.AIR)));
            }
            else if (!scriptEntry.hasObject("natural")
                    && arg.matchesPrefix("naturally")
                    && arg.matchesArgumentType(ItemTag.class)) {
                scriptEntry.addObject("natural", arg.asType(ItemTag.class));
            }
            else if (!scriptEntry.hasObject("max_delay_ms")
                    && arg.matchesPrefix("max_delay_ms")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("max_delay_ms", arg.asElement());
            }
            else if (!scriptEntry.hasObject("delayed")
                    && arg.matches("delayed")) {
                scriptEntry.addObject("delayed", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.limitToOnlyPrefix("script")
                    && arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("percents")
                    && arg.limitToOnlyPrefix("percents")) {
                scriptEntry.addObject("percents", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("materials")) {
            throw new InvalidArgumentsException("Missing material argument!");
        }
        if (!scriptEntry.hasObject("locations") && !scriptEntry.hasObject("location_list")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
        scriptEntry.defaultObject("radius", new ElementTag(0))
                .defaultObject("max_delay_ms", new ElementTag(50))
                .defaultObject("height", new ElementTag(0))
                .defaultObject("depth", new ElementTag(0))
                .defaultObject("physics", new ElementTag(true))
                .defaultObject("delayed", new ElementTag(false));
    }

    public static LocationTag getLocAt(ListTag list, int index, ScriptEntry entry) {
        ObjectTag obj = list.getObject(index);
        if (obj instanceof LocationTag) {
            return (LocationTag) obj;
        }
        else {
            return LocationTag.valueOf(obj.toString(), entry.context);
        }
    }

    public static boolean isLocationBad(ScriptEntry entry, LocationTag loc) {
        if (loc == null) {
            Debug.echoError(entry, "Input is not a valid LocationTag");
            return true;
        }
        if (loc.getWorld() == null) {
            Debug.echoError(entry, "Input '" + loc + "' is missing a world value");
            return true;
        }
        return false;
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final ListTag materials = scriptEntry.getObjectTag("materials");
        final List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        final ListTag location_list = scriptEntry.getObjectTag("location_list");
        final ElementTag physics = scriptEntry.getElement("physics");
        final ItemTag natural = scriptEntry.getObjectTag("natural");
        final ElementTag delayed = scriptEntry.getElement("delayed");
        final ElementTag maxDelayMs = scriptEntry.getElement("max_delay_ms");
        final ElementTag radiusElement = scriptEntry.getElement("radius");
        final ElementTag heightElement = scriptEntry.getElement("height");
        final ElementTag depthElement = scriptEntry.getElement("depth");
        final ScriptTag script = scriptEntry.getObjectTag("script");
        final PlayerTag source = scriptEntry.getObjectTag("source");
        ListTag percents = scriptEntry.getObjectTag("percents");
        if (percents != null && percents.size() != materials.size()) {
            Debug.echoError(scriptEntry, "Percents length != materials length");
            percents = null;
        }
        final List<MaterialTag> materialList = materials.filter(MaterialTag.class, scriptEntry);
        for (MaterialTag mat : materialList) {
            if (!mat.getMaterial().isBlock()) {
                Debug.echoError("Material '" + mat.getMaterial().name() + "' is not a block material");
                scriptEntry.setFinished(true);
                return;
            }
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), materials, physics, radiusElement, heightElement, depthElement, natural,
                    delayed, maxDelayMs, script, percents, source, (locations == null ? location_list : db("locations", locations)));
        }
        Player sourcePlayer = source == null ? null : source.getPlayerEntity();
        final boolean doPhysics = physics.asBoolean();
        final int radius = radiusElement.asInt();
        final int height = heightElement.asInt();
        final int depth = depthElement.asInt();
        List<Float> percentages = null;
        if (percents != null) {
            percentages = new ArrayList<>();
            for (String str : percents) {
                percentages.add(new ElementTag(str).asFloat());
            }
        }
        final List<Float> percs = percentages;
        if (locations == null && location_list == null) {
            Debug.echoError("Must specify a valid location!");
            scriptEntry.setFinished(true);
            return;
        }
        if ((location_list != null && location_list.isEmpty()) || (locations != null && locations.isEmpty())) {
            scriptEntry.setFinished(true);
            return;
        }
        if (materialList.isEmpty()) {
            Debug.echoError("Must specify a valid material!");
            scriptEntry.setFinished(true);
            return;
        }
        no_physics = !doPhysics;
        if (delayed.asBoolean()) {
            final long maxDelay = maxDelayMs.asLong();
            new BukkitRunnable() {
                int index = 0;
                @Override
                public void run() {
                    try {
                        long start = CoreUtilities.monotonicMillis();
                        LocationTag loc;
                        if (locations != null) {
                            loc = locations.get(0);
                        }
                        else {
                            loc = getLocAt(location_list, 0, scriptEntry);
                        }
                        if (isLocationBad(scriptEntry, loc)) {
                            scriptEntry.setFinished(true);
                            cancel();
                            return;
                        }
                        boolean was_static = preSetup(loc);
                        while ((locations != null && locations.size() > index) || (location_list != null && location_list.size() > index)) {
                            LocationTag nLoc;
                            if (locations != null) {
                                nLoc = locations.get(index);
                            }
                            else {
                                nLoc = getLocAt(location_list, index, scriptEntry);
                            }
                            if (isLocationBad(scriptEntry, nLoc)) {
                                scriptEntry.setFinished(true);
                                cancel();
                                return;
                            }
                            handleLocation(nLoc, index, materialList, doPhysics, natural, radius, height, depth, percs, sourcePlayer, scriptEntry);
                            index++;
                            if (CoreUtilities.monotonicMillis() - start > maxDelay) {
                                break;
                            }
                        }
                        postComplete(loc, was_static);
                        if ((locations != null && locations.size() == index) || (location_list != null && location_list.size() == index)) {
                            if (script != null) {
                                ScriptUtilities.createAndStartQueue(script.getContainer(), null, scriptEntry.entryData, null, null, null, null, null, scriptEntry);
                            }
                            scriptEntry.setFinished(true);
                            cancel();
                        }
                    }
                    catch (Throwable ex) {
                        Debug.echoError(ex);
                    }
                }
            }.runTaskTimer(Denizen.getInstance(), 1, 1);
        }
        else {
            LocationTag loc;
            if (locations != null) {
                loc = locations.get(0);
            }
            else {
                loc = getLocAt(location_list, 0, scriptEntry);
            }
            if (isLocationBad(scriptEntry, loc)) {
                scriptEntry.setFinished(true);
                return;
            }
            boolean was_static = preSetup(loc);
            int index = 0;
            if (locations != null) {
                for (LocationTag obj : locations) {
                    if (isLocationBad(scriptEntry, obj)) {
                        scriptEntry.setFinished(true);
                        return;
                    }
                    handleLocation(obj, index, materialList, doPhysics, natural, radius, height, depth, percentages, sourcePlayer, scriptEntry);
                    index++;
                }
            }
            else {
                for (int i = 0; i < location_list.size(); i++) {
                    LocationTag obj = getLocAt(location_list, i, scriptEntry);
                    if (isLocationBad(scriptEntry, obj)) {
                        scriptEntry.setFinished(true);
                        return;
                    }
                    handleLocation(obj, index, materialList, doPhysics, natural, radius, height, depth, percentages, sourcePlayer, scriptEntry);
                    index++;
                }
            }
            postComplete(loc, was_static);
            scriptEntry.setFinished(true);
        }
    }

    boolean preSetup(LocationTag loc0) {
        // Freeze the first world in the list.
        World world = loc0.getWorld();
        boolean was_static = NMSHandler.worldHelper.isStatic(world);
        if (no_physics) {
            NMSHandler.worldHelper.setStatic(world, true);
        }
        return was_static;
    }

    void postComplete(Location loc, boolean was_static) {
        // Unfreeze the first world in the list.
        if (no_physics) {
            NMSHandler.worldHelper.setStatic(loc.getWorld(), was_static);
        }
        no_physics = false;
    }

    <T extends Event & Cancellable> boolean callEvent(T event, ScriptEntry scriptEntry) {
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Debug.echoDebug(scriptEntry, "Source event cancelled, not changing block.");
            return true;
        }
        return false;
    }

    void handleLocation(LocationTag location, int index, List<MaterialTag> materialList, boolean doPhysics,
                        ItemTag natural, int radius, int height, int depth, List<Float> percents, Player source, ScriptEntry entry) {
        MaterialTag material;
        if (percents == null) {
            material = materialList.get(index % materialList.size());
        }
        else {
            material = null;
            for (int i = 0; i < materialList.size(); i++) {
                float perc = percents.get(i) / 100f;
                if (CoreUtilities.getRandom().nextDouble() <= perc) {
                    material = materialList.get(i);
                    break;
                }
            }
            if (material == null) {
                return;
            }
        }
        location = location.getBlockLocation();
        World world = location.getWorld();
        if (source != null) {
            if (material.getMaterial() == Material.AIR) {
                if (callEvent(new BlockBreakEvent(location.getBlock(), source), entry)) {
                    return;
                }
                setBlock(location, material, doPhysics, natural);
            }
            else {
                Block block = location.getBlock();
                BlockState originalState = block.getState();
                setBlock(location, material, doPhysics, natural);
                if (callEvent(new BlockPlaceEvent(block, originalState, block, new ItemStack(material.getMaterial()), source, true, EquipmentSlot.HAND), entry)) {
                    originalState.update(true, doPhysics);
                    return;
                }
            }
        }
        else {
            setBlock(location, material, doPhysics, natural);
        }
        if (radius != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    setBlock(new Location(world, location.getX() + x - radius, location.getY(), location.getZ() + z - radius), material, doPhysics, natural);
                }
            }
        }
        if (height != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    for (int y = 1; y < height + 1; y++) {
                        setBlock(new Location(world, location.getX() + x - radius, location.getY() + y, location.getZ() + z - radius), material, doPhysics, natural);
                    }
                }
            }
        }
        if (depth != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    for (int y = 1; y < depth + 1; y++) {
                        setBlock(new Location(world, location.getX() + x - radius, location.getY() - y, location.getZ() + z - radius), material, doPhysics, natural);
                    }
                }
            }
        }
    }

    public static void setBlock(Location location, MaterialTag material, boolean physics, ItemTag natural) {
        if (physics) {
            block_physics.remove(location);
        }
        else {
            block_physics.add(location);
            physitick = tick;
        }
        if (!Utilities.isLocationYSafe(location)) {
            Debug.echoError("Invalid modifyblock location: " + new LocationTag(location));
            return;
        }
        if (natural != null && material.getMaterial() == Material.AIR) {
            int xp = NMSHandler.blockHelper.getExpDrop(location.getBlock(), natural.getItemStack());
            if (xp > 0) {
                ExperienceOrb orb = (ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
                orb.setExperience(xp);
            }
            location.getBlock().breakNaturally(natural.getItemStack());
        }
        else {
            location.getBlock().setBlockData(material.getModernData(), physics);
        }
    }

    public static boolean no_physics = false;

    public static final HashSet<Location> block_physics = new HashSet<>();

    public static long tick = 0;

    public static long physitick = 0;

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (no_physics) {
            event.setCancelled(true);
        }
        if (block_physics.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockChanges(EntityChangeBlockEvent event) {
        if (event.getEntity().getType() != EntityType.FALLING_BLOCK) {
            return;
        }
        if (no_physics) {
            event.setCancelled(true);
        }
        if (block_physics.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
