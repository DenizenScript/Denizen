package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.BubbleColumn;

public class MaterialDrags implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof BubbleColumn;
    }

    public static MaterialDrags getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDrags((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "drags"
    };

    private MaterialDrags(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.drags>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.drags
        // @group properties
        // @description
        // Returns whether force is applied on entities moving through this BubbleColumn material.
        // -->
        PropertyParser.<MaterialDrags>registerTag("drags", (attribute, material) -> {
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
        // @name drags
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this material will apply force on entities moving through this BubbleColumn block.
        // @tags
        // <MaterialTag.drags>
        // -->
        if (mechanism.matches("drags") && mechanism.requireBoolean()) {
            getBubbleColumn().setDrag(mechanism.getValue().asBoolean());
        }
    }
}
