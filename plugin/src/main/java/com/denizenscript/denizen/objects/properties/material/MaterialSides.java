package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Wall;

public class MaterialSides extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name sides
    // @input ListTag
    // @description
    // Controls the list of heights for a wall block, or connections for a redstone wire, in order North|East|South|West|Vertical.
    // For wall blocks: For n/e/s/w, can be "tall", "low", or "none". For vertical, can be "tall" or "none".
    // For redstone wires: For n/e/s/w, can be "none", "side", or "up". No vertical.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Wall || data instanceof RedstoneWire;
    }

    MaterialTag material;

    @Override
    public ListTag getPropertyValue() {
        return getSidesList();
    }

    @Override
    public String getPropertyId() {
        return "sides";
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name heights
        // @input ElementTag
        // @deprecated Use 'sides'
        // @description
        // Deprecated in favor of <@link property MaterialTag.sides>
        // @tags
        // <MaterialTag.heights>
        // -->
        if (isWall()) {
            if (list.size() != 5) {
                mechanism.echoError("Invalid sides list, size must be 5.");
                return;
            }
            Wall wall = getWall();
            wall.setHeight(BlockFace.NORTH, Wall.Height.valueOf(list.get(0).toUpperCase()));
            wall.setHeight(BlockFace.EAST, Wall.Height.valueOf(list.get(1).toUpperCase()));
            wall.setHeight(BlockFace.SOUTH, Wall.Height.valueOf(list.get(2).toUpperCase()));
            wall.setHeight(BlockFace.WEST, Wall.Height.valueOf(list.get(3).toUpperCase()));
            wall.setUp(CoreUtilities.toLowerCase(list.get(4)).equals("tall"));
        }
        else if (isWire()) {
            if (list.size() != 4) {
                mechanism.echoError("Invalid sides list, size must be 4.");
                return;
            }
            RedstoneWire wire = getWire();
            wire.setFace(BlockFace.NORTH, RedstoneWire.Connection.valueOf(list.get(0).toUpperCase()));
            wire.setFace(BlockFace.EAST, RedstoneWire.Connection.valueOf(list.get(1).toUpperCase()));
            wire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.valueOf(list.get(2).toUpperCase()));
            wire.setFace(BlockFace.WEST, RedstoneWire.Connection.valueOf(list.get(3).toUpperCase()));
        }
    }

    public static void register() {
        autoRegister("sides", MaterialSides.class, ListTag.class, true, "heights");

        // <--[tag]
        // @attribute <MaterialTag.heights>
        // @returns ListTag
        // @mechanism MaterialTag.heights
        // @group properties
        // @deprecated Use 'sides'
        // @description
        // Deprecated in favor of <@link property MaterialTag.sides>
        // -->
    }

    public boolean isWall() {
        return material.getModernData() instanceof Wall;
    }

    public Wall getWall() {
        return (Wall) material.getModernData();
    }

    public boolean isWire() {
        return material.getModernData() instanceof RedstoneWire;
    }

    public RedstoneWire getWire() {
        return (RedstoneWire) material.getModernData();
    }

    public ListTag getSidesList() {
        ListTag list = new ListTag(5);
        if (isWall()) {
            Wall wall = getWall();
            list.add(wall.getHeight(BlockFace.NORTH).name());
            list.add(wall.getHeight(BlockFace.EAST).name());
            list.add(wall.getHeight(BlockFace.SOUTH).name());
            list.add(wall.getHeight(BlockFace.WEST).name());
            list.add(wall.isUp() ? "TALL" : "NONE");
        }
        else if (isWire()) {
            RedstoneWire wire = getWire();
            list.add(wire.getFace(BlockFace.NORTH).name());
            list.add(wire.getFace(BlockFace.EAST).name());
            list.add(wire.getFace(BlockFace.SOUTH).name());
            list.add(wire.getFace(BlockFace.WEST).name());
        }
        return list;
    }
}
