package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.block.data.type.BubbleColumn;

@Deprecated
public class MaterialDrags implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof BubbleColumn;
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

    public MaterialDrags(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {
        PropertyParser.registerTag(MaterialDrags.class, ElementTag.class, "drags", (attribute, material) -> {
            BukkitImplDeprecations.materialDrags.warn(attribute.context);
            return new ElementTag(material.isDrag());
        });
    }

    public BubbleColumn getBubbleColumn() {
        return (BubbleColumn) material.getModernData();
    }

    public boolean isDrag() {
        return getBubbleColumn().isDrag();
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "drags";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        if (mechanism.matches("drags") && mechanism.requireBoolean()) {
            BukkitImplDeprecations.materialDrags.warn(mechanism.context);
            getBubbleColumn().setDrag(mechanism.getValue().asBoolean());
        }
    }
}
