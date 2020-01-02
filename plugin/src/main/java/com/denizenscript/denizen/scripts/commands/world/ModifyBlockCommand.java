package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.EllipsoidTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ModifyBlockCommand extends AbstractCommand implements Listener, Holdable {

    // <--[command]
    // @Name ModifyBlock
    // @Syntax modifyblock [<location>|.../<ellipsoid>/<cuboid>] [<material>|...] (no_physics/naturally) (delayed) (<script>) (<percent chance>|...)
    // @Required 2
    // @Short Modifies blocks.
    // @Group world
    //
    // @Description
    // Changes blocks in the world based on the criteria given.
    // Use 'no_physics' to place the blocks without
    // physics taking over the modified blocks. This is useful for block types such as portals. This does NOT
    // control physics for an extended period of time.
    // Specify (<percent chance>|...) to give a chance of each material being placed (in any material at all).
    // Use 'naturally' when setting a block to air to break it naturally, meaning that it will drop items.
    // Use 'delayed' to make the modifyblock slowly edit blocks at a time pace roughly equivalent to the server's limits.
    // Note that specify a list of locations will take more time in parsing than in the actual block modification.
    // Optionally, specify a script to be ran after the delayed edits finish. (Doesn't fire if delayed is not set.)
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
    // - modifyblock <cuboid[<player.location>|<player.cursor_on>]> stone|dirt
    //
    // @Usage
    // Use to modify an entire cuboid to some stone, some dirt, and some left as it is.
    // - modifyblock <cuboid[<player.location>|<player.cursor_on>]> stone|dirt 25|25
    //
    // @Usage
    // Use to modify the ground beneath the player's feet.
    // - modifyblock <cuboid[<player.location.add[2,-1,2]>|<player.location.add[-2,-1,-2]>]> RED_WOOL
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (Argument arg : scriptEntry.getProcessedArgs()) {


            if (arg.matchesArgumentType(CuboidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && arg.startsWith("cu@")) {
                scriptEntry.addObject("locations", arg.asType(CuboidTag.class).getBlockLocationsUnfiltered());
            }
            else if (arg.matchesArgumentType(EllipsoidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && arg.startsWith("ellipsoid@")) {
                scriptEntry.addObject("locations", arg.asType(EllipsoidTag.class).getBlockLocationsUnfiltered());
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
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("radius", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height", "h")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth", "d")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("depth", new ElementTag(arg.getValue()));
            }
            else if (arg.matches("no_physics")) {
                scriptEntry.addObject("physics", new ElementTag(false));
            }
            else if (arg.matches("naturally")) {
                scriptEntry.addObject("natural", new ElementTag(true));
            }
            else if (arg.matches("delayed")) {
                scriptEntry.addObject("delayed", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("percents")) {
                scriptEntry.addObject("percents", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Must have material
        if (!scriptEntry.hasObject("materials")) {
            throw new InvalidArgumentsException("Missing material argument!");
        }

        // ..and at least one location.
        if (!scriptEntry.hasObject("locations") && !scriptEntry.hasObject("location_list")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }

        // Set some defaults
        scriptEntry.defaultObject("radius", new ElementTag(0))
                .defaultObject("height", new ElementTag(0))
                .defaultObject("depth", new ElementTag(0))
                .defaultObject("physics", new ElementTag(true))
                .defaultObject("natural", new ElementTag(false))
                .defaultObject("delayed", new ElementTag(false));

    }


    @Override
    public void execute(final ScriptEntry scriptEntry) {

        final ListTag materials = scriptEntry.getObjectTag("materials");
        final List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("locations");
        final ListTag location_list = scriptEntry.getObjectTag("location_list");
        final ElementTag physics = scriptEntry.getElement("physics");
        final ElementTag natural = scriptEntry.getElement("natural");
        final ElementTag delayed = scriptEntry.getElement("delayed");
        final ElementTag radiusElement = scriptEntry.getElement("radius");
        final ElementTag heightElement = scriptEntry.getElement("height");
        final ElementTag depthElement = scriptEntry.getElement("depth");
        final ScriptTag script = scriptEntry.getObjectTag("script");
        ListTag percents = scriptEntry.getObjectTag("percents");

        if (percents != null && percents.size() != materials.size()) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Percents length != materials length");
            percents = null;
        }

        final List<MaterialTag> materialList = materials.filter(MaterialTag.class, scriptEntry);

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), (locations == null ? location_list.debug() : ArgumentHelper.debugList("locations", locations))
                    + materials.debug()
                    + physics.debug()
                    + radiusElement.debug()
                    + heightElement.debug()
                    + depthElement.debug()
                    + natural.debug()
                    + delayed.debug()
                    + (script != null ? script.debug() : "")
                    + (percents != null ? percents.debug() : ""));

        }

        final boolean doPhysics = physics.asBoolean();
        final boolean isNatural = natural.asBoolean();
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
            return;
        }
        if ((location_list != null && location_list.size() == 0) || (locations != null && locations.size() == 0)) {
            return;
        }
        if (materialList.size() == 0) {
            Debug.echoError("Must specify a valid material!");
            return;
        }

        no_physics = !doPhysics;
        if (delayed.asBoolean()) {
            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    LocationTag loc;
                    if (locations != null) {
                        loc = locations.get(0);
                    }
                    else {
                        loc = LocationTag.valueOf(location_list.get(0));
                    }
                    boolean was_static = preSetup(loc);
                    while ((locations != null && locations.size() > index) || (location_list != null && location_list.size() > index)) {
                        LocationTag nLoc;
                        if (locations != null) {
                            nLoc = locations.get(index);
                        }
                        else {
                            nLoc = LocationTag.valueOf(location_list.get(index));
                        }
                        handleLocation(nLoc, index, materialList, doPhysics, isNatural, radius, height, depth, percs);
                        index++;
                        if (System.currentTimeMillis() - start > 50) {
                            break;
                        }
                    }
                    postComplete(loc, was_static);
                    if ((locations != null && locations.size() == index) || (location_list != null && location_list.size() == index)) {
                        if (script != null) {
                            List<ScriptEntry> entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
                            ScriptQueue queue = new InstantQueue(script.getContainer().getName())
                                    .addEntries(entries);
                            queue.start();
                        }
                        scriptEntry.setFinished(true);
                        cancel();
                    }
                }
            }.runTaskTimer(DenizenAPI.getCurrentInstance(), 1, 1);
        }
        else {
            LocationTag loc;
            if (locations != null) {
                loc = locations.get(0);
            }
            else {
                loc = LocationTag.valueOf(location_list.get(0));
            }
            boolean was_static = preSetup(loc);
            int index = 0;
            if (locations != null) {
                for (ObjectTag obj : locations) {
                    handleLocation((LocationTag) obj, index, materialList, doPhysics, isNatural, radius, height, depth, percentages);
                    index++;
                }
            }
            else {
                for (String str : location_list) {
                    handleLocation(LocationTag.valueOf(str), index, materialList, doPhysics, isNatural, radius, height, depth, percentages);
                    index++;
                }
            }
            postComplete(loc, was_static);
            scriptEntry.setFinished(true);
        }
    }

    boolean preSetup(LocationTag loc0) {
        // Freeze the first world in the list.
        WorldHelper worldHelper = NMSHandler.getWorldHelper();
        World world = loc0.getWorld();
        boolean was_static = worldHelper.isStatic(world);
        if (no_physics) {
            worldHelper.setStatic(world, true);
        }
        return was_static;
    }

    void postComplete(Location loc, boolean was_static) {
        // Unfreeze the first world in the list.
        if (no_physics) {
            NMSHandler.getWorldHelper().setStatic(loc.getWorld(), was_static);
        }
        no_physics = false;
    }

    void handleLocation(LocationTag location, int index, List<MaterialTag> materialList, boolean doPhysics,
                        boolean isNatural, int radius, int height, int depth, List<Float> percents) {

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

        World world = location.getWorld();

        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        setBlock(location, material, doPhysics, isNatural);

        if (radius != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    setBlock(new Location(world, location.getX() + x - radius, location.getY(), location.getZ() + z - radius), material, doPhysics, isNatural);
                }
            }
        }

        if (height != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    for (int y = 1; y < height + 1; y++) {
                        setBlock(new Location(world, location.getX() + x - radius, location.getY() + y, location.getZ() + z - radius), material, doPhysics, isNatural);
                    }
                }
            }
        }

        if (depth != 0) {
            for (int x = 0; x < 2 * radius + 1; x++) {
                for (int z = 0; z < 2 * radius + 1; z++) {
                    for (int y = 1; y < depth + 1; y++) {
                        setBlock(new Location(world, location.getX() + x - radius, location.getY() - y, location.getZ() + z - radius), material, doPhysics, isNatural);
                    }
                }
            }
        }
    }

    void setBlock(Location location, MaterialTag material, boolean physics, boolean natural) {
        if (physics) {
            for (int i = 0; i < block_physics.size(); i++) {
                if (compareloc(block_physics.get(i), location)) {
                    block_physics.remove(i--);
                }
            }
        }
        else {
            block_physics.add(location);
            physitick = tick;
        }
        if (location.getY() < 0 || location.getY() > 255) {
            Debug.echoError("Invalid modifyblock location: " + new LocationTag(location).toString());
            return;
        }
        if (natural && material.getMaterial() == Material.AIR) {
            location.getBlock().breakNaturally();
        }
        else {
            material.getNmsBlockData().setBlock(location.getBlock(), physics);
        }
    }

    boolean no_physics = false;

    public final List<Location> block_physics = new ArrayList<>();

    long tick = 0;

    long physitick = 0;

    @Override
    public void onEnable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Keep the list empty automatically - we don't want to still block physics so much later that something else edited the block!
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                tick++;
                if (physitick < tick - 1) {
                    block_physics.clear();
                }
            }
        }, 2, 2);
    }


    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (no_physics) {
            event.setCancelled(true);
        }
        for (Location loc : block_physics) {
            if (compareloc(event.getBlock().getLocation(), loc)) {
                event.setCancelled(true);
            }
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
        for (Location loc : block_physics) {
            if (compareloc(event.getBlock().getLocation(), loc)) {
                event.setCancelled(true);
            }
        }
    }

    boolean compareloc(Location lone, Location ltwo) {
        return lone.getBlockX() == ltwo.getBlockX() && lone.getBlockY() == ltwo.getBlockY() &&
                lone.getBlockZ() == ltwo.getBlockZ() && lone.getWorld().getName().equalsIgnoreCase(ltwo.getWorld().getName());
    }
}
