package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.type.Leaves;

public class MaterialLeaves implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Leaves;
    }

    public static MaterialLeaves getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLeaves((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "persistent"
    };

    public static final String[] handledMechs = new String[] {
            "persistent"
    };


    private MaterialLeaves(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.persistent>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether this block will decay from being too far away from a tree.
        // -->
        if (attribute.startsWith("persistent")) {
            return new ElementTag(getLeaves().isPersistent()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Leaves getLeaves() {
        return (Leaves) material.getModernData().data;
    }

    public int getDistance() { return getLeaves().getDistance(); }

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
        // @input Element(Boolean)
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
