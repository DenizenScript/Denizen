package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.BubbleColumn;

public class MaterialBubbleColumn implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof BubbleColumn;
    }

    public static MaterialBubbleColumn getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialBubbleColumn((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "drags"
    };

    private MaterialBubbleColumn(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.drags>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.snowy
        // @group properties
        // @description
        // Returns whether to apply force on entities moving through this material.
        // -->
        PropertyParser.<MaterialBubbleColumn>registerTag("drags", (attribute, material) -> {
            return new ElementTag(material.isDrag());
        });
    }

    public BubbleColumn getBubbleColumn() {
        return (BubbleColumn) material.getModernData().data;
    }

    public boolean isDrag() {
        return getBubbleColumn().isDrag();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isDrag());
    }

    @Override
    public String getPropertyId() {
        return "drags";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name snowy
        // @input ElementTag(Boolean)
        // @description
        // Sets this material to apply force on entities moving through this block.
        // @tags
        // <MaterialTag.snowy>
        // -->
        if (mechanism.matches("drags") && mechanism.requireBoolean()) {
            getBubbleColumn().setDrag(mechanism.getValue().asBoolean());
        }
    }
}
