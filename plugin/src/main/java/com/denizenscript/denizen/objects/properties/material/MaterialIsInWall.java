package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Gate;

public class MaterialIsInWall implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Gate;
    }

    public static MaterialIsInWall getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialIsInWall((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "is_in_wall"
    };

    private MaterialIsInWall(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.is_in_wall>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.is_in_wall
        // @group properties
        // @description
        // Returns whether this gate is attached to a wall,
        // and if true the texture is lowered by a small amount to blend in better..
        // -->
        PropertyParser.<MaterialIsInWall>registerTag("is_in_wall", (attribute, material) -> {
            return new ElementTag(material.getGate().isInWall());
        });
    }

    public Gate getGate() {
        return (Gate) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getGate().isInWall());
    }

    @Override
    public String getPropertyId() {
        return "is_in_wall";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name is_in_wall
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this gate is attached to a wall,
        // and if true the texture is lowered by a small amount to blend in better.
        // @tags
        // <MaterialTag.is_in_wall>
        // -->
        if (mechanism.matches("is_in_wall") && mechanism.requireBoolean()) {
            getGate().setInWall(mechanism.getValue().asBoolean());
        }
    }
}
