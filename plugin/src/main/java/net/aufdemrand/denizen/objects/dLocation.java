package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.scripts.commands.world.SwitchCommand;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.PathFinder;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.DenizenEntityType;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.objects.notable.Note;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dLocation extends org.bukkit.Location implements dObject, Notable, Adjustable {

    // This pattern correctly reads both 0.9 and 0.8 notables
    final static Pattern notablePattern =
            Pattern.compile("(\\w+)[;,]((-?\\d+\\.?\\d*,){3,5}.+)",
                    Pattern.CASE_INSENSITIVE);

    /////////////////////
    //   STATIC METHODS
    /////////////////

    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }

    @Note("Locations")
    public String getSaveObject() {
        return (getBlockX() + 0.5)
                + "," + getBlockY()
                + "," + (getBlockZ() + 0.5)
                + "," + getPitch()
                + "," + getYaw()
                + "," + getWorld().getName();
    }

    public static String getSaved(dLocation location) {
        for (dLocation saved : NotableManager.getAllType(dLocation.class)) {
            if (saved.getBlockX() != location.getX()) {
                continue;
            }
            if (saved.getBlockY() != location.getY()) {
                continue;
            }
            if (saved.getBlockZ() != location.getZ()) {
                continue;
            }
            if ((saved.getWorld() == null && location.getWorld() == null)
                    || (saved.getWorld() != null && location.getWorld() != null && saved.getWorld().getName().equals(location.getWorld().getName()))) {
                return NotableManager.getSavedId(saved);
            }
        }
        return null;
    }

    public void forget() {
        NotableManager.remove(this);
    }

    /*
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        for (String location : loclist) {
            Matcher m = notablePattern.matcher(location);
            if (m.matches()) {
                String id = m.group(1);
                dLocation loc = valueOf(m.group(2));
                NotableManager.saveAs(loc, id);
            }
        }
        DenizenAPI.getCurrentInstance().getSaves().set("dScript.Locations", null);
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static dLocation valueOf(String string) {
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
    public static dLocation valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        if (string.startsWith("l@")) {
            string = string.substring(2);
        }

        if (NotableManager.isSaved(string) && NotableManager.isType(string, dLocation.class)) {
            return (dLocation) NotableManager.getSavedObject(string);
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
                return new dLocation(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    dB.log("Minor: valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 3)
        // If 3 values, either worldless location format
        // x,y,z or 2D location format x,y,world
        {
            try {
                World world = Bukkit.getWorld(split.get(2));
                if (world != null) {
                    return new dLocation(world,
                            Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1)));
                }
                return new dLocation(null,
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    dB.log("Minor: valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 4)
        // If 4 values, standard dScript location format
        // x,y,z,world
        {
            try {
                return new dLocation(Bukkit.getWorld(split.get(3)),
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)));
            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    dB.log("Minor: valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }
        else if (split.size() == 6)

        // If 6 values, location with pitch/yaw
        // x,y,z,yaw,pitch,world
        {
            try {
                return new dLocation(Bukkit.getWorld(split.get(5)),
                        Double.valueOf(split.get(0)),
                        Double.valueOf(split.get(1)),
                        Double.valueOf(split.get(2)),
                        Float.valueOf(split.get(3)),
                        Float.valueOf(split.get(4)));

            }
            catch (Exception e) {
                if (context == null || context.debug) {
                    dB.log("Minor: valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                }
                return null;
            }
        }

        if (context == null || context.debug) {
            dB.log("Minor: valueOf dLocation returning null: " + string);
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

        return dLocation.valueOf(string, new BukkitTagContext(null, null, false, null, false, null)) != null;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    private boolean is2D = false;

    /**
     * Turns a Bukkit Location into a Location, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public dLocation(Location location) {
        // Just save the yaw and pitch as they are; don't check if they are
        // higher than 0, because Minecraft yaws are weird and can have
        // negative values
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public dLocation(Vector vector) {
        super(null, vector.getX(), vector.getY(), vector.getZ());
    }

    public dLocation(World world, double x, double y) {
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
    public dLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public dLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, pitch, yaw);
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // A boolean that determines whether this location will identify
    // as a notable or not
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
        return getBlock().getState() instanceof InventoryHolder;
    }

    public Inventory getBukkitInventory() {
        return hasInventory() ? ((InventoryHolder) getBlock().getState()).getInventory() : null;
    }

    public dInventory getInventory() {
        return hasInventory() ? dInventory.mirrorBukkitInventory(getBukkitInventory()) : null;
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
        if (!(o instanceof dLocation)) {
            return false;
        }
        dLocation other = (dLocation) o;
        if ((other.getWorld() == null && getWorld() != null)
                || (getWorld() == null && other.getWorld() != null)
                || (getWorld() != null && other.getWorld() != null
                && !getWorld().getName().equalsIgnoreCase(other.getWorld().getName()))) {
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
    public dLocation setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return (isUnique() ? "<G>" + prefix + "='<A>" + identify() + "(<Y>" + identifyRaw() + "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
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
        else if (getWorld() == null) {
            return "l@" + getBlockX() + "," + getBlockY() + (!is2D ? "," + getBlockZ() : "");
        }
        else {
            return "l@" + getBlockX() + "," + getBlockY() + (!is2D ? "," + getBlockZ() : "")
                    + "," + getWorld().getName();
        }
    }

    public String identifyRaw() {
        if (getYaw() != 0.0 || getPitch() != 0.0) {
            return "l@" + getX() + "," + getY() + "," + getZ() + "," + getPitch() + "," + getYaw()
                    + (getWorld() != null ? "," + getWorld().getName() : "");
        }
        else {
            return "l@" + getX() + "," + getY() + (!is2D ? "," + getZ() : "")
                    + (getWorld() != null ? "," + getWorld().getName() : "");
        }
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }


        /////////////////////
        //   BLOCK ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.above>
        // @returns dLocation
        // @description
        // Returns the location one block above this location.
        // -->
        if (attribute.startsWith("above")) {
            return new dLocation(this.clone().add(0, 1, 0))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.below>
        // @returns dLocation
        // @description
        // Returns the location one block below this location.
        // -->
        if (attribute.startsWith("below")) {
            return new dLocation(this.clone().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.block>
        // @returns dLocation
        // @description
        // Returns the location of the block this location is on,
        // i.e. returns a location without decimals or direction.
        // -->
        if (attribute.startsWith("block")) {
            return new dLocation(getWorld(), getBlockX(), getBlockY(), getBlockZ())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.center>
        // @returns dLocation
        // @description
        // Returns the location at the center of the block this location is on.
        // -->
        if (attribute.startsWith("center")) {
            return new dLocation(getWorld(), getBlockX() + 0.5, getBlockY() + 0.5, getBlockZ() + 0.5)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.highest>
        // @returns dLocation
        // @description
        // Returns the location of the highest solid block at the location.
        // -->
        if (attribute.startsWith("highest")) {
            return new dLocation(getWorld().getHighestBlockAt(this).getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.base_color>
        // @returns Element
        // @description
        // Returns the base color of the banner at this location.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // -->
        if (attribute.startsWith("base_color")) {
            DyeColor color = ((Banner) getBlock().getState()).getBaseColor();
            return new Element(color != null ? color.name() : "BLACK").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.has_inventory>
        // @returns Element(Boolean)
        // @description
        // Returns whether the block at the location has an inventory.
        // -->
        if (attribute.startsWith("has_inventory")) {
            return new Element(getBlock().getState() instanceof InventoryHolder).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.inventory>
        // @returns dInventory
        // @description
        // Returns the dInventory of the block at the location. If the
        // block is not a container, returns null.
        // -->
        if (attribute.startsWith("inventory")) {
            dObject obj = Element.handleNull(identify() + ".inventory", getInventory(), "dInventory", attribute.hasAlternative());
            return obj == null ? null : obj.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.material>
        // @returns dMaterial
        // @description
        // Returns the material of the block at the location.
        // -->
        if (attribute.startsWith("material")) {
            return dMaterial.getMaterialFrom(getBlock().getType(), getBlock().getData()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.patterns>
        // @returns dList
        // @group properties
        // @mechanism dLocation.patterns
        // @description
        // Lists the patterns of the banner at this location in the form "li@COLOR/PATTERN|COLOR/PATTERN" etc.
        // Available colors: black, blue, brown, cyan, gray, green, light_blue, lime, magenta, orange, pink
        // purple, red, silver, white, and yellow.
        // Available patterns: base, border, bricks, circle_middle, creeper, cross, curly_border, diagonal_left,
        // diagonal_left_mirror, diagonal_right, diagonal_right_mirror, flower, gradient, gradient_up, half_horizontal,
        // half_horizontal_mirror, half_vertical, half_vertical_mirror, mojang, rhombus_middle, skull,
        // square_bottom_left, square_bottom_right, square_top_left, square_top_right, straight_cross, stripe_bottom,
        // stripe_center, stripe_downleft, stripe_downright, stripe_left, stripe_middle, stripe_right, stripe_small,
        // stripe_top, triangle_bottom, triangle_top, triangles_bottom, and triangles_top
        // -->
        if (attribute.startsWith("patterns")) {
            dList list = new dList();
            for (org.bukkit.block.banner.Pattern pattern : ((Banner) getBlock().getState()).getPatterns()) {
                list.add(pattern.getColor().name() + "/" + pattern.getPattern().name());
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.head_rotation>
        // @returns Element(Number)
        // @description
        // Gets the rotation of the head at this location. Can be 1-16.
        // @mechanism dLocation.head_rotation
        // -->
        if (attribute.startsWith("head_rotation")) {
            return new Element(getSkullRotation(((Skull) getBlock().getState()).getRotation()) + 1)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.switched>
        // @returns Element(Boolean)
        // @description
        // Returns whether the block at the location is considered to be switched on.
        // (For buttons, levers, etc.)
        // To change this, see <@link command Switch>
        // -->
        if (attribute.startsWith("switched")) {
            return new Element(SwitchCommand.switchState(getBlock()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.sign_contents>
        // @returns dList
        // @mechanism dLocation.sign_contents
        // @description
        // Returns a list of lines on a sign.
        // -->
        if (attribute.startsWith("sign_contents")) {
            if (getBlock().getState() instanceof Sign) {
                return new dList(Arrays.asList(((Sign) getBlock().getState()).getLines()))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <l@location.spawner_type>
        // @mechanism dLocation.spawner_type
        // @returns dEntity
        // @description
        // Returns the type of entity spawned by a mob spawner.
        // -->
        if (attribute.startsWith("spawner_type")) {
            if (getBlock().getState() instanceof CreatureSpawner) {
                return new dEntity(DenizenEntityType.getByName(((CreatureSpawner) getBlock().getState())
                        .getSpawnedType().name())).getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <l@location.lock>
        // @mechanism dLocation.lock
        // @returns Element
        // @description
        // Returns the password to a locked container.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)
                && attribute.startsWith("lock") && getBlock().getState() instanceof Lockable) {
            Lockable lock = (Lockable) getBlock().getState();
            return new Element(lock.isLocked() ? lock.getLock() : null)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.is_locked>
        // @mechanism dLocation.lock
        // @returns Element(Boolean)
        // @description
        // Returns whether the container is locked.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)
                && attribute.startsWith("is_locked") && getBlock().getState() instanceof Lockable) {
            return new Element(((Lockable) getBlock().getState()).isLocked())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.is_lockable>
        // @mechanism dLocation.lock
        // @returns Element(Boolean)
        // @description
        // Returns whether the container is lockable.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)
                && attribute.startsWith("is_lockable")) {
            return new Element(getBlock().getState() instanceof Lockable)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.drops[(<item>)]>
        // @returns dList(dItem)
        // @description
        // Returns what items the block at the location would drop if broken naturally.
        // Optionally specifier a breaker item.
        // -->
        if (attribute.startsWith("drops")) {
            Collection<ItemStack> its;
            if (attribute.hasContext(1)) {
                dItem item = dItem.valueOf(attribute.getContext(1));
                its = getBlock().getDrops(item.getItemStack());
            }
            else {
                its = getBlock().getDrops();
            }
            dList list = new dList();
            for (ItemStack it : its) {
                list.add(new dItem(it).identify());
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.flowerpot_contents>
        // @returns Element
        // @description
        // Returns the flower pot contents at the location.
        // NOTE: Replaced by materials (such as POTTED_CACTUS) in 1.13 and above.
        // -->
        if (attribute.startsWith("flowerpot_contents")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                dB.echoError("As of Minecraft version 1.13 potted flowers each have their own material, such as POTTED_CACTUS.");
            }
            else if (getBlock().getType() == Material.FLOWER_POT) {
                MaterialData contents = NMSHandler.getInstance().getBlockHelper().getFlowerpotContents(getBlock());
                return dMaterial.getMaterialFrom(contents.getItemType(), contents.getData())
                        .getAttribute(attribute.fulfill(1));
            }
            return null;
        }


        // <--[tag]
        // @attribute <l@location.skull_type>
        // @returns Element
        // @description
        // Returns the type of the skull.
        // -->
        if (attribute.startsWith("skull_type")) {
            BlockState blockState = getBlock().getState();
            if (blockState instanceof Skull) {
                String t = ((Skull) blockState).getSkullType().name();
                return new Element(t).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.skull_name>
        // @returns Element
        // @mechanism dLocation.skull_skin
        // @description
        // Returns the name of the skin the skull is displaying.
        // -->
        if (attribute.startsWith("skull_name")) {
            BlockState blockState = getBlock().getState();
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.getInstance().getBlockHelper().getPlayerProfile((Skull) blockState);
                String n = profile.getName();
                if (n == null) {
                    n = ((Skull) blockState).getOwningPlayer().getName();
                }
                return new Element(n).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.skull_skin>
        // @returns Element
        // @mechanism dLocation.skull_skin
        // @description
        // Returns the skin the skull is displaying - just the name or UUID as text, not a player object.
        // -->
        if (attribute.startsWith("skull_skin")) {
            BlockState blockState = getBlock().getState();
            if (blockState instanceof Skull) {
                PlayerProfile profile = NMSHandler.getInstance().getBlockHelper().getPlayerProfile((Skull) blockState);
                String name = profile.getName();
                UUID uuid = profile.getUniqueId();
                String texture = profile.getTexture();
                attribute = attribute.fulfill(1);
                // <--[tag]
                // @attribute <l@location.skull_skin.full>
                // @returns Element|Element
                // @mechanism dLocation.skull_skin
                // @description
                // Returns the skin the skull item is displaying - just the name or UUID as text, not a player object,
                // along with the permanently cached texture property.
                // -->
                if (attribute.startsWith("full")) {
                    return new Element((uuid != null ? uuid : name != null ? name : null)
                            + (texture != null ? "|" + texture : ""))
                            .getAttribute(attribute.fulfill(1));
                }
                return new Element(uuid != null ? uuid.toString() : name != null ? name : null).getAttribute(attribute);
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <l@location.simple.formatted>
        // @returns Element
        // @description
        // Returns the formatted simple version of the dLocation's block coordinates.
        // In the format: X 'x', Y 'y', Z 'z', in world 'world'
        // For example, X '1', Y '2', Z '3', in world 'world_nether'
        // -->
        if (attribute.startsWith("simple.formatted")) {
            return new Element("X '" + getBlockX()
                    + "', Y '" + getBlockY()
                    + "', Z '" + getBlockZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.simple>
        // @returns Element
        // @description
        // Returns a simple version of the dLocation's block coordinates.
        // In the format: x,y,z,world
        // For example: 1,2,3,world_nether
        // -->
        if (attribute.startsWith("simple")) {
            if (getWorld() == null) {
                return new Element(getBlockX() + "," + getBlockY() + "," + getBlockZ())
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new Element(getBlockX() + "," + getBlockY() + "," + getBlockZ()
                        + "," + getWorld().getName()).getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   DIRECTION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.precise_impact_normal[<range>]>
        // @returns dLocation
        // @description
        // Returns the exact impact normal at the location this location is pointing at.
        // Optionally, specify a maximum range to find the location from.
        // -->
        if (attribute.startsWith("precise_impact_normal")) {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            double xzLen = Math.cos((getPitch() % 360) * (Math.PI / 180));
            double nx = xzLen * Math.sin(-getYaw() * (Math.PI / 180));
            double ny = Math.sin(getPitch() * (Math.PI / 180));
            double nz = xzLen * Math.cos(getYaw() * (Math.PI / 180));
            Location location = NMSHandler.getInstance().getEntityHelper().getImpactNormal(this, new org.bukkit.util.Vector(nx, -ny, nz), range);
            if (location != null) {
                return new dLocation(location).getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <l@location.precise_cursor_on[<range>]>
        // @returns dLocation
        // @description
        // Returns the exact location this location is pointing at.
        // Optionally, specify a maximum range to find the location from.
        // -->
        if (attribute.startsWith("precise_cursor_on")) {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            double xzLen = Math.cos((getPitch() % 360) * (Math.PI / 180));
            double nx = xzLen * Math.sin(-getYaw() * (Math.PI / 180));
            double ny = Math.sin(getPitch() * (Math.PI / 180));
            double nz = xzLen * Math.cos(getYaw() * (Math.PI / 180));
            Location location = NMSHandler.getInstance().getEntityHelper().rayTrace(this, new org.bukkit.util.Vector(nx, -ny, nz), range);
            if (location != null) {
                return new dLocation(location).getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <l@location.points_between[<location>]>
        // @returns dList(dLocation)
        // @description
        // Finds all locations between this location and another, separated by 1 block-width each.
        // -->
        if (attribute.startsWith("points_between")) {
            dLocation target = dLocation.valueOf(attribute.getContext(1));
            if (target == null) {
                return null;
            }
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <l@location.points_between[<location>].distance[<#.#>]>
            // @returns dList(dLocation)
            // @description
            // Finds all locations between this location and another, separated by the specified distance each.
            // -->
            double rad = 1d;
            if (attribute.startsWith("distance")) {
                rad = attribute.getDoubleContext(1);
                attribute = attribute.fulfill(1);
            }
            dList list = new dList();
            org.bukkit.util.Vector rel = target.toVector().subtract(this.toVector());
            double len = rel.length();
            rel = rel.multiply(1d / len);
            for (double i = 0d; i < len; i += rad) {
                list.add(new dLocation(this.clone().add(rel.clone().multiply(i))).identify());
            }
            return list.getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <l@location.facing_blocks[<#>]>
        // @returns dList(dLocation)
        // @description
        // Finds all block locations in the direction this location is facing,
        // optionally with a custom range (default is 100).
        // For example a location at 0,0,0 facing straight up
        // will include 0,1,0 0,2,0 and so on.
        // -->
        if (attribute.startsWith("facing_blocks")) {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 100;
            }
            dList list = new dList();
            BlockIterator iterator = new BlockIterator(this, 0, range);
            while (iterator.hasNext()) {
                list.add(new dLocation(iterator.next().getLocation()).identify());
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.line_of_sight[<location>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the specified location is within this location's
        // line of sight.
        // -->
        if (attribute.startsWith("line_of_sight") && attribute.hasContext(1)) {
            dLocation location = dLocation.valueOf(attribute.getContext(1));
            if (location != null) {
                return new Element(NMSHandler.getInstance().getEntityHelper().canTrace(getWorld(), toVector(), location.toVector()))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.direction.vector>
        // @returns dLocation
        // @description
        // Returns the location's direction as a one-length vector.
        // -->
        if (attribute.startsWith("direction.vector")) {
            double xzLen = Math.cos((getPitch() % 360) * (Math.PI / 180));
            double nx = xzLen * Math.sin(-getYaw() * (Math.PI / 180));
            double ny = Math.sin(getPitch() * (Math.PI / 180));
            double nz = xzLen * Math.cos(getYaw() * (Math.PI / 180));
            return new dLocation(getWorld(), nx, -ny, nz).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.direction[<location>]>
        // @returns Element
        // @description
        // Returns the compass direction between two locations.
        // If no second location is specified, returns the direction of the location.
        // Example returns include "north", "southwest", ...
        // -->
        if (attribute.startsWith("direction")) {
            // Get the cardinal direction from this location to another
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                // Subtract this location's vector from the other location's vector,
                // not the other way around
                dLocation target = dLocation.valueOf(attribute.getContext(1));
                attribute = attribute.fulfill(1);
                EntityHelper entityHelper = NMSHandler.getInstance().getEntityHelper();
                // <--[tag]
                // @attribute <l@location.direction[<location>].yaw>
                // @returns Element(Decimal)
                // @description
                // Returns the yaw direction between two locations.
                // -->
                if (attribute.startsWith("yaw")) {
                    return new Element(entityHelper.normalizeYaw(entityHelper.getYaw
                            (target.toVector().subtract(this.toVector())
                                    .normalize())))
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(entityHelper.getCardinal(entityHelper.getYaw
                            (target.toVector().subtract(this.toVector())
                                    .normalize())))
                            .getAttribute(attribute);
                }
            }
            // Get a cardinal direction from this location's yaw
            else {
                return new Element(NMSHandler.getInstance().getEntityHelper().getCardinal(getYaw()))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.face[<location>]>
        // @returns dLocation
        // @description
        // Returns a location containing a yaw/pitch that point from the current location
        // to the target location.
        // -->
        if (attribute.startsWith("face")
                && attribute.hasContext(1)) {
            Location two = dLocation.valueOf(attribute.getContext(1));
            return new dLocation(NMSHandler.getInstance().getEntityHelper().faceLocation(this, two))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.facing[<entity>/<location>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the location's yaw is facing another
        // entity or location.
        // -->
        if (attribute.startsWith("facing")) {
            if (attribute.hasContext(1)) {

                // The default number of degrees if there is no degrees attribute
                int degrees = 45;

                // The attribute to fulfill from
                int attributePos = 1;

                // <--[tag]
                // @attribute <l@location.facing[<entity>/<location>].degrees[<#>]>
                // @returns Element(Boolean)
                // @description
                // Returns whether the location's yaw is facing another
                // entity or location, within a specified degree range.
                // -->
                if (attribute.getAttribute(2).startsWith("degrees") &&
                        attribute.hasContext(2) &&
                        aH.matchesInteger(attribute.getContext(2))) {

                    degrees = attribute.getIntContext(2);
                    attributePos++;
                }

                if (dLocation.matches(attribute.getContext(1))) {
                    return new Element(NMSHandler.getInstance().getEntityHelper().isFacingLocation
                            (this, dLocation.valueOf(attribute.getContext(1)), degrees))
                            .getAttribute(attribute.fulfill(attributePos));
                }
                else if (dEntity.matches(attribute.getContext(1))) {
                    return new Element(NMSHandler.getInstance().getEntityHelper().isFacingLocation
                            (this, dEntity.valueOf(attribute.getContext(1))
                                    .getBukkitEntity().getLocation(), degrees))
                            .getAttribute(attribute.fulfill(attributePos));
                }
            }
        }

        // <--[tag]
        // @attribute <l@location.pitch>
        // @returns Element(Decimal)
        // @description
        // Returns the pitch of the object at the location.
        // -->
        if (attribute.startsWith("pitch")) {
            return new Element(getPitch()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.with_pose[<entity>/<pitch>,<yaw>]>
        // @returns dLocation
        // @description
        // Returns the location with pitch and yaw.
        // -->
        if (attribute.startsWith("with_pose")) {
            String context = attribute.getContext(1);
            Float pitch = 0f;
            Float yaw = 0f;
            if (dEntity.matches(context)) {
                dEntity ent = dEntity.valueOf(context);
                if (ent.isSpawned()) {
                    pitch = ent.getBukkitEntity().getLocation().getPitch();
                    yaw = ent.getBukkitEntity().getLocation().getYaw();
                }
            }
            else if (context.split(",").length == 2) {
                String[] split = context.split(",");
                pitch = Float.valueOf(split[0]);
                yaw = Float.valueOf(split[1]);
            }
            dLocation loc = dLocation.valueOf(identify());
            loc.setPitch(pitch);
            loc.setYaw(yaw);
            return loc.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.yaw.simple>
        // @returns Element
        // @description
        // Returns the yaw as 'North', 'South', 'East', or 'West'.
        // -->
        if (attribute.startsWith("yaw.simple")) {
            float yaw = NMSHandler.getInstance().getEntityHelper().normalizeYaw(getYaw());
            if (yaw < 45) {
                return new Element("South")
                        .getAttribute(attribute.fulfill(2));
            }
            else if (yaw < 135) {
                return new Element("West")
                        .getAttribute(attribute.fulfill(2));
            }
            else if (yaw < 225) {
                return new Element("North")
                        .getAttribute(attribute.fulfill(2));
            }
            else if (yaw < 315) {
                return new Element("East")
                        .getAttribute(attribute.fulfill(2));
            }
            else {
                return new Element("South")
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <l@location.yaw.raw>
        // @returns Element(Decimal)
        // @description
        // Returns the raw yaw of the object at the location.
        // -->
        if (attribute.startsWith("yaw.raw")) {
            return new Element(getYaw())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.yaw>
        // @returns Element(Decimal)
        // @description
        // Returns the normalized yaw of the object at the location.
        // -->
        if (attribute.startsWith("yaw")) {
            return new Element(NMSHandler.getInstance().getEntityHelper().normalizeYaw(getYaw()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.rotate_around_x[<#.#>]>
        // @returns dLocation
        // @description
        // Returns the location rotated around the x axis by a specified angle in radians.
        // -->
        if (attribute.startsWith("rotate_around_x") && attribute.hasContext(1)) {
            double angle = attribute.getDoubleContext(1);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double y = (getY() * cos) - (getZ() * sin);
            double z = (getY() * sin) + (getZ() * cos);
            Location location = clone();
            location.setY(y);
            location.setZ(z);
            return new dLocation(location).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.rotate_around_y[<#.#>]>
        // @returns dLocation
        // @description
        // Returns the location rotated around the y axis by a specified angle in radians.
        // -->
        if (attribute.startsWith("rotate_around_y") && attribute.hasContext(1)) {
            double angle = attribute.getDoubleContext(1);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = (getX() * cos) + (getZ() * sin);
            double z = (getX() * -sin) + (getZ() * cos);
            Location location = clone();
            location.setX(x);
            location.setZ(z);
            return new dLocation(location).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.rotate_around_z[<#.#>]>
        // @returns dLocation
        // @description
        // Returns the location rotated around the z axis by a specified angle in radians.
        // -->
        if (attribute.startsWith("rotate_around_z") && attribute.hasContext(1)) {
            double angle = attribute.getDoubleContext(1);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = (getX() * cos) - (getY() * sin);
            double y = (getZ() * sin) + (getY() * cos);
            Location location = clone();
            location.setX(x);
            location.setY(y);
            return new dLocation(location).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   ENTITY AND BLOCK LIST ATTRIBUTES
        /////////////////

        if (attribute.matches("find") || attribute.startsWith("nearest")) {
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <l@location.find.blocks[<block>|...].within[<#>]>
            // @returns dList
            // @description
            // Returns a list of matching blocks within a radius.
            // Note: current implementation measures the center of nearby block's distance from the exact given location.
            // -->
            if (attribute.startsWith("blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dLocation> found = new ArrayList<dLocation>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                List<dMaterial> materials = new ArrayList<dMaterial>();
                if (attribute.hasContext(1)) {
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);
                }
                // Avoid NPE from invalid materials
                if (materials == null) {
                    return null;
                }
                int max = Settings.blockTagsMaxBlocks();
                int index = 0;

                attribute.fulfill(2);
                Location tstart = getBlock().getLocation();
                double tstartY = tstart.getY();

                fullloop:
                for (int x = -(radius); x <= radius; x++) {
                    for (int y = -(radius); y <= radius; y++) {
                        double newY = y + tstartY;
                        if (newY < 0 || newY > 255) {
                            continue;
                        }
                        for (int z = -(radius); z <= radius; z++) {
                            index++;
                            if (index > max) {
                                break fullloop;
                            }
                            if (Utilities.checkLocation(this, tstart.clone().add(x + 0.5, y + 0.5, z + 0.5), radius)) {
                                if (!materials.isEmpty()) {
                                    for (dMaterial material : materials) {
                                        if (material.hasData() && material.getData() != 0) { // TODO: less arbitrary matching
                                            if (material.matchesMaterialData(tstart.clone().add(x, y, z).getBlock().getState().getData())) {
                                                found.add(new dLocation(tstart.clone().add(x, y, z)));
                                            }
                                        }
                                        else if (material.getMaterial() == tstart.clone().add(x, y, z).getBlock().getType()) {
                                            found.add(new dLocation(tstart.clone().add(x, y, z)));
                                        }
                                    }
                                }
                                else {
                                    found.add(new dLocation(tstart.clone().add(x, y, z)));
                                }
                            }
                        }
                    }
                }

                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return dLocation.this.compare(loc1, loc2);
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.surface_blocks[<block>|...].within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of matching surface blocks within a radius.
            // -->
            else if (attribute.startsWith("surface_blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dLocation> found = new ArrayList<dLocation>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                List<dMaterial> materials = new ArrayList<dMaterial>();
                if (attribute.hasContext(1)) {
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);
                }
                // Avoid NPE from invalid materials
                if (materials == null) {
                    return null;
                }
                int max = Settings.blockTagsMaxBlocks();
                int index = 0;

                attribute.fulfill(2);
                Location loc = getBlock().getLocation().add(0.5f, 0.5f, 0.5f);

                fullloop:
                for (double x = -(radius); x <= radius; x++) {
                    for (double y = -(radius); y <= radius; y++) {
                        for (double z = -(radius); z <= radius; z++) {
                            index++;
                            if (index > max) {
                                break fullloop;
                            }
                            if (Utilities.checkLocation(loc, getBlock().getLocation().add(x + 0.5, y + 0.5, z + 0.5), radius)) {
                                Location l = getBlock().getLocation().clone().add(x, y, z);
                                if (!materials.isEmpty()) {
                                    for (dMaterial material : materials) {
                                        if (material.matchesMaterialData(getBlock()
                                                .getLocation().clone().add(x, y, z).getBlock().getType().getNewData(getBlock()
                                                        .getLocation().clone().add(x, y, z).getBlock().getData()))) {
                                            if (l.clone().add(0, 1, 0).getBlock().getType() == Material.AIR
                                                    && l.clone().add(0, 2, 0).getBlock().getType() == Material.AIR
                                                    && l.getBlock().getType() != Material.AIR) {
                                                found.add(new dLocation(getBlock().getLocation().clone().add(x + 0.5, y, z + 0.5)));
                                            }
                                        }
                                    }
                                }
                                else {
                                    if (l.clone().add(0, 1, 0).getBlock().getType() == Material.AIR
                                            && l.clone().add(0, 2, 0).getBlock().getType() == Material.AIR
                                            && l.getBlock().getType() != Material.AIR) {
                                        found.add(new dLocation(getBlock().getLocation().clone().add(x + 0.5, y, z + 0.5)));
                                    }
                                }
                            }
                        }
                    }
                }

                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return dLocation.this.compare(loc1, loc2);
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.players.within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of players within a radius.
            // -->
            else if (attribute.startsWith("players")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dPlayer> found = new ArrayList<dPlayer>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                attribute.fulfill(2);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isDead() && Utilities.checkLocation(this, player.getLocation(), radius)) {
                        found.add(new dPlayer(player));
                    }
                }

                Collections.sort(found, new Comparator<dPlayer>() {
                    @Override
                    public int compare(dPlayer pl1, dPlayer pl2) {
                        return dLocation.this.compare(pl1.getLocation(), pl2.getLocation());
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.npcs.within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of NPCs within a radius.
            // -->
            else if (attribute.startsWith("npcs")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dNPC> found = new ArrayList<dNPC>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                attribute.fulfill(2);
                for (dNPC npc : DenizenAPI.getSpawnedNPCs()) {
                    if (Utilities.checkLocation(this.getBlock().getLocation(), npc.getLocation(), radius)) {
                        found.add(npc);
                    }
                }

                Collections.sort(found, new Comparator<dNPC>() {
                    @Override
                    public int compare(dNPC npc1, dNPC npc2) {
                        return dLocation.this.compare(npc1.getLocation(), npc2.getLocation());
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.entities[<entity>|...].within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of entities within a radius, with an optional search parameter
            // for the entity type.
            // -->
            else if (attribute.startsWith("entities")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                dList ent_list = new dList();
                if (attribute.hasContext(1)) {
                    ent_list = dList.valueOf(attribute.getContext(1));
                }
                ArrayList<dEntity> found = new ArrayList<dEntity>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                attribute.fulfill(2);
                for (Entity entity : getWorld().getEntities()) {
                    if (Utilities.checkLocation(this, entity.getLocation(), radius)) {
                        dEntity current = new dEntity(entity);
                        if (!ent_list.isEmpty()) {
                            for (String ent : ent_list) {
                                if (current.comparedTo(ent)) {
                                    found.add(current);
                                    break;
                                }
                            }
                        }
                        else {
                            found.add(current);
                        }
                    }
                }

                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return dLocation.this.compare(ent1.getLocation(), ent2.getLocation());
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.living_entities.within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of living entities within a radius.
            // -->
            else if (attribute.startsWith("living_entities")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dEntity> found = new ArrayList<dEntity>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                attribute.fulfill(2);
                for (Entity entity : getWorld().getEntities()) {
                    if (entity instanceof LivingEntity
                            && Utilities.checkLocation(this, entity.getLocation(), radius)) {
                        found.add(new dEntity(entity));
                    }
                }

                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return dLocation.this.compare(ent1.getLocation(), ent2.getLocation());
                    }
                });

                return new dList(found).getAttribute(attribute);
            }
        }

        // <--[tag]
        // @attribute <l@location.find_path[<location>]>
        // @returns dList(dLocation)
        // @description
        // Returns a full list of points along the path from this location to the given location.
        // Uses a max range of 100 blocks from the start.
        // -->
        if (attribute.startsWith("find_path")
                && attribute.hasContext(1)) {
            dLocation two = dLocation.valueOf(attribute.getContext(1));
            if (two == null) {
                return null;
            }
            List<dLocation> locs = PathFinder.getPath(this, two);
            dList list = new dList();
            for (dLocation loc : locs) {
                list.add(loc.identify());
            }
            return list.getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.formatted.citizens>
        // @returns Element
        // @description
        // Returns the location formatted for a Citizens command.
        // In the format: x.x:y.y:z.z:world
        // For example: 1.0:2.0:3.0:world_nether
        // -->
        if (attribute.startsWith("formatted.citizens")) {
            return new Element(getX() + ":" + getY() + ":" + getZ() + ":" + getWorld().getName()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.formatted>
        // @returns Element
        // @description
        // Returns the formatted version of the dLocation.
        // In the format: X 'x.x', Y 'y.y', Z 'z.z', in world 'world'
        // For example: X '1.0', Y '2.0', Z '3.0', in world 'world_nether'
        // -->
        if (attribute.startsWith("formatted")) {
            return new Element("X '" + getX()
                    + "', Y '" + getY()
                    + "', Z '" + getZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.chunk>
        // @returns dChunk
        // @description
        // Returns the chunk that this location belongs to.
        // -->
        if (attribute.startsWith("chunk") ||
                attribute.startsWith("get_chunk")) {
            return new dChunk(this).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.raw>
        // @returns dLocation
        // @description
        // Returns the raw representation of this location,
        //         ignoring any notables it might match.
        // -->
        if (attribute.startsWith("raw")) {
            dLocation rawLocation = new dLocation(this);
            rawLocation.setRaw(true);
            return rawLocation.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.world>
        // @returns dWorld
        // @description
        // Returns the world that the location is in.
        // -->
        if (attribute.startsWith("world")) {
            return dWorld.mirrorBukkitWorld(getWorld())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.x>
        // @returns Element(Decimal)
        // @description
        // Returns the X coordinate of the location.
        // -->
        if (attribute.startsWith("x")) {
            return new Element(getX()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.y>
        // @returns Element(Decimal)
        // @description
        // Returns the Y coordinate of the location.
        // -->
        if (attribute.startsWith("y")) {
            return new Element(getY()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.z>
        // @returns Element(Decimal)
        // @description
        // Returns the Z coordinate of the location.
        // -->
        if (attribute.startsWith("z")) {
            return new Element(getZ()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.notable_name>
        // @returns Element
        // @description
        // Gets the name of a Notable dLocation. If the location isn't noted,
        // this is null.
        // -->
        if (attribute.startsWith("notable_name")) {
            String notname = NotableManager.getSavedId(this);
            if (notname == null) {
                return null;
            }
            return new Element(notname).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   MATHEMATICAL ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.add[<location>]>
        // @returns dLocation
        // @description
        // Returns the location with the specified coordinates added to it.
        // -->
        if (attribute.startsWith("add")
                && attribute.hasContext(1)) {
            String[] ints = attribute.getContext(1).replace("l@", "").split(",", 4); // TODO: Just dLocation.valueOf?
            if (ints.length >= 3) {
                if ((aH.matchesDouble(ints[0]) || aH.matchesInteger(ints[0]))
                        && (aH.matchesDouble(ints[1]) || aH.matchesInteger(ints[1]))
                        && (aH.matchesDouble(ints[2]) || aH.matchesInteger(ints[2]))) {
                    return new dLocation(this.clone().add(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2]))).getAttribute(attribute.fulfill(1));
                }
            }
            else if (dLocation.matches(attribute.getContext(1))) {
                return new dLocation(this.clone().add(dLocation.valueOf(attribute.getContext(1))))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.sub[<location>]>
        // @returns dLocation
        // @description
        // Returns the location with the specified coordinates subtracted from it.
        // -->
        if (attribute.startsWith("sub")
                && attribute.hasContext(1)) {
            String[] ints = attribute.getContext(1).replace("l@", "").split(",", 4); // TODO: Just dLocation.valueOf?
            if (ints.length == 3 || ints.length == 4) {
                if ((aH.matchesDouble(ints[0]) || aH.matchesInteger(ints[0]))
                        && (aH.matchesDouble(ints[1]) || aH.matchesInteger(ints[1]))
                        && (aH.matchesDouble(ints[2]) || aH.matchesInteger(ints[2]))) {
                    return new dLocation(this.clone().subtract(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2]))).getAttribute(attribute.fulfill(1));
                }
            }
            else if (dLocation.matches(attribute.getContext(1))) {
                return new dLocation(this.clone().subtract(dLocation.valueOf(attribute.getContext(1))))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.mul[<length>]>
        // @returns dLocation
        // @description
        // Returns the location multiplied by the specified length.
        // -->
        if (attribute.startsWith("mul") &&
                attribute.hasContext(1)) {
            return new dLocation(this.clone().multiply(Double.parseDouble(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.div[<length>]>
        // @returns dLocation
        // @description
        // Returns the location divided by the specified length.
        // -->
        if (attribute.startsWith("div") &&
                attribute.hasContext(1)) {
            return new dLocation(this.clone().multiply(1D / Double.parseDouble(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.normalize>
        // @returns dLocation
        // @description
        // Returns a 1-length vector in the same direction as this vector location.
        // -->
        if (attribute.startsWith("normalize")) {
            double len = Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2));
            if (len == 0) {
                return this.getAttribute(attribute.fulfill(1));
            }
            else {
                return new dLocation(this.clone().multiply(1D / len))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.vector_length>
        // @returns Element(Decimal)
        // @description
        // Returns the 3D length of the vector/location.
        // -->
        if (attribute.startsWith("vector_length")) {
            return new Element(Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.distance_squared[<location>]>
        // @returns Element(Decimal)
        // @description
        // Returns the distance between 2 locations, squared.
        // -->
        if (attribute.startsWith("distance_squared")
                && attribute.hasContext(1)) {
            if (dLocation.matches(attribute.getContext(1))) {
                dLocation toLocation = dLocation.valueOf(attribute.getContext(1));
                if (!getWorld().getName().equalsIgnoreCase(toLocation.getWorld().getName())) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("Can't measure distance between two different worlds!");
                    }
                    return null;
                }
                return new Element(this.distanceSquared(toLocation))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.distance[<location>]>
        // @returns Element(Decimal)
        // @description
        // Returns the distance between 2 locations.
        // -->
        if (attribute.startsWith("distance")
                && attribute.hasContext(1)) {
            if (dLocation.matches(attribute.getContext(1))) {
                dLocation toLocation = dLocation.valueOf(attribute.getContext(1));

                // <--[tag]
                // @attribute <l@location.distance[<location>].horizontal>
                // @returns Element(Decimal)
                // @description
                // Returns the horizontal distance between 2 locations.
                // -->
                if (attribute.getAttribute(2).startsWith("horizontal")) {

                    // <--[tag]
                    // @attribute <l@location.distance[<location>].horizontal.multiworld>
                    // @returns Element(Decimal)
                    // @description
                    // Returns the horizontal distance between 2 multiworld locations.
                    // -->
                    if (attribute.getAttribute(3).startsWith("multiworld")) {
                        return new Element(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(this.getZ() - toLocation.getZ(), 2)))
                                .getAttribute(attribute.fulfill(3));
                    }
                    else if (this.getWorld() == toLocation.getWorld()) {
                        return new Element(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(this.getZ() - toLocation.getZ(), 2)))
                                .getAttribute(attribute.fulfill(2));
                    }
                }

                // <--[tag]
                // @attribute <l@location.distance[<location>].vertical>
                // @returns Element(Decimal)
                // @description
                // Returns the vertical distance between 2 locations.
                // -->
                else if (attribute.getAttribute(2).startsWith("vertical")) {

                    // <--[tag]
                    // @attribute <l@location.distance[<location>].vertical.multiworld>
                    // @returns Element(Decimal)
                    // @description
                    // Returns the vertical distance between 2 multiworld locations.
                    // -->
                    if (attribute.getAttribute(3).startsWith("multiworld")) {
                        return new Element(Math.abs(this.getY() - toLocation.getY()))
                                .getAttribute(attribute.fulfill(3));
                    }
                    else if (this.getWorld() == toLocation.getWorld()) {
                        return new Element(Math.abs(this.getY() - toLocation.getY()))
                                .getAttribute(attribute.fulfill(2));
                    }
                }

                if (!getWorld().getName().equalsIgnoreCase(toLocation.getWorld().getName())) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("Can't measure distance between two different worlds!");
                    }
                    return null;
                }
                else {
                    return new Element(this.distance(toLocation))
                            .getAttribute(attribute.fulfill(1));
                }
            }
        }

        // <--[tag]
        // @attribute <l@location.is_within_border>
        // @returns Element(Boolean)
        // @description
        // Returns whether the location is within the world border.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1) && attribute.startsWith("is_within_border")) {
            return new Element(getWorld().getWorldBorder().isInside(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.is_within[<cuboid>/<ellipsoid>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the location is within the cuboid or ellipsoid.
        // -->
        if (attribute.startsWith("is_within")
                && attribute.hasContext(1)) {
            if (dEllipsoid.matches(attribute.getContext(1))) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(attribute.getContext(1));
                if (ellipsoid != null) {
                    return new Element(ellipsoid.contains(this))
                            .getAttribute(attribute.fulfill(1));
                }
            }
            else {
                dCuboid cuboid = dCuboid.valueOf(attribute.getContext(1));
                if (cuboid != null) {
                    return new Element(cuboid.isInsideCuboid(this))
                            .getAttribute(attribute.fulfill(1));
                }
            }
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.biome.formatted>
        // @returns Element
        // @description
        // Returns the formatted biome name at the location.
        // -->
        if (attribute.startsWith("biome.formatted")) {
            return new Element(CoreUtilities.toLowerCase(getBlock().getBiome().name()).replace('_', ' '))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.biome>
        // @mechanism dLocation.biome
        // @returns dBiome
        // @description
        // Returns the biome at the location.
        // -->
        if (attribute.startsWith("biome")) {
            return new dBiome(getBlock().getBiome())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.cuboids>
        // @returns dList(dCuboid)
        // @description
        // Returns a dList of all notable dCuboids that include this location.
        // -->
        if (attribute.startsWith("cuboids")) {
            List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(this);
            dList cuboid_list = new dList();
            for (dCuboid cuboid : cuboids) {
                cuboid_list.add(cuboid.identify());
            }
            return cuboid_list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.ellipsoids>
        // @returns dList(dCuboid)
        // @description
        // Returns a dList of all notable dEllipsoids that include this location.
        // -->
        if (attribute.startsWith("ellipsoids")) {
            List<dEllipsoid> ellipsoids = dEllipsoid.getNotableEllipsoidsContaining(this);
            dList ellipsoid_list = new dList();
            for (dEllipsoid ellipsoid : ellipsoids) {
                ellipsoid_list.add(ellipsoid.identify());
            }
            return ellipsoid_list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.is_liquid>
        // @returns Element(Boolean)
        // @description
        // Returns whether the block at the location is a liquid.
        // -->
        if (attribute.startsWith("is_liquid")) {
            return new Element(getBlock().isLiquid()).getAttribute(attribute.fulfill(1));
        }


        // <--[tag]
        // @attribute <l@location.light.blocks>
        // @returns Element(Number)
        // @description
        // Returns the amount of light from light blocks that is
        // on the location.
        // -->
        if (attribute.startsWith("light.from_blocks") ||
                attribute.startsWith("light.blocks")) {
            return new Element(getBlock().getLightFromBlocks())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.light.sky>
        // @returns Element(Number)
        // @description
        // Returns the amount of light from the sky that is
        // on the location.
        // -->
        if (attribute.startsWith("light.from_sky") ||
                attribute.startsWith("light.sky")) {
            return new Element(getBlock().getLightFromSky())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.light>
        // @returns Element(Number)
        // @description
        // Returns the total amount of light on the location.
        // -->
        if (attribute.startsWith("light")) {
            return new Element(getBlock().getLightLevel())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.power>
        // @returns Element(Number)
        // @description
        // Returns the current redstone power level of a block.
        // -->
        if (attribute.startsWith("power")) {
            return new Element(getBlock().getBlockPower())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.type>
        // @returns Element
        // @description
        // Always returns 'Location' for dLocation objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Location").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.command_block_name>
        // @returns Element
        // @mechanism command_block_name
        // @description
        // Returns the name a command block is set to.
        // -->
        if (attribute.startsWith("command_block_name") && getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
            return new Element(((CommandBlock) getBlock().getState()).getName())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.command_block>
        // @returns Element
        // @mechanism command_block
        // @description
        // Returns the command a command block is set to.
        // -->
        if (attribute.startsWith("command_block") && getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
            return new Element(((CommandBlock) getBlock().getState()).getCommand())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.furnace_burn_time>
        // @returns Element(Number)
        // @mechanism furnace_burn_time
        // @description
        // Returns the burn time a furnace has left.
        // -->
        if (attribute.startsWith("furnace_burn_time")) {
            return new Element(((Furnace) getBlock().getState()).getBurnTime())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.furnace_cook_time>
        // @returns Element(Number)
        // @mechanism furnace_cook_time
        // @description
        // Returns the cook time a furnace has left.
        // -->
        if (attribute.startsWith("furnace_cook_time")) {
            return new Element(((Furnace) getBlock().getState()).getCookTime())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.attached_to>
        // @returns dLocation
        // @description
        // Returns the block this block is attached to.
        // (Only if it is a lever or button!)
        // -->
        if (attribute.startsWith("attached_to")) {
            BlockFace face = BlockFace.SELF;
            MaterialData data = getBlock().getState().getData();
            if (data instanceof Attachable) {
                face = ((Attachable) data).getAttachedFace();
            }
            if (face != BlockFace.SELF) {
                return new dLocation(getBlock().getRelative(face).getLocation()).getAttribute(attribute.fulfill(1));
            }
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a location!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        if (mechanism.matches("data") && mechanism.hasValue()) {
            dB.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release.");
            BlockData blockData = NMSHandler.getInstance().getBlockHelper().getBlockData(getBlock().getType(), (byte) value.asInt());
            blockData.setBlock(getBlock(), false);
        }

        // <--[mechanism]
        // @object dLocation
        // @name block_type
        // @input dMaterial
        // @description
        // Sets the type of the block.
        // @tags
        // <l@location.material>
        // -->
        if (mechanism.matches("block_type") && mechanism.requireObject(dMaterial.class)) {
            dMaterial mat = value.asType(dMaterial.class);
            byte data = mat.hasData() ? mat.getData() : 0;
            BlockData blockData = NMSHandler.getInstance().getBlockHelper().getBlockData(mat.getMaterial(), data);
            blockData.setBlock(getBlock(), false);
        }

        // <--[mechanism]
        // @object dLocation
        // @name biome
        // @input dBiome
        // @description
        // Sets the biome of the block.
        // @tags
        // <l@location.biome>
        // -->
        if (mechanism.matches("biome") && mechanism.requireObject(dBiome.class)) {
            value.asType(dBiome.class).getBiome().changeBlockBiome(this);
        }

        // <--[mechanism]
        // @object dLocation
        // @name spawner_type
        // @input dEntity
        // @description
        // Sets the entity that a mob spawner will spawn.
        // @tags
        // <l@location.spawner_type>
        // -->
        if (mechanism.matches("spawner_type") && mechanism.requireObject(dEntity.class)
                && getBlock().getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = ((CreatureSpawner) getBlock().getState());
            spawner.setSpawnedType(value.asType(dEntity.class).getBukkitEntityType());
            spawner.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name lock
        // @input Element
        // @description
        // Sets the container's lock password.
        // Locked containers can only be opened while holding an item with the name of the lock.
        // Leave blank to remove a container's lock.
        // @tags
        // <l@location.lock>
        // <l@location.is_locked>
        // <l@location.is_lockable>
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)
                && mechanism.matches("lock") && getBlock().getState() instanceof Lockable) {
            BlockState state = getBlock().getState();
            ((Lockable) state).setLock(mechanism.hasValue() ? mechanism.getValue().asString() : null);
            state.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name sign_contents
        // @input dList
        // @description
        // Sets the contents of a sign block.
        // Note that this takes an escaped list.
        // See <@link language property escaping>.
        // @tags
        // <l@location.sign_contents>
        // -->
        if (mechanism.matches("sign_contents") && getBlock().getState() instanceof Sign) {
            Sign state = (Sign) getBlock().getState();
            for (int i = 0; i < 4; i++) {
                state.setLine(i, "");
            }
            dList list = value.asType(dList.class);
            if (list.size() > 4) {
                dB.echoError("Sign can only hold four lines!");
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    state.setLine(i, EscapeTags.unEscape(list.get(i)));
                }
            }
            state.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name skull_skin
        // @input Element(|Element(|Element))
        // @description
        // Sets the skin of a skull block.
        // The first Element is a UUID.
        // Optionally, use the second Element for the skin texture cache.
        // Optionally, use the third Element for a player name.
        // @tags
        // <l@location.skull_skin>
        // -->
        if (mechanism.matches("skull_skin")) {
            final BlockState blockState = getBlock().getState();
            Material material = getBlock().getType();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)
                    && material != Material.PLAYER_HEAD && material != Material.PLAYER_WALL_HEAD) {
                dB.echoError("As of Minecraft version 1.13 you may only set the skin of a PLAYER_HEAD or PLAYER_WALL_HEAD.");
            }
            else if (blockState instanceof Skull) {
                dList list = mechanism.getValue().asType(dList.class);
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
                NMSHandler.getInstance().getBlockHelper().setPlayerProfile((Skull) blockState, profile);
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name flowerpot_contents
        // @input dMaterial
        // @description
        // Sets the contents of a flower pot.
        // NOTE: Replaced by materials (such as POTTED_CACTUS) in 1.13 and above.
        // NOTE: Flowerpot contents will not update client-side until players refresh the chunk.
        // Refresh a chunk manually with mechanism: refresh_chunk_sections for dChunk objects
        // @tags
        // <l@location.flowerpot_contents>
        // -->
        if (mechanism.matches("flowerpot_contents") && mechanism.requireObject(dMaterial.class)) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                dB.echoError("As of Minecraft version 1.13 potted flowers each have their own material, such as POTTED_CACTUS.");
            }
            else if (getBlock().getType() == Material.FLOWER_POT) {
                MaterialData data = value.asType(dMaterial.class).getMaterialData();
                NMSHandler.getInstance().getBlockHelper().setFlowerpotContents(getBlock(), data);
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name command_block_name
        // @input Element
        // @description
        // Sets the name of a command block.
        // @tags
        // <l@location.command_block_name>
        // -->
        if (mechanism.matches("command_block_name")) {
            if (getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
                CommandBlock block = ((CommandBlock) getBlock().getState());
                block.setName(value.asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name command_block
        // @input Element
        // @description
        // Sets the command of a command block.
        // @tags
        // <l@location.command_block>
        // -->
        if (mechanism.matches("command_block")) {
            if (getBlock().getType() == MaterialCompat.COMMAND_BLOCK) {
                CommandBlock block = ((CommandBlock) getBlock().getState());
                block.setCommand(value.asString());
                block.update();
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name furnace_burn_time
        // @input Element(Number)
        // @description
        // Sets the burn time for a furnace in ticks. Maximum is 32767.
        // @tags
        // <l@location.furnace_burn_time>
        // -->
        if (mechanism.matches("furnace_burn_time")) {
            if (MaterialCompat.isFurnace(getBlock().getType())) {
                Furnace furnace = (Furnace) getBlock().getState();
                furnace.setBurnTime((short) value.asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name furnace_cook_time
        // @input Element(Number)
        // @description
        // Sets the cook time for a furnace in ticks. Maximum is 32767.
        // @tags
        // <l@location.furnace_cook_time>
        // -->
        if (mechanism.matches("furnace_cook_time")) {
            if (MaterialCompat.isFurnace(getBlock().getType())) {
                Furnace furnace = (Furnace) getBlock().getState();
                furnace.setCookTime((short) value.asInt());
                furnace.update();
            }
        }

        // <--[mechanism]
        // @object dLocation
        // @name base_color
        // @input Element
        // @description
        // Changes the base color of the banner at this location.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // @tags
        // <l@location.base_color>
        // -->
        if (mechanism.matches("base_color")) {
            Banner banner = (Banner) getBlock().getState();
            banner.setBaseColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            banner.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name patterns
        // @input dList
        // @description
        // Changes the patterns of the banner at this location. Input must be in the form
        // "li@COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // @tags
        // <l@location.patterns>
        // -->
        if (mechanism.matches("patterns")) {
            List<org.bukkit.block.banner.Pattern> patterns = new ArrayList<org.bukkit.block.banner.Pattern>();
            dList list = mechanism.getValue().asType(dList.class);
            List<String> split;
            for (String string : list) {
                try {
                    split = CoreUtilities.split(string, '/', 2);
                    patterns.add(new org.bukkit.block.banner.Pattern(DyeColor.valueOf(split.get(0).toUpperCase()),
                            PatternType.valueOf(split.get(1).toUpperCase())));
                }
                catch (Exception e) {
                    dB.echoError("Could not apply pattern to banner: " + string);
                }
            }
            Banner banner = (Banner) getBlock().getState();
            banner.setPatterns(patterns);
            banner.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name head_rotation
        // @input Element(Number)
        // @description
        // Sets the rotation of the head at this location. Must be an integer 1 to 16.
        // @tags
        // <l@location.head_rotation>
        // -->
        if (mechanism.matches("head_rotation") && mechanism.requireInteger()) {
            Skull sk = (Skull) getBlock().getState();
            sk.setRotation(getSkullBlockFace(value.asInt() - 1));
            sk.update();
        }

        // <--[mechanism]
        // @object dLocation
        // @name generate_tree
        // @input Element
        // @description
        // Generates a tree at this location if possible.
        // For a list of valid tree types, see <@link url http://bit.ly/2o7m1je>
        // @tags
        // None
        // -->
        if (mechanism.matches("generate_tree") && mechanism.requireEnum(false, TreeType.values())) {
            boolean generated = getWorld().generateTree(this, TreeType.valueOf(value.asString().toUpperCase()));
            if (!generated) {
                dB.echoError("Could not generate tree at " + identifySimple() + ". Make sure this location can naturally generate a tree!");
            }
        }

        if (!mechanism.fulfilled()) {
            mechanism.reportInvalid();
        }
    }
}
