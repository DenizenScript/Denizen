package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Leaves;

public class MaterialPersistent implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Leaves;
    }

    public static MaterialPersistent getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialPersistent((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "persistent"
    };

    public MaterialPersistent(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.persistent>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.persistent
        // @group properties
        // @description
        // Returns whether this block will decay from being too far away from a tree.
        // -->
        PropertyParser.registerStaticTag(MaterialPersistent.class, ElementTag.class, "persistent", (attribute, material) -> {
            return new ElementTag(material.getLeaves().isPersistent());
        });
    }

    public Leaves getLeaves() {
        return (Leaves) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getLeaves().isPersistent());
    }

    @Override
    public String getPropertyId() {
        return "persistent";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name persistent
        // @input ElementTag(Boolean)
        // @description
        // Sets leaves blocks to ignore decay, or to obey it.
        // @tags
        // <MaterialTag.persistent>
        // -->
        if (mechanism.matches("persistent") && mechanism.requireBoolean()) {
            getLeaves().setPersistent(mechanism.getValue().asBoolean());
        }
    }
}
