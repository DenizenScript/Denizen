package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
import org.bukkit.util.Vector;

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
    // These use the object notation "ellipsoid@".
    // The identity format for ellipsoids is <x>,<y>,<z>,<world>,<x-radius>,<y-radius>,<z-radius>
    // For example, 'ellipsoid@1,2,3,space,7,7,7'.
    //
    // -->

    public static List<EllipsoidTag> getNotableEllipsoidsContaining(Location location) {
        List<EllipsoidTag> ellipsoids = new ArrayList<>();
        for (EllipsoidTag ellipsoid : NotableManager.getAllType(EllipsoidTag.class)) {
            if (ellipsoid.contains(location)) {
                ellipsoids.add(ellipsoid);
            }
        }

        return ellipsoids;
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
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

        Notable noted = NotableManager.getSavedObject(string);
        if (noted instanceof EllipsoidTag) {
            return (EllipsoidTag) noted;
        }

        List<String> split = CoreUtilities.split(string, ',');

        if (split.size() != 7) {
            return null;
        }

        WorldTag world = WorldTag.valueOf(split.get(3), false);
        if (world == null) {
            return null;
        }

        for (int i = 0; i < 7; i++) {
            if (i != 3 && !ArgumentHelper.matchesDouble(split.get(i))) {
                if (context == null || context.debug) {
                    Debug.echoError("EllipsoidTag input is not a valid decimal number: " + split.get(i));
                    return null;
                }
            }
        }

        LocationTag location = new LocationTag(world.getWorld(), Double.parseDouble(split.get(0)), Double.parseDouble(split.get(1)), Double.parseDouble(split.get(2)));
        LocationTag size = new LocationTag(null, Double.parseDouble(split.get(4)), Double.parseDouble(split.get(5)), Double.parseDouble(split.get(6)));
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
            return EllipsoidTag.valueOf(arg, CoreUtilities.noDebugContext) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public ObjectTag duplicate() {
        try {
            return (ObjectTag) clone();
        }
        catch (CloneNotSupportedException ex) {
            Debug.echoError(ex);
            return null;
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
                list.addObject(loc);
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
            return "ellipsoid@" + NotableManager.getSavedId(this) + "<GR> (" + identifyFull() + ")";
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public boolean isUnique() {
        return NotableManager.isSaved(this);
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
                return new ListTag(object.getBlocks(attribute.contextAsType(1, ListTag.class).filter(MaterialTag.class, attribute.context), attribute));
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
        // @attribute <EllipsoidTag.add[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, shifted by the input location.
        // -->
        registerTag("add", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("ellipsoid.add[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(object.loc.clone().add(attribute.contextAsType(1, LocationTag.class)), object.size.clone());
        });

        // <--[tag]
        // @attribute <EllipsoidTag.contains[<location>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns a boolean indicating whether the specified location is inside this ellipsoid.
        // -->
        registerTag("contains", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("ellipsoid.contains[...] tag must have an input.");
                return null;
            }
            return new ElementTag(object.contains(attribute.contextAsType(1, LocationTag.class)));
        });

        // <--[tag]
        // @attribute <EllipsoidTag.with_location[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, set to the specified location.
        // -->
        registerTag("with_location", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("ellipsoid.with_location[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(attribute.contextAsType(1, LocationTag.class), object.size.clone());
        });

        // <--[tag]
        // @attribute <EllipsoidTag.with_size[<location>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, set to the specified size.
        // -->
        registerTag("with_size", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("ellipsoid.with_size[...] tag must have an input.");
                return null;
            }
            return new EllipsoidTag(object.loc.clone(), attribute.contextAsType(1, LocationTag.class));
        });

        // <--[tag]
        // @attribute <EllipsoidTag.with_world[<world>]>
        // @returns EllipsoidTag
        // @description
        // Returns a copy of this ellipsoid, set to the specified world.
        // -->
        registerTag("with_world", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("ellipsoid.with_world[...] tag must have an input.");
                return null;
            }
            LocationTag loc = object.loc.clone();
            loc.setWorld(attribute.contextAsType(1, WorldTag.class).getWorld());
            return new EllipsoidTag(loc, object.size.clone());
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
