package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.objects.properties.material.MaterialDirectional;
import com.denizenscript.denizen.objects.properties.material.MaterialHalf;
import com.denizenscript.denizen.objects.properties.material.MaterialSwitchFace;
import com.denizenscript.denizen.objects.properties.material.MaterialPersistent;
import com.denizenscript.denizen.scripts.commands.world.SwitchCommand;
import com.denizenscript.denizen.utilities.AdvancedTextImpl;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizen.utilities.flags.LocationFlagSearchHelper;
import com.denizenscript.denizen.utilities.world.PathFinder;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Directional;
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

public class LocationTag extends org.bukkit.Location implements ObjectTag, Notable, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name LocationTag
    // @prefix l
    // @base ElementTag
    // @implements FlaggableObject
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
    // -->

    /**
     * The world name if a world reference is bad.
     */
    public String backupWorld;

    public String getWorldName() {
        if (backupWorld != null) {
            return backupWorld;
        }
        World w = super.getWorld();
        if (w != null) {
            return w.getName();
        }
        return null;
    }

    @Override
    public World getWorld() {
        World w = super.getWorld();
        if (w != null) {
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

    /////////////////////
    //   STATIC METHODS
    /////////////////

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

    //////////////////
    //    OBJECT FETCHER
    ////////////////

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
        Notable noted = NoteManager.getSavedObject(string);
        if (noted instanceof LocationTag) {
            return (LocationTag) noted;
        }
        List<String> split = CoreUtilities.split(string, ',');
        if (split.size() == 2)
        // If 4 values, world-less 2D location format
        // x,y
        {
            try {
                return new LocationTag(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)));
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
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new LocationTag(world,
                            Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1)));
                }
                if (ArgumentHelper.matchesDouble(split.get(2))) {
                    return new LocationTag(null,
                            Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1)),
                            Double.valueOf(split.get(2)));
                }
                LocationTag output = new LocationTag(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)));
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
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new LocationTag(world,
                            Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1)),
                            Double.valueOf(split.get(2)));
                }
                LocationTag output = new LocationTag(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)));
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
                float pitch = Float.valueOf(split.get(3));
                float yaw = Float.valueOf(split.get(4));
                return new LocationTag((World) null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)),
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
                float pitch = Float.valueOf(split.get(3));
                float yaw = Float.valueOf(split.get(4));
                return new LocationTag(worldName,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)),
                        yaw, pitch);
            }
            catch (Exception e) {
                if (context == null || context.showErrors()) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        if (context == null || context.showErrors()) {
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
        return LocationTag.valueOf(string, new BukkitTagContext(null, null, null, false, null)) != null;
    }

    @Override
    public ObjectTag duplicate() {
        return clone();
    }

    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    /**
     * Turns a Bukkit Location into a LocationTag, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public LocationTag(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
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
            NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
            return isChunkLoaded();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
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
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                }
                return null;
            }
            if (!isChunkLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return super.getBlock();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public Material getBlockTypeForTag(Attribute attribute) {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                }
                return null;
            }
            if (!isChunkLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return super.getBlock().getType();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public static BlockState getBlockStateSafe(Block block) {
        NMSHandler.getChunkHelper().changeChunkServerThread(block.getWorld());
        try {
            return block.getState();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(block.getWorld());
        }
    }

    public BiomeNMS getBiome() {
        return NMSHandler.getInstance().getBiomeAt(super.getBlock());
    }

    public BiomeNMS getBiomeForTag(Attribute attribute) {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                }
                return null;
            }
            if (!isChunkLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return getBiome();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public Location getHighestBlockForTag(Attribute attribute) {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                }
                return null;
            }
            if (!isChunkLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return getWorld().getHighestBlockAt(this).getLocation();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    public Collection<ItemStack> getDropsForTag(Attribute attribute, ItemStack item) {
        NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
        try {
            if (getWorld() == null) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because no world is specified.");
                }
                return null;
            }
            if (!isChunkLoaded()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("LocationTag trying to read block, but cannot because the chunk is unloaded. Use the 'chunkload' command to ensure the chunk is loaded.");
                }
                return null;
            }
            return item == null ? super.getBlock().getDrops() : super.getBlock().getDrops(item);
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
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
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            Debug.echoError("Location flags are only available in 1.16+");
            return null;
        }
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

    /**
     * Indicates whether this location is forced to identify as not-a-note or not.
     */
    private boolean raw = false;

    private void setRaw(boolean state) {
        this.raw = state;
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
            if (matcher == null ? loc.getBlock().getType() != requiredMaterial : !BukkitScriptEvent.tryLocation(loc, matcher)) {
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
    public String getObjectType() {
        return "Location";
    }

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
        if (raw) {
            return identifyRaw();
        }
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

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        /////////////////////
        //   BLOCK ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.block_facing>
        // @returns LocationTag
        // @mechanism LocationTag.block_facing
        // @description
        // Returns the relative location vector of where this block is facing.
        // Only works for block types that have directionality (such as signs, chests, stairs, etc.).
        // This can return for example "1,0,0" to mean the block is facing towards the positive X axis.
        // You can use <some_block_location.add[<some_block_location.block_facing>]> to get the block directly in front of this block (based on its facing direction).
        // -->
        tagProcessor.registerTag(LocationTag.class, "block_facing", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
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
        // @description
        // Returns the location with its direction set to the block's facing direction.
        // Only works for block types that have directionality (such as signs, chests, stairs, etc.).
        // You can use <some_block_location.with_facing_direction.forward[1]> to get the block directly in front of this block (based on its facing direction).
        // -->
        tagProcessor.registerTag(LocationTag.class, "with_facing_direction", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
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
        // @description
        // Returns the location at the center of the block this location is on.
        // -->
        tagProcessor.registerTag(LocationTag.class, "center", (attribute, object) -> {
            return new LocationTag(object.getWorld(), object.getBlockX() + 0.5, object.getBlockY() + 0.5, object.getBlockZ() + 0.5);
        });

        // <--[tag]
        // @attribute <LocationTag.random_offset[<limit>]>
        // @returns LocationTag
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
        // @description
        // Returns the location of the highest solid block at the location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "highest", (attribute, object) -> {
            Location result = object.getHighestBlockForTag(attribute);
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_14)) {
                result = result.subtract(0, 1, 0);
            }
            return new LocationTag(result);
        });

        // <--[tag]
        // @attribute <LocationTag.has_inventory>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the block at the location has an inventory.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_inventory", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof InventoryHolder);
        });

        // <--[tag]
        // @attribute <LocationTag.inventory>
        // @returns InventoryTag
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
        // @description
        // Returns the material of the block at the location.
        // -->
        tagProcessor.registerTag(MaterialTag.class, "material", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
            if (block == null) {
                return null;
            }
            return new MaterialTag(block);
        });

        // <--[tag]
        // @attribute <LocationTag.patterns>
        // @returns ListTag
        // @group properties
        // @mechanism LocationTag.patterns
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
        // @description
        // Gets the rotation of the head at this location. Can be 1-16.
        // -->
        tagProcessor.registerTag(ElementTag.class, "head_rotation", (attribute, object) -> {
            return new ElementTag(object.getSkullRotation(((Skull) object.getBlockStateForTag(attribute)).getRotation()) + 1);
        });

        // <--[tag]
        // @attribute <LocationTag.switched>
        // @returns ElementTag(Boolean)
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
        // @description
        // Returns a list of lines on a sign.
        // -->
        tagProcessor.registerTag(ListTag.class, "sign_contents", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof Sign) {
                return new ListTag(Arrays.asList(AdvancedTextImpl.instance.getSignLines(((Sign) object.getBlockStateForTag(attribute)))));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_type>
        // @mechanism LocationTag.spawner_type
        // @returns EntityTag
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
        // @description
        // Returns the full "display entity" for the spawner. This can contain more data than just a type.
        // -->
        tagProcessor.registerTag(EntityTag.class, "spawner_display_entity", (attribute, object) -> {
            if (!(object.getBlockStateForTag(attribute) instanceof CreatureSpawner)) {
                return null;
            }
            return NMSHandler.getEntityHelper().getMobSpawnerDisplayEntity(((CreatureSpawner) object.getBlockStateForTag(attribute))).describe(attribute.context);
        });

        // <--[tag]
        // @attribute <LocationTag.spawner_spawn_delay>
        // @mechanism LocationTag.spawner_delay_data
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_delay_data
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_delay_data
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_player_range
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_range
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_max_nearby_entities
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.spawner_count
        // @returns ElementTag(Number)
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
        // @mechanism LocationTag.lock
        // @returns ElementTag
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
        // @mechanism LocationTag.lock
        // @returns ElementTag(Boolean)
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
        // @mechanism LocationTag.lock
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the container is lockable.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_lockable", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof Lockable);
        });

        // <--[tag]
        // @attribute <LocationTag.drops[(<item>)]>
        // @returns ListTag(ItemTag)
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
        // @attribute <LocationTag.hive_bee_count>
        // @returns ElementTag(Number)
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
        // @description
        // Returns the maximum number of bees allowed inside a hive.
        // -->
        tagProcessor.registerTag(ElementTag.class, "hive_max_bees", (attribute, object) -> {
            return new ElementTag(((Beehive) object.getBlockStateForTag(attribute)).getMaxEntities());
        });

        // <--[tag]
        // @attribute <LocationTag.skull_type>
        // @returns ElementTag
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
        // @description
        // Returns the name of the skin the skull is displaying.
        // -->
        tagProcessor.registerTag(ElementTag.class, "skull_name", (attribute, object) -> {
            BlockState blockState = object.getBlockStateForTag(attribute);
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.getBlockHelper().getPlayerProfile((Skull) blockState);
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
        // @description
        // Returns the skin the skull is displaying - just the name or UUID as text, not a player object.
        // -->
        tagProcessor.registerTag(ElementTag.class, "skull_skin", (attribute, object) -> {
            BlockState blockState = object.getBlockStateForTag(attribute);
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.getBlockHelper().getPlayerProfile((Skull) blockState);
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
            float precision = (float) attribute.getDoubleParam();
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
        // @description
        // Returns a simple version of the LocationTag's block coordinates.
        // In the format: x,y,z,world
        // For example: 1,2,3,world_nether
        // -->
        tagProcessor.registerTag(ElementTag.class, "simple", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.simple.formatted>
            // @returns ElementTag
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

        /////////////////////
        //   DIRECTION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.precise_impact_normal[(<range>)]>
        // @returns LocationTag
        // @description
        // Returns the exact impact normal at the location this location is pointing at.
        // In minecraft, the impact normal is generally the side of the block that the location is facing.
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_impact_normal", (attribute, object) -> {
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
        // @description
        // Returns the block location this location is pointing at.
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_cursor_on_block", (attribute, object) -> {
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
        // @description
        // Returns the exact location this location is pointing at.
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_cursor_on", (attribute, object) -> {
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
        // @description
        // Returns the entity this location is pointing at, using precise ray trace logic.
        // Optionally, specify a maximum range to find the entity from (defaults to 100).
        // -->
        tagProcessor.registerTag(EntityTag.class, "precise_target", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 100;
            }
            RayTraceResult result;
            // <--[tag]
            // @attribute <LocationTag.precise_target[(<range>)].type[<entity_type>|...]>
            // @returns EntityTag
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
                return new EntityTag(result.getHitEntity());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_target_position[(<range>)]>
        // @returns LocationTag
        // @description
        // Returns the precise location this location is pointing at, when tracing against entities.
        // Optionally, specify a maximum range to find the entity from (defaults to 100).
        // -->
        tagProcessor.registerTag(LocationTag.class, "precise_target_position", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 100;
            }
            RayTraceResult result;
            // <--[tag]
            // @attribute <LocationTag.precise_target_position[(<range>)].type[<entity_type>|...]>
            // @returns LocationTag
            // @description
            // Returns the precise location this location is pointing at, when tracing against entities.
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
            if (result != null) {
                return new LocationTag(object.getWorld(), result.getHitPosition());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.points_between[<location>]>
        // @returns ListTag(LocationTag)
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
            // @description
            // Finds all locations between this location and another, separated by the specified distance each.
            // -->
            double rad = 1d;
            if (attribute.startsWith("distance", 2)) {
                rad = attribute.getDoubleContext(2);
                attribute.fulfill(1);
            }
            ListTag list = new ListTag();
            org.bukkit.util.Vector rel = target.toVector().subtract(object.toVector());
            double len = rel.length();
            if (len < 0.0001) {
                return new ListTag();
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
        // @description
        // Finds all block locations in the direction this location is facing,
        // optionally with a custom range (default is 100).
        // For example a location at 0,0,0 facing straight up
        // will include 0,1,0 0,2,0 and so on.
        // -->
        tagProcessor.registerTag(ListTag.class, "facing_blocks", (attribute, object) -> {
            int range = attribute.getIntParam();
            if (range < 1) {
                range = 100;
            }
            ListTag list = new ListTag();
            try {
                NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                BlockIterator iterator = new BlockIterator(object, 0, range);
                while (iterator.hasNext()) {
                    list.addObject(new LocationTag(iterator.next().getLocation()));
                }
            }
            finally {
                NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.line_of_sight[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the specified location is within this location's
        // line of sight.
        // -->
        tagProcessor.registerTag(ElementTag.class, "line_of_sight", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            LocationTag location = attribute.paramAsType(LocationTag.class);
            if (location != null) {
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    return new ElementTag(NMSHandler.getEntityHelper().canTrace(object.getWorld(), object.toVector(), location.toVector()));
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.direction[(<location>)]>
        // @returns ElementTag
        // @description
        // Returns the compass direction between two locations.
        // If no second location is specified, returns the direction of the location.
        // Example returns include "north", "southwest", ...
        // -->
        tagProcessor.registerTag(ObjectTag.class, "direction", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.direction.vector>
            // @returns LocationTag
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
                EntityHelper entityHelper = NMSHandler.getEntityHelper();

                // <--[tag]
                // @attribute <LocationTag.direction[<location>].yaw>
                // @returns ElementTag(Decimal)
                // @description
                // Returns the yaw direction between two locations.
                // -->
                if (attribute.startsWith("yaw", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(EntityHelper.normalizeYaw(entityHelper.getYaw
                            (target.toVector().subtract(object.toVector())
                                    .normalize())));
                }
                else {
                    return new ElementTag(entityHelper.getCardinal(entityHelper.getYaw
                            (target.toVector().subtract(object.toVector())
                                    .normalize())));
                }
            }
            // Get a cardinal direction from this location's yaw
            else {
                return new ElementTag(NMSHandler.getEntityHelper().getCardinal(object.getYaw()));
            }
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_yaw[<#.#>]>
        // @returns LocationTag
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
        // @description
        // Returns a location containing a yaw/pitch that point from the current location
        // to the target location.
        // -->
        tagProcessor.registerTag(LocationTag.class, "face", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            Location two = attribute.paramAsType(LocationTag.class);
            return new LocationTag(NMSHandler.getEntityHelper().faceLocation(object, two));
        });

        // <--[tag]
        // @attribute <LocationTag.facing[<entity>/<location>]>
        // @returns ElementTag(Boolean)
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
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Tag location.facing[...] was given an invalid facing target.");
                    }
                    return null;
                }

                // <--[tag]
                // @attribute <LocationTag.facing[<entity>/<location>].degrees[<#>(,<#>)]>
                // @returns ElementTag(Boolean)
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
                        return new ElementTag(NMSHandler.getEntityHelper().isFacingLocation(object, facingLoc, degrees, pitchDegrees));
                    }
                    else {
                        degrees = Integer.parseInt(context);
                    }
                }

                return new ElementTag(NMSHandler.getEntityHelper().isFacingLocation(object, facingLoc, degrees));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.pitch>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the pitch of the object at the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "pitch", (attribute, object) -> {
            return new ElementTag(object.getPitch());
        });

        // <--[tag]
        // @attribute <LocationTag.with_pose[<entity>/<pitch>,<yaw>]>
        // @returns LocationTag
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
            // @description
            // Returns the raw yaw of the object at the location.
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
        // @description
        // Returns the location-vector rotated around the x axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[0,1,0].rotate_around_x[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_x", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double angle = attribute.getDoubleParam();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double y = (object.getY() * cos) - (object.getZ() * sin);
            double z = (object.getY() * sin) + (object.getZ() * cos);
            Location location = object.clone();
            location.setY(y);
            location.setZ(z);
            return new LocationTag(location);
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_y[<#.#>]>
        // @returns LocationTag
        // @description
        // Returns the location-vector rotated around the y axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[1,0,0].rotate_around_y[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_y", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double angle = attribute.getDoubleParam();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = (object.getX() * cos) + (object.getZ() * sin);
            double z = (object.getX() * -sin) + (object.getZ() * cos);
            Location location = object.clone();
            location.setX(x);
            location.setZ(z);
            return new LocationTag(location);
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_z[<#.#>]>
        // @returns LocationTag
        // @description
        // Returns the location-vector rotated around the z axis by a specified angle in radians.
        // Generally used in a format like <player.location.add[<location[1,0,0].rotate_around_z[<[some_angle]>]>]>.
        // -->
        tagProcessor.registerTag(LocationTag.class, "rotate_around_z", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            double angle = attribute.getDoubleParam();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = (object.getX() * cos) - (object.getY() * sin);
            double y = (object.getX() * sin) + (object.getY() * cos);
            Location location = object.clone();
            location.setX(x);
            location.setY(y);
            return new LocationTag(location);
        });

        /////////////////////
        //   ENTITY AND BLOCK LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.flood_fill[<limit>]>
        // @returns ListTag(LocationTag)
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
            NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
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
                NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
            }
            return new ListTag((Collection<LocationTag>) flooder.result);
        });


        // <--[tag]
        // @attribute <LocationTag.find_nearest_biome[<biome>]>
        // @returns LocationTag
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
            Location result = NMSHandler.getWorldHelper().getNearestBiomeLocation(object, biome);
            if (result == null) {
                return null;
            }
            return new LocationTag(result);
        });

        // <--[tag]
        // @attribute <LocationTag.find_blocks_flagged[<flag_name>].within[<#>]>
        // @returns ListTag(LocationTag)
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
            for (Entity entity : new WorldTag(object.getWorld()).getPossibleEntitiesForBoundary(box)) {
                if (Utilities.checkLocationWithBoundingBox(object, entity, radius)) {
                    EntityTag current = new EntityTag(entity);
                    if (matcher == null || BukkitScriptEvent.tryEntity(current, matcher)) {
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
            int radiusInt = (int) radius;
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
                            if (matcher == null || BukkitScriptEvent.tryMaterial(new LocationTag(tstart.clone().add(x, y, z)).getBlockTypeForTag(attribute), matcher)) {
                                found.addObject(new LocationTag(tstart.clone().add(x, y, z)));
                            }
                        }
                    }
                }
            }
            found.objectForms.sort((loc1, loc2) -> object.compare((LocationTag) loc1, (LocationTag) loc2));
            return found;
        });

        tagProcessor.registerTag(ObjectTag.class, "find", (attribute, object) -> {
            if (!attribute.startsWith("within", 3) || !attribute.hasContext(3)) {
                return null;
            }
            double radius = attribute.getDoubleContext(3);

            if (attribute.startsWith("blocks", 2)) {
                Deprecations.locationFindEntities.warn(attribute.context);
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

            // <--[tag]
            // @attribute <LocationTag.find.players.within[<#.#>]>
            // @returns ListTag(PlayerTag)
            // @description
            // Returns a list of players within a radius.
            // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
            // -->
            else if (attribute.startsWith("players", 2)) {
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

            // <--[tag]
            // @attribute <LocationTag.find.npcs.within[<#.#>]>
            // @returns ListTag(NPCTag)
            // @description
            // Returns a list of NPCs within a radius.
            // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
            // -->
            else if (attribute.startsWith("npcs", 2)) {
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
                Deprecations.locationFindEntities.warn(attribute.context);
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
            // @description
            // Returns a list of living entities within a radius.
            // This includes Players, mobs, NPCs, etc., but excludes dropped items, experience orbs, etc.
            // Result list is sorted by closeness (1 = closest, 2 = next closest, ... last = farthest).
            // -->
            else if (attribute.startsWith("living_entities", 2)) {
                ListTag found = new ListTag();
                attribute.fulfill(2);
                BoundingBox box = BoundingBox.of(object, radius, radius, radius);
                for (Entity entity : new WorldTag(object.getWorld()).getPossibleEntitiesForBoundary(box)) {
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

        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.formatted>
        // @returns ElementTag
        // @description
        // Returns the formatted version of the LocationTag.
        // In the format: X 'x.x', Y 'y.y', Z 'z.z', in world 'world'
        // For example: X '1.0', Y '2.0', Z '3.0', in world 'world_nether'
        // -->
        tagProcessor.registerTag(ElementTag.class, "formatted", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.formatted.citizens>
            // @returns ElementTag
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
        // @description
        // Returns the chunk that this location belongs to.
        // -->
        tagProcessor.registerTag(ChunkTag.class, "chunk", (attribute, object) -> {
            return new ChunkTag(object);
        }, "get_chunk");

        // <--[tag]
        // @attribute <LocationTag.raw>
        // @returns LocationTag
        // @description
        // Returns the raw representation of this location, without any note name.
        // -->
        tagProcessor.registerTag(LocationTag.class, "raw", (attribute, object) -> {
            LocationTag rawLocation = new LocationTag(object);
            rawLocation.setRaw(true);
            return rawLocation;
        });

        // <--[tag]
        // @attribute <LocationTag.world>
        // @returns WorldTag
        // @description
        // Returns the world that the location is in.
        // -->
        tagProcessor.registerTag(WorldTag.class, "world", (attribute, object) -> {
            return WorldTag.mirrorBukkitWorld(object.getWorld());
        });

        // <--[tag]
        // @attribute <LocationTag.x>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the X coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "x", (attribute, object) -> {
            return new ElementTag(object.getX());
        });

        // <--[tag]
        // @attribute <LocationTag.y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the Y coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "y", (attribute, object) -> {
            return new ElementTag(object.getY());
        });

        // <--[tag]
        // @attribute <LocationTag.z>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the Z coordinate of the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "z", (attribute, object) -> {
            return new ElementTag(object.getZ());
        });

        // <--[tag]
        // @attribute <LocationTag.xyz>
        // @returns ElementTag
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

        /////////////////////
        //   MATHEMATICAL ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.add[<location>]>
        // @returns LocationTag
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
                    return new LocationTag(object.clone().add(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2])));
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
                    return new LocationTag(object.clone().subtract(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2])));
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
                return new ElementTag(face.name());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.distance_squared[<location>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the distance between 2 locations, squared.
        // -->
        tagProcessor.registerTag(ElementTag.class, "distance_squared", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (LocationTag.matches(attribute.getParam())) {
                LocationTag toLocation = attribute.paramAsType(LocationTag.class);
                if (!object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Can't measure distance between two different worlds!");
                    }
                    return null;
                }
                return new ElementTag(object.distanceSquared(toLocation));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.distance[<location>]>
        // @returns ElementTag(Decimal)
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
                // @description
                // Returns the horizontal distance between 2 locations.
                // -->
                if (attribute.startsWith("horizontal", 2)) {

                    // <--[tag]
                    // @attribute <LocationTag.distance[<location>].horizontal.multiworld>
                    // @returns ElementTag(Decimal)
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
                // @description
                // Returns the vertical distance between 2 locations.
                // -->
                else if (attribute.startsWith("vertical", 2)) {

                    // <--[tag]
                    // @attribute <LocationTag.distance[<location>].vertical.multiworld>
                    // @returns ElementTag(Decimal)
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

                if (!object.getWorldName().equalsIgnoreCase(toLocation.getWorldName())) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Can't measure distance between two different worlds!");
                    }
                    return null;
                }
                else {
                    return new ElementTag(object.distance(toLocation));
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.is_within_border>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the location is within the world border.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_within_border", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().isInside(object));
        });

        // <--[tag]
        // @attribute <LocationTag.is_within[<area>]>
        // @returns ElementTag(Boolean)
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

        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.biome>
        // @mechanism LocationTag.biome
        // @returns BiomeTag
        // @description
        // Returns the biome at the location.
        // -->
        tagProcessor.registerTag(ObjectTag.class, "biome", (attribute, object) -> {
            if (attribute.startsWith("formatted", 2)) {
                Deprecations.locationBiomeFormattedTag.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(CoreUtilities.toLowerCase(object.getBiomeForTag(attribute).getName()).replace('_', ' '));
            }
            return new BiomeTag(object.getBiomeForTag(attribute));
        });

        // <--[tag]
        // @attribute <LocationTag.cuboids>
        // @returns ListTag(CuboidTag)
        // @description
        // Returns a ListTag of all noted CuboidTags that include this location.
        // -->
        tagProcessor.registerTag(ListTag.class, "cuboids", (attribute, object) -> {
            List<CuboidTag> cuboids = CuboidTag.getNotableCuboidsContaining(object);
            ListTag cuboid_list = new ListTag();
            for (CuboidTag cuboid : cuboids) {
                cuboid_list.addObject(cuboid);
            }
            return cuboid_list;
        });

        // <--[tag]
        // @attribute <LocationTag.ellipsoids>
        // @returns ListTag(EllipsoidTag)
        // @description
        // Returns a ListTag of all noted EllipsoidTags that include this location.
        // -->
        tagProcessor.registerTag(ListTag.class, "ellipsoids", (attribute, object) -> {
            List<EllipsoidTag> ellipsoids = EllipsoidTag.getNotableEllipsoidsContaining(object);
            ListTag ellipsoid_list = new ListTag();
            for (EllipsoidTag ellipsoid : ellipsoids) {
                ellipsoid_list.addObject(ellipsoid);
            }
            return ellipsoid_list;
        });

        // <--[tag]
        // @attribute <LocationTag.polygons>
        // @returns ListTag(PolygonTag)
        // @description
        // Returns a ListTag of all noted PolygonTags that include this location.
        // -->
        tagProcessor.registerTag(ListTag.class, "polygons", (attribute, object) -> {
            List<PolygonTag> polygons = PolygonTag.getNotedPolygonsContaining(object);
            ListTag polygon_list = new ListTag();
            for (PolygonTag polygon : polygons) {
                polygon_list.addObject(polygon);
            }
            return polygon_list;
        });

        // <--[tag]
        // @attribute <LocationTag.is_liquid>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the block at the location is a liquid.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_liquid", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    return new ElementTag(b.isLiquid());
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.light>
        // @returns ElementTag(Number)
        // @description
        // Returns the total amount of light on the location.
        // -->
        tagProcessor.registerTag(ElementTag.class, "light", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());

                    // <--[tag]
                    // @attribute <LocationTag.light.blocks>
                    // @returns ElementTag(Number)
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
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.power>
        // @returns ElementTag(Number)
        // @description
        // Returns the current redstone power level of a block.
        // -->
        tagProcessor.registerTag(ElementTag.class, "power", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            if (b != null) {
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(object.getWorld());
                    return new ElementTag(object.getBlockForTag(attribute).getBlockPower());
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(object.getWorld());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.lectern_page>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.lectern_page
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
        // @mechanism LocationTag.clear_loot_table
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
        // @group properties
        // @description
        // Returns a number of how many blocks away from a connected tree leaves are.
        // Defaults to 7 if not connected to a tree.
        // -->
        tagProcessor.registerTag(ElementTag.class, "tree_distance", (attribute, object) -> {
            MaterialTag material = new MaterialTag(object.getBlockForTag(attribute));
            if (MaterialPersistent.describes(material)) {
                return new ElementTag(MaterialPersistent.getFrom(material).getDistance());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.command_block_name>
        // @returns ElementTag
        // @mechanism LocationTag.command_block_name
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
        // @description
        // Returns the burn time a furnace has left.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_burn_duration", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getBurnTime());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_burn_time", (attribute, object) -> {
            Deprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getBurnTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_duration>
        // @returns DurationTag
        // @mechanism LocationTag.furnace_cook_duration
        // @description
        // Returns the cook time a furnace has been cooking its current item for.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_cook_duration", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getCookTime());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_cook_time", (attribute, object) -> {
            Deprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_duration_total>
        // @returns DurationTag
        // @mechanism LocationTag.furnace_cook_duration_total
        // @description
        // Returns the total cook time a furnace has left.
        // -->
        tagProcessor.registerTag(DurationTag.class, "furnace_cook_duration_total", (attribute, object) -> {
            return new DurationTag((long) ((Furnace) object.getBlockStateForTag(attribute)).getCookTimeTotal());
        });
        tagProcessor.registerTag(ElementTag.class, "furnace_cook_time_total", (attribute, object) -> {
            Deprecations.furnaceTimeTags.warn(attribute.context);
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTimeTotal());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_tier>
        // @returns ElementTag(Number)
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
        // @description
        // Returns the block this block is attached to.
        // (For buttons, levers, signs, torches, etc).
        // -->
        tagProcessor.registerTag(LocationTag.class, "attached_to", (attribute, object) -> {
            BlockFace face = BlockFace.SELF;
            MaterialTag material = new MaterialTag(object.getBlockForTag(attribute));
            if (material.getMaterial() == Material.TORCH || material.getMaterial() == Material.REDSTONE_TORCH || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16) && material.getMaterial() == Material.SOUL_TORCH)) {
                face = BlockFace.DOWN;
            }
            else if (material.getMaterial() == Material.WALL_TORCH || material.getMaterial() == Material.REDSTONE_WALL_TORCH || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16) && material.getMaterial() == Material.SOUL_WALL_TORCH)) {
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
        // @description
        // If the location is part of a double-block structure (double chests, double plants, doors, beds, etc),
        // returns the location of the other block in the double-block structure.
        // -->
        tagProcessor.registerTag(LocationTag.class, "other_block", (attribute, object) -> {
            Block b = object.getBlockForTag(attribute);
            MaterialTag material = new MaterialTag(b);
            if (MaterialHalf.describes(material)) {
                Vector vec = MaterialHalf.getFrom(material).getRelativeBlockVector();
                if (vec != null) {
                    return new LocationTag(object.clone().add(vec));
                }
            }
            if (!attribute.hasAlternative()) {
                Debug.echoError("Block of type " + object.getBlockTypeForTag(attribute).name() + " isn't supported by other_block.");
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.custom_name>
        // @returns ElementTag
        // @mechanism LocationTag.custom_name
        // @description
        // Returns the custom name of this block.
        // Only works for nameable blocks, such as chests and dispensers.
        // -->
        tagProcessor.registerTag(ElementTag.class, "custom_name", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof Nameable) {
                return new ElementTag(((Nameable) object.getBlockStateForTag(attribute)).getCustomName());
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.local_difficulty>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the local difficulty (damage scaler) at the location.
        // This is based internally on multiple factors, including <@link tag ChunkTag.inhabited_time> and <@link tag WorldTag.difficulty>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "local_difficulty", (attribute, object) -> {
            return new ElementTag(NMSHandler.getWorldHelper().getLocalDifficulty(object));
        });

        // <--[tag]
        // @attribute <LocationTag.jukebox_record>
        // @returns ItemTag
        // @mechanism LocationTag.jukebox_record
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
        // @returns ElementTag
        // @mechanism LocationTag.jukebox_play
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
    }

    public static ObjectTagProcessor<LocationTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a location!");
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
                Debug.echoError("LocationTag.block_facing mechanism failed: block is not directional.");
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
        // @name spawner_type
        // @input EntityTag
        // @description
        // Sets the entity that a mob spawner will spawn.
        // @tags
        // <LocationTag.spawner_type>
        // -->
        if (mechanism.matches("spawner_type") && mechanism.requireObject(EntityTag.class) && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setSpawnedType(mechanism.valueAsType(EntityTag.class).getBukkitEntityType());
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
                AdvancedTextImpl.instance.setSignLine(state, i, "");
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            CoreUtilities.fixNewLinesToListSeparation(list);
            if (list.size() > 4) {
                Debug.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    AdvancedTextImpl.instance.setSignLine(state, i, list.get(i));
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
                profile = NMSHandler.getInstance().fillPlayerProfile(profile);
                if (texture != null) { // Ensure we didn't get overwritten
                    profile.setTexture(texture);
                }
                NMSHandler.getBlockHelper().setPlayerProfile((Skull) blockState, profile);
            }
            else {
                Debug.echoError("Unable to set skull_skin on block of type " + material.name() + " with state " + blockState.getClass().getCanonicalName());
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
                ((Nameable) state).setCustomName(title);
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
            Deprecations.furnaceTimeTags.warn(mechanism.context);
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
            Deprecations.furnaceTimeTags.warn(mechanism.context);
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
            Deprecations.furnaceTimeTags.warn(mechanism.context);
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
                    Debug.echoError("Could not apply pattern to banner: " + string);
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
        if (mechanism.matches("generate_tree") && mechanism.requireEnum(false, TreeType.values())) {
            boolean generated = getWorld().generateTree(this, TreeType.valueOf(mechanism.getValue().asString().toUpperCase()));
            if (!generated) {
                Debug.echoError("Could not generate tree at " + identifySimple() + ". Make sure this location can naturally generate a tree!");
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
                Debug.echoError("'activate' mechanism does not work for blocks of type: " + state.getType().name());
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
                Debug.echoError("'lectern_page' mechanism can only be called on a lectern block.");
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
                Debug.echoError("'clear_loot_table' mechanism can only be called on a lootable block (like a chest).");
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
                    NMSHandler.getBlockHelper().makeBlockStateRaw(state);
                    ((Jukebox) state).setRecord(null);
                }
                state.update();
            }
            else {
                Debug.echoError("'jukebox_record' mechanism can only be called on a jukebox block.");
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
                        Debug.echoError("'jukebox_play' cannot play nothing.");
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
                Debug.echoError("'jukebox_play' mechanism can only be called on a jukebox block.");
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
                Debug.echoError("'age' mechanism can only be called on end gateway blocks.");
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
                Debug.echoError("'is_exact_teleport' mechanism can only be called on end gateway blocks.");
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
                Debug.echoError("'exit_location' mechanism can only be called on end gateway blocks.");
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
            NMSHandler.getBlockHelper().doRandomTick(this);
        }

        // <--[mechanism]
        // @object LocationTag
        // @name apply_bonemeal
        // @input ElementTag
        // @description
        // Applies bonemeal to the block, on the given block face. Input is NORTH, EAST, SOUTH, WEST, UP, or DOWN.
        // For example: - adjust <player.location.below> apply_bonemeal:up
        // -->
        if (mechanism.matches("apply_bonemeal") && mechanism.requireEnum(false, BlockFace.values())) {
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
                Debug.echoError("'campfire_items' mechanism can only be called on campfire blocks.");
            }
            else {
                Campfire fire = (Campfire) state;
                List<ItemTag> list = mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context);
                for (int i = 0; i < list.size(); i++) {
                    if (i >= fire.getSize()) {
                        Debug.echoError("Cannot add item for index " + (i + 1) + " as the campfire can only hold " + fire.getSize() + " items.");
                        break;
                    }
                    fire.setItem(i, list.get(i).getItemStack());
                }
                fire.update();
            }
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }

    @Override
    public boolean advancedMatches(String matcher) {
        return BukkitScriptEvent.tryLocation(this, matcher);
    }
}
