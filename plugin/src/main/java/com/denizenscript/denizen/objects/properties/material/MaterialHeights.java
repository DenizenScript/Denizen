package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Wall;

public class MaterialHeights implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData().data;
        if (!(data instanceof Wall)) {
            return false;
        }
        return true;
    }

    public static MaterialHeights getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHeights((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "heights"
    };

    private MaterialHeights(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.heights>
        // @returns ListTag
        // @mechanism MaterialTag.heights
        // @group properties
        // @description
        // Returns the list of heights for a wall block, in order North|East|South|West|Vertical.
        // For n/e/s/w, can be "tall", "low", or "none". For vertical, can be "tall" or "none".
        // -->
        PropertyParser.<MaterialHeights>registerTag("heights", (attribute, material) -> {
            return material.getHeightsList();
        });
    }

    public Wall getWall() {
        return (Wall) material.getModernData().data;
    }

    public ListTag getHeightsList() {
        ListTag list = new ListTag(5);
        Wall wall = getWall();
        list.add(wall.getHeight(BlockFace.NORTH).name());
        list.add(wall.getHeight(BlockFace.EAST).name());
        list.add(wall.getHeight(BlockFace.SOUTH).name());
        list.add(wall.getHeight(BlockFace.WEST).name());
        list.add(wall.isUp() ? "TALL" : "NONE");
        return list;
    }

    @Override
    public String getPropertyString() {
        return getHeightsList().identify();
    }

    @Override
    public String getPropertyId() {
        return "heights";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name heights
        // @input ElementTag
        // @description
        // Sets the list of heights for a wall block, in order North|East|South|West|Vertical.
        // For n/e/s/w, can be "tall", "low", or "none". For vertical, can be "tall" or "none".
        // @tags
        // <MaterialTag.heights>
        // -->
        if (mechanism.matches("heights") && mechanism.requireObject(ListTag.class)) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() != 5) {
                mechanism.echoError("Invalid heights list, size must be 5.");
                return;
            }
            Wall wall = getWall();
            wall.setHeight(BlockFace.NORTH, Wall.Height.valueOf(list.get(0).toUpperCase()));
            wall.setHeight(BlockFace.EAST, Wall.Height.valueOf(list.get(1).toUpperCase()));
            wall.setHeight(BlockFace.SOUTH, Wall.Height.valueOf(list.get(2).toUpperCase()));
            wall.setHeight(BlockFace.WEST, Wall.Height.valueOf(list.get(3).toUpperCase()));
            wall.setUp(list.get(4).toUpperCase().equals("TALL"));
        }
    }
}
