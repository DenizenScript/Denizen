package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.CommandBlock;

public class MaterialCommandBlock implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof CommandBlock;
    }

    public static MaterialCommandBlock getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialCommandBlock((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "conditional"
    };

    private MaterialCommandBlock(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.conditional>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.conditional
        // @group properties
        // @description
        // Returns whether this command block is conditional, or not.
        // -->
        PropertyParser.<MaterialCommandBlock>registerTag("conditional", (attribute, material) -> {
            return new ElementTag(material.getCommandBlock().isConditional());
        });
    }

    public CommandBlock getCommandBlock() {
        return (CommandBlock) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCommandBlock().isConditional());
    }

    @Override
    public String getPropertyId() {
        return "conditional";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name conditional
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this command block is conditional, or not.
        // @tags
        // <MaterialTag.conditional>
        // -->
        if (mechanism.matches("conditional") && mechanism.requireBoolean()) {
            getCommandBlock().setConditional(mechanism.getValue().asBoolean());
        }
    }
}
