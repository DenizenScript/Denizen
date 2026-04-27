package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.MossyCarpet;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Wall;

import java.util.function.BiConsumer;

public class MaterialSides extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name sides
    // @input ListTag
    // @description
    // Controls the heights for a wall block or mossy carpet, or connections for a redstone wire, in order North|East|South|West|Vertical.
    // For wall blocks: For n/e/s/w, can be "tall", "low", or "none". For vertical, can be "tall" or "none".
    // For redstone wires: For n/e/s/w, can be "none", "side", or "up". No vertical.
    // For mossy carpets: For n/e/s/w, can be "tall", "low", or "none". Vertical controls the bottom, and can either be "bottom" or "none".
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Wall
                || data instanceof RedstoneWire
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && data instanceof MossyCarpet);
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
            list.add(carpet.isBottom() ? "BOTTOM" : "NONE");
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
            setSide(wall::setHeight, Wall.Height.class, BlockFace.NORTH, list, 0, mechanism);
            setSide(wall::setHeight, Wall.Height.class, BlockFace.EAST, list, 1, mechanism);
            setSide(wall::setHeight, Wall.Height.class, BlockFace.SOUTH, list, 2, mechanism);
            setSide(wall::setHeight, Wall.Height.class, BlockFace.WEST, list, 3, mechanism);
            wall.setUp(CoreUtilities.equalsIgnoreCase(list.get(4), "tall"));
        }
        else if (getBlockData() instanceof RedstoneWire wire) {
            if (list.size() != 4) {
                mechanism.echoError("Invalid sides list, size must be 4.");
                return;
            }
            setSide(wire::setFace, RedstoneWire.Connection.class, BlockFace.NORTH, list, 0, mechanism);
            setSide(wire::setFace, RedstoneWire.Connection.class, BlockFace.EAST, list, 1, mechanism);
            setSide(wire::setFace, RedstoneWire.Connection.class, BlockFace.SOUTH, list, 2, mechanism);
            setSide(wire::setFace, RedstoneWire.Connection.class, BlockFace.WEST, list, 3, mechanism);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof MossyCarpet carpet) {
            if (list.size() != 5) {
                mechanism.echoError("Invalid sides list, size must be 5.");
                return;
            }
            setSide(carpet::setHeight, MossyCarpet.Height.class, BlockFace.NORTH, list, 0, mechanism);
            setSide(carpet::setHeight, MossyCarpet.Height.class, BlockFace.EAST, list, 1, mechanism);
            setSide(carpet::setHeight, MossyCarpet.Height.class, BlockFace.SOUTH, list, 2, mechanism);
            setSide(carpet::setHeight, MossyCarpet.Height.class, BlockFace.WEST, list, 3, mechanism);
            carpet.setBottom(CoreUtilities.equalsIgnoreCase(list.get(4), "bottom"));
        }
    }

    public static <T extends Enum<T>> void setSide(BiConsumer<BlockFace, T> consumer, Class<T> type, BlockFace face, ListTag list, int index, Mechanism mechanism) {
        T value = new ElementTag(list.get(index)).asEnum(type);
        if (value == null) {
            mechanism.echoError("'" + list.get(index) + "' is not a valid " + DebugInternals.getClassNameOpti(type) + ".");
            return;
        }
        consumer.accept(face, value);
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
    // @deprecated use 'sides'
    // @description
    // Deprecated in favor of <@link property MaterialTag.sides>
    // -->

    // <--[mechanism]
    // @object MaterialTag
    // @name heights
    // @input ElementTag
    // @deprecated use 'sides'
    // @description
    // Deprecated in favor of <@link property MaterialTag.sides>
    // @tags
    // <MaterialTag.heights>
    // -->

    public static void register() {
        autoRegister("sides", MaterialSides.class, ListTag.class, false, "heights");
    }
}
