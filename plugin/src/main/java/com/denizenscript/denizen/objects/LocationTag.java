package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.objects.properties.material.MaterialDirectional;
import com.denizenscript.denizen.objects.properties.material.MaterialHalf;
import com.denizenscript.denizen.objects.properties.material.MaterialSwitchFace;
import com.denizenscript.denizen.objects.properties.material.MaterialPersistent;
import com.denizenscript.denizen.scripts.commands.world.SwitchCommand;
import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
import com.denizenscript.denizen.utilities.world.PathFinder;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.OldMaterialsHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Comparator;

public class LocationTag extends org.bukkit.Location implements ObjectTag, Notable, Adjustable {

    // <--[language]
    // @name LocationTag Objects
    // @group Object System
    // @description
    // A LocationTag represents a point in the world.
    //
    // For format info, see <@link language l@>
    //
    // -->

    // <--[language]
    // @name l@
    // @group Object Fetcher System
    // @description
    // l@ refers to the 'object identifier' of a LocationTag. The 'l@' is notation for Denizen's Object
    // Fetcher. Note that 'l' is a lowercase 'L', the first letter in 'location'.
    // The full constructor for a LocationTag is: 'l@<x>,<y>,<z>,<pitch>,<yaw>,<world>'
    // Note that you can leave off the world, and/or pitch and yaw, and/or the z value.
    // You cannot leave off both the z and the pitch+yaw at the same time.
    // For example, 'l@1,2.15,3,45,90,space' or 'l@7.5,99,3.2'
    //
    // For general info, see <@link language LocationTag Objects>
    //
    // -->

    /**
     * The world name if a world reference is bad.
     */
    public String backupWorld;

    public String getWorldName() {
        World w = super.getWorld();
        if (w != null) {
            return w.getName();
        }
        return backupWorld;
    }

