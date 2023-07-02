package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Minecart;

public class EntityMaterial extends EntityProperty<MaterialTag> {

    // <--[property]
    // @object EntityTag
    // @name material
    // @input MaterialTag
    // @description
    // An entity's associated block material.
    // For endermen, this is the block being held.
    // For minecarts, this is the block being carried.
    // For block displays, this is the block being displayed.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Enderman
                || entity.getBukkitEntity() instanceof Minecart
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity.getBukkitEntity() instanceof BlockDisplay);
    }

    @Override
    public MaterialTag getPropertyValue() {
        BlockData blockData = null;
        if (getEntity() instanceof Enderman enderman) {
            blockData = enderman.getCarriedBlock();
        }
        else if (getEntity() instanceof Minecart minecart) {
            blockData = minecart.getDisplayBlockData();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            blockData = as(BlockDisplay.class).getBlock();
        }
        return blockData != null ? new MaterialTag(blockData) : new MaterialTag(Material.AIR);
    }

    @Override
    public boolean isDefaultValue(MaterialTag value) {
        return value.getMaterial() == Material.AIR;
    }

    @Override
    public void setPropertyValue(MaterialTag value, Mechanism mechanism) {
        if (getEntity() instanceof Enderman enderman) {
            enderman.setCarriedBlock(value.getModernData());
        }
        else if (getEntity() instanceof Minecart minecart) {
            minecart.setDisplayBlockData(value.getModernData());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            as(BlockDisplay.class).setBlock(value.getModernData());
        }
    }

    @Override
    public String getPropertyId() {
        return "material";
    }

    public static void register() {
        autoRegister("material", EntityMaterial.class, MaterialTag.class, false);
    }
}
