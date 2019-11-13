package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class EllipsoidTag implements ObjectTag, Notable {

    // <--[language]
    // @name EllipsoidTag Objects
    // @group Object System
    // @description
    // An EllipsoidTag represents an ellipsoidal region in the world.
    //
    // The word 'ellipsoid' means a less strict sphere.
    // Basically: an "ellipsoid" is to a 3D "sphere" what an "ellipse" (or "oval") is to a 2D "circle".
    //
    // For format info, see <@link language ellipsoid@>
    //
    // -->

    // <--[language]
    // @name ellipsoid@
    // @group Object Fetcher System
    // @description
    // ellipsoid@ refers to the 'object identifier' of an EllipsoidTag. The 'ellipsoid@' is notation for Denizen's Object
    // Fetcher. The constructor for an EllipsoidTag is <x>,<y>,<z>,<world>,<x-radius>,<y-radius>,<z-radius>
    // For example, 'ellipsoid@1,2,3,space,7,7,7'.
    //
    // For general info, see <@link language EllipsoidTag Objects>
    //
    // -->

    public static List<EllipsoidTag> getNotableEllipsoidsContaining(Location location) {
        List<EllipsoidTag> cuboids = new ArrayList<>();
        for (EllipsoidTag ellipsoid : NotableManager.getAllType(EllipsoidTag.class)) {
            if (ellipsoid.contains(location)) {
                cuboids.add(ellipsoid);
            }
        }

        return cuboids;
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static EllipsoidTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets an Ellipsoid Object from a string form.
     *
     * @param string the string
     */
    @Fetchable("ellipsoid")
    public static EllipsoidTag valueOf(String string, TagContext context) {

        if (string.startsWith("ellipsoid@")) {
            string = string.substring(10);
        }

        if (NotableManager.isType(string, EllipsoidTag.class)) {
            return (EllipsoidTag) NotableManager.getSavedObject(string);
        }

        List<String> split = CoreUtilities.split(string, ',');

        if (split.size() != 7) {
            return null;
        }

        WorldTag world = WorldTag.valueOf(split.get(3), false);
        if (world == null) {
            return null;
        }

        LocationTag location = new LocationTag(world.getWorld(),
                ArgumentHelper.getDoubleFrom(split.get(0)), ArgumentHelper.getDoubleFrom(split.get(1)), ArgumentHelper.getDoubleFrom(split.get(2)));
        LocationTag size = new LocationTag(null, ArgumentHelper.getDoubleFrom(split.get(4)),
                ArgumentHelper.getDoubleFrom(split.get(5)), ArgumentHelper.getDoubleFrom(split.get(6)));
        return new EllipsoidTag(location, size);
    }

    /**
     * Determines whether a string is a valid ellipsoid.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        try {
            return EllipsoidTag.valueOf(arg) != null;
        }
        catch (Exception e) {
            return false;
        }
    }


    ///////////////
    //   Constructors
    /////////////

    public EllipsoidTag(LocationTag loc, LocationTag size) {
        this.loc = loc;
        this.size = size;
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private LocationTag loc;

    private LocationTag size;

    public ListTag getBlocks(Attribute attribute) {
        return getBlocks(null, attribute);
    }

    public ListTag getBlocks(List<MaterialTag> materials, Attribute attribute) {
        List<LocationTag> initial = new CuboidTag(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(materials, attribute);
        ListTag list = new ListTag();
        for (LocationTag loc : initial) {
            if (contains(loc)) {
                list.add(loc.identify());
            }
        }
        return list;
    }

    public List<LocationTag> getBlockLocationsUnfiltered() {
        List<LocationTag> initial = new CuboidTag(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlockLocationsUnfiltered();
        List<LocationTag> locations = new ArrayList<>();
        for (LocationTag loc : initial) {
            if (contains(loc)) {
                locations.add(loc);
            }
        }
        return locations;
    }

    public List<LocationTag> getBlockLocations(Attribute attribute) {
        List<LocationTag> initial = new CuboidTag(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(null, attribute);
        List<LocationTag> locations = new ArrayList<>();
        for (LocationTag loc : initial) {
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
    public String debuggable() {
        if (isUnique()) {
            return "cu@" + NotableManager.getSavedId(this) + "<GR> (" + identifyFull() + ")";
        }
        else {
            return identifyFull();
        }
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
        if (isUnique()) {
            return "ellipsoid@" + NotableManager.getSavedId(this);
        }
        else {
            return identifyFull();
        }
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EllipsoidTag.blocks[<material>|...]>
        // @returns ListTag(LocationTag)
        // @description
        // Returns each block location within the EllipsoidTag.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("blocks", (attribute, object) -> {
            if (attribute.hasContext(1)) {
                return new ListTag(object.getBlocks(ListTag.valueOf(attribute.getContext(1)).filter(MaterialTag.class, attribute.context), attribute));
            }
            else {
                return new ListTag(object.getBlocks(attribute));
            }
        }, "get_blocks");

        // <--[tag]
        // @attribute <EllipsoidTag.location>
        // @returns LocationTag
        // @description
        // Returns the location of the ellipsoid.
        // -->
        registerTag("location", (attribute, object) -> {
            return object.loc;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.size>
        // @returns LocationTag
        // @description
        // Returns the size of the ellipsoid.
        // -->
        registerTag("size", (attribute, object) -> {
            return object.size;
        });

        // <--[tag]
        // @attribute <EllipsoidTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Ellipsoid' for EllipsoidTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, object) -> {
            return new ElementTag("Ellipsoid");
        });
    }

    public static ObjectTagProcessor<EllipsoidTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<EllipsoidTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}
