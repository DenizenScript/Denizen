package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class EntityMaterial implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof Enderman
                || entity instanceof Minecart;
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

    private EntityMaterial(EntityTag _entity) {
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
        // -->
        PropertyParser.registerTag(EntityMaterial.class, MaterialTag.class, "material", (attribute, object) -> {
            return object.getMaterial();
        });
    }

    public boolean isEnderman() {
        return entity.getBukkitEntity() instanceof Enderman;
    }

    public boolean isMinecart() {
        return entity.getBukkitEntity() instanceof Minecart;
    }

    public Enderman getEnderman() {
        return (Enderman) entity.getBukkitEntity();
    }

    public Minecart getMinecart() {
        return (Minecart) entity.getBukkitEntity();
    }

    public MaterialTag getMaterial() {
        BlockData data = null;
        if (isEnderman()) {
            data = getEnderman().getCarriedBlock();
        }
        else if (isMinecart()) {
            data = getMinecart().getDisplayBlockData();
        }
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
            if (isEnderman()) {
                getEnderman().setCarriedBlock(data);
            }
            else if (isMinecart()) {
                getMinecart().setDisplayBlockData(data);
            }
        }
    }
}
