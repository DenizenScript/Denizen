package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.MossyCarpet;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Wall;

public class MaterialSides extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name sides
    // @input ListTag
    // @description
    // Controls the heights for a wall block or mossy carpet, or connections for a redstone wire, in order North|East|South|West|Vertical.
    // For wall blocks: For n/e/s/w, can be "tall", "low", or "none". For vertical, can be "tall" or "none".
    // For redstone wires: For n/e/s/w, can be "none", "side", or "up". No vertical.
    // For mossy carpets: For n/e/s/w, can be "tall", "low", or "none". Vertical controls the bottom, and can either be "true" or "false".
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return (data instanceof Wall
                || data instanceof RedstoneWire
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && data instanceof MossyCarpet));
    }

    @Override
    public ListTag getPropertyValue() {
        ListTag list = new ListTag(5);
        if (getBlockData() instanceof Wall wall) {
            list.add(wall.getHeight(BlockFace.NORTH).name());
            list.add(wall.getHeight(BlockFace.EAST).name());
            list.add(wall.getHeight(BlockFace.SOUTH).name());
            list.add(wall.getHeight(BlockFace.WEST).name());
            list.add(wall.isUp() ? "TALL" : "NONE");
        }
        else if (getBlockData() instanceof RedstoneWire wire) {
            list.add(wire.getFace(BlockFace.NORTH).name());
            list.add(wire.getFace(BlockFace.EAST).name());
            list.add(wire.getFace(BlockFace.SOUTH).name());
            list.add(wire.getFace(BlockFace.WEST).name());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof MossyCarpet carpet) {
            list.add(carpet.getHeight(BlockFace.NORTH).name());
            list.add(carpet.getHeight(BlockFace.EAST).name());
            list.add(carpet.getHeight(BlockFace.SOUTH).name());
            list.add(carpet.getHeight(BlockFace.WEST).name());
            list.add(carpet.isBottom() ? "TRUE" : "FALSE");
        }
        return list;
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        if (getBlockData() instanceof Wall wall) {
            if (list.size() != 5) {
                mechanism.echoError("Invalid sides list, size must be 5.");
                return;
            }
            wall.setHeight(BlockFace.NORTH, Wall.Height.valueOf(list.get(0).toUpperCase()));
            wall.setHeight(BlockFace.EAST, Wall.Height.valueOf(list.get(1).toUpperCase()));
            wall.setHeight(BlockFace.SOUTH, Wall.Height.valueOf(list.get(2).toUpperCase()));
            wall.setHeight(BlockFace.WEST, Wall.Height.valueOf(list.get(3).toUpperCase()));
            wall.setUp(list.get(4).equalsIgnoreCase("tall"));
        }
        else if (getBlockData() instanceof RedstoneWire wire) {
            if (list.size() != 4) {
                mechanism.echoError("Invalid sides list, size must be 4.");
                return;
            }
            wire.setFace(BlockFace.NORTH, RedstoneWire.Connection.valueOf(list.get(0).toUpperCase()));
            wire.setFace(BlockFace.EAST, RedstoneWire.Connection.valueOf(list.get(1).toUpperCase()));
            wire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.valueOf(list.get(2).toUpperCase()));
            wire.setFace(BlockFace.WEST, RedstoneWire.Connection.valueOf(list.get(3).toUpperCase()));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof MossyCarpet carpet) {
            if (list.size() != 5) {
                mechanism.echoError("Invalid sides list, size must be 5.");
                return;
            }
            carpet.setHeight(BlockFace.NORTH, MossyCarpet.Height.valueOf(list.get(0).toUpperCase()));
            carpet.setHeight(BlockFace.EAST, MossyCarpet.Height.valueOf(list.get(1).toUpperCase()));
            carpet.setHeight(BlockFace.SOUTH, MossyCarpet.Height.valueOf(list.get(2).toUpperCase()));
            carpet.setHeight(BlockFace.WEST, MossyCarpet.Height.valueOf(list.get(3).toUpperCase()));
            carpet.setBottom(list.get(4).equalsIgnoreCase("true"));
        }
    }

    @Override
    public String getPropertyId() {
        return "sides";
    }

    // <--[tag]
    // @attribute <MaterialTag.heights>
    // @returns ListTag
    // @mechanism MaterialTag.heights
    // @group properties
    // @deprecated Use 'sides'
    // @description
    // Deprecated in favor of <@link property MaterialTag.sides>
    // -->

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

    public static void register() {
        autoRegister("sides", MaterialSides.class, ListTag.class, false, "heights");
    }
}