    @Override
    public World getWorld() {
        World w = super.getWorld();
        if (w != null) {
            return w;
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
        NotableManager.saveAs(this, id);
    }

    @Note("Locations")
    public String getSaveObject() {
        return (getX())
                + "," + getY()
                + "," + (getZ())
                + "," + getPitch()
                + "," + getYaw()
                + "," + getWorldName();
    }

    public static String getSaved(LocationTag location) {
        for (LocationTag saved : NotableManager.getAllType(LocationTag.class)) {
            if (saved.getX() != location.getX()) {
                continue;
            }
            if (saved.getY() != location.getY()) {
                continue;
            }
            if (saved.getZ() != location.getZ()) {
                continue;
            }
            if (saved.getYaw() != location.getYaw()) {
                continue;
            }
            if (saved.getPitch() != location.getPitch()) {
                continue;
            }
            if ((saved.getWorldName() == null && location.getWorldName() == null)
                    || (saved.getWorldName() != null && location.getWorldName() != null && saved.getWorldName().equals(location.getWorldName()))) {
                return NotableManager.getSavedId(saved);
            }
        }
        return null;
    }

    public void forget() {
        NotableManager.remove(this);
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////


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

        Notable noted = NotableManager.getSavedObject(string);
        if (noted instanceof LocationTag) {
            return (LocationTag) noted;
        }

        ////////
        // Match location formats

        // Split values
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
                if (context == null || context.debug) {
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
                if (context == null || context.debug) {
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
                if (context == null || context.debug) {
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
                return new LocationTag(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)),
                        Float.valueOf(split.get(3)),
                        Float.valueOf(split.get(4)));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
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
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new LocationTag(world,
                            Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1)),
                            Double.valueOf(split.get(2)),
                            Float.valueOf(split.get(3)),
                            Float.valueOf(split.get(4)));
                }
                LocationTag output = new LocationTag(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)),
                        Float.valueOf(split.get(3)),
                        Float.valueOf(split.get(4)));
                output.backupWorld = worldName;
                return output;
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    Debug.log("Minor: valueOf LocationTag returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }

        if (context == null || context.debug) {
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

        return LocationTag.valueOf(string, new BukkitTagContext(null, null, false, null, false, null)) != null;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    private boolean is2D = false;

    /**
     * Turns a Bukkit Location into a LocationTag, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public LocationTag(Location location) {
        // Just save the yaw and pitch as they are; don't check if they are
        // higher than 0, because Minecraft yaws are weird and can have
        // negative values
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public LocationTag(Vector vector) {
        super(null, vector.getX(), vector.getY(), vector.getZ());
    }

    public LocationTag(World world, Vector vector) {
        super(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public LocationTag(World world, double x, double y) {
        this(world, x, y, 0);
        this.is2D = true;
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
        super(world, x, y, z);
    }

    public LocationTag(double x, double y, double z, String worldName) {
        super(worldName == null ? null : Bukkit.getWorld(worldName), x, y, z);
        backupWorld = worldName;
    }

    public LocationTag(World world, double x, double y, double z, float pitch, float yaw) {
        super(world, x, y, z, yaw, pitch);
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

    public Biome getBiomeForTag(Attribute attribute) {
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
            return super.getBlock().getBiome();
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

    public LocationTag getBlockLocation() {
        return new LocationTag(getWorld(), getBlockX(), getBlockY(), getBlockZ());
    }

    /**
     * Indicates whether this location is forced to identify as a notable or not.
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

    public boolean hasInventory() {
        return getBlockState() instanceof InventoryHolder;
    }

    public Inventory getBukkitInventory() {
        return hasInventory() ? ((InventoryHolder) getBlockState()).getInventory() : null;
    }

    public InventoryTag getInventory() {
        return hasInventory() ? InventoryTag.mirrorBukkitInventory(getBukkitInventory()) : null;
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

    public int compare(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.equals(loc2)) {
            return 0;
        }
        else {
            double dist = distanceSquared(loc1) - distanceSquared(loc2);
            return dist == 0 ? 0 : (dist > 0 ? 1 : -1);
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
                && !getWorldName().equalsIgnoreCase(other.getWorldName()))) {
            return false;
        }
        return Math.floor(getX()) == Math.floor(other.getX())
                && Math.floor(getY()) == Math.floor(other.getY())
                && Math.floor(getZ()) == Math.floor(other.getZ());
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
        if (isUnique()) {
            return "<Y>" + getSaved(this) + "<GR> (" + identifyRaw().replace(",", "<G>,<GR> ") + "<GR>)";
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
        if (!raw && isUnique()) {
            return "l@" + getSaved(this);
        }
        else {
            return identifyRaw();
        }
    }

    @Override
    public String identifySimple() {
        if (isUnique()) {
            return "l@" + getSaved(this);
        }
        else if (getWorldName() == null) {
            return "l@" + getBlockX() + "," + getBlockY() + (!is2D ? "," + getBlockZ() : "");
        }
        else {
            return "l@" + getBlockX() + "," + getBlockY() + (!is2D ? "," + getBlockZ() : "")
                    + "," + getWorldName();
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
                    + (!is2D ? "," + CoreUtilities.doubleToString(getZ()) : "")
                    + (getWorldName() != null ? "," + getWorldName() : "");
        }
    }

    @Override
    public String toString() {
        return identify();
    }

    public static void registerTags() {

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
        registerTag("block_facing", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
            MaterialTag material = new MaterialTag(block);
            if (!MaterialDirectional.describes(material)) {
                return null;
            }
            return new LocationTag(object.getWorld(), MaterialDirectional.getFrom(material).getDirectionVector());
        });

        // <--[tag]
        // @attribute <LocationTag.with_facing_direction>
        // @returns LocationTag
        // @description
        // Returns the location with its direction set to the block's facing direction.
        // Only works for block types that have directionality (such as signs, chests, stairs, etc.).
        // You can use <some_block_location.with_facing_direction.forward[1]> to get the block directly in front of this block (based on its facing direction).
        // -->
        registerTag("with_facing_direction", (attribute, object) -> {
            Block block = object.getBlockForTag(attribute);
            MaterialTag material = new MaterialTag(block);
            if (!MaterialDirectional.describes(material)) {
                return null;
            }
            Vector facing = MaterialDirectional.getFrom(material).getDirectionVector();
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
        registerTag("above", (attribute, object) -> {
            return new LocationTag(object.clone().add(0, attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1, 0));
        });

        // <--[tag]
        // @attribute <LocationTag.below[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location below this location. Optionally specify a number of blocks to go down.
        // This just moves straight along the Y axis, equivalent to <@link tag LocationTag.sub> with input 0,1,0 (or the input value instead of '1').
        // -->
        registerTag("below", (attribute, object) -> {
            return new LocationTag(object.clone().subtract(0, attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1, 0));
        });

        // <--[tag]
        // @attribute <LocationTag.forward_flat[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location in front of this location based on yaw but not pitch. Optionally specify a number of blocks to go forward.
        // -->
        registerTag("forward_flat", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.backward_flat[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location behind this location based on yaw but not pitch. Optionally specify a number of blocks to go backward.
        // This is equivalent to <@link tag LocationTag.forward_flat> in the opposite direction.
        // -->
        registerTag("backward_flat", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.forward[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location in front of this location based on pitch and yaw. Optionally specify a number of blocks to go forward.
        // -->
        registerTag("forward", (attribute, object) -> {
            Vector vector = object.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.backward[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location behind this location based on pitch and yaw. Optionally specify a number of blocks to go backward.
        // This is equivalent to <@link tag LocationTag.forward> in the opposite direction.
        // -->
        registerTag("backward", (attribute, object) -> {
            Vector vector = object.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.left[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location to the left of this location based on pitch and yaw. Optionally specify a number of blocks to go left.
        // This is equivalent to <@link tag LocationTag.forward> with a +90 degree rotation to the yaw and the pitch set to 0.
        // -->
        registerTag("left", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().rotateAroundY(Math.PI / 2).multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.right[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location to the right of this location based on pitch and yaw. Optionally specify a number of blocks to go right.
        // This is equivalent to <@link tag LocationTag.forward> with a -90 degree rotation to the yaw and the pitch set to 0.
        // -->
        registerTag("right", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(0);
            Vector vector = loc.getDirection().rotateAroundY(Math.PI / 2).multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.up[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location above this location based on pitch and yaw. Optionally specify a number of blocks to go up.
        // This is equivalent to <@link tag LocationTag.forward> with a +90 degree rotation to the pitch.
        // -->
        registerTag("up", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(loc.getPitch() - 90);
            Vector vector = loc.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().add(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.down[(<#.#>)]>
        // @returns LocationTag
        // @description
        // Returns the location below this location based on pitch and yaw. Optionally specify a number of blocks to go down.
        // This is equivalent to <@link tag LocationTag.forward> with a -90 degree rotation to the pitch.
        // -->
        registerTag("down", (attribute, object) -> {
            Location loc = object.clone();
            loc.setPitch(loc.getPitch() - 90);
            Vector vector = loc.getDirection().multiply(attribute.hasContext(1) ? attribute.getDoubleContext(1) : 1);
            return new LocationTag(object.clone().subtract(vector));
        });

        // <--[tag]
        // @attribute <LocationTag.relative[<location>]>
        // @returns LocationTag
        // @description
        // Returns the location relative to this location. Input is a vector location of the form left,up,forward.
        // For example, input -1,1,1 will return a location 1 block to the right, 1 block up, and 1 block forward.
        // -->
        registerTag("relative", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag offsetLoc = LocationTag.valueOf(attribute.getContext(1));
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
        // -->
        registerTag("block", (attribute, object) -> {
            return new LocationTag(object.getWorld(), object.getBlockX(), object.getBlockY(), object.getBlockZ());
        });

        // <--[tag]
        // @attribute <LocationTag.center>
        // @returns LocationTag
        // @description
        // Returns the location at the center of the block this location is on.
        // -->
        registerTag("center", (attribute, object) -> {
            return new LocationTag(object.getWorld(), object.getBlockX() + 0.5, object.getBlockY() + 0.5, object.getBlockZ() + 0.5);
        });

        // <--[tag]
        // @attribute <LocationTag.highest>
        // @returns LocationTag
        // @description
        // Returns the location of the highest solid block at the location.
        // -->
        registerTag("highest", (attribute, object) -> {
            return new LocationTag(object.getHighestBlockForTag(attribute).add(0, -1, 0));
        });

        // <--[tag]
        // @attribute <LocationTag.base_color>
        // @mechanism LocationTag.base_color
        // @returns ElementTag
        // @description
        // Returns the base color of the banner at this location.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // As of 1.13+, this tag is no longer relevant.
        // -->
        registerTag("base_color", (attribute, object) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                Debug.echoError("Base_Color tag no longer relevant: banner types are now distinct materials.");
            }
            DyeColor color = ((Banner) object.getBlockStateForTag(attribute)).getBaseColor();
            return new ElementTag(color != null ? color.name() : "BLACK");
        });

        // <--[tag]
        // @attribute <LocationTag.has_inventory>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the block at the location has an inventory.
        // -->
        registerTag("has_inventory", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof InventoryHolder);
        });

        // <--[tag]
        // @attribute <LocationTag.inventory>
        // @returns InventoryTag
        // @description
        // Returns the InventoryTag of the block at the location. If the
        // block is not a container, returns null.
        // -->
        registerTag("inventory", (attribute, object) -> {
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
        registerTag("material", (attribute, object) -> {
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
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // -->
        registerTag("patterns", (attribute, object) -> {
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
        registerTag("head_rotation", (attribute, object) -> {
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
        registerTag("switched", (attribute, object) -> {
            return new ElementTag(SwitchCommand.switchState(object.getBlockForTag(attribute)));
        });

        // <--[tag]
        // @attribute <LocationTag.sign_contents>
        // @returns ListTag
        // @mechanism LocationTag.sign_contents
        // @description
        // Returns a list of lines on a sign.
        // -->
        registerTag("sign_contents", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof Sign) {
                return new ListTag(Arrays.asList(((Sign) object.getBlockStateForTag(attribute)).getLines()));
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
        registerTag("spawner_type", (attribute, object) -> {
            if (object.getBlockStateForTag(attribute) instanceof CreatureSpawner) {
                return new EntityTag(DenizenEntityType.getByName(((CreatureSpawner) object.getBlockStateForTag(attribute))
                        .getSpawnedType().name()));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <LocationTag.lock>
        // @mechanism LocationTag.lock
        // @returns ElementTag
        // @description
        // Returns the password to a locked container.
        // -->
        registerTag("lock", (attribute, object) -> {
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
        registerTag("is_locked", (attribute, object) -> {
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
        registerTag("is_lockable", (attribute, object) -> {
            return new ElementTag(object.getBlockStateForTag(attribute) instanceof Lockable);
        });

        // <--[tag]
        // @attribute <LocationTag.drops[(<item>)]>
        // @returns ListTag(ItemTag)
        // @description
        // Returns what items the block at the location would drop if broken naturally.
        // Optionally specifier a breaker item.
        // -->
        registerTag("drops", (attribute, object) -> {
            ItemStack inputItem = null;
            if (attribute.hasContext(1)) {
                inputItem = ItemTag.valueOf(attribute.getContext(1), attribute.context).getItemStack();
            }
            ListTag list = new ListTag();
            for (ItemStack it : object.getDropsForTag(attribute, inputItem)) {
                list.addObject(new ItemTag(it));
            }
            return list;
        });

        // <--[tag]
        // @attribute <LocationTag.flowerpot_contents>
        // @returns ElementTag
        // @mechanism LocationTag.flowerpot_contents
        // @description
        // Returns the flower pot contents at the location.
        // NOTE: Replaced by materials (such as POTTED_CACTUS) in 1.13 and above.
        // -->
        registerTag("flowerpot_contents", (attribute, object) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                Debug.echoError("As of Minecraft version 1.13 potted flowers each have their own material, such as POTTED_CACTUS.");
            }
            else if (object.getBlockTypeForTag(attribute) == Material.FLOWER_POT) {
                MaterialData contents = NMSHandler.getBlockHelper().getFlowerpotContents(object.getBlockForTag(attribute));
                return OldMaterialsHelper.getMaterialFrom(contents.getItemType(), contents.getData());
            }
            return null;
        });


        // <--[tag]
        // @attribute <LocationTag.skull_type>
        // @returns ElementTag
        // @description
        // Returns the type of the skull.
        // -->
        registerTag("skull_type", (attribute, object) -> {
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
        registerTag("skull_name", (attribute, object) -> {
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
        registerTag("skull_skin", (attribute, object) -> {
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
                // @returns ElementTag|Element
                // @mechanism LocationTag.skull_skin
                // @description
                // Returns the skin the skull item is displaying - just the name or UUID as text, not a player object,
                // along with the permanently cached texture property.
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
        // @attribute <LocationTag.simple>
        // @returns ElementTag
        // @description
        // Returns a simple version of the LocationTag's block coordinates.
        // In the format: x,y,z,world
        // For example: 1,2,3,world_nether
        // -->
        registerTag("simple", (attribute, object) -> {
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
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // -->
        registerTag("precise_impact_normal", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            // TODO: after 1.12 support is dropped, World#rayTraceBlocks should be used.
            Location location = NMSHandler.getEntityHelper().getImpactNormal(object, object.getDirection(), range);
            if (location != null) {
                return new LocationTag(location);
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
        registerTag("precise_cursor_on_block", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            // TODO: after 1.12 support is dropped, World#rayTraceBlocks should be used.
            Location location = NMSHandler.getEntityHelper().rayTraceBlock(object, object.getDirection(), range);
            if (location != null) {
                return new LocationTag(location).getBlockLocation();
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
        registerTag("precise_cursor_on", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            // TODO: after 1.12 support is dropped, World#rayTraceBlocks should be used.
            Location location = NMSHandler.getEntityHelper().rayTrace(object, object.getDirection(), range);
            if (location != null) {
                return new LocationTag(location);
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.precise_target[(<range>)]>
        // @returns EntityTag
        // @description
        // Returns the entity this location is pointing at, using precise ray trace logic.
        // Optionally, specify a maximum range to find the entity from (defaults to 200).
        // -->
        registerTag("precise_target", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            RayTraceResult result = object.getWorld().rayTraceEntities(object, object.getDirection(), range);
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
        // Optionally, specify a maximum range to find the entity from (defaults to 200).
        // -->
        registerTag("precise_target_position", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            RayTraceResult result = object.getWorld().rayTraceEntities(object, object.getDirection(), range);
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
        registerTag("points_between", (attribute, object) -> {
            LocationTag target = LocationTag.valueOf(attribute.getContext(1));
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
        registerTag("facing_blocks", (attribute, object) -> {
            int range = attribute.getIntContext(1);
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
        registerTag("line_of_sight", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag location = LocationTag.valueOf(attribute.getContext(1));
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
        registerTag("direction", (attribute, object) -> {
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
            if (attribute.hasContext(1) && LocationTag.matches(attribute.getContext(1))) {
                // Subtract this location's vector from the other location's vector,
                // not the other way around
                LocationTag target = LocationTag.valueOf(attribute.getContext(1));
                EntityHelper entityHelper = NMSHandler.getEntityHelper();

                // <--[tag]
                // @attribute <LocationTag.direction[<location>].yaw>
                // @returns ElementTag(Decimal)
                // @description
                // Returns the yaw direction between two locations.
                // -->
                if (attribute.startsWith("yaw", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(entityHelper.normalizeYaw(entityHelper.getYaw
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
        // @attribute <LocationTag.face[<location>]>
        // @returns LocationTag
        // @description
        // Returns a location containing a yaw/pitch that point from the current location
        // to the target location.
        // -->
        registerTag("face", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            Location two = LocationTag.valueOf(attribute.getContext(1));
            return new LocationTag(NMSHandler.getEntityHelper().faceLocation(object, two));
        });

        // <--[tag]
        // @attribute <LocationTag.facing[<entity>/<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the location's yaw is facing another entity or location, within a limit of 45 degrees of yaw.
        // -->
        registerTag("facing", (attribute, object) -> {
            if (attribute.hasContext(1)) {

                // The default number of degrees if there is no degrees attribute
                int degrees = 45;
                LocationTag facingLoc;
                if (LocationTag.matches(attribute.getContext(1))) {
                    facingLoc = LocationTag.valueOf(attribute.getContext(1));
                }
                else if (EntityTag.matches(attribute.getContext(1))) {
                    facingLoc = EntityTag.valueOf(attribute.getContext(1)).getLocation();
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
        registerTag("pitch", (attribute, object) -> {
            return new ElementTag(object.getPitch());
        });

        // <--[tag]
        // @attribute <LocationTag.with_pose[<entity>/<pitch>,<yaw>]>
        // @returns LocationTag
        // @description
        // Returns the location with pitch and yaw.
        // -->
        registerTag("with_pose", (attribute, object) -> {
            String context = attribute.getContext(1);
            float pitch = 0f;
            float yaw = 0f;
            if (EntityTag.matches(context)) {
                EntityTag ent = EntityTag.valueOf(context);
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
            LocationTag loc = LocationTag.valueOf(object.identify());
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
        registerTag("yaw", (attribute, object) -> {
            // <--[tag]
            // @attribute <LocationTag.yaw.simple>
            // @returns ElementTag
            // @description
            // Returns the yaw as 'North', 'South', 'East', or 'West'.
            // -->
            if (attribute.startsWith("simple", 2)) {
                attribute.fulfill(1);
                float yaw = NMSHandler.getEntityHelper().normalizeYaw(object.getYaw());
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
            return new ElementTag(NMSHandler.getEntityHelper().normalizeYaw(object.getYaw()));
        });

        // <--[tag]
        // @attribute <LocationTag.rotate_around_x[<#.#>]>
        // @returns LocationTag
        // @description
        // Returns the location rotated around the x axis by a specified angle in radians.
        // -->
        registerTag("rotate_around_x", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            double angle = attribute.getDoubleContext(1);
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
        // Returns the location rotated around the y axis by a specified angle in radians.
        // -->
        registerTag("rotate_around_y", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            double angle = attribute.getDoubleContext(1);
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
        // Returns the location rotated around the z axis by a specified angle in radians.
        // -->
        registerTag("rotate_around_z", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            double angle = attribute.getDoubleContext(1);
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

        registerTag("find", (attribute, object) -> {
            if (!attribute.startsWith("within", 3) || !attribute.hasContext(3)) {
                return null;
            }
            double radius = attribute.getDoubleContext(3);

            // <--[tag]
            // @attribute <LocationTag.find.blocks[<block>|...].within[<#>]>
            // @returns ListTag
            // @description
            // Returns a list of matching blocks within a radius.
            // Note: current implementation measures the center of nearby block's distance from the exact given location.
            // -->
            if (attribute.startsWith("blocks", 2)) {
                ArrayList<LocationTag> found = new ArrayList<>();
                List<MaterialTag> materials = new ArrayList<>();
                if (attribute.hasContext(2)) {
                    materials = ListTag.valueOf(attribute.getContext(2)).filter(MaterialTag.class, attribute.context);
                }
                // Avoid NPE from invalid materials
                if (materials == null) {
                    return null;
                }
                int max = Settings.blockTagsMaxBlocks();
                int index = 0;

                attribute.fulfill(2);
                Location tstart = object.getBlockForTag(attribute).getLocation();
                double tstartY = tstart.getY();
                int radiusInt = (int) radius;

                fullloop:
                for (int x = -radiusInt; x <= radiusInt; x++) {
                    for (int y = -radiusInt; y <= radiusInt; y++) {
                        double newY = y + tstartY;
                        if (newY < 0 || newY > 255) {
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
                                        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12) && material.hasData() && material.getData() != 0) {
                                            BlockState bs = new LocationTag(tstart.clone().add(x, y, z)).getBlockStateForTag(attribute);
                                            if (bs != null && material.matchesMaterialData(bs.getData())) {
                                                found.add(new LocationTag(tstart.clone().add(x, y, z)));
                                            }
                                        }
                                        else if (material.getMaterial() == new LocationTag(tstart.clone().add(x, y, z)).getBlockTypeForTag(attribute)) {
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

                Collections.sort(found, new Comparator<LocationTag>() {
                    @Override
                    public int compare(LocationTag loc1, LocationTag loc2) {
                        return object.compare(loc1, loc2);
                    }
                });

                return new ListTag(found);
            }

            // <--[tag]
            // @attribute <LocationTag.find.surface_blocks[<block>|...].within[<#.#>]>
            // @returns ListTag
            // @description
            // Returns a list of matching surface blocks within a radius.
            // -->
            else if (attribute.startsWith("surface_blocks", 2)) {
                ArrayList<LocationTag> found = new ArrayList<>();
                List<MaterialTag> materials = new ArrayList<>();
                if (attribute.hasContext(2)) {
                    materials = ListTag.valueOf(attribute.getContext(2)).filter(MaterialTag.class, attribute.context);
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
                                        if (material.matchesBlock(l.getBlockForTag(attribute))) {
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

                Collections.sort(found, new Comparator<LocationTag>() {
                    @Override
                    public int compare(LocationTag loc1, LocationTag loc2) {
                        return object.compare(loc1, loc2);
                    }
                });

                return new ListTag(found);
            }

            // <--[tag]
            // @attribute <LocationTag.find.players.within[<#.#>]>
            // @returns ListTag
            // @description
            // Returns a list of players within a radius.
            // -->
            else if (attribute.startsWith("players", 2)) {
                ArrayList<PlayerTag> found = new ArrayList<>();
                attribute.fulfill(2);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isDead() && Utilities.checkLocation(object, player.getLocation(), radius)) {
                        found.add(new PlayerTag(player));
                    }
                }

                Collections.sort(found, new Comparator<PlayerTag>() {
                    @Override
                    public int compare(PlayerTag pl1, PlayerTag pl2) {
                        return object.compare(pl1.getLocation(), pl2.getLocation());
                    }
                });

                return new ListTag(found);
            }

            // <--[tag]
            // @attribute <LocationTag.find.npcs.within[<#.#>]>
            // @returns ListTag
            // @description
            // Returns a list of NPCs within a radius.
            // -->
            else if (attribute.startsWith("npcs", 2)) {
                ArrayList<NPCTag> found = new ArrayList<>();
                attribute.fulfill(2);
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.isSpawned() && Utilities.checkLocation(object.getBlockForTag(attribute).getLocation(), npc.getStoredLocation(), radius)) {
                        found.add(new NPCTag(npc));
                    }
                }

                Collections.sort(found, new Comparator<NPCTag>() {
                    @Override
                    public int compare(NPCTag npc1, NPCTag npc2) {
                        return object.compare(npc1.getLocation(), npc2.getLocation());
                    }
                });

                return new ListTag(found);
            }

            // <--[tag]
            // @attribute <LocationTag.find.entities[<entity>|...].within[<#.#>]>
            // @returns ListTag
            // @description
            // Returns a list of entities within a radius, with an optional search parameter
            // for the entity type.
            // -->
            else if (attribute.startsWith("entities", 2)) {
                ListTag ent_list = attribute.hasContext(2) ? ListTag.valueOf(attribute.getContext(2)) : new ListTag();
                ListTag found = new ListTag();
                attribute.fulfill(2);
                for (Entity entity : new WorldTag(object.getWorld()).getEntitiesForTag()) {
                    if (Utilities.checkLocation(object, entity.getLocation(), radius)) {
                        EntityTag current = new EntityTag(entity);
                        if (!ent_list.isEmpty()) {
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

                Collections.sort(found.objectForms, new Comparator<ObjectTag>() {
                    @Override
                    public int compare(ObjectTag ent1, ObjectTag ent2) {
                        return object.compare(((EntityFormObject) ent1).getLocation(), ((EntityFormObject) ent2).getLocation());
                    }
                });

                return new ListTag(found.objectForms);
            }

            // <--[tag]
            // @attribute <LocationTag.find.living_entities.within[<#.#>]>
            // @returns ListTag
            // @description
            // Returns a list of living entities within a radius.
            // -->
            else if (attribute.startsWith("living_entities", 2)) {
                ListTag found = new ListTag();
                attribute.fulfill(2);
                for (Entity entity : new WorldTag(object.getWorld()).getEntitiesForTag()) {
                    if (entity instanceof LivingEntity
                            && Utilities.checkLocation(object, entity.getLocation(), radius)) {
                        found.addObject(new EntityTag(entity).getDenizenObject());
                    }
                }

                Collections.sort(found.objectForms, new Comparator<ObjectTag>() {
                    @Override
                    public int compare(ObjectTag ent1, ObjectTag ent2) {
                        return object.compare(((EntityFormObject) ent1).getLocation(), ((EntityFormObject) ent2).getLocation());
                    }
                });

                return new ListTag(found.objectForms);
            }

            // <--[tag]
            // @attribute <LocationTag.find.structure[<type>].within[<#.#>]>
            // @returns LocationTag
            // @description
            // Returns the location of the nearest structure of the given type, within a maximum radius.
            // To get a list of valid structure types, use <@link tag server.list_structure_types>.
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
            // To get a list of valid structure types, use <@link tag server.list_structure_types>.
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
        registerTag("find_path", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag two = LocationTag.valueOf(attribute.getContext(1));
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
        registerTag("formatted", (attribute, object) -> {
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
        registerTag("chunk", (attribute, object) -> {
            return new ChunkTag(object);
        }, "get_chunk");

        // <--[tag]
        // @attribute <LocationTag.raw>
        // @returns LocationTag
        // @description
        // Returns the raw representation of this location,
        //         ignoring any notables it might match.
        // -->
        registerTag("raw", (attribute, object) -> {
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
        registerTag("world", (attribute, object) -> {
            return WorldTag.mirrorBukkitWorld(object.getWorld());
        });

        // <--[tag]
        // @attribute <LocationTag.x>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the X coordinate of the location.
        // -->
        registerTag("x", (attribute, object) -> {
            return new ElementTag(object.getX());
        });

        // <--[tag]
        // @attribute <LocationTag.y>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the Y coordinate of the location.
        // -->
        registerTag("y", (attribute, object) -> {
            return new ElementTag(object.getY());
        });

        // <--[tag]
        // @attribute <LocationTag.z>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the Z coordinate of the location.
        // -->
        registerTag("z", (attribute, object) -> {
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
        registerTag("xyz", (attribute, object) -> {
            return new ElementTag(object.getX() + "," + object.getY() + "," + object.getZ());
        });

        // <--[tag]
        // @attribute <LocationTag.with_x[<number>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed X value.
        // -->
        registerTag("with_x", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            output.setX(attribute.getDoubleContext(1));
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_y[<number>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed Y value.
        // -->
        registerTag("with_y", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            output.setY(attribute.getDoubleContext(1));
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_z[<number>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed Z value.
        // -->
        registerTag("with_z", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            output.setZ(attribute.getDoubleContext(1));
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_yaw[<number>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed yaw value.
        // -->
        registerTag("with_yaw", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            output.setYaw((float) attribute.getDoubleContext(1));
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_pitch[<number>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed pitch value.
        // -->
        registerTag("with_pitch", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            output.setPitch((float) attribute.getDoubleContext(1));
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.with_world[<world>]>
        // @returns LocationTag
        // @description
        // Returns a copy of the location with a changed world value.
        // -->
        registerTag("with_world", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            LocationTag output = object.clone();
            WorldTag world = WorldTag.valueOf(attribute.getContext(1));
            output.setWorld(world.getWorld());
            return output;
        });

        // <--[tag]
        // @attribute <LocationTag.notable_name>
        // @returns ElementTag
        // @description
        // Gets the name of a Notable LocationTag. If the location isn't noted,
        // this is null.
        // -->
        registerTag("notable_name", (attribute, object) -> {
            String notname = NotableManager.getSavedId((object));
            if (notname == null) {
                return null;
            }
            return new ElementTag(notname);
        });


        /////////////////////
        //   MATHEMATICAL ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <LocationTag.add[<location>]>
        // @returns LocationTag
        // @description
        // Returns the location with the specified coordinates added to it.
        // -->
        registerTag("add", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            String[] ints = attribute.getContext(1).replace("l@", "").split(",", 4); // TODO: Just LocationTag.valueOf?
            if (ints.length >= 3) {
                if ((ArgumentHelper.matchesDouble(ints[0]) || ArgumentHelper.matchesInteger(ints[0]))
                        && (ArgumentHelper.matchesDouble(ints[1]) || ArgumentHelper.matchesInteger(ints[1]))
                        && (ArgumentHelper.matchesDouble(ints[2]) || ArgumentHelper.matchesInteger(ints[2]))) {
                    return new LocationTag(object.clone().add(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2])));
                }
            }
            else if (LocationTag.matches(attribute.getContext(1))) {
                return new LocationTag(object.clone().add(LocationTag.valueOf(attribute.getContext(1))));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.sub[<location>]>
        // @returns LocationTag
        // @description
        // Returns the location with the specified coordinates subtracted from it.
        // -->
        registerTag("sub", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            String[] ints = attribute.getContext(1).replace("l@", "").split(",", 4); // TODO: Just LocationTag.valueOf?
            if (ints.length == 3 || ints.length == 4) {
                if ((ArgumentHelper.matchesDouble(ints[0]) || ArgumentHelper.matchesInteger(ints[0]))
                        && (ArgumentHelper.matchesDouble(ints[1]) || ArgumentHelper.matchesInteger(ints[1]))
                        && (ArgumentHelper.matchesDouble(ints[2]) || ArgumentHelper.matchesInteger(ints[2]))) {
                    return new LocationTag(object.clone().subtract(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2])));
                }
            }
            else if (LocationTag.matches(attribute.getContext(1))) {
                return new LocationTag(object.clone().subtract(LocationTag.valueOf(attribute.getContext(1))));
            }
            return null;
        });

        // <--[tag]
        // @attribute <LocationTag.mul[<length>]>
        // @returns LocationTag
        // @description
        // Returns the location multiplied by the specified length.
        // -->
        registerTag("mul", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new LocationTag(object.clone().multiply(Double.parseDouble(attribute.getContext(1))));
        });

        // <--[tag]
        // @attribute <LocationTag.div[<length>]>
        // @returns LocationTag
        // @description
        // Returns the location divided by the specified length.
        // -->
        registerTag("div", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            return new LocationTag(object.clone().multiply(1D / Double.parseDouble(attribute.getContext(1))));
        });

        // <--[tag]
        // @attribute <LocationTag.normalize>
        // @returns LocationTag
        // @description
        // Returns a 1-length vector in the same direction as this vector location.
        // -->
        registerTag("normalize", (attribute, object) -> {
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
        registerTag("vector_length", (attribute, object) -> {
            return new ElementTag(Math.sqrt(Math.pow(object.getX(), 2) + Math.pow(object.getY(), 2) + Math.pow(object.getZ(), 2)));
        });

        // <--[tag]
        // @attribute <LocationTag.vector_to_face>
        // @returns ElementTag
        // @description
        // Returns the name of the BlockFace represented by a vector.
        // Result can be any of the following:
        // NORTH, EAST, SOUTH, WEST, UP, DOWN, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST,
        // WEST_NORTH_WEST, NORTH_NORTH_WEST, NORTH_NORTH_EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST,
        // SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST, WEST_SOUTH_WEST, SELF
        // -->
        registerTag("vector_to_face", (attribute, object) -> {
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
        registerTag("distance_squared", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            if (LocationTag.matches(attribute.getContext(1))) {
                LocationTag toLocation = LocationTag.valueOf(attribute.getContext(1));
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
        registerTag("distance", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            if (LocationTag.matches(attribute.getContext(1))) {
                LocationTag toLocation = LocationTag.valueOf(attribute.getContext(1));

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
        registerTag("is_within_border", (attribute, object) -> {
            return new ElementTag(object.getWorld().getWorldBorder().isInside(object));
        });

        // <--[tag]
        // @attribute <LocationTag.is_within[<cuboid>/<ellipsoid>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the location is within the cuboid or ellipsoid.
        // -->
        registerTag("is_within", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            if (EllipsoidTag.matches(attribute.getContext(1))) {
                EllipsoidTag ellipsoid = EllipsoidTag.valueOf(attribute.getContext(1));
                if (ellipsoid != null) {
                    return new ElementTag(ellipsoid.contains(object));
                }
            }
            else {
                CuboidTag cuboid = CuboidTag.valueOf(attribute.getContext(1));
                if (cuboid != null) {
                    return new ElementTag(cuboid.isInsideCuboid(object));
                }
            }
            return null;
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
        registerTag("biome", (attribute, object) -> {
            if (attribute.startsWith("formatted", 2)) {
                Deprecations.locationBiomeFormattedTag.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(CoreUtilities.toLowerCase(object.getBiomeForTag(attribute).name()).replace('_', ' '));
            }
            return new BiomeTag(object.getBiomeForTag(attribute));
        });

        // <--[tag]
        // @attribute <LocationTag.cuboids>
        // @returns ListTag(CuboidTag)
        // @description
        // Returns a ListTag of all notable CuboidTags that include this location.
        // -->
        registerTag("cuboids", (attribute, object) -> {
            List<CuboidTag> cuboids = CuboidTag.getNotableCuboidsContaining(object);
            ListTag cuboid_list = new ListTag();
            for (CuboidTag cuboid : cuboids) {
                cuboid_list.addObject(cuboid);
            }
            return cuboid_list;
        });

        // <--[tag]
        // @attribute <LocationTag.ellipsoids>
        // @returns ListTag(CuboidTag)
        // @description
        // Returns a ListTag of all notable EllipsoidTags that include this location.
        // -->
        registerTag("ellipsoids", (attribute, object) -> {
            List<EllipsoidTag> ellipsoids = EllipsoidTag.getNotableEllipsoidsContaining(object);
            ListTag ellipsoid_list = new ListTag();
            for (EllipsoidTag ellipsoid : ellipsoids) {
                ellipsoid_list.addObject(ellipsoid);
            }
            return ellipsoid_list;
        });

        // <--[tag]
        // @attribute <LocationTag.is_liquid>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the block at the location is a liquid.
        // -->
        registerTag("is_liquid", (attribute, object) -> {
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
        registerTag("light", (attribute, object) -> {
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
        registerTag("power", (attribute, object) -> {
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
        // @attribute <LocationTag.tree_distance>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns a number of how many blocks away from a connected tree leaves are.
        // Defaults to 7 if not connected to a tree.
        // -->
        registerTag("tree_distance", (attribute, object) -> {
            MaterialTag material = new MaterialTag(object.getBlockForTag(attribute));
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)
                    && MaterialPersistent.describes(material)) {
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
        registerTag("command_block_name", (attribute, object) -> {
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
        registerTag("command_block", (attribute, object) -> {
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
        registerTag("brewing_time", (attribute, object) -> {
            return new DurationTag((long) ((BrewingStand) object.getBlockStateForTag(attribute)).getBrewingTime());
        });

        // <--[tag]
        // @attribute <LocationTag.brewing_fuel_level>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.brewing_fuel_level
        // @description
        // Returns the level of fuel a brewing stand has. Each unit of fuel can power one brewing operation.
        // -->
        registerTag("brewing_fuel_level", (attribute, object) -> {
            return new ElementTag(((BrewingStand) object.getBlockStateForTag(attribute)).getFuelLevel());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_burn_time>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.furnace_burn_time
        // @description
        // Returns the burn time a furnace has left.
        // -->
        registerTag("furnace_burn_time", (attribute, object) -> {
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getBurnTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_time>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.furnace_cook_time
        // @description
        // Returns the cook time a furnace has been cooking its current item for.
        // -->
        registerTag("furnace_cook_time", (attribute, object) -> {
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTime());
        });

        // <--[tag]
        // @attribute <LocationTag.furnace_cook_time_total>
        // @returns ElementTag(Number)
        // @mechanism LocationTag.furnace_cook_time_total
        // @description
        // Returns the total cook time a furnace has left.
        // -->
        registerTag("furnace_cook_time_total", (attribute, object) -> {
            return new ElementTag(((Furnace) object.getBlockStateForTag(attribute)).getCookTimeTotal());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_tier>
        // @returns ElementTag(Number)
        // @description
        // Returns the tier level of a beacon pyramid (0-4).
        // -->
        registerTag("beacon_tier", (attribute, object) -> {
            return new ElementTag(((Beacon) object.getBlockStateForTag(attribute)).getTier());
        });

        // <--[tag]
        // @attribute <LocationTag.beacon_primary_effect>
        // @returns ElementTag
        // @mechanism LocationTag.beacon_primary_effect
        // @description
        // Returns the primary effect of the beacon. The return is simply a potion effect type name.
        // -->
        registerTag("beacon_primary_effect", (attribute, object) -> {
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
        registerTag("beacon_secondary_effect", (attribute, object) -> {
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
        // (Only if it is a lever or button!)
        // -->
        registerTag("attached_to", (attribute, object) -> {
            BlockFace face = BlockFace.SELF;
            MaterialTag material = new MaterialTag(object.getBlockForTag(attribute));
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)
                    && MaterialSwitchFace.describes(material)) {
                face = MaterialSwitchFace.getFrom(material).getAttachedTo();
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
        // You can test if this will be valid with <@link tag MaterialTag.is_bisected>.
        // -->
        registerTag("other_block", (attribute, object) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                Block b = object.getBlockForTag(attribute);
                MaterialTag material = new MaterialTag(b);
                if (MaterialHalf.describes(material)) {
                    return new LocationTag(object.clone().add(MaterialHalf.getFrom(material).getRelativeBlockVector()));
                }
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Block of type " + object.getBlockTypeForTag(attribute).name() + " isn't supported by other_block.");
                }
                return null;
            }
            BlockState state = object.getBlockStateForTag(attribute);
            if (state instanceof Chest) {
                // There is no remotely sane API for this.
                InventoryHolder holder = ((Chest) state).getBlockInventory().getHolder();
                if (holder instanceof DoubleChest) {
                    Location left = ((DoubleChest) holder).getLeftSide().getInventory().getLocation();
                    Location right = ((DoubleChest) holder).getRightSide().getInventory().getLocation();
                    if (left.getBlockX() == object.getBlockX() && left.getBlockY() == object.getBlockY() && left.getBlockZ() == object.getBlockZ()) {
                        return new LocationTag(right);
                    }
                    else {
                        return new LocationTag(left);
                    }
                }
            }
            else if (state.getData() instanceof Door) {
                if (((Door) state.getData()).isTopHalf()) {
                    return new LocationTag(object.clone().subtract(0, 1, 0));
                }
                else {
                    return new LocationTag(object.clone().add(0, 1, 0));
                }
            }
            else {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Block of type " + object.getBlockTypeForTag(attribute).name() + " isn't supported by other_block.");
                }
                return null;
            }
            if (!attribute.hasAlternative()) {
                Debug.echoError("Block of type " + object.getBlockTypeForTag(attribute).name() + " doesn't have an other block.");
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
        registerTag("custom_name", (attribute, object) -> {
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
        registerTag("local_difficulty", (attribute, object) -> {
            return new ElementTag(NMSHandler.getWorldHelper().getLocalDifficulty(object));
        });

        // <--[tag]
        // @attribute <LocationTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Location' for LocationTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, object) -> {
            return new ElementTag("Location");
        });
    }

    public static ObjectTagProcessor<LocationTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<LocationTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a location!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("data") && mechanism.hasValue()) {
            Deprecations.materialIds.warn(mechanism.context);
            BlockData blockData = NMSHandler.getBlockHelper().getBlockData(getBlock().getType(), (byte) mechanism.getValue().asInt());
            blockData.setBlock(getBlock(), false);
        }

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
            material.getModernData().setToBlock(block);
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
            mat.getNmsBlockData().setBlock(getBlock(), false);
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
            mechanism.valueAsType(BiomeTag.class).getBiome().changeBlockBiome(this);
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
        if (mechanism.matches("spawner_type") && mechanism.requireObject(EntityTag.class)
                && getBlockState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlockState());
            spawner.setSpawnedType(mechanism.valueAsType(EntityTag.class).getBukkitEntityType());
            spawner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name lock
        // @input Element
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
        // Note that this takes an escaped list.
        // See <@link language property escaping>.
        // @tags
        // <LocationTag.sign_contents>
        // -->
        if (mechanism.matches("sign_contents") && getBlockState() instanceof Sign) {
            Sign state = (Sign) getBlockState();
            for (int i = 0; i < 4; i++) {
                state.setLine(i, "");
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() > 4) {
                Debug.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    state.setLine(i, EscapeTagBase.unEscape(list.get(i)));
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
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)
                    && material != Material.PLAYER_HEAD && material != Material.PLAYER_WALL_HEAD) {
                Deprecations.skullSkinMaterials.warn(mechanism.context);
            }
            else if (blockState instanceof Skull) {
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
        // @name flowerpot_contents
        // @input MaterialTag
        // @description
        // Sets the contents of a flower pot.
        // NOTE: Replaced by materials (such as POTTED_CACTUS) in 1.13 and above.
        // NOTE: Flowerpot contents will not update client-side until players refresh the chunk.
        // Refresh a chunk manually with mechanism: refresh_chunk_sections for ChunkTag objects
        // @tags
        // <LocationTag.flowerpot_contents>
        // -->
        if (mechanism.matches("flowerpot_contents") && mechanism.requireObject(MaterialTag.class)) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                Deprecations.flowerpotMechanism.warn(mechanism.context);
            }
            else if (getBlock().getType() == Material.FLOWER_POT) {
                MaterialData data = mechanism.valueAsType(MaterialTag.class).getMaterialData();
                NMSHandler.getBlockHelper().setFlowerpotContents(getBlock(), data);
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name command_block_name
        // @input Element
        // @description
        // Sets the name of a command block.
        // @tags
        // <LocationTag.command_block_name>
        // -->
        if (mechanism.matches("command_block_name")) {
            if (getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
                CommandBlock block = ((CommandBlock) getBlockState());
                block.setName(mechanism.getValue().asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name command_block
        // @input Element
        // @description
        // Sets the command of a command block.
        // @tags
        // <LocationTag.command_block>
        // -->
        if (mechanism.matches("command_block")) {
            if (getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
                CommandBlock block = ((CommandBlock) getBlockState());
                block.setCommand(mechanism.getValue().asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name custom_name
        // @input Element
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
        // @name furnace_burn_time
        // @input ElementTag(Number)
        // @description
        // Sets the burn time for a furnace in ticks. Maximum is 32767.
        // @tags
        // <LocationTag.furnace_burn_time>
        // -->
        if (mechanism.matches("furnace_burn_time")) {
            if (MaterialCompat.isFurnace(getBlock().getType())) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setBurnTime((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name furnace_cook_time
        // @input ElementTag(Number)
        // @description
        // Sets the current cook time for a furnace in ticks. Maximum is 32767.
        // @tags
        // <LocationTag.furnace_cook_time>
        // -->
        if (mechanism.matches("furnace_cook_time")) {
            if (MaterialCompat.isFurnace(getBlock().getType())) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTime((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name furnace_cook_time_total
        // @input ElementTag(Number)
        // @description
        // Sets the total cook time for a furnace in ticks. Maximum is 32767.
        // @tags
        // <LocationTag.furnace_cook_time_total>
        // -->
        if (mechanism.matches("furnace_cook_time_total")) {
            if (MaterialCompat.isFurnace(getBlock().getType())) {
                Furnace furnace = (Furnace) getBlockState();
                furnace.setCookTimeTotal((short) mechanism.getValue().asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object LocationTag
        // @name base_color
        // @input Element
        // @description
        // Changes the base color of the banner at this location.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // As of 1.13+, this mechanism is no longer relevant.
        // @tags
        // <LocationTag.base_color>
        // -->
        if (mechanism.matches("base_color")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                Debug.echoError("Base_Color mechanism no longer relevant: banner types are now distinct materials.");
            }
            Banner banner = (Banner) getBlockState();
            banner.setBaseColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            banner.update();
        }

        // <--[mechanism]
        // @object LocationTag
        // @name patterns
        // @input ListTag
        // @description
        // Changes the patterns of the banner at this location. Input must be in the form "COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // @tags
        // <LocationTag.patterns>
        // <server.list_patterns>
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
        // @input Element
        // @description
        // Generates a tree at this location if possible.
        // For a list of valid tree types, see <@link url http://bit.ly/2o7m1je>
        // @tags
        // <server.list_tree_types>
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
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
