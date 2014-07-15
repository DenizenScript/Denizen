package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.EscapeTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dLocation extends org.bukkit.Location implements dObject, Notable {

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
                + "," + getYaw()
                + "," + getPitch()
                + "," + getWorld().getName();
    }

    public static String getSaved(dLocation location) {
        for (dLocation saved : NotableManager.getAllType(dLocation.class)) {
            if (saved.getBlockX() != location.getBlockX()) continue;
            if (saved.getBlockY() != location.getBlockY()) continue;
            if (saved.getBlockZ() != location.getBlockZ()) continue;
            if (!saved.getWorld().getName().equals(location.getWorld().getName())) continue;
            return NotableManager.getSavedId(saved);
        }
        return null;
    }

    public void load() {}

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

    final static Pattern item_by_saved = Pattern.compile("(l@)(.+)");
    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string  the string or dScript argument String
     * @return  a Location, or null if incorrectly formatted
     *
     */
    @Fetchable("l")
    public static dLocation valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match @object format for saved dLocations
        Matcher m;

        m = item_by_saved.matcher(string);

        if (m.matches() && NotableManager.isSaved(m.group(2)) && NotableManager.isType(m.group(2), dLocation.class))
            return (dLocation) NotableManager.getSavedObject(m.group(2));



        ////////
        // Match location formats

        // Split values
        String[] split = string.replace("l@", "").split(",");

        if (split.length == 3)
            // If 4 values, standard dScript location format
            // x,y,z,world
            try {
                return new dLocation(null,
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
                dB.echoError("valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                return null;
            }

        else if (split.length == 4)
            // If 4 values, standard dScript location format
            // x,y,z,world
            try {
                return new dLocation(Bukkit.getWorld(split[3]),
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
                dB.echoError("valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                return null;
            }


        else if (split.length == 6)

            // If 6 values, location with pitch/yaw
            // x,y,z,yaw,pitch,world
            try
            {    return new dLocation(Bukkit.getWorld(split[5]),
                    Double.valueOf(split[0]),
                    Double.valueOf(split[1]),
                    Double.valueOf(split[2]),
                    Float.valueOf(split[3]),
                    Float.valueOf(split[4]));

            } catch(Exception e) {
                dB.echoError("valueOf dLocation returning null: " + string + "(internal exception:" + e.getMessage() + ")");
                return null;
            }

        dB.log("valueOf dLocation returning null: " + string);

        return null;
    }

    final static Pattern location_by_saved = Pattern.compile("(l@)(.+)");
    final static Pattern location =
            Pattern.compile("(-?\\d+\\.?\\d*,){3,5}[\\w\\s]+",
                    Pattern.CASE_INSENSITIVE);

    public static boolean matches(String string) {
        if (string == null || string.length() == 0)
            return false;

        Matcher m = location_by_saved.matcher(string);
        if (m.matches())
            return true;

        m = location.matcher(string);
        return m.matches();
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

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

    /**
     * Turns a world and coordinates into a Location, which has some helpful methods
     * for working with dScript.
     *
     * @param world  the world in which the location resides
     * @param x  x-coordinate of the location
     * @param y  y-coordinate of the location
     * @param z  z-coordinate of the location
     *
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
    public void setYaw(float yaw) { super.setYaw(yaw); }

    public boolean hasInventory() {
        return getBlock().getState() instanceof InventoryHolder;
    }

    public Inventory getBukkitInventory() {
        return hasInventory() ? ((InventoryHolder) getBlock().getState()).getInventory() : null;
    }

    public dInventory getInventory() {
        return hasInventory() ? new dInventory(getBukkitInventory()) : null;
    }

    int Compare(dLocation loc1, dLocation loc2) {
        if (loc1 == null || loc2 == null || loc1.equals(loc2))
            return 0;
        else {
            double dist = distanceSquared(loc1) - distanceSquared(loc2);
            return dist == 0 ? 0: (dist > 0 ? 1: -1);
        }
    }

    @Override
    public int hashCode() {
        return getBlockX() + getBlockY() + getBlockZ();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof dLocation)) return false;
        return getBlock().getLocation().equals(((dLocation) o).getBlock().getLocation());
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
        return (isUnique() ? "<G>" + prefix + "='<A>" + identify() + "(<Y>" + identifyRaw()+ "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() { return getSaved(this) != null; }

    @Override
    public String identify() {
        if (!raw && isUnique())
            return "l@" + getSaved(this);
        else return identifyRaw();
    }

    @Override
    public String identifySimple() {
        if (isUnique())
            return "l@" + getSaved(this);
        else if (getWorld() != null)
            return "l@" + getBlockX() + "," + getBlockY() + "," + getBlockZ();
        else
            return "l@" + getBlockX() + "," + getBlockY() + "," + getBlockZ()
                + "," + getWorld().getName();
    }

    public String identifyRaw() {
        if (getYaw() != 0.0 || getPitch() != 0.0)
            return "l@" + getX() + "," + getY()
                    + "," + getZ() + "," + getPitch() + "," + getYaw() + (getWorld() != null ? "," + getWorld().getName(): "");
        else
            return "l@" + getX() + "," + getY()
                    + "," + getZ() + (getWorld() != null ? "," + getWorld().getName(): "");
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return null;


        /////////////////////
        //   BLOCK ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.above>
        // @returns dLocation
        // @description
        // Returns the location one block above this location.
        // -->
        if (attribute.startsWith("above"))
            return new dLocation(this.add(0,1,0))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.below>
        // @returns dLocation
        // @description
        // Returns the location one block below this location.
        // -->
        if (attribute.startsWith("below"))
            return new dLocation(this.add(0,-1,0))
                    .getAttribute(attribute.fulfill(1));

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
            return Element.HandleNull(identify() + ".inventory", getInventory(), "dInventory").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.material>
        // @returns dMaterial
        // @description
        // Returns the material of the block at the location.
        // -->
        if (attribute.startsWith("material"))
            return dMaterial.getMaterialFrom(getBlock().getType(), getBlock().getData()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.switched>
        // @returns Element(Boolean)
        // @description
        // Returns whether the block at the location is considered to be switched on.
        // (For buttons, levers, etc.)
        // To change this, see <@link command Switch>
        // -->
        if (attribute.startsWith("switched"))
            return new Element((getBlock().getData() & 0x8) > 0).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.sign_contents.escaped>
        // @returns dList
        // @description
        // Returns a list of lines on a sign, pre-escaped to prevent issues.
        // See <@link language Property Escaping>
        // -->
        //  TODO: Mech for this...
        if (attribute.startsWith("sign_contents.escaped")) {
            if (getBlock().getState() instanceof Sign) {
                dList toReturn = new dList();
                for (String line: ((Sign) getBlock().getState()).getLines())
                    toReturn.add(EscapeTags.Escape(line));
                return toReturn.getAttribute(attribute.fulfill(2));
            }
            else return "null";
        }

        // <--[tag]
        // @attribute <l@location.sign_contents>
        // @returns dList
        // @description
        // Returns a list of lines on a sign.
        // -->
        if (attribute.startsWith("sign_contents")) {
            if (getBlock().getState() instanceof Sign) {
                return new dList(Arrays.asList(((Sign) getBlock().getState()).getLines()))
                        .getAttribute(attribute.fulfill(1));
            }
            else return "null";
        }

        // <--[tag]
        // @attribute <l@location.simple.formatted>
        // @returns Element
        // @description
        // Returns the formatted simple version of the dLocation's block coordinates.
        // EG: X 'x', Y 'y', Z 'z', in world 'world'
        // -->
        if (attribute.startsWith("simple.formatted"))
            return new Element("X '" + getBlockX()
                    + "', Y '" + getBlockY()
                    + "', Z '" + getBlockZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.simple>
        // @returns Element
        // @description
        // Returns a simple version of the dLocation's block coordinates.
        // EG: x,y,z,world
        // -->
        if (attribute.startsWith("simple"))
            return new Element(getBlockX() + "," + getBlockY() + "," + getBlockZ()
                    + "," + getWorld().getName()).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   DIRECTION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.direction.vector>
        // @returns dLocation
        // @description
        // Returns the location's direction as a one-length vector.
        // -->
        if (attribute.startsWith("direction.vector")) {
            double xzLen = Math.cos((getPitch() % 360) * (Math.PI/180));
            double nx = xzLen * Math.sin(-getYaw() * (Math.PI/180));
            double ny = Math.sin(getPitch() * (Math.PI/180));
            double nz = xzLen * Math.cos(getYaw() * (Math.PI/180));
            return new dLocation(getWorld(), nx, -ny, nz).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.direction[<location>]>
        // @returns Element
        // @description
        // Returns the compass direction between two locations.
        // If no second location is specified, returns the direction of the location.
        // -->
        if (attribute.startsWith("direction")) {
            // Get the cardinal direction from this location to another
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                // Subtract this location's vector from the other location's vector,
                // not the other way around
                dLocation target = dLocation.valueOf(attribute.getContext(1));
                attribute = attribute.fulfill(1);
                // <--[tag]
                // @attribute <l@location.direction[<location>].yaw>
                // @returns Element(Decimal)
                // @description
                // Returns the yaw direction between two locations.
                // -->
                if (attribute.startsWith("yaw"))
                    return new Element(Rotation.normalizeYaw(Rotation.getYaw
                            (target.toVector().subtract(this.toVector())
                                    .normalize())))
                            .getAttribute(attribute.fulfill(1));
                else
                    return new Element(Rotation.getCardinal(Rotation.getYaw
                            (target.toVector().subtract(this.toVector())
                                    .normalize())))
                            .getAttribute(attribute);
            }
            // Get a cardinal direction from this location's yaw
            else {
                return new Element(Rotation.getCardinal(getYaw()))
                        .getAttribute(attribute.fulfill(1));
            }
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
                // @attribute <location.facing[<entity>/<location>].degrees[X]>
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
                    return new Element(Rotation.isFacingLocation
                            (this, dLocation.valueOf(attribute.getContext(1)), degrees))
                            .getAttribute(attribute.fulfill(attributePos));
                }
                else if (dEntity.matches(attribute.getContext(1))) {
                    return new Element(Rotation.isFacingLocation
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
            } else if (context.split(",").length == 2) {
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
            float yaw = Rotation.normalizeYaw(getYaw());
            if (yaw < 45)
                return new Element("South")
                        .getAttribute(attribute.fulfill(2));
            else if (yaw < 135)
                return new Element("West")
                        .getAttribute(attribute.fulfill(2));
            else if (yaw < 225)
                return new Element("North")
                        .getAttribute(attribute.fulfill(2));
            else if (yaw < 315)
                return new Element("East")
                        .getAttribute(attribute.fulfill(2));
            else
                return new Element("South")
                        .getAttribute(attribute.fulfill(2));
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
            return new Element(Rotation.normalizeYaw(getYaw()))
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   ENTITY AND BLOCK LIST ATTRIBUTES
        /////////////////

        if (attribute.startsWith("find") || attribute.startsWith("nearest")) {
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <l@location.find.blocks[<block>|...].within[<#.#>]>
            // @returns dList
            // @description
            // Returns a list of matching blocks within a radius.
            // -->
            if (attribute.startsWith("blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dLocation> found = new ArrayList<dLocation>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                List<dMaterial> materials = new ArrayList<dMaterial>();
                if (attribute.hasContext(1))
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);
                // Avoid NPE from invalid materials
                if (materials == null) return Element.NULL.getAttribute(attribute.fulfill(2));

                // dB.log(materials + " " + radius + " ");
                attribute.fulfill(2);

                for (double x = -(radius); x <= radius; x++)
                    for (double y = -(radius); y <= radius; y++)
                        for (double z = -(radius); z <= radius; z++)
                            if (!materials.isEmpty()) {
                                for (dMaterial material : materials) {
                                    if (material.hasData() && material.getData() != 0) {
                                        if (material.matchesMaterialData(getBlock()
                                                .getLocation().add(x,y,z).getBlock().getType().getNewData(getBlock()
                                                        .getLocation().add(x,y,z).getBlock().getData())))
                                            found.add(new dLocation(getBlock().getLocation().add(x + 0.5, y, z + 0.5)));
                                    }
                                    else if (material.getMaterial() == getBlock().getLocation().add(x,y,z).getBlock().getType())
                                        found.add(new dLocation(getBlock().getLocation().add(x + 0.5, y, z + 0.5)));
                                }
                            } else found.add(new dLocation(getBlock().getLocation().add(x + 0.5, y, z + 0.5)));

                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return Compare(loc1, loc2);
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
                if (attribute.hasContext(1))
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);
                // Avoid NPE from invalid materials
                if (materials == null) return Element.NULL.getAttribute(attribute.fulfill(2));

                attribute.fulfill(2);

                for (double x = -(radius); x <= radius; x++)
                    for (double y = -(radius); y <= radius; y++)
                        for (double z = -(radius); z <= radius; z++) {
                            Location l = getBlock().getLocation().clone().add(x,y,z);
                            if (!materials.isEmpty()) {
                                for (dMaterial material : materials) {
                                    if (material.matchesMaterialData(getBlock()
                                            .getLocation().clone().add(x,y,z).getBlock().getType().getNewData(getBlock()
                                                    .getLocation().clone().add(x,y,z).getBlock().getData()))) {
                                        if (l.clone().add(0,1,0).getBlock().getType() == Material.AIR
                                                && l.clone().add(0,2,0).getBlock().getType() == Material.AIR
                                                && l.getBlock().getType() != Material.AIR)
                                            found.add(new dLocation(getBlock().getLocation().clone().add(x + 0.5, y, z + 0.5 )));
                                    }
                                }
                            }
                            else {
                                if (l.clone().add(0,1,0).getBlock().getType() == Material.AIR
                                        && l.clone().add(0,2,0).getBlock().getType() == Material.AIR
                                        && l.getBlock().getType() != Material.AIR) {
                                    found.add(new dLocation(getBlock().getLocation().clone().add(x + 0.5, y, z + 0.5 )));
                                }
                            }
                        }

                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return Compare(loc1, loc2);
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
                for (Player player : Bukkit.getOnlinePlayers())
                    if (!player.isDead() && Utilities.checkLocation(this, player.getLocation(), radius))
                        found.add(new dPlayer(player));

                Collections.sort(found, new Comparator<dPlayer>() {
                    @Override
                    public int compare(dPlayer pl1, dPlayer pl2) {
                        return Compare(pl1.getLocation(), pl2.getLocation());
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
                for (dNPC npc : DenizenAPI.getSpawnedNPCs())
                    if (Utilities.checkLocation(this, npc.getLocation(), radius))
                        found.add(npc);

                Collections.sort(found, new Comparator<dNPC>() {
                    @Override
                    public int compare(dNPC npc1, dNPC npc2) {
                        return Compare(npc1.getLocation(), npc2.getLocation());
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
                    for (String ent : dList.valueOf(attribute.getContext(1))) {
                        if (dEntity.matches(ent))
                            ent_list.add(ent.toUpperCase());
                    }
                }
                ArrayList<dEntity> found = new ArrayList<dEntity>();
                double radius = aH.matchesDouble(attribute.getContext(2)) ? attribute.getDoubleContext(2) : 10;
                attribute.fulfill(2);
                for (Entity entity : getWorld().getEntities()) {
                    if (Utilities.checkLocation(this, entity.getLocation(), radius)) {
                        dEntity current = new dEntity(entity);
                        if (!ent_list.isEmpty()) {
                            for (String ent : ent_list) {
                                if ((entity.getType().name().equals(ent) ||
                                        current.identify().equalsIgnoreCase(ent)) && entity.isValid()) {
                                    found.add(current);
                                    break;
                                }
                            }
                        }
                        else
                            found.add(current);
                    }
                }

                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return Compare(ent1.getLocation(), ent2.getLocation());
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
                for (Entity entity : getWorld().getEntities())
                    if (entity instanceof LivingEntity
                            && Utilities.checkLocation(this, entity.getLocation(), radius))
                        found.add(new dEntity(entity));

                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return Compare(ent1.getLocation(), ent2.getLocation());
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            return new Element("null").getAttribute(attribute);
        }


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.formatted.citizens>
        // @returns Element
        // @description
        // Returns the location formatted for a Citizens command.
        // EG: x.x:y.y:z.z:world
        // -->
        if (attribute.startsWith("formatted.citizens"))
            return new Element(getX() + ":" + getY() + ":" + getZ() + ":" + getWorld().getName()).getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.formatted>
        // @returns Element
        // @description
        // Returns the formatted version of the dLocation.
        // EG: 'X 'x.x', Y 'y.y', Z 'z.z', in world 'world'
        // -->
        if (attribute.startsWith("formatted"))
            return new Element("X '" + getX()
                    + "', Y '" + getY()
                    + "', Z '" + getZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.get_chunk>
        // @returns dChunk
        // @description
        // returns the chunk that this location belongs to.
        // -->
        if (attribute.startsWith("get_chunk") ||
                attribute.startsWith("chunk"))
            return new dChunk(this).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.raw>
        // @returns dLocation
        // @description
        // returns the raw representation of this location,
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


        /////////////////////
        //   MATHEMATICAL ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <l@location.add[<x,y,z>]>
        // @returns dLocation
        // @description
        // Returns the location with the specified coordinates added to it.
        // -->
        if (attribute.startsWith("add")
                && attribute.hasContext(1)) {
            if (attribute.getContext(1).split(",").length == 3) {
                String[] ints = attribute.getContext(1).split(",", 3);
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
        // @attribute <l@location.sub[<x,y,z>]>
        // @returns dLocation
        // @description
        // Returns the location with the specified coordinates subtracted from it.
        // -->
        if (attribute.startsWith("sub")
                && attribute.hasContext(1)) {
            if (attribute.getContext(1).split(",").length == 3) {
                String[] ints = attribute.getContext(1).split(",", 3);
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
                    if (attribute.getAttribute(3).startsWith("multiworld"))
                        return new Element(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(this.getZ() - toLocation.getZ(), 2)))
                                .getAttribute(attribute.fulfill(3));
                    else if (this.getWorld() == toLocation.getWorld())
                        return new Element(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(this.getZ() - toLocation.getZ(), 2)))
                                .getAttribute(attribute.fulfill(2));
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
                    if (attribute.getAttribute(3).startsWith("multiworld"))
                        return new Element(Math.abs(this.getY() - toLocation.getY()))
                                .getAttribute(attribute.fulfill(3));
                    else if (this.getWorld() == toLocation.getWorld())
                        return new Element(Math.abs(this.getY() - toLocation.getY()))
                                .getAttribute(attribute.fulfill(2));
                }

                else return new Element(this.distance(toLocation))
                            .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.is_within[<cuboid>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the location is within the cuboid.
        // -->
        if (attribute.startsWith("is_within")
                && attribute.hasContext(1)) {
            dCuboid cuboid = dCuboid.valueOf(attribute.getContext(1));
            if (cuboid != null)
                return new Element(cuboid.isInsideCuboid(this))
                        .getAttribute(attribute.fulfill(1));
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
        if (attribute.startsWith("biome.formatted"))
            return new Element(getBlock().getBiome().name().toLowerCase().replace('_', ' '))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.biome.humidity>
        // @returns Element(Decimal)
        // @description
        // Returns the current humidity at the location.
        // -->
        if (attribute.startsWith("biome.humidity"))
            return new Element(getBlock().getHumidity())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.biome.temperature>
        // @returns Element(Decimal)
        // @description
        // Returns the current temperature at the location.
        // -->
        if (attribute.startsWith("biome.temperature"))
            return new Element(getBlock().getTemperature())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.biome>
        // @returns Element
        // @description
        // Returns the biome name at the location.
        // -->
        if (attribute.startsWith("biome"))
            return new Element(getBlock().getBiome().name())
                    .getAttribute(attribute.fulfill(1));

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
        // @attribute <l@location.is_liquid>
        // @returns Element(Boolean)
        // @description
        // Returns whether block at the location is a liquid.
        // -->
        if (attribute.startsWith("is_liquid"))
            return new Element(getBlock().isLiquid()).getAttribute(attribute.fulfill(1));


        // <--[tag]
        // @attribute <l@location.light.blocks>
        // @returns Element(Number)
        // @description
        // Returns the amount of light from light blocks that is
        // on the location.
        // -->
        if (attribute.startsWith("light.from_blocks") ||
                attribute.startsWith("light.blocks"))
            return new Element(getBlock().getLightFromBlocks())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.light.sky>
        // @returns Element(Number)
        // @description
        // Returns the amount of light from the sky that is
        // on the location.
        // -->
        if (attribute.startsWith("light.from_sky") ||
                attribute.startsWith("light.sky"))
            return new Element(getBlock().getLightFromSky())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.light>
        // @returns Element(Number)
        // @description
        // Returns the total amount of light on the location.
        // -->
        if (attribute.startsWith("light"))
            return new Element(getBlock().getLightLevel())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.power>
        // @returns Element(Number)
        // @description
        // Returns the current redstone power level of a block.
        // -->
        if (attribute.startsWith("power"))
            return new Element(getBlock().getBlockPower())
                    .getAttribute(attribute.fulfill(1));


        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

}
