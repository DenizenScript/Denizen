package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies blocks based based of single block location.
 * Possibility to do faux animations with blocks.
 *
 * @author Mason Adkins, aufdemrand
 */

public class ModifyBlockCommand extends AbstractCommand implements Listener {

    @Override
    public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentList(dLocation.class)){
                scriptEntry.addObject("locations", arg.asType(dList.class).filter(dLocation.class));
            }

            else if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(dMaterial.class)) {
                scriptEntry.addObject("material", arg.asType(dMaterial.class));
            }

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius, r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("radius", new Element(arg.getValue()));
            }

            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height, h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", new Element(arg.getValue()));
            }

            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth, d")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("depth", new Element(arg.getValue()));
            }

            else if (arg.matches("no_physics"))
                scriptEntry.addObject("physics", new Element(false));

            else if (arg.matches("naturally"))
                scriptEntry.addObject("natural", new Element(true));

            else
                arg.reportUnhandled();
        }

        // Must have material
        if (!scriptEntry.hasObject("material"))
            throw new InvalidArgumentsException("Missing material argument!");

        // ..and at least one location.
        if (!scriptEntry.hasObject("locations"))
            throw new InvalidArgumentsException("Missing location argument!");

        // Set some defaults
        scriptEntry.defaultObject("radius", new Element(0))
                .defaultObject("height", new Element(0))
                .defaultObject("depth", new Element(0))
                .defaultObject("physics", new Element(true))
                .defaultObject("natural", new Element(false));

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dMaterial material = (dMaterial) scriptEntry.getObject("material");
        List<dObject> locations = (List<dObject>) scriptEntry.getObject("locations");
        Element physics = scriptEntry.getElement("physics");
        Element natural = scriptEntry.getElement("natural");
        Element radiusElement = scriptEntry.getElement("radius");
        Element heightElement = scriptEntry.getElement("height");
        Element depthElement = scriptEntry.getElement("depth");

        dB.report(scriptEntry, getName(), aH.debugList("locations", locations)
                                          + material.debug()
                                          + physics.debug()
                                          + radiusElement.debug()
                                          + heightElement.debug()
                                          + depthElement.debug()
                                          + natural.debug());

        boolean doPhysics = physics.asBoolean();
        boolean isNatural = natural.asBoolean();
        int radius = radiusElement.asInt();
        int height = heightElement.asInt();
        int depth = depthElement.asInt();

        no_physics = !doPhysics;

        for (dObject obj : locations) {

            dLocation location = (dLocation) obj;
            World world = location.getWorld();

            location.setX(location.getBlockX());
            location.setY(location.getBlockY());
            location.setZ(location.getBlockZ());
            setBlock(location, material, doPhysics, isNatural);

            if (radius != 0){
                for (int x = 0; x  < 2 * radius + 1;  x++) {
                    for (int z = 0; z < 2 * radius + 1; z++) {
                        setBlock(new Location(world, location.getX() + x - radius, location.getY(), location.getZ() + z - radius), material, doPhysics, isNatural);
                    }
                }
            }

            if (height != 0){
                for (int x = 0; x  < 2 * radius + 1;  x++) {
                    for (int z = 0; z < 2 * radius + 1; z++) {
                        for (int y = 1; y < height + 1; y++) {
                            setBlock(new Location(world, location.getX() + x - radius, location.getY() + y, location.getZ() + z - radius), material, doPhysics, isNatural);
                        }
                    }
                }
            }

            if (depth != 0){
                for (int x = 0; x  < 2 * radius + 1;  x++) {
                    for (int z = 0; z < 2 * radius + 1; z++) {
                        for (int y = 1; y < depth + 1; y++) {
                            setBlock(new Location(world, location.getX() + x - radius, location.getY() - y, location.getZ() + z - radius), material, doPhysics, isNatural);
                        }
                    }
                }
            }
        }
        no_physics = false;
    }

    void setBlock(Location location, dMaterial material, boolean physics, boolean natural) {
        if (physics) {
            for (int i = 0; i < block_physics.size(); i++) {
                if (compareloc(block_physics.get(i), location))
                    block_physics.remove(i--);
            }
        }
        else {
            block_physics.add(location);
            physitick = tick;
        }
        if (natural && material.getMaterial() == Material.AIR)
            location.getBlock().breakNaturally();
        else
            location.getBlock().setTypeIdAndData(material.getMaterial().getId(), material.getMaterialData().getData(), physics);
    }

    boolean no_physics = false;

    public final List<Location> block_physics = new ArrayList<Location>();

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
                if (physitick < tick - 1)
                    block_physics.clear();
            }
        }, 2, 2);
    }


    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (no_physics)
            event.setCancelled(true);
        for (Location loc: block_physics) {
            if (compareloc(event.getBlock().getLocation(), loc))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockChanges(EntityChangeBlockEvent event) {
        if (event.getEntity().getType() != EntityType.FALLING_BLOCK)
            return;
        if (no_physics)
            event.setCancelled(true);
        for (Location loc: block_physics) {
            if (compareloc(event.getBlock().getLocation(), loc))
                event.setCancelled(true);
        }
    }

    boolean compareloc(Location lone, Location ltwo) {
        return lone.getBlockX() == ltwo.getBlockX() && lone.getBlockY() == ltwo.getBlockY() &&
                lone.getBlockZ() == ltwo.getBlockZ() && lone.getWorld().getName().equalsIgnoreCase(ltwo.getWorld().getName());
    }
}
