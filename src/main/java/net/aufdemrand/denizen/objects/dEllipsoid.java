package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.List;


public class dEllipsoid implements dObject {
    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets an Ellipsoid Object from a string form.
     *
     * @param string  the string
     *
     */
    @Fetchable("ellipsoid")
    public static dEllipsoid valueOf(String string) {

        string = string.substring("ellipsoid@".length());

        List<String> split = CoreUtilities.Split(string, ',');

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

    public boolean contains(dLocation test) {
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
    public String getObjectType() {
        return "Ellipsoid";
    }

    @Override
    public String identify() {
        return "ellipsoid@" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getWorld().getName()
                + "," + size.getX() + "," + size.getY() + "," + size.getZ();
    }

    @Override
    public String identifySimple() {
        return identify();
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

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

}
