package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dLocation extends org.bukkit.Location implements dObject {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static Map<String, dLocation> uniqueObjects = new HashMap<String, dLocation>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dLocation location) {
        return uniqueObjects.containsValue(location);
    }

    public static boolean isSaved(Location location) {
        for (Map.Entry<String, dLocation> i : uniqueObjects.entrySet())
            if (i.getValue() == location) return true;

        return uniqueObjects.containsValue(location);
    }

    public static dLocation getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dLocation location) {
        for (Map.Entry<String, dLocation> i : uniqueObjects.entrySet()) {
            if (i.getValue().getBlockX() != location.getBlockX()) continue;
            if (i.getValue().getBlockY() != location.getBlockY()) continue;
            if (i.getValue().getBlockZ() != location.getBlockZ()) continue;
            if (i.getValue().getWorld().getName() != location.getWorld().getName()) continue;
            return i.getKey();
        }
        return null;
    }

    public static String getSaved(Location location) {
        dLocation dLoc = new dLocation(location);
        return getSaved(dLoc);
    }

    public static void saveAs(dLocation location, String id) {
        if (location == null) return;
        uniqueObjects.put(id.toUpperCase(), location);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }

    /*
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallLocations() {
        List<String> loclist = DenizenAPI.getCurrentInstance().getSaves().getStringList("dScript.Locations");
        uniqueObjects.clear();
        for (String location : loclist) {
            String id = location.split(";")[0];
            dLocation loc = valueOf(location.split(";")[1]);
            uniqueObjects.put(id, loc);
        }
    }

    /*
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveLocations() {
        List<String> loclist = new ArrayList<String>();
        for (Map.Entry<String, dLocation> entry : uniqueObjects.entrySet())
            loclist.add(entry.getKey() + ";"
                    + entry.getValue().getBlockX()
                    + "," + entry.getValue().getBlockY()
                    + "," + entry.getValue().getBlockZ()
                    + "," + entry.getValue().getYaw()
                    + "," + entry.getValue().getPitch()
                    + "," + entry.getValue().getWorld().getName());

        DenizenAPI.getCurrentInstance().getSaves().set("dScript.Locations", loclist);
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string  the string or dScript argument String
     * @return  a Location, or null if incorrectly formatted
     *
     */
    public static dLocation valueOf(String string) {
        if (string == null) return null;

        ////////
        // Match @object format for saved dLocations
        Matcher m;

        final Pattern item_by_saved = Pattern.compile("(l@)(.+)");
        m = item_by_saved.matcher(string);

        if (m.matches() && isSaved(m.group(2)))
            return getSaved(m.group(2));


        ////////
        // Match location formats

        // Split values
        String[] split = string.replace("l@", "").split(",");

        if (split.length == 4)
            // If 4 values, standard dScript location format
            // x,y,z,world
            try {
                return new dLocation(Bukkit.getWorld(split[3]),
                        Double.valueOf(split[0]),
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch(Exception e) {
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
                return null;
            }

        dB.log("valueOf dLocation returning null: " + string);

        return null;
    }


    public static boolean matches(String string) {
        final Pattern location_by_saved = Pattern.compile("(l@)(.+)");
        Matcher m = location_by_saved.matcher(string);
        if (m.matches())
            return true;

        final Pattern location =
                Pattern.compile("((-?\\d+(\\.\\d+)?,){3}|(-?\\d+(\\.\\d+)?,){5})\\w+",
                        Pattern.CASE_INSENSITIVE);
        m = location.matcher(string);
        if (m.matches())
            return true;

        return false;
    }

    /**
     * Turns a Bukkit Location into a Location, which has some helpful methods
     * for working with dScript.
     *
     * @param location the Bukkit Location to reference
     */
    public dLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
        // If supplied location has pitch/yaw, set it.
        if (location.getPitch() > 0f
                && location.getYaw() > 0f) {
            hasPitchYaw = true;
            this.setPitch(location.getPitch());
            this.setYaw(location.getYaw());
        }
    }

    /**
     * Turns a world and coordinates into a Location, which has some helpful methods
     * for working with dScript. If working with temporary locations, this is
     * a much better method to use than {@link #dLocation(org.bukkit.World, double, double, double)}.
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
        hasPitchYaw = true;
    }

    boolean hasPitchYaw = false;

    @Override
    public void setPitch(float pitch) {
        hasPitchYaw = true;
        super.setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        hasPitchYaw = true;
        super.setYaw(yaw);
    }

    public boolean hasPitchYaw() {
        return hasPitchYaw;
    }

    public dLocation rememberAs(String id) {
        dLocation.saveAs(this, id);
        return this;
    }



    String prefix = "Location";

    @Override
    public String getType() {
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
        return (isSaved(this) ? "<G>" + prefix + "='<A>" + getSaved(this) + "(<Y>" + identify()+ "<A>)<G>'  "
                : "<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        if (isSaved(this)) return true;
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String identify() {
        if (isSaved(this))
            return "l@" + getSaved(this);
        else if (hasPitchYaw()) return "l@" + getX() + "," + getY()
                + "," + getZ() + "," + getPitch() + "," + getYaw() + "," + getWorld().getName();
        else return "l@" + getX() + "," + getY()
                    + "," + getZ() + "," + getWorld().getName();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return null;

        if (attribute.startsWith("biome.formatted"))
            return new Element(getBlock().getBiome().name().toLowerCase().replace('_', ' '))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome.humidity"))
            return new Element(String.valueOf(getBlock().getHumidity()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome.temperature"))
            return new Element(String.valueOf(getBlock().getTemperature()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("biome"))
            return new Element(String.valueOf(getBlock().getBiome().name()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("block.below"))
            return new dLocation(this.add(0,-1,0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("block.above"))
            return new dLocation(this.add(0,1,0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("add")) {
            if (attribute.hasContext(1) && attribute.getContext(1).split(",").length == 3) {
                String[] ints = attribute.getContext(1).split(",", 3);
                if ((aH.matchesDouble(ints[0]) || aH.matchesInteger(ints[0]))
                        && (aH.matchesDouble(ints[1]) || aH.matchesInteger(ints[1]))
                        && (aH.matchesDouble(ints[2]) || aH.matchesInteger(ints[2])))
                    return new dLocation(this.clone().add(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2]))).getAttribute(attribute.fulfill(1));
            }
        }

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

        if (attribute.startsWith("find")) {
            attribute.fulfill(1);
            ArrayList<dObject> found = new ArrayList<dObject>();
            // <location.find.blocks[sandstone|cobblestone].within[5]>
            if (attribute.startsWith("blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                List<dObject> materials = new ArrayList<dObject>();
                if (attribute.hasContext(1))
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);

                // dB.log(materials + " " + radius + " ");
                attribute.fulfill(2);

                for (int x = -(radius); x <= radius; x++)
                    for (int y = -(radius); y <= radius; y++)
                        for (int z = -(radius); z <= radius; z++)
                            if (!materials.isEmpty()) {
                                for (dObject material : materials)
                                    if (((dMaterial) material).matchesMaterialData(getBlock()
                                            .getRelative(x,y,z).getType().getNewData(getBlock()
                                                    .getRelative(x,y,z).getData())))
                                        found.add(new dLocation(getBlock().getRelative(x,y,z).getLocation()));
                            } else found.add(new dLocation(getBlock().getRelative(x,y,z).getLocation()));
            }

            // <location.find.surface_blocks[sandstone|cobblestone].within[5]>

            else if (attribute.startsWith("surface_blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                List<dObject> materials = new ArrayList<dObject>();
                if (attribute.hasContext(1))
                    materials = dList.valueOf(attribute.getContext(1)).filter(dMaterial.class);

                attribute.fulfill(2);

                for (int x = -(radius); x <= radius; x++)
                    for (int y = -(radius); y <= radius; y++)
                        for (int z = -(radius); z <= radius; z++)
                            if (!materials.isEmpty()) {
                                for (dObject material : materials)
                                    if (((dMaterial) material).matchesMaterialData(getBlock()
                                            .getRelative(x,y,z).getType().getNewData(getBlock()
                                                    .getRelative(x,y,z).getData()))) {
                                        Location l = getBlock().getRelative(x,y,z).getLocation();
                                        if (l.add(0,1,0).getBlock().getType() == Material.AIR
                                                && l.add(0,1,0).getBlock().getType() == Material.AIR)
                                            found.add(new dLocation(getBlock().getRelative(x,y,z).getLocation()));
                                    }
                            } else {
                                Location l = getBlock().getRelative(x,y,z).getLocation();
                                if (l.add(0,1,0).getBlock().getType() == Material.AIR
                                        && l.add(0,1,0).getBlock().getType() == Material.AIR)
                                    found.add(new dLocation(getBlock().getRelative(x,y,z).getLocation()));
                            }
            }

            else return "null";

            return new dList(found).getAttribute(attribute);
        }

        if (attribute.startsWith("block.material"))
            return new Element(getBlock().getType().toString()).getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("direction"))
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1)))
                return new Element(Rotation.getCardinal(Rotation.getYaw
                        (this.toVector().subtract(dLocation.valueOf(attribute.getContext(1)).toVector())
                                .normalize())))
                        .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("distance")) {
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                dLocation toLocation = dLocation.valueOf(attribute.getContext(1));

                if (attribute.getAttribute(2).startsWith("horizontal")) {
                    if (attribute.getAttribute(3).startsWith("multiworld"))
                        return new Element(String.valueOf(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(toLocation.getZ() - toLocation.getZ(), 2))))
                                .getAttribute(attribute.fulfill(3));
                    else if (this.getWorld() == toLocation.getWorld())
                        return new Element(String.valueOf(Math.sqrt(
                                Math.pow(this.getX() - toLocation.getX(), 2) +
                                        Math.pow(toLocation.getZ() - toLocation.getZ(), 2))))
                                .getAttribute(attribute.fulfill(2));
                }

                else if (attribute.getAttribute(2).startsWith("vertical")) {
                    if (attribute.getAttribute(3).startsWith("multiworld"))
                        return new Element(String.valueOf(Math.abs(this.getY() - toLocation.getY())))
                                .getAttribute(attribute.fulfill(3));
                    else if (this.getWorld() == toLocation.getWorld())
                        return new Element(String.valueOf(Math.abs(this.getY() - toLocation.getY())))
                                .getAttribute(attribute.fulfill(2));
                }

                else return new Element(String.valueOf(this.distance(toLocation)))
                            .getAttribute(attribute.fulfill(1));
            }
        }

        if (attribute.startsWith("formatted.simple"))
            return new Element("X '" + getBlockX()
                    + "', Y '" + getBlockY()
                    + "', Z '" + getBlockZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("formatted"))
            return new Element("X '" + getX()
                    + "', Y '" + getY()
                    + "', Z '" + getZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_liquid"))
            return new Element(String.valueOf(getBlock().isLiquid())).getAttribute(attribute.fulfill(1));


        if (attribute.startsWith("light.from_blocks") ||
                attribute.startsWith("light.blocks"))
            return new Element(String.valueOf((int) getBlock().getLightFromBlocks()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("light.from_sky") ||
                attribute.startsWith("light.sky"))
            return new Element(String.valueOf((int) getBlock().getLightFromSky()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("light"))
            return new Element(String.valueOf((int) getBlock().getLightLevel()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("power"))
            return new Element(String.valueOf(getBlock().getBlockPower()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("in_region")) {
            if (Depends.worldGuard == null) {
                dB.echoError("Cannot check region! WorldGuard is not loaded!");
                return null;
            }

            String region = attribute.getContext(1);

            return new Element(String.valueOf(WorldGuardUtilities.checkWGRegion(this, region)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("world")) {
            return dWorld.mirrorBukkitWorld(getWorld())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("block.x")) {
            return new Element(getBlockX()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("block.y")) {
            return new Element(getBlockY()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("block.z")) {
            return new Element(getBlockZ()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("x")) {
            return new Element(getX()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("y")) {
            return new Element(getY()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("z")) {
            return new Element(getZ()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("block.sign_contents")) {
            if (getBlock().getState() instanceof Sign) {
                return new dList(Arrays.asList(((Sign) getBlock().getState()).getLines()))
                        .getAttribute(attribute.fulfill(2));
            }
            else return "null";
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
