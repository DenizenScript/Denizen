package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class EntityMaterial implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag entityTag)) {
            return false;
        }
        Entity entity = entityTag.getBukkitEntity();
        return entity instanceof Enderman
                || entity instanceof Minecart
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof BlockDisplay);
    }

    public static EntityMaterial getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityMaterial((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[]{
            "material"
    };

    public EntityMaterial(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.material>
        // @returns MaterialTag
        // @mechanism EntityTag.material
        // @group properties
        // @description
        // Returns the block material associated with the entity.
        // For endermen, returns the material the enderman is holding.
        // For minecarts, returns the material the minecart is carrying.
        // For block displays, returns the displayed block.
        // -->
        PropertyParser.registerTag(EntityMaterial.class, MaterialTag.class, "material", (attribute, object) -> {
            return object.getMaterial();
        });
    }

    public BlockData getBlockData() {
        if (entity instanceof Enderman enderman) {
            return enderman.getCarriedBlock();
        }
        else if (entity instanceof Minecart minecart) {
            return minecart.getDisplayBlockData();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof BlockDisplay blockDisplay) {
            return blockDisplay.getBlock();
        }
        return null;
    }

    public MaterialTag getMaterial() {
        BlockData data = getBlockData();
        if (data == null) {
            return new MaterialTag(Material.AIR);
        }
        return new MaterialTag(data);
    }

    @Override
    public String getPropertyString() {
        MaterialTag material = getMaterial();
        if (material.getMaterial() != Material.AIR) {
            return material.identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "material";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name material
        // @input MaterialTag
        // @description
        // Sets the block material associated with the entity.
        // For endermen, sets the material the enderman is holding.
        // For minecarts, sets the material the minecart is carrying.
        // @tags
        // <EntityTag.material>
        // -->
        if (mechanism.matches("material") && mechanism.requireObject(MaterialTag.class)) {
            BlockData data = mechanism.valueAsType(MaterialTag.class).getModernData();
            Entity entity = this.entity.getBukkitEntity();
            if (entity instanceof Enderman enderman) {
                enderman.setCarriedBlock(data);
            }
            else if (entity instanceof Minecart minecart) {
                minecart.setDisplayBlockData(data);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof BlockDisplay blockDisplay) {
                blockDisplay.setBlock(data);
            }
        }
    }
}
