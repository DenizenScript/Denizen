package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.util.Vector;

public class MaterialHalf extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name half
    // @input ElementTag
    // @description
    // Controls a bisected material's current half (like a door, double-plant, chest, or a bed).
    // For "Bisected" blocks (doors/double plants/...), values are either "BOTTOM" or "TOP".
    // For beds, values are either "HEAD" or "FOOT".
    // For chests, values are either "LEFT" or "RIGHT".
    // For the relative vector, see <@link tag MaterialTag.relative_vector>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Bisected
                || data instanceof Bed
                || data instanceof Chest;
    }

    public MaterialHalf(MaterialTag material) {
        super(material);
    }

    @Override
    public String getPropertyId() {
        return "half";
    }

    @Override
    public ElementTag getPropertyValue() {
        BlockData data = getBlockData();
        if (data instanceof Bisected bisected) {
            return new ElementTag(bisected.getHalf().name());
        }
        else if (data instanceof Bed bed) {
            return new ElementTag(bed.getPart().name());
        }
        else if (data instanceof Chest chest) {
            if (chest.getType() == Chest.Type.SINGLE) {
                return null;
            }
            return new ElementTag(chest.getType().name());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        BlockData data = getBlockData();
        if (data instanceof Bisected bisected) {
            bisected.setHalf(value.asEnum(Bisected.Half.class));
        }
        else if (data instanceof Bed bed) {
            bed.setPart(value.asEnum(Bed.Part.class));
        }
        else if (data instanceof Chest chest) {
            chest.setType(value.asEnum(Chest.Type.class));
        }
    }

    public static void register() {
        autoRegister("half", MaterialHalf.class, ElementTag.class, true);

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

    public static Vector getRelativeBlockVector(BlockData data) {
        if (data instanceof Bisected bisected) {
            if (bisected.getHalf() == Bisected.Half.TOP) {
                return new Vector(0, -1, 0);
            }
            else {
                return new Vector(0, 1, 0);
            }
        }
        else if (data instanceof Bed bed) {
            BlockFace face = bed.getFacing();
            if (bed.getPart() == Bed.Part.HEAD) {
                face = face.getOppositeFace();
            }
            return face.getDirection();
        }
        else if (data instanceof Chest chest) {
            if (chest.getType() == Chest.Type.SINGLE) {
                return null;
            }
            Vector direction = chest.getFacing().getDirection();
            if (chest.getType() == Chest.Type.LEFT) {
                return new Vector(-direction.getZ(), 0, direction.getX());
            }
            return new Vector(direction.getZ(), 0, -direction.getX());
        }
        return null;
    }

    public Vector getRelativeBlockVector() {
        return getRelativeBlockVector(getBlockData());
    }
}
