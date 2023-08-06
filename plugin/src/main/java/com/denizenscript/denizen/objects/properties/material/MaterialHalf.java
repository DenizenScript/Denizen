package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.util.Vector;

public class MaterialHalf implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && isHalfData(((MaterialTag) material).getModernData());
    }

    public static boolean isHalfData(BlockData data) {
        if (data instanceof Bisected || data instanceof Bed || data instanceof Chest) {
            return true;
        }
        return false;
    }

    public static MaterialHalf getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHalf((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "half"
    };

    public MaterialHalf(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.half>
        // @returns ElementTag
        // @mechanism MaterialTag.half
        // @group properties
        // @description
        // Returns the current half for a bisected material (like a door, double-plant, chest, or a bed).
        // Output for "Bisected" blocks (doors/double plants/...) is "BOTTOM" or "TOP".
        // Output for beds is "HEAD" or "FOOT".
        // Output for chests is "LEFT" or "RIGHT".
        // -->
        PropertyParser.registerStaticTag(MaterialHalf.class, ElementTag.class, "half", (attribute, material) -> {
            String halfName = material.getHalfName();
            if (halfName == null) {
                return null;
            }
            return new ElementTag(halfName);
        });

        // <--[tag]
        // @attribute <MaterialTag.relative_vector>
        // @returns LocationTag
        // @mechanism MaterialTag.half
        // @group properties
        // @description
        // Returns a vector location of the other block for a bisected material.
        // -->
        PropertyParser.registerStaticTag(MaterialHalf.class, LocationTag.class, "relative_vector", (attribute, material) -> {
            Vector vector = material.getRelativeBlockVector();
            if (vector == null) {
                return null;
            }
            return new LocationTag(vector);
        });
    }

    public static String getHalfName(BlockData data) {
        if (data instanceof Bisected) {
            return ((Bisected) data).getHalf().name();
        }
        else if (data instanceof Bed) {
            return ((Bed) data).getPart().name();
        }
        else if (data instanceof Chest) {
            if (((Chest) data).getType() == Chest.Type.SINGLE) {
                return null;
            }
            return ((Chest) data).getType().name();
        }
        return null;
    }

    public String getHalfName() {
        return getHalfName(material.getModernData());
    }

    public static void setHalfByName(BlockData data, String name) {
        if (data instanceof Bisected) {
            ((Bisected) data).setHalf(Bisected.Half.valueOf(name));
        }
        else if (data instanceof Bed) {
            ((Bed) data).setPart(Bed.Part.valueOf(name));
        }
        else if (data instanceof Chest) {
            ((Chest) data).setType(Chest.Type.valueOf(name));
        }
    }

    public static Vector getRelativeBlockVector(BlockData data) {
        if (data instanceof Bisected) {
            if (((Bisected) data).getHalf() == Bisected.Half.TOP) {
                return new Vector(0, -1, 0);
            }
            else {
                return new Vector(0, 1, 0);
            }
        }
        else if (data instanceof Bed) {
            BlockFace face = ((Directional) data).getFacing();
            if (((Bed) data).getPart() == Bed.Part.HEAD) {
                face = face.getOppositeFace();
            }
            return face.getDirection();
        }
        else if (data instanceof Chest) {
            if (((Chest) data).getType() == Chest.Type.SINGLE) {
                return null;
            }
            Vector direction = ((Directional) data).getFacing().getDirection();
            if (((Chest) data).getType() == Chest.Type.LEFT) {
                return new Vector(-direction.getZ(), 0, direction.getX());
            }
            return new Vector(direction.getZ(), 0, -direction.getX());
        }
        return null;
    }

    public Vector getRelativeBlockVector() {
        return getRelativeBlockVector(material.getModernData());
    }

    @Override
    public String getPropertyString() {
        return getHalfName();
    }

    @Override
    public String getPropertyId() {
        return "half";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name half
        // @input ElementTag
        // @description
        // Sets the current half for a bisected material (like a door, double-plant, chest, or a bed).
        // @tags
        // <MaterialTag.half>
        // -->
        if (mechanism.matches("half")) {
            setHalfByName(material.getModernData(), mechanism.getValue().asString().toUpperCase());
        }
    }
}
