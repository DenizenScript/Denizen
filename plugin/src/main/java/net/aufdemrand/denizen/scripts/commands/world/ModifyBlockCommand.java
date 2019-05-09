package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.WorldHelper;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEllipsoid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {


            if (arg.matchesArgumentType(dCuboid.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")) {
                scriptEntry.addObject("locations", arg.asType(dCuboid.class).getBlockLocations());
            }
            else if (arg.matchesArgumentType(dEllipsoid.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")) {
                scriptEntry.addObject("locations", arg.asType(dEllipsoid.class).getBlockLocations());
            }
            else if (arg.matchesArgumentList(dLocation.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")) {
                scriptEntry.addObject("location_list", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("materials")
                    && arg.matchesArgumentList(dMaterial.class)) {
                scriptEntry.addObject("materials", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius", "r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("radius", new Element(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height", "h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", new Element(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth", "d")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("depth", new Element(arg.getValue()));
            }
            else if (arg.matches("no_physics")) {
                scriptEntry.addObject("physics", new Element(false));
            }
            else if (arg.matches("naturally")) {
                scriptEntry.addObject("natural", new Element(true));
            }
            else if (arg.matches("delayed")) {
                scriptEntry.addObject("delayed", new Element(true));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("percents")) {
                scriptEntry.addObject("percents", arg.asType(dList.class));
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
        scriptEntry.defaultObject("radius", new Element(0))
                .defaultObject("height", new Element(0))
                .defaultObject("depth", new Element(0))
                .defaultObject("physics", new Element(true))
                .defaultObject("natural", new Element(false))
                .defaultObject("delayed", new Element(false));

    }


    @Override
    public void execute(final ScriptEntry scriptEntry) {

        final dList materials = scriptEntry.getdObject("materials");
        final List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("locations");
        final dList location_list = scriptEntry.getdObject("location_list");
        final Element physics = scriptEntry.getElement("physics");
        final Element natural = scriptEntry.getElement("natural");
        final Element delayed = scriptEntry.getElement("delayed");
        final Element radiusElement = scriptEntry.getElement("radius");
        final Element heightElement = scriptEntry.getElement("height");
        final Element depthElement = scriptEntry.getElement("depth");
        final dScript script = scriptEntry.getdObject("script");
        dList percents = scriptEntry.getdObject("percents");

        if (percents != null && percents.size() != materials.size()) {
            dB.echoError(scriptEntry.getResidingQueue(), "Percents length != materials length");
            percents = null;
        }

        final List<dMaterial> materialList = materials.filter(dMaterial.class, scriptEntry);

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (locations == null ? location_list.debug() : aH.debugList("locations", locations))
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
                percentages.add(new Element(str).asFloat());
            }
        }
        final List<Float> percs = percentages;

        if ((locations == null || locations.size() == 0) && (location_list == null || location_list.size() == 0)) {
            dB.echoError("Must specify a valid location!");
            return;
        }
        if (materialList.size() == 0) {
            dB.echoError("Must specify a valid material!");
            return;
        }

        no_physics = !doPhysics;
        if (delayed.asBoolean()) {
            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    dLocation loc;
                    if (locations != null) {
                        loc = locations.get(0);
                    }
                    else {
                        loc = dLocation.valueOf(location_list.get(0));
                    }
                    boolean was_static = preSetup(loc);
                    while ((locations != null && locations.size() > index) || (location_list != null && location_list.size() > index)) {
                        dLocation nLoc;
                        if (locations != null) {
                            nLoc = locations.get(index);
                        }
                        else {
                            nLoc = dLocation.valueOf(location_list.get(index));
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
                            ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(script.getContainer().getName()))
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
            dLocation loc;
            if (locations != null) {
                loc = locations.get(0);
            }
            else {
                loc = dLocation.valueOf(location_list.get(0));
            }
            boolean was_static = preSetup(loc);
            int index = 0;
            if (locations != null) {
                for (dObject obj : locations) {
                    handleLocation((dLocation) obj, index, materialList, doPhysics, isNatural, radius, height, depth, percentages);
                    index++;
                }
            }
            else {
                for (String str : location_list) {
                    handleLocation(dLocation.valueOf(str), index, materialList, doPhysics, isNatural, radius, height, depth, percentages);
                    index++;
                }
            }
            postComplete(loc, was_static);
            scriptEntry.setFinished(true);
        }
    }

    boolean preSetup(dLocation loc0) {
        // Freeze the first world in the list.
        WorldHelper worldHelper = NMSHandler.getInstance().getWorldHelper();
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
            NMSHandler.getInstance().getWorldHelper().setStatic(loc.getWorld(), was_static);
        }
        no_physics = false;
    }

    void handleLocation(dLocation location, int index, List<dMaterial> materialList, boolean doPhysics,
                        boolean isNatural, int radius, int height, int depth, List<Float> percents) {

        dMaterial material;
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

    void setBlock(Location location, dMaterial material, boolean physics, boolean natural) {
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
            dB.echoError("Invalid modifyblock location: " + new dLocation(location).toString());
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
