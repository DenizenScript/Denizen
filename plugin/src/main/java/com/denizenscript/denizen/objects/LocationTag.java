package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.objects.properties.material.*;
import com.denizenscript.denizen.scripts.commands.world.SwitchCommand;
import com.denizenscript.denizen.utilities.*;
import com.denizenscript.denizen.utilities.blocks.SpawnableHelper;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizen.utilities.world.PathFinder;
import com.denizenscript.denizen.utilities.world.WorldListChangeTracker;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.SimplexNoise;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.block.structure.UsageMode;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class LocationTag extends org.bukkit.Location implements ObjectTag, Notable, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name LocationTag
    // @prefix l
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase player.location
    // @ExampleValues <player.location>,<npc.location>
    // @ExampleForReturns
    // - teleport <player> %VALUE%
    // @format
    // The identity format for locations is <x>,<y>,<z>,<pitch>,<yaw>,<world>
    // Note that you can leave off the world, and/or pitch and yaw, and/or the z value.
    // You cannot leave off both the z and the pitch+yaw at the same time.
    // For example, 'l@1,2.15,3,45,90,space' or 'l@7.5,99,3.2'
    //
    // @description
    // A LocationTag represents a point in the world.
    //
    // Note that 'l' prefix is a lowercase 'L', the first letter in 'location'.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the chunk file inside the world folder.
    //
    // @Matchable
    // LocationTag matchers, sometimes identified as "<location>" or "<block>":
    // "location" plaintext: always matches.
    // "block_flagged:<flag>": a Flag Matchable for location flags at the given block location.
    // "location_in:<area>": runs AreaObject checks, as defined below.
    // If none of the above are used, and the location is at a real block, a MaterialTag matchable is used. Refer to <@link objecttype MaterialTag> matchable list.
    //
    // -->

    /**
     * The world name if a world reference is bad.
     */
    public String backupWorld;
    public int trackedWorldChange;

    public String getWorldName() {
        if (backupWorld != null) {
            return backupWorld;
        }
        World w = super.getWorld();
        if (w != null) {
            backupWorld = w.getName();
        }
        return backupWorld;
    }

    @Override
    public World getWorld() {
        World w = super.getWorld();
        if (w != null) {
            if (trackedWorldChange != WorldListChangeTracker.changes) {
                trackedWorldChange = WorldListChangeTracker.changes;
                super.setWorld(Bukkit.getWorld(getWorldName()));
                return super.getWorld();
            }
            return w;
        }
        if (backupWorld == null) {
            return null;
        }
        super.setWorld(Bukkit.getWorld(backupWorld));
        return super.getWorld();
    }

    @Override
    public LocationTag clone() {
        return (LocationTag) super.clone();
    }

    public void makeUnique(String id) {
        NoteManager.saveAs(this, id);
    }

    @Note("Locations")
    public String getSaveObject() {
        return getX() + "," + getY() + "," + getZ() + "," + getPitch() + "," + getYaw() + "," + getWorldName();
    }

    public static String getSaved(LocationTag location) {
        return NoteManager.getSavedId(location);
    }

    @Override
    public void forget() {
        NoteManager.remove(this);
    }

    @Deprecated
    public static LocationTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string the string or dScript argument String
     * @return a Location, or null if incorrectly formatted
     */
    @Fetchable("l")
    public static LocationTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("l@")) {
            string = string.substring(2);
        }
        if (!TagManager.isStaticParsing) {
            Notable noted = NoteManager.getSavedObject(string);
            if (noted instanceof LocationTag) {
                return (LocationTag) noted;
            }
        }
        List<String> split = CoreUtilities.split(string, ',');
        if (split.size() == 2)
        // If 4 values, world-less 2D location format
        // x,y
        {
            try {
                return new LocationTag(null,
                        Double.parseDouble(split.get(0)),
                        Double.parseDouble(split.get(1)));
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 3)
        // If 3 values, either worldless location format
        // x,y,z or 2D location format x,y,world
        {
            try {
                String worldName = split.get(2);
                if (worldName.startsWith("w@")) {
                    worldName = worldName.substring("w@".length());
                }
                World world = TagManager.isStaticParsing ? null : Bukkit.getWorld(worldName);
                if (world != null) {
                    return new LocationTag(world,
                            Double.parseDouble(split.get(0)),
                            Double.parseDouble(split.get(1)));
                }
                if (ArgumentHelper.matchesDouble(split.get(2))) {
                    return new LocationTag(null,
                            Double.parseDouble(split.get(0)),
                            Double.parseDouble(split.get(1)),
                            Double.parseDouble(split.get(2)));
                }
                LocationTag output = new LocationTag(null,
                        Double.parseDouble(split.get(0)),
                        Double.parseDouble(split.get(1)));
                output.backupWorld = worldName;
                return output;
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 4)
        // If 4 values, standard dScript location format
        // x,y,z,world
        {
            try {
                String worldName = split.get(3);
                if (worldName.startsWith("w@")) {
                    worldName = worldName.substring("w@".length());
                }
                World world = TagManager.isStaticParsing ? null : Bukkit.getWorld(worldName);
                if (world != null) {
                    return new LocationTag(world,
                            Double.parseDouble(split.get(0)),
                            Double.parseDouble(split.get(1)),
                            Double.parseDouble(split.get(2)));
                }
                LocationTag output = new LocationTag(null,
                        Double.parseDouble(split.get(0)),
                        Double.parseDouble(split.get(1)),
                        Double.parseDouble(split.get(2)));
                output.backupWorld = worldName;
                return output;
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 5)

        // If 5 values, location with pitch/yaw (no world)
        // x,y,z,pitch,yaw
        {
            try {
                float pitch = Float.parseFloat(split.get(3));
                float yaw = Float.parseFloat(split.get(4));
                return new LocationTag((World) null,
                        Double.parseDouble(split.get(0)),
                        Double.parseDouble(split.get(1)),
                        Double.parseDouble(split.get(2)),
                        yaw, pitch);
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 6)

        // If 6 values, location with pitch/yaw
        // x,y,z,pitch,yaw,world
        {
            try {
                String worldName = split.get(5);
                if (worldName.startsWith("w@")) {
                    worldName = worldName.substring("w@".length());
                }
                float pitch = Float.parseFloat(split.get(3));
                float yaw = Float.parseFloat(split.get(4));
                return new LocationTag(worldName,
                        Double.parseDouble(split.get(0)),
                        Double.parseDouble(split.get(1)),
                        Double.parseDouble(split.get(2)),
                        yaw, pitch);
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        if ((context == null || context.showErrors()) && !TagManager.isStaticParsing) {
            Debug.log("Minor: valueOf LocationTag returning null: " + string);
        }
        return null;
    }

    public static boolean matches(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        if (string.startsWith("l@")) {
            return true;
        }
        return LocationTag.valueOf(string, CoreUtilities.noDebugContext) != null;
    }

    @Override
    public ObjectTag duplicate() {
        return clone();
    }

    /**
     * Turns a Bukkit Location into a LocationTag, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public LocationTag(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (location instanceof LocationTag) {
            backupWorld = ((LocationTag) location).backupWorld;
        }
    }

    public LocationTag(Vector vector) {
        this((World) null, vector.getX(), vector.getY(), vector.getZ(), 0, 0);
    }

    public LocationTag(World world, Vector vector) {
        this(world, vector.getX(), vector.getY(), vector.getZ(), 0, 0);
    }

    public LocationTag(World world, double x, double y) {
        this(world, x, y, 0);
    }

    /**
     * Turns a world and coordinates into a Location, which has some helpful methods
     * for working with dScript.
     *
     * @param world the world in which the location resides
     * @param x     x-coordinate of the location
     * @param y     y-coordinate of the location
     * @param z     z-coordinate of the location
     */
    public LocationTag(World world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
    }

    public LocationTag(double x, double y, double z, String worldName) {
        super(worldName == null ? null : Bukkit.getWorld(worldName), x, y, z);
        backupWorld = worldName;
    }

    public LocationTag(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, EntityHelper.normalizeYaw(yaw), pitch);
        if (world != null) {
            backupWorld = world.getName();
        }
    }

    public LocationTag(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(worldName == null ? null : Bukkit.getWorld(worldName), x, y, z, EntityHelper.normalizeYaw(yaw), pitch);
        backupWorld = worldName;
    }

    public boolean isChunkLoaded() {
        return getWorld() != null && getWorld().isChunkLoaded(getBlockX() >> 4, getBlockZ() >> 4);
    }

    public boolean isChunkLoadedSafe() {
        try {
            NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
            return isChunkLoaded();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    @Override
    public Block getBlock() {
        if (getWorld() == null) {
            Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
            return null;
        }
        return super.getBlock();
    }

    public Block getBlockForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return super.getBlock();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public BlockData getBlockDataForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return super.getBlock().getBlockData();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public Material getBlockTypeForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return super.getBlock().getType();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public static BlockState getBlockStateSafe(Block block) {
        NMSHandler.chunkHelper.changeChunkServerThread(block.getWorld());
        try {
            return block.getState();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(block.getWorld());
        }
    }

    public BiomeNMS getBiome() {
        return NMSHandler.instance.getBiomeAt(super.getBlock());
    }

    public BiomeNMS getBiomeForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return getBiome();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public Location getHighestBlockForTag(Attribute attribute) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return getWorld().getHighestBlockAt(this).getLocation();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public Collection<ItemStack> getDropsForTag(Attribute attribute, ItemStack item) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return null;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            return item == null ? super.getBlock().getDrops() : super.getBlock().getDrops(item);
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public int getExpDropForTag(Attribute attribute, ItemStack item) {
        NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                return 0;
            }
            if (!isChunkLoaded()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return 0;
            }
            return NMSHandler.blockHelper.getExpDrop(super.getBlock(), item);
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public BlockState getBlockState() {
        return getBlock().getState();
    }

    public BlockState getBlockStateForTag(Attribute attribute) {
        Block block = getBlockForTag(attribute);
        if (block == null) {
            return null;
        }
        return getBlockStateSafe(block);
    }

    public static boolean isSameBlock(Location a, Location b) {
        return a != null && b != null && a.getWorld() == b.getWorld() && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

    public LocationTag getBlockLocation() {
        return new LocationTag(getWorld(), getBlockX(), getBlockY(), getBlockZ());
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        if (getWorld() == null) {
            return null;
        }
        return new DataPersistenceFlagTracker(getChunk(), "flag_tracker_" + getBlockX() + "_" + getBlockY() + "_" + getBlockZ() + "_");
    }

    @Override
    public AbstractFlagTracker getFlagTrackerForTag() {
        if (!isChunkLoadedSafe()) {
            return null;
        }
        return getFlagTracker();
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    @Override
    public String getReasonNotFlaggable() {
        if (getWorld() == null) {
            return "missing world";
        }
        if (!isChunkLoadedSafe()) {
            return "chunk is not loaded";
        }
        return "unknown reason";
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
    }

    // TODO: Why does this and the above exist?
    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
    }

    @Override
    public LocationTag add(double x, double y, double z) {
        super.add(x, y, z);
        return this;
    }

    @Override
    public LocationTag add(Vector input) {
        super.add(input);
        return this;
    }

    @Override
    public LocationTag add(Location input) {
        super.add(input.getX(), input.getY(), input.getZ());
        return this;
    }

    @Override
    public LocationTag multiply(double input) {
        super.multiply(input);
        return this;
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        backupWorld = world == null ? null : world.getName();
    }

    public double distanceSquaredNoWorld(Location loc2) {
        return NumberConversions.square(getX() - loc2.getX()) + NumberConversions.square(getY() - loc2.getY()) + NumberConversions.square(getZ() - loc2.getZ());
    }

    public Inventory getBukkitInventory() {
        BlockState state = getBlockState();
        if (state instanceof InventoryHolder) {
            return((InventoryHolder) state).getInventory();
        }
        return null;
    }

    public InventoryTag getInventory() {
        Inventory inv = getBukkitInventory();
        if (inv != null) {
            return InventoryTag.mirrorBukkitInventory(inv);
        }
        Material type = getBlock().getType();
        if (type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL) {
            return new InventoryTag(Bukkit.createInventory(null, InventoryType.ANVIL), "location", this.clone());
        }
        return null;
    }

    public BlockFace getSkullBlockFace(int rotation) {
        switch (rotation) {
            case 0:
                return BlockFace.NORTH;
            case 1:
                return BlockFace.NORTH_NORTH_EAST;
            case 2:
                return BlockFace.NORTH_EAST;
            case 3:
                return BlockFace.EAST_NORTH_EAST;
            case 4:
                return BlockFace.EAST;
            case 5:
                return BlockFace.EAST_SOUTH_EAST;
            case 6:
                return BlockFace.SOUTH_EAST;
            case 7:
                return BlockFace.SOUTH_SOUTH_EAST;
            case 8:
                return BlockFace.SOUTH;
            case 9:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 10:
                return BlockFace.SOUTH_WEST;
            case 11:
                return BlockFace.WEST_SOUTH_WEST;
            case 12:
                return BlockFace.WEST;
            case 13:
                return BlockFace.WEST_NORTH_WEST;
            case 14:
                return BlockFace.NORTH_WEST;
            case 15:
                return BlockFace.NORTH_NORTH_WEST;
            default:
                return null;
        }
    }

    public byte getSkullRotation(BlockFace face) {
        switch (face) {
            case NORTH:
                return 0;
            case NORTH_NORTH_EAST:
                return 1;
            case NORTH_EAST:
                return 2;
            case EAST_NORTH_EAST:
                return 3;
            case EAST:
                return 4;
            case EAST_SOUTH_EAST:
                return 5;
            case SOUTH_EAST:
                return 6;
            case SOUTH_SOUTH_EAST:
                return 7;
            case SOUTH:
                return 8;
            case SOUTH_SOUTH_WEST:
                return 9;
            case SOUTH_WEST:
                return 10;
            case WEST_SOUTH_WEST:
                return 11;
            case WEST:
                return 12;
            case WEST_NORTH_WEST:
                return 13;
            case NORTH_WEST:
                return 14;
            case NORTH_NORTH_WEST:
                return 15;
        }
        return -1;
    }

    public static double[] getRotatedAroundX(double angle, double y, double z) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newY = (y * cos) - (z * sin);
        double newZ = (y * sin) + (z * cos);
        return new double[] { newY, newZ };
    }

    public static double[] getRotatedAroundY(double angle, double x, double z) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = (x * cos) + (z * sin);
        double newZ = (x * -sin) + (z * cos);
        return new double[] { newX, newZ };
    }

    public static double[] getRotatedAroundZ(double angle, double x, double y) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = (x * cos) - (y * sin);
        double newY = (x * sin) + (y * cos);
        return new double[] { newX, newY };
    }

    public static double[] parsePointsAroundArgs(Attribute attribute) {
        MapTag inputMap = attribute.inputParameterMap();
        ElementTag radiusElement = inputMap.getRequiredObjectAs("radius", ElementTag.class, attribute);
        ElementTag amountElement = inputMap.getRequiredObjectAs("points", ElementTag.class, attribute);
        if (radiusElement == null || amountElement == null) {
            return null;
        }
        double radius = radiusElement.asDouble();
        int amount = amountElement.asInt();
        if (amount < 1) {
            attribute.echoError("Invalid amount of points! There must be at least 1 point.");
            return null;
        }
        return new double[] { radius, amount };
    }

    public static class FloodFiller {

        public Set<LocationTag> result;

        public int iterationLimit;

        public AreaContainmentObject areaLimit;

        public Material requiredMaterial;

        public String matcher;

        public void run(LocationTag start, AreaContainmentObject area) {
            iterationLimit = Settings.blockTagsMaxBlocks();
            areaLimit = area;
            result = new LinkedHashSet<>();
            flood(start.getBlockLocation());
        }

        public void flood(LocationTag loc) {
            if (iterationLimit-- <= 0 || result.contains(loc) || !areaLimit.doesContainLocation(loc)) {
                return;
            }
            if (!loc.isChunkLoaded()) {
                return;
            }
            if (matcher == null ? loc.getBlock().getType() != requiredMaterial : !loc.tryAdvancedMatcher(matcher)) {
                return;
            }
            result.add(loc);
            flood(loc.clone().add(-1, 0, 0));
            flood(loc.clone().add(1, 0, 0));
            flood(loc.clone().add(0, 0, -1));
            flood(loc.clone().add(0, 0, 1));
            flood(loc.clone().add(0, -1, 0));
            flood(loc.clone().add(0, 1, 0));
        }
    }

    public int compare(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.equals(loc2)) {
            return 0;
        }
        else {
            return Double.compare(distanceSquared(loc1), distanceSquared(loc2));
        }
    }

    @Override
    public int hashCode() {
        return (int) (Math.floor(getX()) + Math.floor(getY()) + Math.floor(getZ()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof LocationTag)) {
            return false;
        }
        LocationTag other = (LocationTag) o;
        if ((other.getWorldName() == null && getWorldName() != null)
                || (getWorldName() == null && other.getWorldName() != null)
                || (getWorldName() != null && other.getWorldName() != null
                && !CoreUtilities.equalsIgnoreCase(getWorldName(), other.getWorldName()))) {
            return false;
        }
        return getX() == other.getX()
                && getY() == other.getY()
                && getZ() == other.getZ()
                && getYaw() == other.getYaw()
                && getPitch() == other.getPitch();
    }

    String prefix = "Location";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public LocationTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debuggable() {
        String saved = getSaved(this);
        if (saved != null) {
            return "<Y>" + saved + "<GR> (" + identifyRaw().replace(",", "<G>,<GR> ") + "<GR>)";
        }
        else {
            return "<Y>" + identifyRaw().replace(",", "<G>,<Y> ");
        }
    }

    @Override
    public boolean isUnique() {
        return getSaved(this) != null;
    }

    @Override
    public String identify() {
        String saved = getSaved(this);
        if (saved != null) {
            return "l@" + saved;
        }
        return identifyRaw();
    }

    @Override
    public String identifySimple() {
        String saved = getSaved(this);
        if (saved != null) {
            return saved;
        }
        else if (getWorldName() == null) {
            return "l@" + getBlockX() + "," + getBlockY() + "," + getBlockZ();
        }
        else {
            return "l@" + getBlockX() + "," + getBlockY() + "," + getBlockZ() + "," + getWorldName();
        }
    }

    public String identifyRaw() {
        if (getYaw() != 0.0 || getPitch() != 0.0) {
            return "l@" + CoreUtilities.doubleToString(getX()) + "," + CoreUtilities.doubleToString(getY())
                    + "," + CoreUtilities.doubleToString(getZ()) + "," + CoreUtilities.doubleToString(getPitch())
                    + "," + CoreUtilities.doubleToString(getYaw())
                    + (getWorldName() != null ? "," + getWorldName() : "");
        }
        else {
            return "l@" + CoreUtilities.doubleToString(getX()) + "," + CoreUtilities.doubleToString(getY())
                    + "," + CoreUtilities.doubleToString(getZ())
                    + (getWorldName() != null ? "," + getWorldName() : "");
        }
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public Object getJavaObject() {
        return clone();
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <LocationTag.block_facing>
        // @returns LocationTag
        // @mechanism LocationTag.block_facing
        // @group world
        // @description
        // Returns the relative location vector of where this block is facing.
        // Only works for block types that have directionality (such as signs, chests, stairs, etc.).
        // This can return for example "1,0,0" to mean the block is facing towards the positive X axis.
        // You can use <some_block_location.add[<some_block_location.block_facing>]> to get the block directly in front of this block (based on its facing direction).
        // -->
        tagProcessor.registerTag(LocationTag.class, "block_facing", (attribute, object) -> {
            BlockData block = object.getBlockDataForTag(attribute);
            MaterialTag material = new MaterialTag(block);
            if (!MaterialDirectional.describes(material)) {
                return null;
            }
            Vector vec = MaterialDirectional.getFrom(material).getDirectionVector();
            if (vec == null) {
                return null;
            }
            return new LocationTag(object.getWorld(), vec);
        });

        // <--[tag]
        // @attribute <LocationTag.with_facing_direction>
        // @returns LocationTag
        // @group world
        // @description
        // Returns the location with its direction set to the block's facing direction.
        // Only works for block types that have directionality (such as signs, chests, stairs, etc.).
        // You can use <some_block_location.with_facing_direction.forward[1]> to get the block directly in front of this block (based on its facing direction).
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_facing_direction", (attribute, object) -> {
            BlockData block = object.getBlockDataForTag(attribute);
            MaterialTag material = new MaterialTag(block);
            if (!MaterialDirectional.describes(material)) {
                return null;
            }
            Vector facing = MaterialDirectional.getFrom(material).getDirectionVector();
            if (facing == null) {
                return null;
            }
            LocationTag result = object.clone();
            result.setDirection(facing);
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.above[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location above this location. Optionally specify a number of blocks to go up.
        // This just moves straight along the Y axis, equivalent to <@link tag LocationTag.add> with input 0,1,0 (or the input value instead of '1').
        // -->
        tagProcessor.registerTag(LocationTag.class, "above", (attribute, object) -> {
            return new LocationTag(object.clone().add(0, attribute.hasParam() ? attribute.getDoubleParam() : 1, 0));
        });

        // <--[tag]
        // @attribute <LocationTag.below[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location below this location. Optionally specify a number of blocks to go down.
        // This just moves straight along the Y axis, equivalent to <@link tag LocationTag.sub> with input 0,1,0 (or the input value instead of '1').
        // -->
        tagProcessor.registerTag(LocationTag.class, "below", (attribute, object) -> {
            return new LocationTag(object.clone().subtract(0, attribute.hasParam() ? attribute.getDoubleParam() : 1, 0));
        });

        // <--[tag]
        // @attribute <LocationTag.forward_flat[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location in front of this location based on yaw but not pitch. Optionally specify a number of blocks to go forward.
        // -->
        tagProcessor.registerTag(LocationTag.class, "forward_flat", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.backward_flat[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location behind this location based on yaw but not pitch. Optionally specify a number of blocks to go backward.
        // This is equivalent to <@link tag LocationTag.forward_flat> in the opposite direction.
        // -->
        tagProcessor.registerTag(LocationTag.class, "backward_flat", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.forward[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location in front of this location based on pitch and yaw. Optionally specify a number of blocks to go forward.
        // -->
        tagProcessor.registerTag(LocationTag.class, "forward", (attribute, object) -> {
            Vector vector = object.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.backward[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location behind this location based on pitch and yaw. Optionally specify a number of blocks to go backward.
        // This is equivalent to <@link tag LocationTag.forward> in the opposite direction.
        // -->
        tagProcessor.registerTag(LocationTag.class, "backward", (attribute, object) -> {
            Vector vector = object.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.left[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location to the left of this location based on pitch and yaw. Optionally specify a number of blocks to go left.
        // This is equivalent to <@link tag LocationTag.forward> with a +90 degree rotation to the yaw and the pitch set to 0.
        // -->
        tagProcessor.registerTag(LocationTag.class, "left", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().rotateAroundY(Math.PI / 2).multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.right[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location to the right of this location based on pitch and yaw. Optionally specify a number of blocks to go right.
        // This is equivalent to <@link tag LocationTag.forward> with a -90 degree rotation to the yaw and the pitch set to 0.
        // -->
        tagProcessor.registerTag(LocationTag.class, "right", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().rotateAroundY(Math.PI / 2).multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.up[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location above this location based on pitch and yaw. Optionally specify a number of blocks to go up.
        // This is equivalent to <@link tag LocationTag.forward> with a +90 degree rotation to the pitch.
        // To just get the location above this location, use <@link tag LocationTag.above> instead.
        // -->
        tagProcessor.registerTag(LocationTag.class, "up", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(loc.getPitch() - 90);
            Vector vector = loc.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.down[(<#.#>)]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location below this location based on pitch and yaw. Optionally specify a number of blocks to go down.
        // This is equivalent to <@link tag LocationTag.forward> with a -90 degree rotation to the pitch.
        // To just get the location above this location, use <@link tag LocationTag.below> instead.
        // -->
        tagProcessor.registerTag(LocationTag.class, "down", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(loc.getPitch() - 90);
            Vector vector = loc.getDirection().multiply(attribute.hasParam() ? attribute.getDoubleParam() : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.relative[<location>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location relative to this location. Input is a vector location of the form left,up,forward.
        // For example, input -1,1,1 will return a location 1 block to the right, 1 block up, and 1 block forward.
        // To just get the location relative to this without rotation math, use <@link tag LocationTag.add> instead.
        // -->
        tagProcessor.registerTag(LocationTag.class, "relative", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag offsetLoc = attribute.paramAsType(LocationTag.class);
            if (offsetLoc == null) {
                return null;
            }

            Location loc = object.clone();
            Vector offset = loc.getDirection().multiply(offsetLoc.getZ());
            loc.setPitch(loc.getPitch() - 90);
            offset = offset.add(loc.getDirection().multiply(offsetLoc.getY()));
            loc.setPitch(0);
            offset = offset.add(loc.getDirection().rotateAroundY(Math.PI / 2).multiply(offsetLoc.getX()));

            return new LocationTag(object.clone().add(offset));
        });

        // <--[tag]
        // @attribute <LocationTag.block>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location of the block this location is on,
        // i.e. returns a location without decimals or direction.
        // Note that you almost never actually need this tag. This does not "get the block", this just rounds coordinates down.
        // If you have this in a script, it is more likely to be a mistake than actually needed.
        // Consider using <@link tag LocationTag.round_down> instead.
        // -->
        tagProcessor.registerTag(LocationTag.class, "block", (attribute, object) -> {
            return new LocationTag(object.getWorld(), object.getBlockX(), object.getBlockY(), object.getBlockZ());
        });

        // <--[tag]
        // @attribute <LocationTag.center>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location at the center of the block this location is on.
        // -->
        tagProcessor.registerTag(LocationTag.class, "center", (attribute, object) -> {
            return new LocationTag(object.getWorld(), object.getBlockX() + 0.5, object.getBlockY() + 0.5, object.getBlockZ() + 0.5);
        });

        // <--[tag]
        // @attribute <LocationTag.simplex_3d>
        // @returns ElementTag(Decimal)
        // @group math
        // @description
        // Returns the 3D simplex noise value (from -1 to 1) for this location's X/Y/Z.
        // See also <@link tag util.random_simplex>
        // -->
        tagProcessor.registerTag(ElementTag.class, "simplex_3d", (attribute, object) -> {
            return new ElementTag(SimplexNoise.noise(object.getX(), object.getY(), object.getZ()));
        });

        // <--[tag]
        // @attribute <LocationTag.random_offset[<limit>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a copy of this location, with the X/Y/Z offset by a random decimal value up to a given limit.
        // The limit can either be an X,Y,Z location vector like [3,1,3] or a single value like [3] (which is equivalent to [3,3,3]).
        // For example, for a location at 0,100,0, ".random_offset[1,2,3]" can return any decimal location within the cuboid from -1,98,-3 to 1,102,3.
        // -->
        tagProcessor.registerTag(LocationTag.class, "random_offset", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("LocationTag.random_offset[...] must have an input.");
                return null;
            }
            Vector offsetLimit;
            if (ArgumentHelper.matchesDouble(attribute.getParam())) {
                double val = attribute.getDoubleParam();
                offsetLimit = new Vector(val, val, val);
            }
            else {
                LocationTag val = attribute.paramAsType(LocationTag.class);
                if (val == null) {
                    return null;
                }
                offsetLimit = val.toVector();
            }
            offsetLimit.setX(offsetLimit.getX() * (CoreUtilities.getRandom().nextDouble() * 2 - 1));
            offsetLimit.setY(offsetLimit.getY() * (CoreUtilities.getRandom().nextDouble() * 2 - 1));
            offsetLimit.setZ(offsetLimit.getZ() * (CoreUtilities.getRandom().nextDouble() * 2 - 1));
            LocationTag output = object.clone();
            output.add(offsetLimit);
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.highest>
        // @returns LocationTag
        // @group world
        // @description
        // Returns the location of the highest solid block at the location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "highest", (attribute, object) -> {
            Location result = object.getHighestBlockForTag(attribute);
            return new LocationTag(result);
        });

        // <--[tag]
        // @attribute <LocationTag.has_inventory>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the block at the location has an inventory.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_inventory", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof InventoryHolder);
        });

        // <--[tag]
        // @attribute <LocationTag.inventory>
        // @returns InventoryTag
        // @group world
        // @description
        // Returns the InventoryTag of the block at the location. If the
        // block is not a container, returns null.
        // -->
        tagProcessor.registerTag(InventoryTag.class, "inventory", (attribute, object) -> {
            if (!object.isChunkLoadedSafe()) {
                return null;
            }
            return ElementTag.handleNull(object.identify() + ".inventory", object.getInventory(), "InventoryTag", attribute.hasAlternative());
        });

        // <--[tag]
        // @attribute <LocationTag.material>
        // @returns MaterialTag
        // @group world
        // @description
        // Returns the material of the block at the location.
        // -->
        tagProcessor.registerTag(MaterialTag.class, "material", (attribute, object) -> {
            BlockData block = object.getBlockDataForTag(attribute);
            if (block == null) {
                return null;
            }
            return new MaterialTag(block);
        });

        // <--[tag]
        // @attribute <LocationTag.is_passable>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the block at this location is non-solid and can be walked through.
        // Note that for example an open door is still solid, and thus will return false.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_passable", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
            if (block == null) {
                return null;
            }
            return new ElementTag(block.isPassable());
        });

        // <--[tag]
        // @attribute <LocationTag.patterns>
        // @returns ListTag
        // @mechanism LocationTag.patterns
        // @group world
        // @description
        // Lists the patterns of the banner at this location in the form "COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // For the list of possible patterns, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // -->
        tagProcessor.registerTag(ListTag.class, "patterns", (attribute, object) -> {
            ListTag list = new ListTag();
            for (org.bukkit.block.banner.Pattern pattern : ((Banner) object.getBlockStateForTag(attribute)).getPatterns()) {
                list.add(pattern.getColor().name() + "/" + pattern.getPattern().name());
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.head_rotation>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.head_rotation
        // @group world
        // @description
        // Gets the rotation of the head at this location. Can be 1-16.
        // -->
        tagProcessor.registerTag(ElementTag.class, "head_rotation", (attribute, object) -> {
            return new ElementTag(object.getSkullRotation(((Skull) object.getBlockStateForTag(attribute)).getRotation()) + 1);
        });

        // <--[tag]
        // @attribute <LocationTag.switched>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the block at the location is considered to be switched on.
        // (For buttons, levers, etc.)
        // To change this, see <@link command Switch>
        // -->
        tagProcessor.registerTag(ElementTag.class, "switched", (attribute, object) -> {
            return new ElementTag(SwitchCommand.switchState(object.getBlockForTag(attribute)));
        });

        // <--[tag]
        // @attribute <LocationTag.sign_contents>
        // @returns ListTag
        // @mechanism LocationTag.sign_contents
        // @group world
        // @description
        // Returns a list of lines on a sign.
        // -->
        tagProcessor.registerTag(ListTag.class, "sign_contents", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof Sign) {
                return new ListTag(Arrays.asList(PaperAPITools.instance.getSignLines(((Sign) object.getBlockStateForTag(attribute)))));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_type>
        // @returns EntityTag
        // @mechanism LocationTag.spawner_type
        // @group world
        // @description
        // Returns the type of entity spawned by a mob spawner.
        // -->
        tagProcessor.registerTag(EntityTag.class, "spawner_type", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new EntityTag(DenizenEntityType.getByName(((CreatureSpawner) object.getBlockStateForTag(attribute)).getSpawnedType().name()));
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_display_entity>
        // @returns EntityTag
        // @group world
        // @description
        // Returns the full "display entity" for the spawner. This can contain more data than just a type.
        // -->
        tagProcessor.registerTag(EntityTag.class, "spawner_display_entity", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return NMSHandler.entityHelper.getMobSpawnerDisplayEntity(((CreatureSpawner) object.getBlockStateForTag(attribute))).describe(attribute.context);
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_delay_data
        // @group world
        // @description
        // Returns the current spawn delay for the spawner.
        // This changes over time between <@link tag LocationTag.spawner_minimum_spawn_delay> and <@link tag LocationTag.spawner_maximum_spawn_delay>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_spawn_delay", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getDelay());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_minimum_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_delay_data
        // @group world
        // @description
        // Returns the minimum spawn delay for the mob spawner.
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_minimum_spawn_delay", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getMinSpawnDelay());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_maximum_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_delay_data
        // @group world
        // @description
        // Returns the maximum spawn delay for the mob spawner.
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_maximum_spawn_delay", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof  CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getMaxSpawnDelay());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_player_range>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_player_range
        // @group world
        // @description
        // Returns the maximum player range for the spawner (ie how close a player must be for this spawner to be active).
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_player_range", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getRequiredPlayerRange());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_range>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_range
        // @group world
        // @description
        // Returns the spawn range for the spawner (the radius mobs will spawn in).
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_range", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getSpawnRange());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_max_nearby_entities>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_max_nearby_entities
        // @group world
        // @description
        // 	Returns the maximum nearby entities for the spawner (the radius mobs will spawn in).
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_max_nearby_entities", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getMaxNearbyEntities());
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_count>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.spawner_count
        // @group world
        // @description
        // Returns the spawn count for the spawner.
        // -->
        tagProcessor.registerTag(ElementTag.class, "spawner_count", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return new ElementTag(((CreatureSpawner) object.getBlockStateForTag(attribute)).getSpawnCount());
        });

        // <--[tag]
        // @attribute <LocationTag.lock>
        // @returns ElementTag
        // @mechanism LocationTag.lock
        // @group world
        // @description
        // Returns the password to a locked container.
        // -->
        tagProcessor.registerTag(ElementTag.class, "lock", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof Lockable)) {
                return null;
            }
            Lockable lock = (Lockable) object.getBlockStateForTag(attribute);
            return new ElementTag(lock.isLocked() ? lock.getLock() : null);
        });

        // <--[tag]
        // @attribute <LocationTag.is_locked>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.lock
        // @group world
        // @description
        // Returns whether the container is locked.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_locked", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof Lockable)) {
                return null;
            }
            return new ElementTag(((Lockable) object.getBlockStateForTag(attribute)).isLocked());
        });

        // <--[tag]
        // @attribute <LocationTag.is_lockable>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.lock
        // @group world
        // @description
        // Returns whether the container is lockable.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_lockable", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof Lockable);
        });

        // <--[tag]
        // @attribute <LocationTag.drops[(<item>)]>
        // @returns ListTag(ItemTag)
        // @group world
        // @description
        // Returns what items the block at the location would drop if broken naturally.
        // Optionally specifier a breaker item.
        // Not guaranteed to contain exactly correct or contain all possible drops (for things like plants that drop only when grown, ores that drop random amounts, etc).
        // -->
        tagProcessor.registerTag(ListTag.class, "drops", (attribute, object) -> {
            ItemStack inputItem = null;
            if (attribute.hasParam()) {
                inputItem = attribute.paramAsType(ItemTag.class).getItemStack();
            }
            ListTag list = new ListTag();
            for (ItemStack it : object.getDropsForTag(attribute, inputItem)) {
                list.addObject(new ItemTag(it));
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.xp_drop[(<item>)]>
        // @returns ElementTag(Number)
        // @group world
        // @description
        // Returns how much experience, if any, the block at the location would drop if broken naturally.
        // Returns 0 if a block wouldn't drop xp.
        // Optionally specifier a breaker item.
        // Not guaranteed to contain exactly the amount that actual drops if then broken later, as the value is usually randomized.
        // -->
        tagProcessor.registerTag(ElementTag.class, "xp_drop", (attribute, object) -> {
            ItemStack inputItem = new ItemStack(Material.AIR);
            if (attribute.hasParam()) {
                inputItem = attribute.paramAsType(ItemTag.class).getItemStack();
            }
            return new ElementTag(object.getExpDropForTag(attribute, inputItem));
        });

        // <--[tag]
        // @attribute <LocationTag.hive_bee_count>
        // @returns ElementTag(Number)
        // @group world
        // @description
        // Returns the number of bees inside a hive.
        // -->
        tagProcessor.registerTag(ElementTag.class, "hive_bee_count", (attribute, object) -> {
            return new ElementTag(((Beehive) object.getBlockStateForTag(attribute)).getEntityCount());
        });

        // <--[tag]
        // @attribute <LocationTag.hive_max_bees>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.hive_max_bees
        // @group world
        // @description
        // Returns the maximum number of bees allowed inside a hive.
        // -->
        tagProcessor.registerTag(ElementTag.class, "hive_max_bees", (attribute, object) -> {
            return new ElementTag(((Beehive) object.getBlockStateForTag(attribute)).getMaxEntities());
        });

        // <--[tag]
        // @attribute <LocationTag.skull_type>
        // @returns ElementTag
        // @group world
        // @description
        // Returns the type of the skull.
        // -->
        tagProcessor.registerTag(ElementTag.class, "skull_type", (attribute, object) -> {
            BlockState blockState = object.getBlockStateForTag(attribute);
            if (blockState instanceof Skull) {
                String t = ((Skull) blockState).getSkullType().name();
                return new ElementTag(t);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.skull_name>
        // @returns ElementTag
        // @mechanism LocationTag.skull_skin
        // @group world
        // @description
        // Returns the name of the skin the skull is displaying.
        // -->
        tagProcessor.registerTag(ElementTag.class, "skull_name", (attribute, object) -> {
            BlockState blockState = object.getBlockStateForTag(attribute);
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.blockHelper.getPlayerProfile((Skull) blockState);
                if (profile == null) {
                    return null;
                }
                String n = profile.getName();
                if (n == null) {
                    n = ((Skull) blockState).getOwningPlayer().getName();
                }
                return new ElementTag(n);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.skull_skin>
        // @returns ElementTag
        // @mechanism LocationTag.skull_skin
        // @group world
        // @description
        // Returns the skin the skull is displaying - just the name or UUID as text, not a player object.
        // -->
        tagProcessor.registerTag(ElementTag.class, "skull_skin", (attribute, object) -> {
            BlockState blockState = object.getBlockStateForTag(attribute);
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.blockHelper.getPlayerProfile((Skull) blockState);
                if (profile == null) {
                    return null;
                }
                String name = profile.getName();
                UUID uuid = profile.getUniqueId();
                String texture = profile.getTexture();

                // <--[tag]
                // @attribute <LocationTag.skull_skin.full>
                // @returns ElementTag
                // @mechanism LocationTag.skull_skin
                // @group world
                // @description
                // Returns the skin the skull item is displaying - just the name or UUID as text, not a player object,
                // along with the permanently cached texture property.
                // In format "uuid|texture" - separated by a pipe, but not a ListTag.
                // -->
                if (attribute.startsWith("full", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag((uuid != null ? uuid : name)
                            + (texture != null ? "|" + texture : ""));
                }
                return new ElementTag(uuid != null ? uuid.toString() : name);
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <LocationTag.round>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a rounded version of the LocationTag's coordinates.
        // That is, each component (X, Y, Z, Yaw, Pitch) is rounded
        // (eg, 0.1 becomes 0.0, 0.5 becomes 1.0, 0.9 becomes 1.0).
        // This is NOT equivalent to the block coordinates. For that, use <@link tag LocationTag.round_down>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "round", (attribute, object) -> {
            LocationTag result = object.clone();
            result.setX(Math.round(result.getX()));
            result.setY(Math.round(result.getY()));
            result.setZ(Math.round(result.getZ()));
            result.setYaw(Math.round(result.getYaw()));
            result.setPitch(Math.round(result.getPitch()));
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.round_up>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a rounded-upward version of the LocationTag's coordinates.
        // That is, each component (X, Y, Z, Yaw, Pitch) is rounded upward
        // (eg, 0.1 becomes 1.0, 0.5 becomes 1.0, 0.9 becomes 1.0).
        // -->
        tagProcessor.registerTag(LocationTag.class, "round_up", (attribute, object) -> {
            LocationTag result = object.clone();
            result.setX(Math.ceil(result.getX()));
            result.setY(Math.ceil(result.getY()));
            result.setZ(Math.ceil(result.getZ()));
            result.setYaw((float) Math.ceil((result.getYaw())));
            result.setPitch((float) Math.ceil(result.getPitch()));
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.round_down>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a rounded-downward version of the LocationTag's coordinates.
        // That is, each component (X, Y, Z, Yaw, Pitch) is rounded downward
        // (eg, 0.1 becomes 0.0, 0.5 becomes 0.0, 0.9 becomes 0.0).
        // This is equivalent to the block coordinates of the location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "round_down", (attribute, object) -> {
            LocationTag result = object.clone();
            result.setX(Math.floor(result.getX()));
            result.setY(Math.floor(result.getY()));
            result.setZ(Math.floor(result.getZ()));
            result.setYaw((float) Math.floor((result.getYaw())));
            result.setPitch((float) Math.floor(result.getPitch()));
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.round_to[<#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a rounded-to-precision version of the LocationTag's coordinates.
        // That is, each component (X, Y, Z, Yaw, Pitch) is rounded to the specified decimal place
        // (eg, 0.12345 .round_to[3] returns "0.123").
        // -->
        tagProcessor.registerTag(LocationTag.class, "round_to", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag LocationTag.round_to[...] must have a value.");
                return null;
            }
            LocationTag result = object.clone();
            int ten = (int) Math.pow(10, attribute.getIntParam());
            result.setX(((double) Math.round(result.getX() * ten)) / ten);
            result.setY(((double) Math.round(result.getY() * ten)) / ten);
            result.setZ(((double) Math.round(result.getZ() * ten)) / ten);
            result.setYaw(((float) Math.round(result.getYaw() * ten)) / ten);
            result.setPitch(((float) Math.round(result.getPitch() * ten)) / ten);
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.round_to_precision[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a rounded-to-precision version of the LocationTag's coordinates.
        // That is, each component (X, Y, Z, Yaw, Pitch) is rounded to the specified precision value
        // (0.12345 .round_to_precision[0.005] returns "0.125").
        // -->
        tagProcessor.registerTag(LocationTag.class, "round_to_precision", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag LocationTag.round_to_precision[...] must have a value.");
                return null;
            }
            LocationTag result = object.clone();
            float precision = 1f / (float) attribute.getDoubleParam();
            result.setX(((double) Math.round(result.getX() * precision)) / precision);
            result.setY(((double) Math.round(result.getY() * precision)) / precision);
            result.setZ(((double) Math.round(result.getZ() * precision)) / precision);
            result.setYaw(((float) Math.round(result.getYaw() * precision)) / precision);
            result.setPitch(((float) Math.round(result.getPitch() * precision)) / precision);
            return result;
        });

        // <--[tag]
        // @attribute <LocationTag.simple>
        // @returns ElementTag
        // @group identity
        // @description
        // Returns a simple version of the LocationTag's block coordinates.
        // In the format: x,y,z,world
        // For example: 1,2,3,world_nether
        // -->
        tagProcessor.registerTag(ElementTag.class, "simple", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.simple.formatted>
            // @returns ElementTag
            // @group identity
            // @description
            // Returns the formatted simple version of the LocationTag's block coordinates.
            // In the format: X 'x', Y 'y', Z 'z', in world 'world'
            // For example, X '1', Y '2', Z '3', in world 'world_nether'
            // -->
            if (attribute.startsWith("formatted", 2)) {
                attribute.fulfill(1);
                return new ElementTag("X '" + object.getBlockX()
                        + "', Y '" + object.getBlockY()
                        + "', Z '" + object.getBlockZ()
                        + "', in world '" + object.getWorldName() + "'");
            }
            if (object.getWorldName() == null) {
                return new ElementTag(object.getBlockX() + "," + object.getBlockY() + "," + object.getBlockZ());
            }
            else {
                return new ElementTag(object.getBlockX() + "," + object.getBlockY() + "," + object.getBlockZ()
                        + "," + object.getWorldName());
            }
        });

        // <--[tag]
        // @attribute <LocationTag.ray_trace[(range=<#.#>/{200});(return=<{precise}/block/normal>);(default=<{null}/air>);(fluids=<true/{false}>);(nonsolids=<true/{false}>);(entities=<matcher>);(ignore=<entity>|...);(raysize=<#.#>/{0})]>
        // @returns LocationTag
        // @synonyms LocationTag.raycast, LocationTag.raytrace, LocationTag.ray_cast
        // @group world
        // @description
        // Traces a line from this location, in the direction its facing, towards whatever block it hits first, and returns the location of where it hit.
        // This tag has also been referred to as 'cursor_on' or 'precise_cursor_on' in the past.
        // Using 'return=normal' instead replaces the old 'precise_impact_normal' tag.
        // Optionally specify:
        // range: (defaults to 200) a maximum distance (in blocks) to trace before giving up.
        // return: (defaults to precise)
        //     specify "precise" to return the exact location of the hit (if it hits a block, returns a location along the edge of the block -- but if it hits an entity, returns a location along the body of the entity)
        //     "normal" to return the normal vector of the impact location,
        //     or "block" to return the location of the block hit (if it hits an entity, returns equivalent to 'precise')
        //     For "precise" and "block", the location's direction is set to the direction of the block face hit (or entity bounding box face), pointing exactly away from whatever was hit (the 'normal' direction).
        // default: (defaults to "null")
        //     specify "null" to return null when nothing is hit,
        //     or "air" to return the location of the air at the end of the trace (NOTE: can potentially be in water or other ignored block type, not just literal air).
        // fluids: (defaults to false) specify "true" to count fluids like water as solid, or "false" to ignore them.
        // nonsolids: (defaults to false) specify "true" to count passable blocks (like tallgrass) as solid, or false to ignore them.
        // entities: (defaults to none) specify an entity matcher for entities to count as blocking the trace, "*" for any entity counts, or leave off (or empty) to ignore entities.
        // ignore: (defaults to none) optional list of EntityTags to ignore even if they match the matcher.
        // raysize: (defaults to 0) sets the radius of the ray being used to trace entities (and NOT for blocks!).
        //
        // @example
        // # Destroys whatever solid block the player is looking at.
        // - define target <player.eye_location.ray_trace[return=block]||null>
        // - if <[target]> != null:
        //     - modifyblock <[target]> air
        //
        // @example
        // # Spawns a heart wherever the player is looking, no more than 5 blocks away.
        // - playeffect effect:heart offset:0 at:<player.eye_location.ray_trace[range=5;entities=*;ignore=<player>;fluids=true;nonsolids=true;default=air]>
        //
        // @example
        // # Spawns a line of fire starting at the player's target location and spewing out in the direction of the blockface hit, demonstrating the concept of a normal vector.
        // - define hit at:<player.eye_location.ray_trace[entities=*;ignore=<player>;fluids=true;nonsolids=true]||null>
        // - if <[hit]> != null:
        //     - playeffect effect:flame offset:0 at:<[hit].points_between[<[hit].forward[2]>].distance[0.2]>
        //
        // -->
        tagProcessor.registerTag(LocationTag.class, "ray_trace", (attribute, object) -> {
            if (object.getWorld() == null) {
                return null;
            }
            MapTag input = attribute.inputParameterMap();
            double range = input.getElement("range", "200").asDouble();
            String returnMode = input.getElement("return", "precise").asString();
            String defaultMode = input.getElement("default", "null").asString();
            boolean fluids = input.getElement("fluids", "false").asBoolean();
            boolean nonsolids = input.getElement("nonsolids", "false").asBoolean();
            String entitiesMatcher = input.getElement("entities", "").asString();
            double raySize = input.getElement("raysize", "0").asDouble();
            List<EntityTag> ignore = input.getObjectAs("ignore", ListTag.class, attribute.context, ListTag::new).filter(EntityTag.class, attribute.context);
            HashSet<UUID> ignoreIds = ignore.stream().map(EntityTag::getUUID).collect(Collectors.toCollection(HashSet::new));
            Vector direction = object.getDirection();
            RayTraceResult traced;
            if (entitiesMatcher.isEmpty()) {
                traced = object.getWorld().rayTraceBlocks(object, direction, range, fluids ? FluidCollisionMode.ALWAYS : FluidCollisionMode.NEVER, !nonsolids);
            }
            else {
                traced = object.getWorld().rayTrace(object, direction, range, fluids ? FluidCollisionMode.ALWAYS : FluidCollisionMode.NEVER, !nonsolids, raySize, (e) -> !ignoreIds.contains(e.getUniqueId()) && new EntityTag(e).tryAdvancedMatcher(entitiesMatcher));
            }
            if (traced != null) {
                LocationTag result = null;
                if (CoreUtilities.equalsIgnoreCase(returnMode, "block")) {
                    if (traced.getHitBlock() != null) {
                        result = new LocationTag(traced.getHitBlock().getLocation());
                    }
                    else if (CoreUtilities.equalsIgnoreCase(defaultMode, "air")) {
                        result = new LocationTag(object.getWorld(), traced.getHitPosition());
                    }
                }
                else if (CoreUtilities.equalsIgnoreCase(returnMode, "normal")) {
                    if (traced.getHitBlockFace() != null) {
                        return new LocationTag(traced.getHitBlockFace().getDirection());
                    }
                }
                else {
                    result = new LocationTag(object.getWorld(), traced.getHitPosition());
                }
                if (result != null) {
                    if (traced.getHitBlockFace() != null) {
                        result.setDirection(traced.getHitBlockFace().getDirection());
                    }
                    return result;
                }
            }
            if (CoreUtilities.equalsIgnoreCase(defaultMode, "air")) {
                return object.clone().add(direction.clone().multiply(range));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_impact_normal[(<range>)]>
        // @returns LocationTag
        // @group world
        // @deprecated use "ray_trace[return=normal]" instead.
        // @description
        // Deprecated in favor of <@link tag LocationTag.ray_trace> with input setting [return=normal].
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_impact_normal", (attribute, object) -> {
            BukkitImplDeprecations.locationOldCursorOn.warn(attribute.context);
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 200;
            }
            RayTraceResult traced = object.getWorld().rayTraceBlocks(object, object.getDirection(), range);
            if (traced != null && traced.getHitBlockFace() != null) {
                return new LocationTag(traced.getHitBlockFace().getDirection());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_cursor_on_block[(<range>)]>
        // @returns LocationTag
        // @group world
        // @deprecated use "ray_trace[return=block]" instead.
        // @description
        // Deprecated in favor of <@link tag LocationTag.ray_trace> with input setting [return=block].
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_cursor_on_block", (attribute, object) -> {
            BukkitImplDeprecations.locationOldCursorOn.warn(attribute.context);
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 200;
            }
            RayTraceResult traced = object.getWorld().rayTraceBlocks(object, object.getDirection(), range);
            if (traced != null && traced.getHitBlock() != null) {
                return new LocationTag(traced.getHitBlock().getLocation());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_cursor_on[(<range>)]>
        // @returns LocationTag
        // @group world
        // @deprecated use "ray_trace" instead.
        // @description
        // Deprecated in favor of <@link tag LocationTag.ray_trace> with all default settings (no input other than optionally the range).
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_cursor_on", (attribute, object) -> {
            BukkitImplDeprecations.locationOldCursorOn.warn(attribute.context);
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 200;
            }
            RayTraceResult traced = object.getWorld().rayTraceBlocks(object, object.getDirection(), range);
            if (traced != null && traced.getHitBlock() != null) {
                return new LocationTag(traced.getHitBlock().getWorld(), traced.getHitPosition());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_target_list[<range>]>
        // @returns ListTag(EntityTag)
        // @group world
        // @description
        // Returns a list of all entities this location is pointing directly at (using precise ray trace logic), up to a given range limit.
        // -->
        tagProcessor.registerTag(ListTag.class, "precise_target_list", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double range = attribute.getDoubleParam();
            HashSet<UUID> hitIDs = new HashSet<>();
            ListTag result = new ListTag();
            Vector direction = object.getDirection();
            World world = object.getWorld();
            while (true) {
                RayTraceResult hit = world.rayTrace(object, direction, range, FluidCollisionMode.NEVER, true, 0, (e) -> !hitIDs.contains(e.getUniqueId()));
                if (hit == null || hit.getHitEntity() == null) {
                    return result;
                }
                hitIDs.add(hit.getHitEntity().getUniqueId());
                result.addObject(new EntityTag(hit.getHitEntity()));
            }
        });

        // <--[tag]
        // @attribute <LocationTag.precise_target[(<range>)]>
        // @returns EntityTag
        // @group world
        // @description
        // Returns the entity this location is pointing at, using precise ray trace logic.
        // Optionally, specify a maximum range to find the entity from (defaults to 100).
        // -->
        tagProcessor.registerTag(EntityFormObject.class, "precise_target", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 100;
            }
            RayTraceResult result;
            // <--[tag]
            // @attribute <LocationTag.precise_target[(<range>)].type[<entity_type>|...]>
            // @returns EntityTag
            // @group world
            // @description
            // Returns the entity this location is pointing at, using precise ray trace logic.
            // Optionally, specify a maximum range to find the entity from (defaults to 100).
            // Accepts a list of types to trace against (types not listed will be ignored).
            // -->
            if (attribute.startsWith("type", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                Set<EntityType> types = new HashSet<>();
                for (String str : attribute.paramAsType(ListTag.class)) {
                    types.add(EntityTag.valueOf(str, attribute.context).getBukkitEntityType());
                }
                result = object.getWorld().rayTrace(object, object.getDirection(), range, FluidCollisionMode.NEVER, true, 0, (e) -> types.contains(e.getType()));
            }
            else {
                result = object.getWorld().rayTrace(object, object.getDirection(), range, FluidCollisionMode.NEVER, true, 0, null);
            }
            if (result != null && result.getHitEntity() != null) {
                return new EntityTag(result.getHitEntity()).getDenizenObject();
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_target_position[(<range>)]>
        // @returns LocationTag
        // @group world
        // @deprecated use "ray_trace[entities=*]" instead.
        // @description
        // Deprecated in favor of <@link tag LocationTag.ray_trace> with input of [entities=*]
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_target_position", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 100;
            }
            BukkitImplDeprecations.locationOldCursorOn.warn(attribute.context);
            RayTraceResult result;
            // <--[tag]
            // @attribute <LocationTag.precise_target_position[(<range>)].type[<entity_type>|...]>
            // @returns LocationTag
            // @group world
            // @deprecated use "ray_trace[entities=(your_types)]" instead.
            // @description
            // Deprecated in favor of <@link tag LocationTag.ray_trace> with "entities=" set to your input types as a matcher.
            // -->
            if (attribute.startsWith("type", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                Set<EntityType> types = new HashSet<>();
                for (String str : attribute.paramAsType(ListTag.class)) {
                    types.add(EntityTag.valueOf(str, attribute.context).getBukkitEntityType());
                }
                result = object.getWorld().rayTrace(object, object.getDirection(), range, FluidCollisionMode.NEVER, true, 0, (e) -> types.contains(e.getType()));
            }
            else {
                result = object.getWorld().rayTrace(object, object.getDirection(), range, FluidCollisionMode.NEVER, true, 0, null);
            }
            if (result != null) {
                return new LocationTag(object.getWorld(), result.getHitPosition());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.points_between[<location>]>
        // @returns ListTag(LocationTag)
        // @group math
        // @description
        // Finds all locations between this location and another, separated by 1 block-width each.
        // -->
        tagProcessor.registerTag(ListTag.class, "points_between", (attribute, object) -> {
            LocationTag target = attribute.paramAsType(LocationTag.class);
            if (target == null) {
                return null;
            }

            // <--[tag]
            // @attribute <LocationTag.points_between[<location>].distance[<#.#>]>
            // @returns ListTag(LocationTag)
            // @group math
            // @description
            // Finds all locations between this location and another, separated by the specified distance each.
            // -->
            double rad = 1d;
            if (attribute.startsWith("distance", 2)) {
                rad = attribute.getDoubleContext(2);
                attribute.fulfill(1);
                if (rad < 0.000001) {
                    attribute.echoError("Distance value cannot be zero or negative.");
                    return null;
                }
            }
            ListTag list = new ListTag();
            org.bukkit.util.Vector rel = target.toVector().subtract(object.toVector());
            double len = rel.length();
            if (len < 0.000001) {
                return list;
            }
            if (len / rad > Settings.cache_blockTagsMaxBlocks) {
                len = rad * Settings.cache_blockTagsMaxBlocks;
            }
            rel = rel.multiply(1d / len);
            for (double i = 0d; i <= len; i += rad) {
                list.addObject(new LocationTag(object.clone().add(rel.clone().multiply(i))));
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.facing_blocks[(<#>)]>
        // @returns ListTag(LocationTag)
        // @group world
        // @description
        // Finds all block locations in the direction this location is facing,
        // optionally with a custom range (default is 100).
        // For example a location at 0,0,0 facing straight up
        // will include 0,1,0 0,2,0 and so on.
        // This is an imperfect block line tracer.
        // -->
        tagProcessor.registerTag(ListTag.class, "facing_blocks", (attribute, object) -> {
            int range = attribute.getIntParam();
            if (range < 1) {
                range = 100;
            }
            ListTag list = new ListTag();
            try {
                NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());
                BlockIterator iterator = new BlockIterator(object, 0, range);
                while (iterator.hasNext()) {
                    list.addObject(new LocationTag(iterator.next().getLocation()));
                }
            }
            finally {
                NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.line_of_sight[<location>]>
        // @returns ElementTag(Boolean)
        // @group math
        // @description
        // Returns whether the specified location is within this location's line of sight.
        // -->
        tagProcessor.registerTag(ElementTag.class, "line_of_sight", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag location = attribute.paramAsType(LocationTag.class);
            if (location != null) {
                try {
                    NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());
                    return new ElementTag(NMSHandler.entityHelper.canTrace(object.getWorld(), object.toVector(), location.toVector()));
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.direction[(<location>)]>
        // @returns ElementTag
        // @group math
        // @description
        // Returns the compass direction between two locations.
        // If no second location is specified, returns the direction of the location.
        // Example returns include "north", "southwest", ...
        // -->
        tagProcessor.registerTag(ObjectTag.class, "direction", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.direction.vector>
            // @returns LocationTag
            // @group math
            // @description
            // Returns the location's direction as a one-length vector.
            // -->
            if (attribute.startsWith("vector", 2)) {
                attribute.fulfill(1);
                return new LocationTag(object.getWorld(), object.getDirection());
            }
            // Get the cardinal direction from this location to another
            if (attribute.hasParam() && LocationTag.matches(attribute.getParam())) {
                // Subtract this location's vector from the other location's vector,
                // not the other way around
                LocationTag target = attribute.paramAsType(LocationTag.class);

                // <--[tag]
                // @attribute <LocationTag.direction[<location>].yaw>
                // @returns ElementTag(Decimal)
                // @group math
                // @description
                // Returns the yaw direction between two locations.
                // -->
                if (attribute.startsWith("yaw", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(EntityHelper.normalizeYaw(NMSHandler.entityHelper.getYaw
                            (target.toVector().subtract(object.toVector())
                                    .normalize())));
                }
                else {
                    return new ElementTag(NMSHandler.entityHelper.getCardinal(NMSHandler.entityHelper.getYaw
                            (target.toVector().subtract(object.toVector())
                                    .normalize())));
                }
            }
            // Get a cardinal direction from this location's yaw
            else {
                return new ElementTag(NMSHandler.entityHelper.getCardinal(object.getYaw()));
            }
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_yaw[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location with the yaw rotated the specified amount (eg 180 to face the location backwards).
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_yaw", (attribute, object) -> {
            LocationTag loc = LocationTag.valueOf(object.identify(), attribute.context).clone();
            loc.setYaw(loc.getYaw() + (float) attribute.getDoubleParam());
            return loc;
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_pitch[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location with the pitch rotated the specified amount. Note that this is capped to +/- 90.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_pitch", (attribute, object) -> {
            LocationTag loc = LocationTag.valueOf(object.identify(), attribute.context).clone();
            loc.setPitch(Math.max(-90, Math.min(90, loc.getPitch() + (float) attribute.getDoubleParam())));
            return loc;
        });

        // <--[tag]
        // @attribute <LocationTag.face[<location>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a location containing a yaw/pitch that point from the current location
        // to the target location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "face", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            Location two = attribute.paramAsType(LocationTag.class);
            return new LocationTag(NMSHandler.entityHelper.faceLocation(object, two));
        });

        // <--[tag]
        // @attribute <LocationTag.facing[<entity>/<location>]>
        // @returns ElementTag(Boolean)
        // @group math
        // @description
        // Returns whether the location's yaw is facing another entity or location, within a limit of 45 degrees of yaw.
        // -->
        tagProcessor.registerTag(ElementTag.class, "facing", (attribute, object) -> {
            if (attribute.hasParam()) {

                // The default number of degrees if there is no degrees attribute
                int degrees = 45;
                LocationTag facingLoc;
                if (LocationTag.matches(attribute.getParam())) {
                    facingLoc = attribute.paramAsType(LocationTag.class);
                }
                else if (EntityTag.matches(attribute.getParam())) {
                    facingLoc = attribute.paramAsType(EntityTag.class).getLocation();
                }
                else {
                    attribute.echoError("Tag location.facing[...] was given an invalid facing target.");
                    return null;
                }

                // <--[tag]
                // @attribute <LocationTag.facing[<entity>/<location>].degrees[<#>(,<#>)]>
                // @returns ElementTag(Boolean)
                // @group math
                // @description
                // Returns whether the location's yaw is facing another
                // entity or location, within a specified degree range.
                // Optionally specify a pitch limit as well.
                // -->
                if (attribute.startsWith("degrees", 2) && attribute.hasContext(2)) {
                    String context = attribute.getContext(2);
                    attribute.fulfill(1);
                    if (context.contains(",")) {
                        String yaw = context.substring(0, context.indexOf(','));
                        String pitch = context.substring(context.indexOf(',') + 1);
                        degrees = Integer.parseInt(yaw);
                        int pitchDegrees = Integer.parseInt(pitch);
                        return new ElementTag(NMSHandler.entityHelper.isFacingLocation(object, facingLoc, degrees, pitchDegrees));
                    }
                    else {
                        degrees = Integer.parseInt(context);
                    }
                }

                return new ElementTag(NMSHandler.entityHelper.isFacingLocation(object, facingLoc, degrees));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.pitch>
        // @returns ElementTag(Decimal)
        // @group identity
        // @description
        // Returns the pitch of the object at the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "pitch", (attribute, object) -> {
            return new ElementTag(object.getPitch());
        });

        // <--[tag]
        // @attribute <LocationTag.with_pose[<entity>/<pitch>,<yaw>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns the location with pitch and yaw.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_pose", (attribute, object) -> {
            String context = attribute.getParam();
            float pitch = 0f;
            float yaw = 0f;
            if (EntityTag.matches(context)) {
                EntityTag ent = EntityTag.valueOf(context, attribute.context);
                if (ent.isSpawnedOrValidForTag()) {
                    pitch = ent.getBukkitEntity().getLocation().getPitch();
                    yaw = ent.getBukkitEntity().getLocation().getYaw();
                }
            }
            else if (context.split(",").length == 2) {
                String[] split = context.split(",");
                pitch = Float.parseFloat(split[0]);
                yaw = Float.parseFloat(split[1]);
            }
            LocationTag loc = object.clone();
            loc.setPitch(pitch);
            loc.setYaw(yaw);
            return loc;
        });

        // <--[tag]
        // @attribute <LocationTag.yaw>
        // @returns ElementTag(Decimal)
        // @group identity
        // @description
        // Returns the normalized yaw of the object at the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "yaw", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.yaw.simple>
            // @returns ElementTag
            // @description
            // Returns the yaw as 'North', 'South', 'East', or 'West'.
            // -->
            if (attribute.startsWith("simple", 2)) {
                attribute.fulfill(1);
                float yaw = EntityHelper.normalizeYaw(object.getYaw());
                if (yaw < 45) {
                    return new ElementTag("South");
                }
                else if (yaw < 135) {
                    return new ElementTag("West");
                }
                else if (yaw < 225) {
                    return new ElementTag("North");
                }
                else if (yaw < 315) {
                    return new ElementTag("East");
                }
                else {
                    return new ElementTag("South");
                }
            }

            // <--[tag]
            // @attribute <LocationTag.yaw.raw>
            // @returns ElementTag(Decimal)
            // @group identity
            // @description
            // Returns the raw (un-normalized) yaw of the object at the location.
            // -->
            if (attribute.startsWith("raw", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getYaw());
            }
            return new ElementTag(EntityHelper.normalizeYaw(object.getYaw()));
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_x[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location-vector rotated around the x axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[0,1,0].rotate_around_x[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_x", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double[] values = getRotatedAroundX(attribute.getDoubleParam(), object.getY(), object.getZ());
            Location location = object.clone();
            location.setY(values[0]);
            location.setZ(values[1]);
            return new LocationTag(location);
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_y[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location-vector rotated around the y axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[1,0,0].rotate_around_y[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_y", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double[] values = getRotatedAroundY(attribute.getDoubleParam(), object.getX(), object.getZ());
            Location location = object.clone();
            location.setX(values[0]);
            location.setZ(values[1]);
            return new LocationTag(location);
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_z[<#.#>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location-vector rotated around the z axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[1,0,0].rotate_around_z[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_z", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double[] values = getRotatedAroundZ(attribute.getDoubleParam(), object.getX(), object.getY());
            Location location = object.clone();
            location.setX(values[0]);
            location.setY(values[1]);
            return new LocationTag(location);
        });

        // <--[tag]
        // @attribute <LocationTag.points_around_x[radius=<#.#>;points=<#>]>
        // @returns ListTag(LocationTag)
        // @group math
        // @description
        // Returns a list of points in a circle around a location's x axis with the specified radius and number of points.
        // For example: <player.location.points_around_x[radius=10;points=16]>
        // -->
        tagProcessor.registerTag(ListTag.class, "points_around_x", (attribute, object) -> {
            double[] values = parsePointsAroundArgs(attribute);
            if (values == null) {
                return null;
            }
            double angle = 2 * Math.PI / values[1];
            ListTag points = new ListTag();
            for (int i = 0; i < values[1]; i++) {
                double[] result = getRotatedAroundX(angle * i, values[0], 0);
                points.addObject(object.clone().add(0, result[0], result[1]));
            }
            return points;
        });

        // <--[tag]
        // @attribute <LocationTag.points_around_y[radius=<#.#>;points=<#>]>
        // @returns ListTag(LocationTag)
        // @group math
        // @description
        // Returns a list of points in a circle around a location's y axis with the specified radius and number of points.
        // For example: <player.location.points_around_y[radius=10;points=16]>
        // -->
        tagProcessor.registerTag(ListTag.class, "points_around_y", (attribute, object) -> {
            double[] values = parsePointsAroundArgs(attribute);
            if (values == null) {
                return null;
            }
            double angle = 2 * Math.PI / values[1];
            ListTag points = new ListTag();
            for (int i = 0; i < values[1]; i++) {
                double[] result = getRotatedAroundY(angle * i, values[0], 0);
                points.addObject(object.clone().add(result[0], 0, result[1]));
            }
            return points;
        });

        // <--[tag]
        // @attribute <LocationTag.points_around_z[radius=<#.#>;points=<#>]>
        // @returns ListTag(LocationTag)
        // @group math
        // @description
        // Returns a list of points in a circle around a location's z axis with the specified radius and number of points.
        // For example: <player.location.points_around_z[radius=10;points=16]>
        // -->
        tagProcessor.registerTag(ListTag.class, "points_around_z", (attribute, object) -> {
            double[] values = parsePointsAroundArgs(attribute);
            if (values == null) {
                return null;
            }
            double angle = 2 * Math.PI / values[1];
            ListTag points = new ListTag();
            for (int i = 0; i < values[1]; i++) {
                double[] result = getRotatedAroundZ(angle * i, 0, values[0]);
                points.addObject(object.clone().add(result[0], result[1], 0));
            }
            return points;
        });

        // <--[tag]
        // @attribute <LocationTag.flood_fill[<limit>]>
        // @returns ListTag(LocationTag)
        // @group world
        // @description
        // Returns the set of all blocks, starting at the given location,
        // that can be directly reached in a way that only travels through blocks of the same type as the starting block.
        // For example, if starting at an air block inside an enclosed building, this will return all air blocks inside the building (but none outside, and no non-air blocks).
        // As another example, if starting on a block of iron_ore in the ground, this will find all other blocks of iron ore that are part of the same vein.
        // This will not travel diagonally, only the 6 cardinal directions (N/E/S/W/Up/Down).
        // As this is potentially infinite should there be any opening however small, a limit must be given.
        // The limit value can be: a CuboidTag, an EllipsoidTag, or an ElementTag(Decimal) to use as a radius.
        // Note that the returned list will not be in any particular order.
        // -->
        tagProcessor.registerTag(ListTag.class, "flood_fill", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            AreaContainmentObject area = CuboidTag.valueOf(attribute.getParam(), CoreUtilities.noDebugContext);
            if (area == null) {
                area = EllipsoidTag.valueOf(attribute.getParam(), CoreUtilities.noDebugContext);
            }
            if (area == null) {
                double radius = attribute.getDoubleParam();
                if (radius <= 0) {
                    return null;
                }
                area = new EllipsoidTag(object.clone(), new LocationTag(object.getWorld(), radius, radius, radius));
            }
            FloodFiller flooder = new FloodFiller();
            NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());
            try {
                if (object.getWorld() == null) {
                    attribute.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                    return null;
                }
                if (!object.isChunkLoaded()) {
                    attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                    return null;
                }

                // <--[tag]
                // @attribute <LocationTag.flood_fill[<limit>].types[<matcher>]>
                // @returns ListTag(LocationTag)
                // @group world
                // @description
                // Returns the set of all blocks, starting at the given location,
                // that can be directly reached in a way that only travels through blocks that match the given LocationTag matcher.
                // This will not travel diagonally, only the 6 cardinal directions (N/E/S/W/Up/Down).
                // As this is potentially infinite for some block types (like air, stone, etc.) should there be any opening however small, a limit must be given.
                // The limit value can be: a CuboidTag, an EllipsoidTag, or an ElementTag(Decimal) to use as a radius.
                // Note that the returned list will not be in any particular order.
                // The result will be an empty list if the block at the start location is not one of the input materials.
                // -->
                if (attribute.startsWith("types", 2) && attribute.hasContext(2)) {
                    flooder.matcher = attribute.getContext(2);
                    attribute.fulfill(1);
                }
                else {
                    flooder.requiredMaterial = object.getBlock().getType();
                }
                flooder.run(object, area);
            }
            finally {
                NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
            }
            return new ListTag((Collection<LocationTag>) flooder.result);
        });


        // <--[tag]
        // @attribute <LocationTag.find_nearest_biome[<biome>]>
        // @returns LocationTag
        // @group finding
        // @description
        // Returns the location of the nearest block of the given biome type (or null).
        // Warning: may be extremely slow to process. Use with caution.
        // -->
        tagProcessor.registerTag(LocationTag.class, "find_nearest_biome", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            BiomeTag biome = attribute.paramAsType(BiomeTag.class);
            if (biome == null) {
                attribute.echoError("Invalid biome input.");
                return null;
            }
            Location result = NMSHandler.worldHelper.getNearestBiomeLocation(object, biome);
            if (result == null) {
                return null;
            }
            return new LocationTag(result);
        });

        // <--[tag]
        // @attribute <LocationTag.find_blocks_flagged[<flag_name>].within[<#>]>
        // @returns ListTag(LocationTag)
        // @group finding
        // @description
        // Returns a list of blocks that have the specified flag within a radius.
        // Note: current implementation measures the center of nearby block's distance from the exact given location.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // Searches the internal flag lists, rather than through all possible blocks.
        // -->
        tagProcessor.registerTag(ListTag.class, "find_blocks_flagged", (attribute, object) -> {
            if (!attribute.hasParam() || !attribute.startsWith("within", 2) || !attribute.hasContext(2)) {
                attribute.echoError("find_blocks_flagged[...].within[...] tag malformed.");
                return null;
            }
            String flagName = CoreUtilities.toLowerCase(attribute.getParam());
            attribute.fulfill(1);
            double radius = attribute.getDoubleParam();
            if (!object.isChunkLoadedSafe()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            double minPossibleX = object.getX() - radius;
            double minPossibleZ = object.getZ() - radius;
            double maxPossibleX = object.getX() + radius;
            double maxPossibleZ = object.getZ() + radius;
            int minChunkX = (int) Math.floor(minPossibleX / 16);
            int minChunkZ = (int) Math.floor(minPossibleZ / 16);
            int maxChunkX = (int) Math.ceil(maxPossibleX / 16);
            int maxChunkZ = (int) Math.ceil(maxPossibleZ / 16);
            ChunkTag testChunk = new ChunkTag(object);
            final ArrayList<LocationTag> found = new ArrayList<>();
            for (int x = minChunkX; x <= maxChunkX; x++) {
                testChunk.chunkX = x;
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    testChunk.chunkZ = z;
                    testChunk.cachedChunk = null;
                    if (testChunk.isLoadedSafe()) {
                        LocationFlagSearchHelper.getFlaggedLocations(testChunk.getChunkForTag(attribute), flagName, (loc) -> {
                            loc.setX(loc.getX() + 0.5);
                            loc.setY(loc.getY() + 0.5);
                            loc.setZ(loc.getZ() + 0.5);
                            if (Utilities.checkLocation(object, loc, radius)) {
                                found.add(new LocationTag(loc));
                            }
                        });
                    }
                }
            }
            found.sort(object::compare);
            return new ListTag(found);
        });

        // <--[tag]
        // @attribute <LocationTag.find_entities[(<matcher>)].within[<#.#>]>
        // @returns ListTag(EntityTag)
        // @group finding
        // @description
        // Returns a list of entities within a radius, with an optional search parameter for the entity type.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_entities", (attribute, object) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            if (!attribute.startsWith("within", 2) || !attribute.hasContext(2)) {
                return null;
            }
            double radius = attribute.getDoubleContext(2);
            attribute.fulfill(1);
            ListTag found = new ListTag();
            BoundingBox box = BoundingBox.of(object, radius, radius, radius);
            for (Entity entity : new WorldTag(object.getWorld()).getPossibleEntitiesForBoundaryForTag(box)) {
                if (Utilities.checkLocationWithBoundingBox(object, entity, radius)) {
                    EntityTag current = new EntityTag(entity);
                    if (matcher == null || current.tryAdvancedMatcher(matcher)) {
                        found.addObject(current.getDenizenObject());
                    }
                }
            }
            found.objectForms.sort((ent1, ent2) -> object.compare(((EntityFormObject) ent1).getLocation(), ((EntityFormObject) ent2).getLocation()));
            return found;
        });

        // <--[tag]
        // @attribute <LocationTag.find_blocks[(<matcher>)].within[<#.#>]>
        // @returns ListTag(LocationTag)
        // @group finding
        // @description
        // Returns a list of blocks within a radius, with an optional search parameter for the block material.
        // Note: current implementation measures the center of nearby block's distance from the exact given location.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_blocks", (attribute, object) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            if (!attribute.startsWith("within", 2) || !attribute.hasContext(2)) {
                return null;
            }
            double radius = attribute.getDoubleContext(2);
            attribute.fulfill(1);
            ListTag found = new ListTag();
            int max = Settings.blockTagsMaxBlocks();
            int index = 0;
            Location tstart = object.getBlockLocation();
            double tstartY = tstart.getY();
            int radiusInt = (int) Math.ceil(radius);
            fullloop:
            for (int y = -radiusInt; y <= radiusInt; y++) {
                double newY = y + tstartY;
                if (!Utilities.isLocationYSafe(newY, object.getWorld())) {
                    continue;
                }
                for (int x = -radiusInt; x <= radiusInt; x++) {
                    for (int z = -radiusInt; z <= radiusInt; z++) {
                        index++;
                        if (index > max) {
                            break fullloop;
                        }
                        if (Utilities.checkLocation(object, tstart.clone().add(x + 0.5, y + 0.5, z + 0.5), radius)) {
                            if (matcher == null || new LocationTag(tstart.clone().add(x, y, z)).tryAdvancedMatcher(matcher)) {
                                found.addObject(new LocationTag(tstart.clone().add(x, y, z)));
                            }
                        }
                    }
                }
            }
            found.objectForms.sort((loc1, loc2) -> object.compare((LocationTag) loc1, (LocationTag) loc2));
            return found;
        });

        // <--[tag]
        // @attribute <LocationTag.find_tile_entities[(<matcher>)].within[<#.#>]>
        // @returns ListTag(LocationTag)
        // @group finding
        // @description
        // Returns a list of tile-entity blocks within a radius, with an optional search parameter for the block material.
        // This can be more efficient that <@link tag LocationTag.find_blocks.within> when only tile-entity blocks are relevant.
        // Note: current implementation measures the center of nearby block's distance from the exact given location.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_tile_entities", (attribute, object) -> {
            String matcher = attribute.hasParam() ? attribute.getParam() : null;
            if (!attribute.startsWith("within", 2) || !attribute.hasContext(2)) {
                return null;
            }
            attribute.fulfill(1);
            double radius = attribute.getDoubleParam();
            ListTag found = new ListTag();
            int max = Settings.blockTagsMaxBlocks();
            int index = 0;
            if (!object.isChunkLoadedSafe()) {
                attribute.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                return null;
            }
            double minPossibleX = object.getX() - radius;
            double minPossibleZ = object.getZ() - radius;
            double maxPossibleX = object.getX() + radius;
            double maxPossibleZ = object.getZ() + radius;
            int minChunkX = (int) Math.floor(minPossibleX / 16);
            int minChunkZ = (int) Math.floor(minPossibleZ / 16);
            int maxChunkX = (int) Math.ceil(maxPossibleX / 16);
            int maxChunkZ = (int) Math.ceil(maxPossibleZ / 16);
            ChunkTag testChunk = new ChunkTag(object);
            Location refLoc = object.clone();
            fullLoop:
            for (int x = minChunkX; x <= maxChunkX; x++) {
                testChunk.chunkX = x;
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    testChunk.chunkZ = z;
                    testChunk.cachedChunk = null;
                    if (testChunk.isLoadedSafe()) {
                        for (BlockState block : testChunk.getChunkForTag(attribute).getTileEntities()) {
                            if (index++ > max) {
                                break fullLoop;
                            }
                            Location current = block.getLocation(refLoc).add(0.5, 0.5, 0.5);
                            if (Utilities.checkLocation(object, current, radius)) {
                                LocationTag actualLoc = new LocationTag(current);
                                if (matcher == null || actualLoc.tryAdvancedMatcher(matcher)) {
                                    found.addObject(actualLoc);
                                }
                            }
                        }
                    }
                }
            }
            found.objectForms.sort((loc1, loc2) -> object.compare((LocationTag) loc1, (LocationTag) loc2));
            return found;
        });

        // <--[tag]
        // @attribute <LocationTag.find_spawnable_blocks_within[<#.#>]>
        // @returns ListTag(LocationTag)
        // @group finding
        // @description
        // Returns a list of blocks within a radius, that are safe for spawning, with the same logic as <@link tag LocationTag.is_spawnable>.
        // Note: current implementation measures the center of nearby block's distance from the exact given location.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_spawnable_blocks_within", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double radius = attribute.getDoubleParam();
            ListTag found = new ListTag();
            int max = Settings.blockTagsMaxBlocks();
            int index = 0;
            Location tstart = object.getBlockLocation();
            double tstartY = tstart.getY();
            int radiusInt = (int) Math.ceil(radius);
            fullloop:
            for (int y = -radiusInt; y <= radiusInt; y++) {
                double newY = y + tstartY;
                if (!Utilities.isLocationYSafe(newY, object.getWorld())) {
                    continue;
                }
                for (int x = -radiusInt; x <= radiusInt; x++) {
                    for (int z = -radiusInt; z <= radiusInt; z++) {
                        index++;
                        if (index > max) {
                            break fullloop;
                        }
                        Location loc = tstart.clone().add(x + 0.5, y + 0.5, z + 0.5);
                        if (Utilities.checkLocation(object, loc, radius) && SpawnableHelper.isSpawnable(loc)) {
                            found.addObject(new LocationTag(loc.add(0, -0.5, 0)));
                        }
                    }
                }
            }
            found.objectForms.sort((loc1, loc2) -> object.compare((LocationTag) loc1, (LocationTag) loc2));
            return found;
        });

        // <--[tag]
        // @attribute <LocationTag.find_players_within[<#.#>]>
        // @returns ListTag(PlayerTag)
        // @group finding
        // @description
        // Returns a list of players within a radius.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_players_within", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double radius = attribute.getDoubleParam();
            ArrayList<PlayerTag> found = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isDead() && Utilities.checkLocationWithBoundingBox(object, player, radius)) {
                    found.add(new PlayerTag(player));
                }
            }
            found.sort((pl1, pl2) -> object.compare(pl1.getLocation(), pl2.getLocation()));
            return new ListTag(found);
        });

        // <--[tag]
        // @attribute <LocationTag.find_npcs_within[<#.#>]>
        // @returns ListTag(NPCTag)
        // @group finding
        // @description
        // Returns a list of NPCs within a radius.
        // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
        // -->
        tagProcessor.registerTag(ListTag.class, "find_npcs_within", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double radius = attribute.getDoubleParam();
            ArrayList<NPCTag> found = new ArrayList<>();
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.isSpawned() && Utilities.checkLocationWithBoundingBox(object, npc.getEntity(), radius)) {
                    found.add(new NPCTag(npc));
                }
            }
            found.sort((npc1, npc2) -> object.compare(npc1.getLocation(), npc2.getLocation()));
            return new ListTag(found);
        });

        tagProcessor.registerTag(ObjectTag.class, "find", (attribute, object) -> {
            if (!attribute.startsWith("within", 3) || !attribute.hasContext(3)) {
                return null;
            }
            double radius = attribute.getDoubleContext(3);

            if (attribute.startsWith("blocks", 2)) {
                BukkitImplDeprecations.locationFindEntities.warn(attribute.context);
                ArrayList<LocationTag> found = new ArrayList<>();
                List<MaterialTag> materials = new ArrayList<>();
                if (attribute.hasContext(2)) {
                    materials = attribute.contextAsType(2, ListTag.class).filter(MaterialTag.class, attribute.context);
                }
                // Avoid NPE from invalid materials
                if (materials == null) {
                    return null;
                }
                int max = Settings.blockTagsMaxBlocks();
                int index = 0;

                attribute.fulfill(2);
                Location tstart = object.getBlockLocation();
                double tstartY = tstart.getY();
                int radiusInt = (int) radius;

                fullloop:
                for (int x = -radiusInt; x <= radiusInt; x++) {
                    for (int y = -radiusInt; y <= radiusInt; y++) {
                        double newY = y + tstartY;
                        if (!Utilities.isLocationYSafe(newY, object.getWorld())) {
                            continue;
                        }
                        for (int z = -radiusInt; z <= radiusInt; z++) {
                            index++;
                            if (index > max) {
                                break fullloop;
                            }
                            if (Utilities.checkLocation(object, tstart.clone().add(x + 0.5, y + 0.5, z + 0.5), radius)) {
                                if (!materials.isEmpty()) {
                                    for (MaterialTag material : materials) {
                                        if (material.getMaterial() == new LocationTag(tstart.clone().add(x, y, z)).getBlockTypeForTag(attribute)) {
                                            found.add(new LocationTag(tstart.clone().add(x, y, z)));
                                        }
                                    }
                                }
                                else {
                                    found.add(new LocationTag(tstart.clone().add(x, y, z)));
                                }
                            }
                        }
                    }
                }
                found.sort(object::compare);
                return new ListTag(found);
            }

            // <--[tag]
            // @attribute <LocationTag.find.surface_blocks[(<material>|...)].within[<#.#>]>
            // @returns ListTag(LocationTag)
            // @group finding
            // @description
            // Returns a list of matching surface blocks within a radius.
            // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
            // -->
            else if (attribute.startsWith("surface_blocks", 2)) {
                ArrayList<LocationTag> found = new ArrayList<>();
                List<MaterialTag> materials = new ArrayList<>();
                if (attribute.hasContext(2)) {
                    materials = attribute.contextAsType(2, ListTag.class).filter(MaterialTag.class, attribute.context);
                }
                // Avoid NPE from invalid materials
                if (materials == null) {
                    return null;
                }
                int max = Settings.blockTagsMaxBlocks();
                int index = 0;
                attribute.fulfill(2);
                Location blockLoc = object.getBlockLocation();
                Location loc = blockLoc.clone().add(0.5f, 0.5f, 0.5f);

                fullloop:
                for (double x = -(radius); x <= radius; x++) {
                    for (double y = -(radius); y <= radius; y++) {
                        for (double z = -(radius); z <= radius; z++) {
                            index++;
                            if (index > max) {
                                break fullloop;
                            }
                            if (Utilities.checkLocation(loc, blockLoc.clone().add(x + 0.5, y + 0.5, z + 0.5), radius)) {
                                LocationTag l = new LocationTag(blockLoc.clone().add(x, y, z));
                                if (!materials.isEmpty()) {
                                    for (MaterialTag material : materials) {
                                        if (material.getMaterial() == l.getBlockTypeForTag(attribute)) {
                                            if (new LocationTag(l.clone().add(0, 1, 0)).getBlockTypeForTag(attribute) == Material.AIR
                                                    && new LocationTag(l.clone().add(0, 2, 0)).getBlockTypeForTag(attribute) == Material.AIR
                                                    && l.getBlockTypeForTag(attribute) != Material.AIR) {
                                                found.add(new LocationTag(blockLoc.clone().add(x + 0.5, y, z + 0.5)));
                                            }
                                        }
                                    }
                                }
                                else {
                                    if (new LocationTag(l.clone().add(0, 1, 0)).getBlockTypeForTag(attribute) == Material.AIR
                                            && new LocationTag(l.clone().add(0, 2, 0)).getBlockTypeForTag(attribute) == Material.AIR
                                            && l.getBlockTypeForTag(attribute) != Material.AIR) {
                                        found.add(new LocationTag(blockLoc.clone().add(x + 0.5, y, z + 0.5)));
                                    }
                                }
                            }
                        }
                    }
                }
                found.sort(object::compare);
                return new ListTag(found);
            }

            else if (attribute.startsWith("players", 2)) {
                BukkitImplDeprecations.locationFindEntities.warn(attribute.context);
                ArrayList<PlayerTag> found = new ArrayList<>();
                attribute.fulfill(2);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isDead() && Utilities.checkLocationWithBoundingBox(object, player, radius)) {
                        found.add(new PlayerTag(player));
                    }
                }
                found.sort((pl1, pl2) -> object.compare(pl1.getLocation(), pl2.getLocation()));
                return new ListTag(found);
            }

            else if (attribute.startsWith("npcs", 2)) {
                BukkitImplDeprecations.locationFindEntities.warn(attribute.context);
                ArrayList<NPCTag> found = new ArrayList<>();
                attribute.fulfill(2);
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.isSpawned() && Utilities.checkLocationWithBoundingBox(object, npc.getEntity(), radius)) {
                        found.add(new NPCTag(npc));
                    }
                }
                found.sort((npc1, npc2) -> object.compare(npc1.getLocation(), npc2.getLocation()));
                return new ListTag(found);
            }

            else if (attribute.startsWith("entities", 2)) {
                BukkitImplDeprecations.locationFindEntities.warn(attribute.context);
                ListTag ent_list = attribute.hasContext(2) ? attribute.contextAsType(2, ListTag.class) : null;
                ListTag found = new ListTag();
                attribute.fulfill(2);
                for (Entity entity : new WorldTag(object.getWorld()).getEntitiesForTag()) {
                    if (Utilities.checkLocationWithBoundingBox(object, entity, radius)) {
                        EntityTag current = new EntityTag(entity);
                        if (ent_list != null) {
                            for (String ent : ent_list) {
                                if (current.comparedTo(ent)) {
                                    found.addObject(current.getDenizenObject());
                                    break;
                                }
                            }
                        }
                        else {
                            found.addObject(current.getDenizenObject());
                        }
                    }
                }
                found.objectForms.sort((ent1, ent2) -> object.compare(((EntityFormObject) ent1).getLocation(), ((EntityFormObject) ent2).getLocation()));
                return new ListTag(found.objectForms);
            }

            // <--[tag]
            // @attribute <LocationTag.find.living_entities.within[<#.#>]>
            // @returns ListTag(EntityTag)
            // @group finding
            // @description
            // Returns a list of living entities within a radius.
            // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
            // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
            // -->
            else if (attribute.startsWith("living_entities", 2)) {
                ListTag found = new ListTag();
                attribute.fulfill(2);
                BoundingBox box = BoundingBox.of(object, radius, radius, radius);
                for (Entity entity : new WorldTag(object.getWorld()).getPossibleEntitiesForBoundaryForTag(box)) {
                    if (entity instanceof LivingEntity
                            && Utilities.checkLocationWithBoundingBox(object, entity, radius)) {
                        found.addObject(new EntityTag(entity).getDenizenObject());
                    }
                }
                found.objectForms.sort((ent1, ent2) -> object.compare(((EntityFormObject) ent1).getLocation(), ((EntityFormObject) ent2).getLocation()));
                return new ListTag(found.objectForms);
            }

            // <--[tag]
            // @attribute <LocationTag.find.structure[<type>].within[<#.#>]>
            // @returns LocationTag
            // @group finding
            // @description
            // Returns the location of the nearest structure of the given type, within a maximum radius.
            // To get a list of valid structure types, use <@link tag server.structure_types>.
            // Note that structure type names are case sensitive, and likely to be all-lowercase in most cases.
            // -->
            else if (attribute.startsWith("structure", 2) && attribute.hasContext(2)) {
                String typeName = attribute.getContext(2);
                StructureType type = StructureType.getStructureTypes().get(typeName);
                if (type == null) {
                    attribute.echoError("Invalid structure type '" + typeName + "'.");
                    return null;
                }
                attribute.fulfill(2);
                Location result = object.getWorld().locateNearestStructure(object, type, (int) radius, false);
                if (result == null) {
                    return null;
                }
                return new LocationTag(result);
            }

            // <--[tag]
            // @attribute <LocationTag.find.unexplored_structure[<type>].within[<#.#>]>
            // @returns LocationTag
            // @group finding
            // @description
            // Returns the location of the nearest unexplored structure of the given type, within a maximum radius.
            // To get a list of valid structure types, use <@link tag server.structure_types>.
            // Note that structure type names are case sensitive, and likely to be all-lowercase in most cases.
            // -->
            else if (attribute.startsWith("unexplored_structure", 2) && attribute.hasContext(2)) {
                String typeName = attribute.getContext(2);
                StructureType type = StructureType.getStructureTypes().get(typeName);
                if (type == null) {
                    attribute.echoError("Invalid structure type '" + typeName + "'.");
                    return null;
                }
                attribute.fulfill(2);
                Location result = object.getWorld().locateNearestStructure(object, type, (int) radius, true);
                if (result == null) {
                    return null;
                }
                return new LocationTag(result);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.find_path[<location>]>
        // @returns ListTag(LocationTag)
        // @group finding
        // @description
        // Returns a full list of points along the path from this location to the given location.
        // Uses a max range of 100 blocks from the start.
        // -->
        tagProcessor.registerTag(ListTag.class, "find_path", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag two = attribute.paramAsType(LocationTag.class);
            if (two == null) {
                return null;
            }
            List<LocationTag> locs = PathFinder.getPath(object, two);
            ListTag list = new ListTag();
            for (LocationTag loc : locs) {
                list.addObject(loc);
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.formatted>
        // @returns ElementTag
        // @group identity
        // @description
        // Returns the formatted version of the LocationTag.
        // In the format: X 'x.x', Y 'y.y', Z 'z.z', in world 'world'
        // For example: X '1.0', Y '2.0', Z '3.0', in world 'world_nether'
        // -->
        tagProcessor.registerTag(ElementTag.class, "formatted", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.formatted.citizens>
            // @returns ElementTag
            // @group identity
            // @description
            // Returns the location formatted for a Citizens command.
            // In the format: x.x:y.y:z.z:world
            // For example: 1.0:2.0:3.0:world_nether
            // -->
            if (attribute.startsWith("citizens", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getX() + ":" + object.getY() + ":" + object.getZ() + ":" + object.getWorldName());
            }
            return new ElementTag("X '" + object.getX()
                    + "', Y '" + object.getY()
                    + "', Z '" + object.getZ()
                    + "', in world '" + object.getWorldName() + "'");
        });

        // <--[tag]
        // @attribute <LocationTag.chunk>
        // @returns ChunkTag
        // @group identity
        // @description
        // Returns the chunk that this location belongs to.
        // -->
        tagProcessor.registerTag(ChunkTag.class, "chunk", (attribute, object) -> {
            return new ChunkTag(object);
        }, "get_chunk");

        // <--[tag]
        // @attribute <LocationTag.raw>
        // @returns ElementTag
        // @group identity
        // @description
        // Returns the raw representation of this location, without any note name.
        // -->
        tagProcessor.registerTag(ElementTag.class, "raw", (attribute, object) -> {
            return new ElementTag(object.identifyRaw());
        });

        // <--[tag]
        // @attribute <LocationTag.world>
        // @returns WorldTag
        // @group identity
        // @description
        // Returns the world that the location is in.
        // -->
        tagProcessor.registerTag(WorldTag.class, "world", (attribute, object) -> {
            return WorldTag.mirrorBukkitWorld(object.getWorld());
        });

        // <--[tag]
        // @attribute <LocationTag.x>
        // @returns ElementTag(Decimal)
        // @group identity
        // @description
        // Returns the X coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "x", (attribute, object) -> {
            return new ElementTag(object.getX());
        });

        // <--[tag]
        // @attribute <LocationTag.y>
        // @returns ElementTag(Decimal)
        // @group identity
        // @description
        // Returns the Y coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "y", (attribute, object) -> {
            return new ElementTag(object.getY());
        });

        // <--[tag]
        // @attribute <LocationTag.z>
        // @returns ElementTag(Decimal)
        // @group identity
        // @description
        // Returns the Z coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "z", (attribute, object) -> {
            return new ElementTag(object.getZ());
        });

        // <--[tag]
        // @attribute <LocationTag.xyz>
        // @returns ElementTag
        // @group identity
        // @description
        // Returns the location in "x,y,z" format.
        // For example: 1,2,3
        // World, yaw, and pitch will be excluded from this output.
        // -->
        tagProcessor.registerTag(ElementTag.class, "xyz", (attribute, object) -> {
            return new ElementTag(CoreUtilities.doubleToString(object.getX()) + "," + CoreUtilities.doubleToString(object.getY()) + "," + CoreUtilities.doubleToString(object.getZ()));
        });

        // <--[tag]
        // @attribute <LocationTag.with_x[<number>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed X value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_x", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            output.setX(attribute.getDoubleParam());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_y[<number>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed Y value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_y", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            output.setY(attribute.getDoubleParam());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_z[<number>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed Z value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_z", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            output.setZ(attribute.getDoubleParam());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_yaw[<number>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed yaw value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_yaw", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            output.setYaw((float) attribute.getDoubleParam());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_pitch[<number>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed pitch value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_pitch", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            output.setPitch((float) attribute.getDoubleParam());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_world[<world>]>
        // @returns LocationTag
        // @group identity
        // @description
        // Returns a copy of the location with a changed world value.
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_world", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag output = object.clone();
            WorldTag world = attribute.paramAsType(WorldTag.class);
            output.setWorld(world.getWorld());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.note_name>
        // @returns ElementTag
        // @group identity
        // @description
        // Gets the name of a noted LocationTag. If the location isn't noted, this is null.
        // -->
        tagProcessor.registerTag(ElementTag.class, "note_name", (attribute, object) -> {
            String noteName = NoteManager.getSavedId((object));
            if (noteName == null) {
                return null;
            }
            return new ElementTag(noteName);
        }, "notable_name");

        // <--[tag]
        // @attribute <LocationTag.add[<location>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location with the specified coordinates added to it.
        // -->
        tagProcessor.registerTag(LocationTag.class, "add", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String[] ints = attribute.getParam().replace("l@", "").split(",", 4); // TODO: Just LocationTag.valueOf?
            if (ints.length >= 3) {
                if (ArgumentHelper.matchesDouble(ints[0])
                        && ArgumentHelper.matchesDouble(ints[1])
                        && ArgumentHelper.matchesDouble(ints[2])) {
                    return new LocationTag(object.clone().add(Double.parseDouble(ints[0]),
                            Double.parseDouble(ints[1]),
                            Double.parseDouble(ints[2])));
                }
            }
            if (LocationTag.matches(attribute.getParam())) {
                return object.clone().add(attribute.paramAsType(LocationTag.class));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.sub[<location>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location with the specified coordinates subtracted from it.
        // -->
        tagProcessor.registerTag(LocationTag.class, "sub", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String[] ints = attribute.getParam().replace("l@", "").split(",", 4); // TODO: Just LocationTag.valueOf?
            if (ints.length == 3 || ints.length == 4) {
                if (ArgumentHelper.matchesDouble(ints[0])
                        && ArgumentHelper.matchesDouble(ints[1])
                        && ArgumentHelper.matchesDouble(ints[2])) {
                    return new LocationTag(object.clone().subtract(Double.parseDouble(ints[0]),
                            Double.parseDouble(ints[1]),
                            Double.parseDouble(ints[2])));
                }
            }
            if (LocationTag.matches(attribute.getParam())) {
                return new LocationTag(object.clone().subtract(attribute.paramAsType(LocationTag.class)));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.mul[<length>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location multiplied by the specified length.
        // -->
        tagProcessor.registerTag(LocationTag.class, "mul", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new LocationTag(object.clone().multiply(Double.parseDouble(attribute.getParam())));
        });

        // <--[tag]
        // @attribute <LocationTag.div[<length>]>
        // @returns LocationTag
        // @group math
        // @description
        // Returns the location divided by the specified length.
        // -->
        tagProcessor.registerTag(LocationTag.class, "div", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new LocationTag(object.clone().multiply(1D / Double.parseDouble(attribute.getParam())));
        });

        // <--[tag]
        // @attribute <LocationTag.normalize>
        // @returns LocationTag
        // @group math
        // @description
        // Returns a 1-length vector in the same direction as this vector location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "normalize", (attribute, object) -> {
            double len = Math.sqrt(Math.pow(object.getX(), 2) + Math.pow(object.getY(), 2) + Math.pow(object.getZ(), 2));
            if (len == 0) {
                len = 1;
            }
            return new LocationTag(object.clone().multiply(1D / len));
        });

        // <--[tag]
        // @attribute <LocationTag.vector_length>
        // @returns ElementTag(Decimal)
        // @synonyms LocationTag.magnitude
        // @group math
        // @description
        // Returns the 3D length of the vector/location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "vector_length", (attribute, object) -> {
            return new ElementTag(Math.sqrt(Math.pow(object.getX(), 2) + Math.pow(object.getY(), 2) + Math.pow(object.getZ(), 2)));
        });

        // <--[tag]
        // @attribute <LocationTag.vector_to_face>
        // @returns ElementTag
        // @description
        // Returns the name of the BlockFace represented by a normal vector.
        // Result can be any of the following:
        // NORTH, EAST, SOUTH, WEST, UP, DOWN, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST,
        // WEST_NORTH_WEST, NORTH_NORTH_WEST, NORTH_NORTH_EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST,
        // SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST, WEST_SOUTH_WEST, SELF
        // -->
        tagProcessor.registerTag(ElementTag.class, "vector_to_face", (attribute, object) -> {
            BlockFace face = Utilities.faceFor(object.toVector());
            if (face != null) {
                return new ElementTag(face);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.distance_squared[<location>]>
        // @returns ElementTag(Decimal)
        // @group math
        // @description
        // Returns the distance between 2 locations, squared.
        // -->
        tagProcessor.registerTag(ElementTag.class, "distance_squared", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (LocationTag.matches(attribute.getParam())) {
                LocationTag toLocation = attribute.paramAsType(LocationTag.class);
                if (object.getWorldName() == null) {
                    return new ElementTag(object.toVector().distanceSquared(toLocation.toVector()));
                }
                if (!object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                    attribute.echoError("Can't measure distance between two different worlds!");
                    return null;
                }
                return new ElementTag(object.distanceSquared(toLocation));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.distance[<location>]>
        // @returns ElementTag(Decimal)
        // @group math
        // @description
        // Returns the distance between 2 locations.
        // -->
        tagProcessor.registerTag(ElementTag.class, "distance", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (LocationTag.matches(attribute.getParam())) {
                LocationTag toLocation = attribute.paramAsType(LocationTag.class);

                // <--[tag]
                // @attribute <LocationTag.distance[<location>].horizontal>
                // @returns ElementTag(Decimal)
                // @group math
                // @description
                // Returns the horizontal distance between 2 locations.
                // -->
                if (attribute.startsWith("horizontal", 2)) {

                    // <--[tag]
                    // @attribute <LocationTag.distance[<location>].horizontal.multiworld>
                    // @returns ElementTag(Decimal)
                    // @group math
                    // @description
                    // Returns the horizontal distance between 2 multiworld locations.
                    // -->
                    if (attribute.startsWith("multiworld", 3)) {
                        attribute.fulfill(2);
                        return new ElementTag(Math.sqrt(Math.pow(object.getX() - toLocation.getX(), 2) +
                                Math.pow(object.getZ() - toLocation.getZ(), 2)));
                    }
                    attribute.fulfill(1);
                    if (object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                        return new ElementTag(Math.sqrt(Math.pow(object.getX() - toLocation.getX(), 2) +
                                Math.pow(object.getZ() - toLocation.getZ(), 2)));
                    }
                }

                // <--[tag]
                // @attribute <LocationTag.distance[<location>].vertical>
                // @returns ElementTag(Decimal)
                // @group math
                // @description
                // Returns the vertical distance between 2 locations.
                // -->
                else if (attribute.startsWith("vertical", 2)) {

                    // <--[tag]
                    // @attribute <LocationTag.distance[<location>].vertical.multiworld>
                    // @returns ElementTag(Decimal)
                    // @group math
                    // @description
                    // Returns the vertical distance between 2 multiworld locations.
                    // -->
                    if (attribute.startsWith("multiworld", 3)) {
                        attribute.fulfill(2);
                        return new ElementTag(Math.abs(object.getY() - toLocation.getY()));
                    }
                    attribute.fulfill(1);
                    if (object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                        return new ElementTag(Math.abs(object.getY() - toLocation.getY()));
                    }
                }
                if (object.getWorldName() == null) {
                    return new ElementTag(object.toVector().distance(toLocation.toVector()));
                }
                if (!object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                    attribute.echoError("Can't measure distance between two different worlds!");
                    return null;
                }
                return new ElementTag(object.distance(toLocation));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.is_within_border>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the location is within the world border.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_within_border", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().isInside(object));
        });

        // <--[tag]
        // @attribute <LocationTag.is_within[<area>]>
        // @returns ElementTag(Boolean)
        // @group areas
        // @description
        // Returns whether the location is within the specified area (cuboid, ellipsoid, polygon, ...).
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_within", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (EllipsoidTag.matches(attribute.getParam())) {
                EllipsoidTag ellipsoid = attribute.paramAsType(EllipsoidTag.class);
                if (ellipsoid != null) {
                    return new ElementTag(ellipsoid.contains(object));
                }
            }
            else if (PolygonTag.matches(attribute.getParam())) {
                PolygonTag polygon = attribute.paramAsType(PolygonTag.class);
                if (polygon != null) {
                    return new ElementTag(polygon.doesContainLocation(object));
                }
            }
            else {
                CuboidTag cuboid = attribute.paramAsType(CuboidTag.class);
                if (cuboid != null) {
                    return new ElementTag(cuboid.isInsideCuboid(object));
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.to_ellipsoid[<size>]>
        // @returns EllipsoidTag
        // @group areas
        // @description
        // Returns an ellipsoid centered at this location with the specified size.
        // Size input is a vector of x,y,z size.
        // -->
        tagProcessor.registerTag(EllipsoidTag.class, "to_ellipsoid", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("to_ellipsoid[...] tag must have input.");
                return null;
            }
            return new EllipsoidTag(object.clone(), attribute.getParamObject().asType(LocationTag.class, attribute.context));
        });

        // <--[tag]
        // @attribute <LocationTag.to_cuboid[<location>]>
        // @returns CuboidTag
        // @group areas
        // @description
        // Returns a cuboid from this location to the specified location.
        // -->
        tagProcessor.registerTag(CuboidTag.class, "to_cuboid", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("to_cuboid[...] tag must have input.");
                return null;
            }
            return new CuboidTag(object.clone(), attribute.getParamObject().asType(LocationTag.class, attribute.context));
        });

        // <--[tag]
        // @attribute <LocationTag.biome>
        // @mechanism LocationTag.biome
        // @returns BiomeTag
        // @group world
        // @description
        // Returns the biome at the location.
        // -->
        tagProcessor.registerTag(ObjectTag.class, "biome", (attribute, object) -> {
            if (attribute.startsWith("formatted", 2)) {
                BukkitImplDeprecations.locationBiomeFormattedTag.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(CoreUtilities.toLowerCase(object.getBiomeForTag(attribute).getName()).replace('_', ' '));
            }
            return new BiomeTag(object.getBiomeForTag(attribute));
        });

        // <--[tag]
        // @attribute <LocationTag.areas[(<matcher>)]>
        // @returns ListTag(AreaObject)
        // @group areas
        // @description
        // Returns a ListTag of all noted areas that include this location.
        // Optionally, specify a matcher to only include areas that match the given AreaObject matcher text.
        // @example
        // # This example shows all areas at the player's location.
        // - narrate "You are inside: <player.location.areas.parse[note_name].formatted>"
        // @example
        // # This example finds which "Town" area the player is in.
        // - narrate "You are inside the town of <player.location.areas[town_*].first.flag[town_name].if_null[Nowhere!]>"
        // -->
        tagProcessor.registerTag(ListTag.class, "areas", (attribute, object) -> {
            String matcher = attribute.getParam();
            ListTag list = new ListTag();
            NotedAreaTracker.forEachAreaThatContains(object, (area) -> {
                if (matcher == null || area.tryAdvancedMatcher(matcher)) {
                    list.addObject(area);
                }
            });
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.cuboids[(<matcher>)]>
        // @returns ListTag(CuboidTag)
        // @group areas
        // @description
        // Returns a ListTag of all noted CuboidTags that include this location.
        // Optionally, specify a matcher to only include areas that match the given AreaObject matcher text.
        // -->
        tagProcessor.registerTag(ListTag.class, "cuboids", (attribute, object) -> {
            String matcher = attribute.getParam();
            ListTag list = new ListTag();
            NotedAreaTracker.forEachAreaThatContains(object, (area) -> {
                if (area instanceof CuboidTag) {
                    if (matcher == null || area.tryAdvancedMatcher(matcher)) {
                        list.addObject(area);
                    }
                }
            });
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.ellipsoids[(<matcher>)]>
        // @returns ListTag(EllipsoidTag)
        // @group areas
        // @description
        // Returns a ListTag of all noted EllipsoidTags that include this location.
        // Optionally, specify a matcher to only include areas that match the given AreaObject matcher text.
        // -->
        tagProcessor.registerTag(ListTag.class, "ellipsoids", (attribute, object) -> {
            String matcher = attribute.getParam();
            ListTag list = new ListTag();
            NotedAreaTracker.forEachAreaThatContains(object, (area) -> {
                if (area instanceof EllipsoidTag) {
                    if (matcher == null || area.tryAdvancedMatcher(matcher)) {
                        list.addObject(area);
                    }
                }
            });
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.polygons[(<matcher>)]>
        // @returns ListTag(PolygonTag)
        // @group areas
        // @description
        // Returns a ListTag of all noted PolygonTags that include this location.
        // Optionally, specify a matcher to only include areas that match the given AreaObject matcher text.
        // -->
        tagProcessor.registerTag(ListTag.class, "polygons", (attribute, object) -> {
            String matcher = attribute.getParam();
            ListTag list = new ListTag();
            NotedAreaTracker.forEachAreaThatContains(object, (area) -> {
                if (area instanceof PolygonTag) {
                    if (matcher == null || area.tryAdvancedMatcher(matcher)) {
                        list.addObject(area);
                    }
                }
            });
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.is_liquid>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the block at the location is a liquid.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_liquid", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());
                    return new ElementTag(b.isLiquid());
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.light>
        // @returns ElementTag(Number)
        // @group world
        // @description
        // Returns the total amount of light on the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "light", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());

                    // <--[tag]
                    // @attribute <LocationTag.light.blocks>
                    // @returns ElementTag(Number)
                    // @group world
                    // @description
                    // Returns the amount of light from light blocks that is
                    // on the location.
                    // -->
                    if (attribute.startsWith("blocks", 2)) {
                        attribute.fulfill(1);
                        return new ElementTag(object.getBlockForTag(attribute).getLightFromBlocks());
                    }

                    // <--[tag]
                    // @attribute <LocationTag.light.sky>
                    // @returns ElementTag(Number)
                    // @group world
                    // @description
                    // Returns the amount of light from the sky that is
                    // on the location.
                    // -->
                    if (attribute.startsWith("sky", 2)) {
                        attribute.fulfill(1);
                        return new ElementTag(object.getBlockForTag(attribute).getLightFromSky());
                    }
                    return new ElementTag(object.getBlockForTag(attribute).getLightLevel());
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.power>
        // @returns ElementTag(Number)
        // @group world
        // @description
        // Returns the current redstone power level of a block.
        // -->
        tagProcessor.registerTag(ElementTag.class, "power", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.chunkHelper.changeChunkServerThread(object.getWorld());
                    return new ElementTag(object.getBlockForTag(attribute).getBlockPower());
                }
                finally {
                    NMSHandler.chunkHelper.restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.lectern_page>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.lectern_page
        // @group world
        // @description
        // Returns the current page on display in the book on this Lectern block.
        // -->
        tagProcessor.registerTag(ElementTag.class, "lectern_page", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (state instanceof Lectern) {
                return new ElementTag(((Lectern) state).getPage());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.has_loot_table>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.clear_loot_table
        // @group world
        // @description
        // Returns an element indicating whether the chest at this location has a loot-table set.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_loot_table", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (state instanceof Lootable) {
                return new ElementTag(((Lootable) state).getLootTable() != null);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.loot_table_id>
        // @returns ElementTag
        // @mechanism LocationTag.loot_table_id
        // @group world
        // @description
        // Returns an element indicating the minecraft key for the loot-table for the chest at this location (if any).
        // -->
        tagProcessor.registerTag(ElementTag.class, "loot_table_id", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (state instanceof Lootable) {
                LootTable table = ((Lootable) state).getLootTable();
                if (table != null) {
                    return new ElementTag(table.getKey().toString());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.tree_distance>
        // @returns ElementTag(Number)
        // @group world
        // @deprecated Use MaterialTag.distance
        // @description
        // Deprecated in favor of <@link tag MaterialTag.distance>
        // Used like <[location].material.distance>
        // -->
        tagProcessor.registerTag(ElementTag.class, "tree_distance", (attribute, object) -> {
            BukkitImplDeprecations.locationDistanceTag.warn(attribute.context);
            MaterialTag material = new MaterialTag(object.getBlockDataForTag(attribute));
            if (MaterialDistance.describes(material)) {
                return new ElementTag(MaterialDistance.getFrom(material).getDistance());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.command_block_name>
        // @returns ElementTag
        // @mechanism LocationTag.command_block_name
        // @group world
        // @description
        // Returns the name a command block is set to.
        // -->
        tagProcessor.registerTag(ElementTag.class, "command_block_name", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CommandBlock)) {
                return null;
            }
            return new ElementTag(((CommandBlock) object.getBlockStateForTag(attribute)).getName());
        });

        // <--[tag]
        // @attribute <LocationTag.command_block>
        // @returns ElementTag
        // @mechanism LocationTag.command_block
        // @group world
        // @description
        // Returns the command a command block is set to.
        // -->
        tagProcessor.registerTag(ElementTag.class, "command_block", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CommandBlock)) {
                return null;
            }
            return new ElementTag(((CommandBlock) object.getBlockStateForTag(attribute)).getCommand());
        });

        // <--[tag]
        // @attribute <LocationTag.brewing_time>
        // @returns DurationTag
        // @mechanism LocationTag.brewing_time
        // @group world
        // @description
        // Returns the brewing time a brewing stand has left.
        // -->
        tagProcessor.registerTag(DurationTag.class, "brewing_time", (attribute, object) -> {
            return new DurationTag((long) ((BrewingStand) object.getBlockStateForTag(attribute)).getBrewingTime());
        });

        // <--[tag]
        // @attribute <LocationTag.brewing_fuel_level>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.brewing_fuel_level
        // @group world
        // @description
        // Returns the level of fuel a brewing stand has. Each unit of fuel can power one brewing operation.
        // -->
        tagProcessor.registerTag(ElementTag.class, "brewing_fuel_level", (attribute, object) -> {
            return new ElementTag(((BrewingStand) object.getBlockStateForTag(attribute)).getFuelLevel());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_burn_duration>
        // @returns DurationTag
        // @mechanism LocationTag.furnace_burn_duration
        // @group world
        // @description
        // Returns the burn time a furnace has left.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_burn_duration", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getBurnTime());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_burn_time", (attribute, object) -> {
            BukkitImplDeprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getBurnTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_duration>
        // @returns DurationTag
        // @mechanism LocationTag.furnace_cook_duration
        // @group world
        // @description
        // Returns the cook time a furnace has been cooking its current item for.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_cook_duration", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getCookTime());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_cook_time", (attribute, object) -> {
            BukkitImplDeprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_duration_total>
        // @returns DurationTag
        // @mechanism LocationTag.furnace_cook_duration_total
        // @group world
        // @description
        // Returns the total cook time a furnace has left.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_cook_duration_total", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getCookTimeTotal());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_cook_time_total", (attribute, object) -> {
            BukkitImplDeprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTimeTotal());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_tier>
        // @returns ElementTag(Number)
        // @group world
        // @description
        // Returns the tier level of a beacon pyramid (0-4).
        // -->
        tagProcessor.registerTag(ElementTag.class, "beacon_tier", (attribute, object) -> {
            return new ElementTag(((Beacon) object.getBlockStateForTag(attribute)).getTier());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_primary_effect>
        // @returns ElementTag
        // @mechanism LocationTag.beacon_primary_effect
        // @group world
        // @description
        // Returns the primary effect of the beacon. The return is simply a potion effect type name.
        // -->
        tagProcessor.registerTag(ElementTag.class, "beacon_primary_effect", (attribute, object) -> {
            PotionEffect effect = ((Beacon) object.getBlockStateForTag(attribute)).getPrimaryEffect();
            if (effect == null) {
                return null;
            }
            return new ElementTag(effect.getType().getName());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_secondary_effect>
        // @returns ElementTag
        // @mechanism LocationTag.beacon_secondary_effect
        // @group world
        // @description
        // Returns the secondary effect of the beacon. The return is simply a potion effect type name.
        // -->
        tagProcessor.registerTag(ElementTag.class, "beacon_secondary_effect", (attribute, object) -> {
            PotionEffect effect = ((Beacon) object.getBlockStateForTag(attribute)).getSecondaryEffect();
            if (effect == null) {
                return null;
            }
            return new ElementTag(effect.getType().getName());
        });

        // <--[tag]
        // @attribute <LocationTag.attached_to>
        // @returns LocationTag
        // @group world
        // @description
        // Returns the block this block is attached to.
        // (For buttons, levers, signs, torches, etc).
        // -->
        tagProcessor.registerTag(LocationTag.class, "attached_to", (attribute, object) -> {
            BlockFace face = BlockFace.SELF;
            MaterialTag material = new MaterialTag(object.getBlockDataForTag(attribute));
            if (material.getMaterial() == Material.TORCH || material.getMaterial() == Material.REDSTONE_TORCH || material.getMaterial() == Material.SOUL_TORCH) {
                face = BlockFace.DOWN;
            }
            else if (material.getMaterial() == Material.WALL_TORCH || material.getMaterial() == Material.REDSTONE_WALL_TORCH || material.getMaterial() == Material.SOUL_WALL_TORCH) {
                face = ((Directional) material.getModernData()).getFacing().getOppositeFace();
            }
            else if (MaterialSwitchFace.describes(material)) {
                face = MaterialSwitchFace.getFrom(material).getAttachedTo();
            }
            else if (material.hasModernData() && material.getModernData() instanceof org.bukkit.block.data.type.WallSign) {
                face = ((org.bukkit.block.data.type.WallSign) material.getModernData()).getFacing().getOppositeFace();
            }
            else {
                MaterialData data = object.getBlockStateForTag(attribute).getData();
                if (data instanceof Attachable) {
                    face = ((Attachable) data).getAttachedFace();
                }
            }
            if (face != BlockFace.SELF) {
                return new LocationTag(object.getBlockForTag(attribute).getRelative(face).getLocation());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.other_block>
        // @returns LocationTag
        // @group world
        // @description
        // If the location is part of a double-block structure (double chests, double plants, doors, beds, etc),
        // returns the location of the other block in the double-block structure.
        // -->
        tagProcessor.registerTag(LocationTag.class, "other_block", (attribute, object) -> {
            BlockData b = object.getBlockDataForTag(attribute);
            MaterialTag material = new MaterialTag(b);
            if (MaterialHalf.describes(material)) {
                Vector vec = MaterialHalf.getFrom(material).getRelativeBlockVector();
                if (vec != null) {
                    return new LocationTag(object.clone().add(vec));
                }
            }
            attribute.echoError("Block of type " + object.getBlockTypeForTag(attribute).name() + " isn't supported by other_block.");
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.custom_name>
        // @returns ElementTag
        // @mechanism LocationTag.custom_name
        // @group world
        // @description
        // Returns the custom name of this block.
        // Only works for nameable blocks, such as chests and dispensers.
        // -->
        tagProcessor.registerTag(ElementTag.class, "custom_name", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof Nameable) {
                return new ElementTag(PaperAPITools.instance.getCustomName((Nameable) object.getBlockStateForTag(attribute)));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.local_difficulty>
        // @returns ElementTag(Decimal)
        // @group world
        // @description
        // Returns the local difficulty (damage scaler) at the location.
        // This is based internally on multiple factors, including <@link tag ChunkTag.inhabited_time> and <@link tag WorldTag.difficulty>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "local_difficulty", (attribute, object) -> {
            return new ElementTag(NMSHandler.worldHelper.getLocalDifficulty(object));
        });

        // <--[tag]
        // @attribute <LocationTag.jukebox_record>
        // @returns ItemTag
        // @mechanism LocationTag.jukebox_record
        // @group world
        // @description
        // Returns the record item currently inside the jukebox.
        // If there's no record, will return air.
        // -->
        tagProcessor.registerTag(ItemTag.class, "jukebox_record", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Jukebox)) {
                attribute.echoError("'jukebox_record' tag is only valid for jukebox blocks.");
                return null;
            }

            return new ItemTag(((Jukebox) state).getRecord());
        });

        // <--[tag]
        // @attribute <LocationTag.jukebox_is_playing>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.jukebox_play
        // @group world
        // @description
        // Returns whether the jukebox is currently playing a song.
        // -->
        tagProcessor.registerTag(ElementTag.class, "jukebox_is_playing", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Jukebox)) {
                attribute.echoError("'jukebox_is_playing' tag is only valid for jukebox blocks.");
                return null;
            }

            return new ElementTag(((Jukebox) state).isPlaying());
        });

        // <--[tag]
        // @attribute <LocationTag.age>
        // @returns DurationTag
        // @mechanism LocationTag.age
        // @group world
        // @description
        // Returns the age of an end gateway.
        // -->
        tagProcessor.registerTag(DurationTag.class, "age", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof EndGateway)) {
                attribute.echoError("'age' tag is only valid for end_gateway blocks.");
                return null;
            }

            return new DurationTag(((EndGateway) state).getAge());
        });

        // <--[tag]
        // @attribute <LocationTag.is_exact_teleport>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.is_exact_teleport
        // @group world
        // @description
        // Returns whether an end gateway is 'exact teleport' - if false, the destination will be randomly chosen *near* the destination.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_exact_teleport", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof EndGateway)) {
                attribute.echoError("'is_exact_teleport' tag is only valid for end_gateway blocks.");
                return null;
            }

            return new ElementTag(((EndGateway) state).isExactTeleport());
        });

        // <--[tag]
        // @attribute <LocationTag.exit_location>
        // @returns LocationTag
        // @mechanism LocationTag.exit_location
        // @group world
        // @description
        // Returns the exit location of an end gateway block.
        // -->
        tagProcessor.registerTag(LocationTag.class, "exit_location", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof EndGateway)) {
                attribute.echoError("'exit_location' tag is only valid for end_gateway blocks.");
                return null;
            }
            Location loc = ((EndGateway) state).getExitLocation();
            if (loc == null) {
                return null;
            }
            return new LocationTag(loc);
        });

        // <--[tag]
        // @attribute <LocationTag.is_in[<matcher>]>
        // @returns ElementTag(Boolean)
        // @group areas
        // @description
        // Returns whether the location is in an area, using the same logic as an event "in" switch.
        // Invalid input may produce odd error messages, as this is passed through the event system as a fake event.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_in", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(BukkitScriptEvent.inCheckInternal(attribute.context, "is_in tag", object, attribute.getParam(), "is_in tag", "is_in tag"));
        });

        // <--[tag]
        // @attribute <LocationTag.campfire_items>
        // @returns ListTag(ItemTag)
        // @mechanism LocationTag.campfire_items
        // @group world
        // @description
        // Returns a list of items currently in this campfire.
        // This list has air items in empty slots, and is always sized exactly the same as the number of spaces a campfire has.
        // (A standard campfire has exactly 4 slots).
        // -->
        tagProcessor.registerTag(ListTag.class, "campfire_items", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Campfire)) {
                return null;
            }
            Campfire fire = (Campfire) state;
            ListTag output = new ListTag();
            for (int i = 0; i < fire.getSize(); i++) {
                output.addObject(new ItemTag(fire.getItem(i)));
            }
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.is_spawnable>
        // @returns ElementTag(Boolean)
        // @group world
        // @description
        // Returns whether the location is safe to spawn at, for a player or player-like entity.
        // Specifically this verifies that:
        // - The block above this location is air.
        // - The block at this location is non-solid.
        // - The block below this location is solid.
        // - All relevant blocks are not dangerous (like fire, lava, etc.), or unstable/small/awkward (like fences, doors, etc.) or otherwise likely to go wrong (like pressure plates).
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_spawnable", (attribute, object) -> {
            return new ElementTag(SpawnableHelper.isSpawnable(object));
        });

        // <--[tag]
        // @attribute <LocationTag.sign_glowing>
        // @returns ElementTag(Boolean)
        // @mechanism LocationTag.sign_glowing
        // @group world
        // @description
        // Returns whether the location is a Sign block that is glowing.
        // -->
        tagProcessor.registerTag(ElementTag.class, "sign_glowing", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Sign)) {
                attribute.echoError("Location is not a valid Sign block.");
                return null;
            }
            return new ElementTag(((Sign) state).isGlowingText());
        });

        // <--[tag]
        // @attribute <LocationTag.sign_glow_color>
        // @returns ElementTag
        // @mechanism LocationTag.sign_glow_color
        // @group world
        // @description
        // Returns the name of the glow-color of the sign at the location.
        // See also <@link tag LocationTag.sign_glowing>
        // -->
        tagProcessor.registerTag(ElementTag.class, "sign_glow_color", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Sign)) {
                attribute.echoError("Location is not a valid Sign block.");
                return null;
            }
            return new ElementTag(((Sign) state).getColor());
        });

        // <--[tag]
        // @attribute <LocationTag.map_color>
        // @returns ColorTag
        // @group world
        // @description
        // Returns the color of the block at the location, as seen in a map.
        // -->
        tagProcessor.registerTag(ColorTag.class, "map_color", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
            if (block == null) {
                return null;
            }
            return new ColorTag(NMSHandler.blockHelper.getMapColor(block));
        });

        // <--[tag]
        // @attribute <LocationTag.structure_block_data>
        // @returns MapTag
        // @mechanism LocationTag.structure_block_data
        // @group world
        // @description
        // Returns the structure block data of the structure block at the location as a map with the following keys:
        // - author: ElementTag: The name of the structure's creator. set to "?" for most vanilla structures.
        // - integrity: ElementTag(Decimal): The integrity of the structure (0-1). Lower integrity values will result in more blocks being removed when loading a structure.
        // used with the seed to determine which blocks are randomly removed to mimic "decay".
        // - metadata: ElementTag: Only applies in DATA mode, sets specific functions that can be applied to the structure,
        // check the Minecraft wiki (<@link url https://minecraft.gamepedia.com/Structure_Block#Data>) for more information.
        // - mirror: ElementTag: How the structure is mirrored; "NONE", "LEFT_RIGHT", or "FRONT_BACK".
        // - box_position: LocationTag: The position of the structure's bounding box, relative to the position of the structure block. Maximum allowed distance is 48 blocks in any direction.
        // - rotation: ElementTag: The rotation of the structure; "NONE", "CLOCKWISE_90", "CLOCKWISE_180", or "COUNTERCLOCKWISE_90".
        // - seed: ElementTag(Number): The seed used to determine how many blocks are removed upon loading of this structure (see "integrity" for more information).
        // - structure_name: ElementTag: The name of the structure.
        // - size: LocationTag: The size of the structure's bounding box, The maximum structure size is 48,48,48.
        // - mode: ElementTag: The structure block's mode; "CORNER", "DATA", "LOAD", or "SAVE". See also <@link mechanism MaterialTag.mode>.
        // - box_visible: ElementTag(Boolean): Whether the structure's bounding box is visible, only applies in LOAD mode.
        // - ignore_entities: ElementTag(Boolean): Whether entities in the structure are ignored, only applies in SAVE mode.
        // - show_invisible: ElementTag(Boolean): Whether invisible blocks in the structure are shown.
        // -->
        tagProcessor.registerTag(MapTag.class, "structure_block_data", (attribute, object) -> {
            BlockState state = object.getBlockStateForTag(attribute);
            if (!(state instanceof Structure)) {
                attribute.echoError("Location is not a valid Structure block.");
                return null;
            }
            Structure structure = (Structure) state;
            MapTag output = new MapTag();
            output.putObject("author", new ElementTag(structure.getAuthor()));
            output.putObject("integrity", new ElementTag(structure.getIntegrity()));
            output.putObject("metadata", new ElementTag(structure.getMetadata()));
            output.putObject("mirror", new ElementTag(structure.getMirror()));
            output.putObject("box_position", new LocationTag(structure.getRelativePosition()));
            output.putObject("rotation", new ElementTag(structure.getRotation()));
            output.putObject("seed", new ElementTag(structure.getSeed()));
            output.putObject("structure_name", new ElementTag(structure.getStructureName()));
            output.putObject("size", new LocationTag(structure.getStructureSize()));
            output.putObject("mode", new ElementTag(structure.getUsageMode()));
            output.putObject("box_visible", new ElementTag(structure.isBoundingBoxVisible()));
            output.putObject("ignore_entities", new ElementTag(structure.isIgnoreEntities()));
            output.putObject("show_invisible", new ElementTag(structure.isShowAir()));
            return output;
        });
    }

    public static ObjectTagProcessor<LocationTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a location!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object LocationTag
        // @name block_facing
        // @input LocationTag
        // @description
        // Sets the facing direction of the block, as a vector.
        // @tags
        // <LocationTag.block_facing>
        // -->
        if (mechanism.matches("block_facing") && mechanism.requireObject(LocationTag.class)) {
            LocationTag faceVec = mechanism.valueAsType(LocationTag.class);
            Block block = getBlock();
            MaterialTag material = new MaterialTag(block);
            if (!MaterialDirectional.describes(material)) {
                mechanism.echoError("LocationTag.block_facing mechanism failed: block is not directional.");
                return;
            }
            MaterialDirectional.getFrom(material).setFacing(Utilities.faceFor(faceVec.toVector()));
            block.setBlockData(material.getModernData());
        }

        // <--[mechanism]
        // @object LocationTag
        // @name block_type
        // @input MaterialTag
        // @description
        // Sets the type of the block.
        // @tags
        // <LocationTag.material>
        // -->
        if (mechanism.matches("block_type") && mechanism.requireObject(MaterialTag.class)) {
            MaterialTag mat = mechanism.valueAsType(MaterialTag.class);
            getBlock().setBlockData(mat.getModernData(), false);
        }

        // <--[mechanism]
        // @object LocationTag
        // @name biome
        // @input BiomeTag
        // @description
        // Sets the biome of the block.
        // @tags
        // <LocationTag.biome>
        // -->
        if (mechanism.matches("biome") && mechanism.requireObject(BiomeTag.class)) {
            mechanism.valueAsType(BiomeTag.class).getBiome().setTo(getBlock());
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_custom_rules
        // @input MapTag
        // @description
        // Sets the custom spawner rules for this spawner. Input is a map, like: [sky_min=0;sky_max=15;block_min=0;block_max=15]
        // -->
        if (mechanism.matches("spawner_custom_rules") && mechanism.requireObject(MapTag.class) && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            MapTag map = mechanism.valueAsType(MapTag.class);
            ElementTag skyMin = map.getElement("sky_min"), skyMax = map.getElement("sky_max"), blockMin = map.getElement("block_min"), blockMax = map.getElement("block_max");
            if (skyMin == null || skyMax == null || blockMin == null || blockMax == null) {
                mechanism.echoError("Invalid spawner_custom_rules input, missing map keys.");
                return;
            }
            NMSHandler.blockHelper.setSpawnerCustomRules(spawner, skyMin.asInt(), skyMax.asInt(), blockMin.asInt(), blockMax.asInt());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_type
        // @input EntityTag
        // @description
        // Sets the entity that a mob spawner will spawn.
        // @tags
        // <LocationTag.spawner_type>
        // -->
        if (mechanism.matches("spawner_type") && mechanism.requireObject(EntityTag.class) && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            NMSHandler.blockHelper.setSpawnerSpawnedType(spawner, mechanism.valueAsType(EntityTag.class));
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_delay_data
        // @input ListTag
        // @description
        // Sets the current spawn delay, minimum spawn delay, and maximum spawn delay of the mob spawner.
        // For example, -1|200|800
        // @tags
        // <LocationTag.spawner_spawn_delay>
        // <LocationTag.spawner_minimum_spawn_delay>
        // <LocationTag.spawner_maximum_spawn_delay>
        // -->
        if (mechanism.matches("spawner_delay_data") && getBlockState() instanceof CreatureSpawner) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() < 3) {
                return;
            }
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setDelay(Integer.parseInt(list.get(0)));
            int minDelay = Integer.parseInt(list.get(1));
            int maxDelay = Integer.parseInt(list.get(2));
            // Minecraft won't set the limits if the new max would be lower than the current min
            // or new min would be higher than the current max
            if (minDelay > spawner.getMaxSpawnDelay()) {
                spawner.setMaxSpawnDelay(maxDelay);
                spawner.setMinSpawnDelay(minDelay);
            } else {
                spawner.setMinSpawnDelay(minDelay);
                spawner.setMaxSpawnDelay(maxDelay);
            }
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_max_nearby_entities
        // @input ElementTag(Number)
        // @description
        // Sets the maximum nearby entities of the spawner.
        // @tags
        // <LocationTag.spawner_max_nearby_entities>
        // -->
        if (mechanism.matches("spawner_max_nearby_entities") && mechanism.requireInteger() && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setMaxNearbyEntities(mechanism.getValue().asInt());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_player_range
        // @input ElementTag(Number)
        // @description
        // Sets the maximum player range of the spawner.
        // @tags
        // <LocationTag.spawner_player_range>
        // -->
        if (mechanism.matches("spawner_player_range") && mechanism.requireInteger() && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setRequiredPlayerRange(mechanism.getValue().asInt());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_range
        // @input ElementTag(Number)
        // @description
        // Sets the spawn range of the spawner (the radius mobs will spawn in).
        // @tags
        // <LocationTag.spawner_range>
        // -->
        if (mechanism.matches("spawner_range") && mechanism.requireInteger() && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setSpawnRange(mechanism.getValue().asInt());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name spawner_count
        // @input ElementTag(Number)
        // @description
        // Sets the spawn count of the spawner.
        // @tags
        // <LocationTag.spawner_count>
        // -->
        if (mechanism.matches("spawner_count") && mechanism.requireInteger() && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setSpawnCount(mechanism.getValue().asInt());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name lock
        // @input ElementTag
        // @description
        // Sets the container's lock password.
        // Locked containers can only be opened while holding an item with the name of the lock.
        // Leave blank to remove a container's lock.
        // @tags
        // <LocationTag.lock>
        // <LocationTag.is_locked>
        // <LocationTag.is_lockable>
        // -->
        if (mechanism.matches("lock") && getBlockState() instanceof Lockable) {
            BlockState state = getBlockState();
            ((Lockable) state).setLock(mechanism.hasValue() ? mechanism.getValue().asString() : null);
            state.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name sign_contents
        // @input ListTag
        // @description
        // Sets the contents of a sign block.
        // @tags
        // <LocationTag.sign_contents>
        // -->
        if (mechanism.matches("sign_contents") && getBlockState() instanceof Sign) {
            Sign state = (Sign) getBlockState();
            for (int i = 0; i < 4; i++) {
                PaperAPITools.instance.setSignLine(state, i, "");
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            CoreUtilities.fixNewLinesToListSeparation(list);
            if (list.size() > 4) {
                mechanism.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    PaperAPITools.instance.setSignLine(state, i, list.get(i));
                }
            }
            state.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name skull_skin
        // @input ElementTag(|ElementTag(|ElementTag))
        // @description
        // Sets the skin of a skull block.
        // The first ElementTag is a UUID.
        // Optionally, use the second ElementTag for the skin texture cache.
        // Optionally, use the third ElementTag for a player name.
        // @tags
        // <LocationTag.skull_skin>
        // -->
        if (mechanism.matches("skull_skin")) {
            final BlockState blockState = getBlockState();
            Material material = getBlock().getType();
            if (blockState instanceof Skull) {
                ListTag list = mechanism.valueAsType(ListTag.class);
                String idString = list.get(0);
                String texture = null;
                if (list.size() > 1) {
                    texture = list.get(1);
                }
                PlayerProfile profile;
                if (idString.contains("-")) {
                    UUID uuid = UUID.fromString(idString);
                    String name = null;
                    if (list.size() > 2) {
                        name = list.get(2);
                    }
                    profile = new PlayerProfile(name, uuid, texture);
                }
                else {
                    profile = new PlayerProfile(idString, null, texture);
                }
                if (texture == null || profile.getUniqueId() == null) { // Load if needed
                    profile = NMSHandler.instance.fillPlayerProfile(profile);
                }
                if (texture != null) {
                    profile.setTexture(texture);
                }
                NMSHandler.blockHelper.setPlayerProfile((Skull) blockState, profile);
            }
            else {
                mechanism.echoError("Unable to set skull_skin on block of type " + material.name() + " with state " + blockState.getClass().getCanonicalName());
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name hive_max_bees
        // @input ElementTag(Number)
        // @description
        // Sets the maximum allowed number of bees in a beehive.
        // @tags
        // <LocationTag.hive_max_bees>
        // -->
        if (mechanism.matches("hive_max_bees") && mechanism.requireInteger()) {
            Beehive hive = (Beehive) getBlockState();
            hive.setMaxEntities(mechanism.getValue().asInt());
            hive.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name release_bees
        // @input None
        // @description
        // Causes a beehive to release all its bees.
        // Will do nothing if the hive is empty.
        // @tags
        // <LocationTag.hive_bee_count>
        // -->
        if (mechanism.matches("release_bees")) {
            Beehive hive = (Beehive) getBlockState();
            hive.releaseEntities();
            hive.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name add_bee
        // @input EntityTag
        // @description
        // Adds a bee into a beehive.
        // Will do nothing if there's no room left in the hive.
        // @tags
        // <LocationTag.hive_bee_count>
        // -->
        if (mechanism.matches("add_bee") && mechanism.requireObject(EntityTag.class)) {
            Beehive hive = (Beehive) getBlockState();
            hive.addEntity((Bee) mechanism.valueAsType(EntityTag.class).getBukkitEntity());
            hive.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name command_block_name
        // @input ElementTag
        // @description
        // Sets the name of a command block.
        // @tags
        // <LocationTag.command_block_name>
        // -->
        if (mechanism.matches("command_block_name")) {
            if (getBlock().getState() instanceof CommandBlock) {
                CommandBlock block = ((CommandBlock) getBlockState());
                block.setName(mechanism.getValue().asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name command_block
        // @input ElementTag
        // @description
        // Sets the command of a command block.
        // @tags
        // <LocationTag.command_block>
        // -->
        if (mechanism.matches("command_block")) {
            if (getBlock().getState() instanceof CommandBlock) {
                CommandBlock block = ((CommandBlock) getBlockState());
                block.setCommand(mechanism.getValue().asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name custom_name
        // @input ElementTag
        // @description
        // Sets the custom name of the block.
        // Use no value to reset the block's name.
        // @tags
        // <LocationTag.custom_name>
        // -->
        if (mechanism.matches("custom_name")) {
            if (getBlockState() instanceof Nameable) {
                String title = null;
                if (mechanism.hasValue()) {
                    title = mechanism.getValue().asString();
                }
                BlockState state = getBlockState();
                PaperAPITools.instance.setCustomName((Nameable) state, title);
                state.update(true);
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name brewing_time
        // @input DurationTag
        // @description
        // Sets the brewing time a brewing stand has left.
        // @tags
        // <LocationTag.brewing_time>
        // -->
        if (mechanism.matches("brewing_time")) {
            if (getBlockState() instanceof BrewingStand) {
                BrewingStand stand = (BrewingStand) getBlockState();
                stand.setBrewingTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
                stand.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name brewing_fuel_level
        // @input ElementTag(Number)
        // @description
        // Sets the brewing fuel level a brewing stand has.
        // @tags
        // <LocationTag.brewing_fuel_level>
        // -->
        if (mechanism.matches("brewing_fuel_level")) {
            if (getBlockState() instanceof BrewingStand) {
                BrewingStand stand = (BrewingStand) getBlockState();
                stand.setFuelLevel(mechanism.getValue().asInt());
                stand.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name furnace_burn_duration
        // @input DurationTag
        // @description
        // Sets the burn time for a furnace in ticks. Maximum is 32767 ticks.
        // @tags
        // <LocationTag.furnace_burn_duration>
        // -->
        if (mechanism.matches("furnace_burn_duration") && mechanism.requireObject(DurationTag.class)) {
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setBurnTime((short) mechanism.valueAsType(DurationTag.class).getTicks());
                furnace.update();
            }
        }
        if (mechanism.matches("furnace_burn_time")) {
            BukkitImplDeprecations.furnaceTimeTags.warn(mechanism.context);
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setBurnTime((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name furnace_cook_duration
        // @input DurationTag
        // @description
        // Sets the current cook time for a furnace in ticks. Maximum is 32767 ticks.
        // @tags
        // <LocationTag.furnace_cook_duration>
        // -->
        if (mechanism.matches("furnace_cook_duration")) {
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTime((short) mechanism.valueAsType(DurationTag.class).getTicks());
                furnace.update();
            }
        }
        if (mechanism.matches("furnace_cook_time")) {
            BukkitImplDeprecations.furnaceTimeTags.warn(mechanism.context);
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTime((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name furnace_cook_duration_total
        // @input DurationTag
        // @description
        // Sets the total cook time for a furnace in ticks. Maximum is 32767 ticks.
        // @tags
        // <LocationTag.furnace_cook_duration_total>
        // -->
        if (mechanism.matches("furnace_cook_duration_total")) {
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTimeTotal((short) mechanism.valueAsType(DurationTag.class).getTicks());
                furnace.update();
            }
        }
        if (mechanism.matches("furnace_cook_time_total")) {
            BukkitImplDeprecations.furnaceTimeTags.warn(mechanism.context);
            if (getBlockState() instanceof Furnace) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTimeTotal((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name patterns
        // @input ListTag
        // @description
        // Changes the patterns of the banner at this location. Input must be in the form "COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // For the list of possible patterns, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // @tags
        // <LocationTag.patterns>
        // <server.pattern_types>
        // -->
        if (mechanism.matches("patterns")) {
            List<org.bukkit.block.banner.Pattern> patterns = new ArrayList<>();
            ListTag list = mechanism.valueAsType(ListTag.class);
            List<String> split;
            for (String string : list) {
                try {
                    split = CoreUtilities.split(string, '/', 2);
                    patterns.add(new org.bukkit.block.banner.Pattern(DyeColor.valueOf(split.get(0).toUpperCase()),
                            PatternType.valueOf(split.get(1).toUpperCase())));
                }
                catch (Exception e) {
                    mechanism.echoError("Could not apply pattern to banner: " + string);
                }
            }
            Banner banner = (Banner) getBlockState();
            banner.setPatterns(patterns);
            banner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name head_rotation
        // @input ElementTag(Number)
        // @description
        // Sets the rotation of the head at this location. Must be an integer 1 to 16.
        // @tags
        // <LocationTag.head_rotation>
        // -->
        if (mechanism.matches("head_rotation") && mechanism.requireInteger()) {
            Skull sk = (Skull) getBlockState();
            sk.setRotation(getSkullBlockFace(mechanism.getValue().asInt() - 1));
            sk.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name generate_tree
        // @input ElementTag
        // @description
        // Generates a tree at this location if possible.
        // For a list of valid tree types, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/TreeType.html>
        // @tags
        // <server.tree_types>
        // -->
        if (mechanism.matches("generate_tree") && mechanism.requireEnum(TreeType.class)) {
            boolean generated = getWorld().generateTree(this, TreeType.valueOf(mechanism.getValue().asString().toUpperCase()));
            if (!generated) {
                mechanism.echoError("Could not generate tree at " + identifySimple() + ". Make sure this location can naturally generate a tree!");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name beacon_primary_effect
        // @input ElementTag
        // @description
        // Sets the primary effect of a beacon, with input as just an effect type name.
        // @tags
        // <LocationTag.beacon_primary_effect>
        // -->
        if (mechanism.matches("beacon_primary_effect")) {
            Beacon beacon = (Beacon) getBlockState();
            beacon.setPrimaryEffect(PotionEffectType.getByName(mechanism.getValue().asString().toUpperCase()));
            beacon.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name beacon_secondary_effect
        // @input ElementTag
        // @description
        // Sets the secondary effect of a beacon, with input as just an effect type name.
        // @tags
        // <LocationTag.beacon_secondary_effect>
        // -->
        if (mechanism.matches("beacon_secondary_effect")) {
            Beacon beacon = (Beacon) getBlockState();
            beacon.setSecondaryEffect(PotionEffectType.getByName(mechanism.getValue().asString().toUpperCase()));
            beacon.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name activate
        // @input None
        // @description
        // Activates the block at the location if possible.
        // Works for blocks like dispensers, which have explicit 'activation' methods.
        // -->
        if (mechanism.matches("activate")) {
            BlockState state = getBlockState();
            if (state instanceof Dispenser) {
                ((Dispenser) state).dispense();
            }
            else if (state instanceof Dropper) {
                ((Dropper) state).drop();
            }
            else {
                mechanism.echoError("'activate' mechanism does not work for blocks of type: " + state.getType().name());
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name lectern_page
        // @input ElementTag(Number)
        // @description
        // Changes the page currently displayed on the book in a lectern block.
        // @tags
        // <LocationTag.lectern_page>
        // -->
        if (mechanism.matches("lectern_page") && mechanism.requireInteger()) {
            BlockState state = getBlockState();
            if (state instanceof Lectern) {
                ((Lectern) state).setPage(mechanism.getValue().asInt());
                state.update();
            }
            else {
                mechanism.echoError("'lectern_page' mechanism can only be called on a lectern block.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name clear_loot_table
        // @input None
        // @description
        // Removes the loot table from the chest at this location.
        // @tags
        // <LocationTag.has_loot_table>
        // -->
        if (mechanism.matches("clear_loot_table")) {
            BlockState state = getBlockState();
            if (state instanceof Lootable) {
                ((Lootable) state).setLootTable(null);
                state.update();
            }
            else {
                mechanism.echoError("'clear_loot_table' mechanism can only be called on a lootable block (like a chest).");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name loot_table_id
        // @input ElementTag
        // @description
        // Sets the loot table of a lootable container at this location.
        // This is the namespaced path of the loot table, provided by a datapack or Minecraft's default data.
        // @tags
        // <LocationTag.loot_table_id>
        // <LocationTag.has_loot_table>
        // @Example
        // # Sets the chest's loot table to a bonus chest
        // - adjust <[location]> loot_table:chests/ancient_city
        // -->
        if (mechanism.matches("loot_table_id")) {
            BlockState state = getBlockState();
            if (state instanceof Lootable) {
                LootTable table = Bukkit.getLootTable(Utilities.parseNamespacedKey(mechanism.getValue().asString()));
                if (table == null) {
                    mechanism.echoError("Invalid loot table ID.");
                    return;
                }
                ((Lootable) state).setLootTable(table);
                state.update();
            }
            else {
                mechanism.echoError("'loot_table_id' mechanism can only be called on a lootable block (like a chest).");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name jukebox_record
        // @input ItemTag
        // @description
        // Sets the record item played by a jukebox. Give no input to set the jukebox to empty.
        // See also <@link mechanism LocationTag.jukebox_play>.
        // @tags
        // <LocationTag.jukebox_record>
        // -->
        if (mechanism.matches("jukebox_record")) {
            BlockState state = getBlockState();
            if (state instanceof Jukebox) {
                if (mechanism.hasValue() && mechanism.requireObject(ItemTag.class)) {
                    ((Jukebox) state).setRecord(mechanism.valueAsType(ItemTag.class).getItemStack());
                }
                else {
                    NMSHandler.blockHelper.makeBlockStateRaw(state);
                    ((Jukebox) state).setRecord(null);
                }
                state.update();
            }
            else {
                mechanism.echoError("'jukebox_record' mechanism can only be called on a jukebox block.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name jukebox_play
        // @input ElementTag(Boolean)
        // @description
        // If 'true', starts playing the record inside. If 'false', stops playing any song.
        // See also <@link mechanism LocationTag.jukebox_record>.
        // @tags
        // <LocationTag.jukebox_is_playing>
        // -->
        if (mechanism.matches("jukebox_play") && mechanism.requireBoolean()) {
            BlockState state = getBlockState();
            if (state instanceof Jukebox) {
                if (mechanism.getValue().asBoolean()) {
                    Material mat = ((Jukebox) state).getRecord().getType();
                    if (mat == Material.AIR) {
                        mechanism.echoError("'jukebox_play' cannot play nothing.");
                        return;
                    }
                    ((Jukebox) state).setPlaying(mat);
                }
                else {
                    ((Jukebox) state).stopPlaying();
                }
                state.update();
            }
            else {
                mechanism.echoError("'jukebox_play' mechanism can only be called on a jukebox block.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name age
        // @input DurationTag
        // @description
        // Sets the age of an end gateway.
        // @tags
        // <LocationTag.age>
        // -->
        if (mechanism.matches("age") && mechanism.requireObject(DurationTag.class)) {
            BlockState state = getBlockState();
            if (state instanceof EndGateway) {
                ((EndGateway) state).setAge(mechanism.valueAsType(DurationTag.class).getTicks());
                state.update();
            }
            else {
                mechanism.echoError("'age' mechanism can only be called on end gateway blocks.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name is_exact_teleport
        // @input ElementTag(Boolean)
        // @description
        // Sets whether an end gateway is 'exact teleport' - if false, the destination will be randomly chosen *near* the destination.
        // @tags
        // <LocationTag.is_exact_teleport>
        // -->
        if (mechanism.matches("is_exact_teleport") && mechanism.requireBoolean()) {
            BlockState state = getBlockState();
            if (state instanceof EndGateway) {
                ((EndGateway) state).setExactTeleport(mechanism.getValue().asBoolean());
                state.update();
            }
            else {
                mechanism.echoError("'is_exact_teleport' mechanism can only be called on end gateway blocks.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name exit_location
        // @input LocationTag
        // @description
        // Sets the exit location of an end gateway block.
        // See also <@link mechanism LocationTag.is_exact_teleport>.
        // @tags
        // <LocationTag.exit_location>
        // -->
        if (mechanism.matches("exit_location") && mechanism.requireObject(LocationTag.class)) {
            BlockState state = getBlockState();
            if (state instanceof EndGateway) {
                ((EndGateway) state).setExitLocation(mechanism.valueAsType(LocationTag.class));
                state.update();
            }
            else {
                mechanism.echoError("'exit_location' mechanism can only be called on end gateway blocks.");
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name vanilla_tick
        // @input None
        // @description
        // Causes an immediate vanilla tick at a block location (normally processed at random according to the randomTickSpeed gamerule).
        // -->
        if (mechanism.matches("vanilla_tick")) {
            NMSHandler.blockHelper.doRandomTick(this);
        }

        // <--[mechanism]
        // @object LocationTag
        // @name apply_bonemeal
        // @input ElementTag
        // @description
        // Applies bonemeal to the block, on the given block face. Input is NORTH, EAST, SOUTH, WEST, UP, or DOWN.
        // For example: - adjust <player.location.below> apply_bonemeal:up
        // -->
        if (mechanism.matches("apply_bonemeal") && mechanism.requireEnum(BlockFace.class)) {
            getBlock().applyBoneMeal(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object LocationTag
        // @name campfire_items
        // @input ListTag(ItemTag)
        // @description
        // Sets the items in this campfire, as a list of items, where the index in the list directly corresponds to index in the campfire slots.
        // @tags
        // <LocationTag.campfire_items>
        // -->
        if (mechanism.matches("campfire_items") && mechanism.requireObject(ListTag.class)) {
            BlockState state = getBlockState();
            if (!(state instanceof Campfire)) {
                mechanism.echoError("'campfire_items' mechanism can only be called on campfire blocks.");
            }
            else {
                Campfire fire = (Campfire) state;
                List<ItemTag> list = mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context);
                for (int i = 0; i < list.size(); i++) {
                    if (i >= fire.getSize()) {
                        mechanism.echoError("Cannot add item for index " + (i + 1) + " as the campfire can only hold " + fire.getSize() + " items.");
                        break;
                    }
                    fire.setItem(i, list.get(i).getItemStack());
                }
                fire.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name ring_bell
        // @input None
        // @description
        // Causes the bell to ring.
        // -->
        if (mechanism.matches("ring_bell")) {
            BlockState state = getBlockState();
            if (!(state instanceof Bell)) {
                mechanism.echoError("'ring_bell' mechanism can only be called on Bell blocks.");
            }
            else {
                NMSHandler.blockHelper.ringBell((Bell) state);
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name sign_glowing
        // @input ElementTag(Boolean)
        // @description
        // Changes whether the sign at the location is glowing.
        // @tags
        // <LocationTag.sign_glow_color>
        // <LocationTag.sign_glowing>
        // -->
        if (mechanism.matches("sign_glowing") && mechanism.requireBoolean()) {
            BlockState state = getBlockState();
            if (!(state instanceof Sign)) {
                mechanism.echoError("'sign_glowing' mechanism can only be called on Sign blocks.");
            }
            else {
                Sign sign = (Sign) state;
                sign.setGlowingText(mechanism.getValue().asBoolean());
                sign.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name sign_glow_color
        // @input ElementTag
        // @description
        // Changes the glow color of a sign.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // If a sign is not glowing, this is equivalent to applying a chat color to the sign.
        // Use <@link mechanism LocationTag.sign_glowing> to toggle whether the sign is glowing.
        // @tags
        // <LocationTag.sign_glow_color>
        // <LocationTag.sign_glowing>
        // -->
        if (mechanism.matches("sign_glow_color") && mechanism.requireEnum(DyeColor.class)) {
            BlockState state = getBlockState();
            if (!(state instanceof Sign)) {
                mechanism.echoError("'sign_glow_color' mechanism can only be called on Sign blocks.");
            }
            else {
                Sign sign = (Sign) state;
                sign.setColor(mechanism.getValue().asEnum(DyeColor.class));
                sign.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name structure_block_data
        // @input MapTag
        // @description
        // Sets the structure block data of the structure block at the location. Input is a map with the following keys (all keys are optional):
        // - author: EntityTag: The Structure's author, can also input an ElementTag to set the name directly (set to "?" for most vanilla structures).
        // - integrity: ElementTag(Decimal): The integrity of the structure (0-1). Lower integrity values will result in more blocks being removed when loading a structure.
        // used with the seed to determine which blocks are randomly removed to mimic "decay".
        // - metadata: ElementTag: Can only be set while in DATA mode. sets specific functions that can be applied to the structure,
        // check the Minecraft wiki (<@link url https://minecraft.gamepedia.com/Structure_Block#Data>) for more information.
        // - mirror: ElementTag: How the structure is mirrored; "NONE", "LEFT_RIGHT", or "FRONT_BACK".
        // - box_position: LocationTag: The position of the structure's bounding box, relative to the position of the structure block. Maximum allowed distance is 48 blocks in any direction.
        // - rotation: ElementTag: The rotation of the structure; "NONE", "CLOCKWISE_90", "CLOCKWISE_180", or "COUNTERCLOCKWISE_90".
        // - seed: ElementTag(Number): The seed used to determine how many blocks are removed upon loading of this structure (see "integrity" for more information).
        // - structure_name: ElementTag: The name of the structure.
        // - size: LocationTag: The size of the structure's bounding box, The maximum structure size is 48,48,48.
        // - mode: ElementTag: The structure block's mode; "CORNER", "DATA", "LOAD", or "SAVE". See also <@link mechanism MaterialTag.mode>.
        // - box_visible: ElementTag(Boolean): Whether the structure's bounding box is visible, only applies in LOAD mode.
        // - ignore_entities: ElementTag(Boolean): Whether entities in the structure are ignored, only applies in SAVE mode.
        // - show_invisible: ElementTag(Boolean): Whether invisible blocks in the structure are shown.
        // @tags
        // <LocationTag.structure_block_data>
        // -->
        if (mechanism.matches("structure_block_data") && mechanism.requireObject(MapTag.class)) {
            BlockState state = getBlockState();
            if (!(state instanceof Structure)) {
                mechanism.echoError("'structure_block_data' mechanism can only be called on Structure blocks.");
                return;
            }
            Structure structure = (Structure) state;
            MapTag input = mechanism.valueAsType(MapTag.class);
            ObjectTag author = input.getObject("author");
            if (author != null) {
                if (author.shouldBeType(EntityTag.class)) {
                    EntityTag entity = author.asType(EntityTag.class, mechanism.context);
                    if (!entity.isLivingEntity()) {
                        mechanism.echoError("Invalid author entity input '" + author + "': entity must be living.");
                        return;
                    }
                    structure.setAuthor(entity.getLivingEntity());
                }
                else {
                    structure.setAuthor(author.toString());
                }
            }
            ElementTag integrity = input.getElement("integrity");
            if (integrity != null) {
                float integrityFloat = integrity.isFloat() ? integrity.asFloat() : -1;
                if (integrityFloat < 0 || integrityFloat > 1) {
                    mechanism.echoError("Invalid integrity input '" + integrity + "': must be a decimal between 0 and 1.");
                    return;
                }
                structure.setIntegrity(integrityFloat);
            }
            ElementTag metadata = input.getElement("metadata");
            if (metadata != null) {
                if (structure.getUsageMode() != UsageMode.DATA) {
                    mechanism.echoError("metadata can only be set while in DATA mode.");
                    return;
                }
                structure.setMetadata(metadata.toString());
            }
            ElementTag mirror = input.getElement("mirror");
            if (mirror != null) {
                Mirror mirrorEnum = mirror.asEnum(Mirror.class);
                if (mirrorEnum == null) {
                    mechanism.echoError("Invalid mirror input '" + mirror + "': check meta docs for more information.");
                    return;
                }
                structure.setMirror(mirrorEnum);
            }
            LocationTag boxPositionLoc = input.getObjectAs("box_position", LocationTag.class, mechanism.context);
            if (boxPositionLoc != null) {
                int x = boxPositionLoc.getBlockX();
                int y = boxPositionLoc.getBlockY();
                int z = boxPositionLoc.getBlockZ();
                if (x < -48 || x > 48 || y < -48 || y > 48 || z < -48 || z > 48) {
                    mechanism.echoError("Invalid box_position input '" + boxPositionLoc + "': must be within 48 blocks of the structure block.");
                    return;
                }
                structure.setRelativePosition(new BlockVector(boxPositionLoc.toVector()));
            }
            ElementTag rotation = input.getElement("rotation");
            if (rotation != null) {
                StructureRotation rotationEnum = rotation.asEnum(StructureRotation.class);
                if (rotationEnum == null) {
                    mechanism.echoError("Invalid rotation input '" + rotation + "': check meta docs for more information.");
                    return;
                }
                structure.setRotation(rotationEnum);
            }
            ElementTag seed = input.getElement("seed");
            if (seed != null) {
                if (!seed.isInt()) {
                    mechanism.echoError("Invalid seed input '" + seed + "': must be an integer.");
                    return;
                }
                structure.setSeed(seed.asLong());
            }
            ElementTag structureName = input.getElement("structure_name");
            if (structureName != null) {
                structure.setStructureName(structureName.toString());
            }
            LocationTag sizeLoc = input.getObjectAs("size", LocationTag.class, mechanism.context);
            if (sizeLoc != null) {
                int x = sizeLoc.getBlockX();
                int y = sizeLoc.getBlockY();
                int z = sizeLoc.getBlockZ();
                if (x < 0 || x > 48 || y < 0 || y > 48 || z < 0 || z > 48) {
                    mechanism.echoError("Invalid size input '" + sizeLoc + "': cannot be larger than 48,48,48 or smaller than 0,0,0.");
                    return;
                }
                structure.setStructureSize(new BlockVector(sizeLoc.toVector()));
            }
            ElementTag mode = input.getElement("mode");
            if (mode != null) {
                UsageMode usageMode = mode.asEnum(UsageMode.class);
                if (usageMode == null) {
                    mechanism.echoError("Invalid mode input '" + mode + "': check meta docs for more information.");
                    return;
                }
                structure.setUsageMode(usageMode);
            }
            ElementTag boxVisible = input.getElement("box_visible");
            if (boxVisible != null) {
                if (!boxVisible.isBoolean()) {
                    mechanism.echoError("Invalid box_visible input '" + boxVisible + "': must be a boolean.");
                    return;
                }
                structure.setBoundingBoxVisible(boxVisible.asBoolean());
            }
            ElementTag ignoreEntities = input.getElement("ignore_entities");
            if (ignoreEntities != null) {
                if (!ignoreEntities.isBoolean()) {
                    mechanism.echoError("Invalid ignore_entities input '" + ignoreEntities + "': must be a boolean.");
                    return;
                }
                structure.setIgnoreEntities(ignoreEntities.asBoolean());
            }
            ElementTag showInvisible = input.getElement("show_invisible");
            if (showInvisible != null) {
                if (!showInvisible.isBoolean()) {
                    mechanism.echoError("Invalid show_invisible input '" + showInvisible + "': must be a boolean.");
                    return;
                }
                structure.setShowAir(showInvisible.asBoolean());
            }
            structure.update();
        }

        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public boolean advancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.equals("location")) {
            return true;
        }
        if (matcherLow.contains(":")) {
            if (matcherLow.startsWith("block_flagged:")) {
                return BukkitScriptEvent.coreFlaggedCheck(matcher.substring("block_flagged:".length()), getFlagTracker());
            }
            if (matcherLow.startsWith("location_in:")) {
                return BukkitScriptEvent.inCheckInternal(CoreUtilities.noDebugContext, "tryLocation", this, matcher.substring("location_in:".length()), "tryLocation", "tryLocation");
            }
        }
        if (getWorld() == null) {
            return false;
        }
        if (getY() < getWorld().getMinHeight() || getY() >= getWorld().getMaxHeight()) {
            return false;
        }
        return MaterialTag.advancedMatchesInternal(getBlock().getType(), matcher, true);
    }
}
