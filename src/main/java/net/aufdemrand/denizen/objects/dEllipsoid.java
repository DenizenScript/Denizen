package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizencore.objects.notable.Note;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;


public class dEllipsoid implements dObject, Notable {

    public static List<dEllipsoid> getNotableEllipsoidsContaining(Location location) {
        List<dEllipsoid> cuboids = new ArrayList<dEllipsoid>();
        for (dEllipsoid ellipsoid : NotableManager.getAllType(dEllipsoid.class))
            if (ellipsoid.contains(location))
                cuboids.add(ellipsoid);

        return cuboids;
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dEllipsoid valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets an Ellipsoid Object from a string form.
     *
     * @param string  the string
     *
     */
    @Fetchable("ellipsoid")
    public static dEllipsoid valueOf(String string, TagContext context) {

        if (string.startsWith("ellipsoid@"))
            string = string.substring(10);

        if (NotableManager.isType(string, dEllipsoid.class))
            return (dEllipsoid) NotableManager.getSavedObject(string);

        List<String> split = CoreUtilities.split(string, ',');

        if (split.size() != 7)
            return null;

        dLocation location = new dLocation(dWorld.valueOf(split.get(3)).getWorld(),
                aH.getDoubleFrom(split.get(0)), aH.getDoubleFrom(split.get(1)), aH.getDoubleFrom(split.get(2)));
        dLocation size = new dLocation(null, aH.getDoubleFrom(split.get(4)),
                aH.getDoubleFrom(split.get(5)), aH.getDoubleFrom(split.get(6)));
        return new dEllipsoid(location, size);
    }

    /**
     * Determines whether a string is a valid ellipsoid.
     *
     * @param arg  the string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

        return arg.startsWith("ellipsoid@");
    }


    ///////////////
    //   Constructors
    /////////////

    public dEllipsoid(dLocation loc, dLocation size) {
        this.loc = loc;
        this.size = size;
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private dLocation loc;

    private dLocation size;

    public dList getBlocks() {
        return getBlocks(null);
    }

    public dList getBlocks(List<dMaterial> materials) {
        List<dLocation> initial = new dCuboid(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(materials);
        dList list = new dList();
        for (dLocation loc: initial) {
            if (contains(loc)) {
                list.add(loc.identify());
            }
        }
        return list;
    }

    public List<dLocation> getBlockLocations() {
        List<dLocation> initial = new dCuboid(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(null);
        List<dLocation> locations = new ArrayList<dLocation>();
        for (dLocation loc: initial) {
            if (contains(loc)) {
                locations.add(loc);
            }
        }
        return locations;
    }

    public boolean contains(Location test) {
        double xbase = test.getX() - loc.getX();
        double ybase = test.getY() - loc.getY();
        double zbase = test.getZ() - loc.getZ();
        return ((xbase * xbase) / (size.getX() * size.getX())
            + (ybase * ybase) / (size.getY() * size.getY())
            + (zbase * zbase) / (size.getZ() * size.getZ()) < 1);
    }

    String prefix = "ellipsoid";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    @Note("Ellipsoids")
    public Object getSaveObject() {
        return identifyFull().substring(10);
    }

    @Override
    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }

    @Override
    public void forget() {
        NotableManager.remove(this);
    }

    @Override
    public String getObjectType() {
        return "Ellipsoid";
    }

    @Override
    public String identify() {
        if (isUnique())
            return "ellipsoid@" + NotableManager.getSavedId(this);
        else
            return identifyFull();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    public String identifyFull() {
        return "ellipsoid@" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getWorld().getName()
                + "," + size.getX() + "," + size.getY() + "," + size.getZ();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null)
            this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.get_blocks[<material>|...]>
        // @returns dList(dLocation)
        // @description
        // Returns each block location within the dEllipsoid.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        if (attribute.startsWith("get_blocks")) {
            if (attribute.hasContext(1))
                return new dList(getBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class)))
                        .getAttribute(attribute.fulfill(1));
            else
                return new dList(getBlocks())
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.location>
        // @returns dLocation
        // @description
        // Returns the location of the ellipsoid.
        // -->
        if (attribute.startsWith("location")) {
            return loc.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.size>
        // @returns dLocation
        // @description
        // Returns the size of the ellipsoid.
        // -->
        if (attribute.startsWith("size")) {
            return size.getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

}
