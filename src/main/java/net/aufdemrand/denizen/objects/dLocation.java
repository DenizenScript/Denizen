package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;
import net.aufdemrand.denizen.utilities.entity.Rotation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dLocation extends org.bukkit.Location implements dObject {

    // This pattern correctly reads both 0.9 and 0.8 notables
    final static Pattern notablePattern =
            Pattern.compile("(\\w+)[;,]((-?\\d+\\.?\\d*,){3,5}\\w+)",
                    Pattern.CASE_INSENSITIVE);

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
            if (!i.getValue().getWorld().getName().equals(location.getWorld().getName())) continue;
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
            Matcher m = notablePattern.matcher(location);
            if (m.matches()) {
                String id = m.group(1);
                dLocation loc = valueOf(m.group(2));
                uniqueObjects.put(id, loc);
            }
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
    @ObjectFetcher("l")
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
                Pattern.compile("(-?\\d+\\.?\\d*,){3,5}\\w+",
                        Pattern.CASE_INSENSITIVE);
        m = location.matcher(string);
        return m.matches();
    }

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
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
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
        return isSaved(this);
    }

    @Override
    public String identify() {
        if (isSaved(this))
            return "l@" + getSaved(this);
        else if (getYaw() != 0.0 && getPitch() != 0.0) return "l@" + getX() + "," + getY()
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
        // @returns Element(Number)
        // @description
        // Returns the current humidity at the location.
        // -->
        if (attribute.startsWith("biome.humidity"))
            return new Element(String.valueOf(getBlock().getHumidity()))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.biome.temperature>
        // @returns Element(Number)
        // @description
        // Returns the current temperature at the location.
        // -->
        if (attribute.startsWith("biome.temperature"))
            return new Element(String.valueOf(getBlock().getTemperature()))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.biome.humidity>
        // @returns Element
        // @description
        // Returns Bukkit biome name at the location.
        // -->
        if (attribute.startsWith("biome"))
            return new Element(String.valueOf(getBlock().getBiome().name()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.block.below>
        // @returns dLocation
        // @description
        // Returns the dLocation of the block below the location.
        // -->
        if (attribute.startsWith("block.below"))
            return new dLocation(this.add(0,-1,0))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.block.above>
        // @returns dLocation
        // @description
        // Returns the dLocation of the block above the location.
        // -->
        if (attribute.startsWith("block.above"))
            return new dLocation(this.add(0,1,0))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.add[x,y,z]>
        // @returns dLocation
        // @description
        // Adds to location coordinates, and returns the sum.
        // -->
        if (attribute.startsWith("add")) {
            if (attribute.hasContext(1) && attribute.getContext(1).split(",").length == 3) {
                String[] ints = attribute.getContext(1).split(",", 3);
                if ((aH.matchesDouble(ints[0]) || aH.matchesInteger(ints[0]))
                        && (aH.matchesDouble(ints[1]) || aH.matchesInteger(ints[1]))
                        && (aH.matchesDouble(ints[2]) || aH.matchesInteger(ints[2]))) {
                    return new dLocation(this.clone().add(Double.valueOf(ints[0]),
                            Double.valueOf(ints[1]),
                            Double.valueOf(ints[2]))).getAttribute(attribute.fulfill(1));
                }
            }
        }

        // <--[tag]
        // @attribute <l@location.with_pose>
        // @returns dLocation
        // @description
        // Returns the dLocation with pitch and yaw.
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

        if (attribute.startsWith("find") || attribute.startsWith("nearest")) {
            attribute.fulfill(1);
            
            // <--[tag]
            // @attribute <l@location.find.blocks[<block>|...].within[X]>
            // @returns dList
            // @description
            // Returns a dList of blocks within a radius.
            // -->
            if (attribute.startsWith("blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dLocation> found = new ArrayList<dLocation>();
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
                
                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return (int) (distanceSquared(loc1) - distanceSquared(loc2));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.surface_blocks[<block>|...].within[X]>
            // @returns dList
            // @description
            // Returns a dList of surface blocks within a radius.
            // -->
            else if (attribute.startsWith("surface_blocks")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dLocation> found = new ArrayList<dLocation>();
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
                
                Collections.sort(found, new Comparator<dLocation>() {
                    @Override
                    public int compare(dLocation loc1, dLocation loc2) {
                        return (int) (distanceSquared(loc1) - distanceSquared(loc2));
                    }
                });

                return new dList(found).getAttribute(attribute); 
            }
            
            // <--[tag]
            // @attribute <l@location.find.players.within[X]>
            // @returns dList
            // @description
            // Returns a dList of players within a radius.
            // -->
            else if (attribute.startsWith("players")
                && attribute.getAttribute(2).startsWith("within")
                && attribute.hasContext(2)) {
                ArrayList<dPlayer> found = new ArrayList<dPlayer>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                attribute.fulfill(2);
                for (Player player : Bukkit.getOnlinePlayers())
                    if (Utilities.checkLocation(this, player.getLocation(), radius))
                        found.add(new dPlayer(player));
                
                Collections.sort(found, new Comparator<dPlayer>() {
                    @Override
                    public int compare(dPlayer pl1, dPlayer pl2) {
                        return (int) (distanceSquared(pl1.getLocation()) - distanceSquared(pl2.getLocation()));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }
            
            // <--[tag]
            // @attribute <l@location.find.npcs.within[X]>
            // @returns dList
            // @description
            // Returns a dList of NPCs within a radius.
            // -->
            else if (attribute.startsWith("npcs")
                && attribute.getAttribute(2).startsWith("within")
                && attribute.hasContext(2)) {
                ArrayList<dNPC> found = new ArrayList<dNPC>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                attribute.fulfill(2);
                for (dNPC npc : DenizenAPI.getSpawnedNPCs())
                    if (Utilities.checkLocation(this, npc.getLocation(), radius))
                        found.add(npc);
                
                Collections.sort(found, new Comparator<dNPC>() {
                    @Override
                    public int compare(dNPC npc1, dNPC npc2) {
                        return (int) (distanceSquared(npc1.getLocation()) - distanceSquared(npc2.getLocation()));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }
            
            // <--[tag]
            // @attribute <l@location.find.npcs.within[X]>
            // @returns dList
            // @description
            // Returns a dList of NPCs within a radius.
            // -->
            else if (attribute.startsWith("npcs")
                && attribute.getAttribute(2).startsWith("within")
                && attribute.hasContext(2)) {
                ArrayList<dNPC> found = new ArrayList<dNPC>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                attribute.fulfill(2);
                for (dNPC npc : DenizenAPI.getSpawnedNPCs())
                    if (Utilities.checkLocation(this, npc.getLocation(), radius))
                        found.add(npc);
                
                Collections.sort(found, new Comparator<dNPC>() {
                    @Override
                    public int compare(dNPC npc1, dNPC npc2) {
                        return (int) (distanceSquared(npc1.getLocation()) - distanceSquared(npc2.getLocation()));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.entities.within[X]>
            // @returns dList
            // @description
            // Returns a dList of entities within a radius.
            // -->
            else if (attribute.startsWith("entities")
                && attribute.getAttribute(2).startsWith("within")
                && attribute.hasContext(2)) {
                ArrayList<dEntity> found = new ArrayList<dEntity>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                attribute.fulfill(2);
                for (Entity entity : getWorld().getEntities())
                    if (Utilities.checkLocation(this, entity.getLocation(), radius))
                                found.add(new dEntity(entity));
                
                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return (int) (distanceSquared(ent1.getBukkitEntity().getLocation()) - distanceSquared(ent2.getBukkitEntity().getLocation()));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            // <--[tag]
            // @attribute <l@location.find.living_entities.within[X]>
            // @returns dList
            // @description
            // Returns a dList of living entities within a radius.
            // -->
            else if (attribute.startsWith("living_entities")
                    && attribute.getAttribute(2).startsWith("within")
                    && attribute.hasContext(2)) {
                ArrayList<dEntity> found = new ArrayList<dEntity>();
                int radius = aH.matchesInteger(attribute.getContext(2)) ? attribute.getIntContext(2) : 10;
                attribute.fulfill(2);
                for (Entity entity : getWorld().getEntities())
                    if (entity instanceof LivingEntity
                            && Utilities.checkLocation(this, entity.getLocation(), radius))
                        found.add(new dEntity(entity));

                Collections.sort(found, new Comparator<dEntity>() {
                    @Override
                    public int compare(dEntity ent1, dEntity ent2) {
                        return (int) (distanceSquared(ent1.getBukkitEntity().getLocation()) - distanceSquared(ent2.getBukkitEntity().getLocation()));
                    }
                });

                return new dList(found).getAttribute(attribute);
            }

            return new Element("null").getAttribute(attribute);
        }
        
        // <--[tag]
        // @attribute <l@location.inventory>
        // @returns dInventory
        // @description
        // Returns the dInventory of the block at the location. If the
        // block is not a container, returns null.
        // -->
        if (attribute.startsWith("inventory")) {
            if (getBlock().getState() instanceof InventoryHolder)
                return new dInventory(getBlock().getState()).getAttribute(attribute.fulfill(1));
            return new Element("null").getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <l@location.block.material>
        // @returns Element
        // @description
        // Returns the Bukkit material name of the block at the
        // location.
        // -->
        if (attribute.startsWith("block.material"))
            return new Element(getBlock().getType().toString()).getAttribute(attribute.fulfill(2));

        
        // <--[tag]
        // @attribute <l@location.direction>
        // @returns Element
        // @description
        // Returns the compass direction of the block or entity
        // at the location.
        // -->
        if (attribute.startsWith("direction")) {
            // Get the cardinal direction from this location to another
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                // Subtract this location's vector from the other location's vector,
                // not the other way around
                return new Element(Rotation.getCardinal(Rotation.getYaw
                        (dLocation.valueOf(attribute.getContext(1)).toVector().subtract(this.toVector())
                                .normalize())))
                        .getAttribute(attribute.fulfill(1));
            }
            // Get a cardinal direction from this location's yaw
            else {
                return new Element(Rotation.getCardinal(getYaw()))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <l@location.distance[<location>]>
        // @returns Element(Number)
        // @description
        // Returns the distance between 2 locations.
        // -->
        if (attribute.startsWith("distance")) {
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                dLocation toLocation = dLocation.valueOf(attribute.getContext(1));

                // <--[tag]
                // @attribute <l@location.distance[<location>].horizontal>
                // @returns Element(Number)
                // @description
                // Returns the horizontal distance between 2 locations.
                // -->
                if (attribute.getAttribute(2).startsWith("horizontal")) {
                    
                    // <--[tag]
                    // @attribute <l@location.distance[<location>].horizontal.multiworld>
                    // @returns Element(Number)
                    // @description
                    // Returns the horizontal distance between 2 multiworld locations.
                    // -->
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

                // <--[tag]
                // @attribute <l@location.distance[<location>].vertical>
                // @returns Element(Number)
                // @description
                // Returns the vertical distance between 2 locations.
                // -->
                else if (attribute.getAttribute(2).startsWith("vertical")) {
                    
                    // <--[tag]
                    // @attribute <l@location.distance[<location>].vertical.multiworld>
                    // @returns Element(Number)
                    // @description
                    // Returns the vertical distance between 2 multiworld locations.
                    // -->
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

        // <--[tag]
        // @attribute <l@location.simple>
        // @returns Element
        // @description
        // Returns the simple version of a dLocation.
        // -->
        if (attribute.startsWith("simple"))
            return new Element(getBlockX() + "," + getBlockY() + "," + getBlockZ()
            + "," + getWorld().getName()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.formatted.simple>
        // @returns Element
        // @description
        // Returns the formatted simple version of a dLocation.
        // -->
        if (attribute.startsWith("formatted.simple"))
            return new Element("X '" + getBlockX()
                    + "', Y '" + getBlockY()
                    + "', Z '" + getBlockZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.formatted>
        // @returns Element
        // @description
        // Returns the formatted version of a dLocation.
        // -->
        if (attribute.startsWith("formatted"))
            return new Element("X '" + getX()
                    + "', Y '" + getY()
                    + "', Z '" + getZ()
                    + "', in world '" + getWorld().getName() + "'").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.is_liquid>
        // @returns Element(Boolean)
        // @description
        // If the block at the location is a liquid, return
        // true. Otherwise, returns false.
        // -->
        if (attribute.startsWith("is_liquid"))
            return new Element(String.valueOf(getBlock().isLiquid())).getAttribute(attribute.fulfill(1));


        // <--[tag]
        // @attribute <l@location.light.blocks>
        // @returns Element(Number)
        // @description
        // Returns the amount of light from blocks that is
        // on the location.
        // -->
        if (attribute.startsWith("light.from_blocks") ||
                attribute.startsWith("light.blocks"))
            return new Element(String.valueOf((int) getBlock().getLightFromBlocks()))
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
            return new Element(String.valueOf((int) getBlock().getLightFromSky()))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <l@location.light.blocks>
        // @returns Element(Number)
        // @description
        // Returns the total amount of light on the location.
        // -->
        if (attribute.startsWith("light"))
            return new Element(String.valueOf((int) getBlock().getLightLevel()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.pitch>
        // @returns Element(Number)
        // @description
        // Returns the pitch of the object at the location.
        // -->
        if (attribute.startsWith("pitch")) {
            return new Element(String.valueOf(getPitch())).getAttribute(attribute.fulfill(1));
        }
        
        // <--[tag]
        // @attribute <l@location.yaw.raw>
        // @returns Element(Number)
        // @description
        // Returns the raw yaw of the object at the location.
        // -->
        if (attribute.startsWith("yaw.raw")) {
            return new Element(String.valueOf
                    (getYaw())).getAttribute(attribute.fulfill(2));
        }
        
        // <--[tag]
        // @attribute <l@location.yaw>
        // @returns Element(Number)
        // @description
        // Returns the normalized yaw of the object at the location.
        // -->
        if (attribute.startsWith("yaw")) {
            return new Element(String.valueOf
                    (Rotation.normalizeYaw(getYaw()))).getAttribute(attribute.fulfill(1));
        }
        
        // <--[tag]
        // @attribute <l@location.facing[<value>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the location's yaw is facing another
        // entity or location. Otherwise, returns false.
        // -->
        if (attribute.startsWith("facing")) {
            if (attribute.hasContext(1)) {
                
                // The default number of degrees if there is no degrees attribute
                int degrees = 45;
                
                // The attribute to fulfill from
                int attributePos = 1;
                
                // <--[tag]
                // @attribute <location.facing[<value>].degrees[X]>
                // @returns Element(Boolean)
                // @description
                // Returns true if the location's yaw is facing another
                // entity or location, within a specified degree range.
                // Otherwise, returns false.
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
        // @attribute <l@location.power>
        // @returns Element(Number)
        // @description
        // Returns the current power level of a block.
        // -->
        if (attribute.startsWith("power"))
            return new Element(String.valueOf(getBlock().getBlockPower()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <l@location.in_region[<name>|...]>
        // @returns Element(Boolean)
        // @description
        // If a region name or list of names is specified, returns whether the
        // location is in one of the listed regions, otherwise it returns whether
        // the location is in any region.
        // -->
        if (attribute.startsWith("in_region")) {
            if (Depends.worldGuard == null) {
                dB.echoError("Cannot check region! WorldGuard is not loaded!");
                return null;
            }

            // Check if the player is in the specified region
            if (attribute.hasContext(1)) {
                dList region_list = dList.valueOf(attribute.getContext(1));
                for(String region: region_list)
                    if(WorldGuardUtilities.inRegion(this, region))
                        return Element.TRUE.getAttribute(attribute.fulfill(1));
                return Element.FALSE.getAttribute(attribute.fulfill(1));
            }
            
            // Check if the player is in any region
            else {
                return new Element(String.valueOf(WorldGuardUtilities.inRegion(this)))
                    .getAttribute(attribute.fulfill(1));
            }
        }
        
        // <--[tag]
        // @attribute <l@location.regions>
        // @returns dList
        // @description
        // Returns the list of regions that the location is in.
        // -->
        if (attribute.startsWith("regions")) {
            return new dList(WorldGuardUtilities.getRegions(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.world>
        // @returns dWorld
        // @description
        // Returns the dWorld that the location is in.
        // -->
        if (attribute.startsWith("world")) {
            return dWorld.mirrorBukkitWorld(getWorld())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <location.block.x>
        // @returns Element(Number)
        // @description
        // Returns the X coordinate of the block.
        // -->
        if (attribute.startsWith("block.x")) {
            return new Element(getBlockX()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.block.y>
        // @returns Element(Number)
        // @description
        // Returns the Y coordinate of the block.
        // -->
        if (attribute.startsWith("block.y")) {
            return new Element(getBlockY()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.block.z>
        // @returns Element(Number)
        // @description
        // Returns the Z coordinate of the block.
        // -->
        if (attribute.startsWith("block.z")) {
            return new Element(getBlockZ()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <l@location.x>
        // @returns Element(Number)
        // @description
        // Returns the X coordinate of the location.
        // -->
        if (attribute.startsWith("x")) {
            return new Element(getX()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.y>
        // @returns Element(Number)
        // @description
        // Returns the Y coordinate of the location.
        // -->
        if (attribute.startsWith("y")) {
            return new Element(getY()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.z>
        // @returns Element(Number)
        // @description
        // Returns the Z coordinate of the location.
        // -->
        if (attribute.startsWith("z")) {
            return new Element(getZ()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <l@location.block.sign_contents>
        // @returns dList
        // @description
        // Returns a list of lines on a sign.
        // -->
        if (attribute.startsWith("block.sign_contents")) {
            if (getBlock().getState() instanceof Sign) {
                return new dList(Arrays.asList(((Sign) getBlock().getState()).getLines()))
                        .getAttribute(attribute.fulfill(2));
            }
            else return "null";
        }

        // <--[tag]
        // @attribute <l@location.highest>
        // @returns dLocation
        // @description
        // Returns the location of the highest at x,z that isn't air.
        // -->
        if (attribute.startsWith("highest")) {
            return new dLocation(getWorld().getHighestBlockAt(this).getLocation())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
