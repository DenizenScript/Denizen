package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.block.data.Lightable;

@Deprecated
public class MaterialLightable implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Lightable;
    }

    public static MaterialLightable getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLightable((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "lit"
    };

    public MaterialLightable(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        PropertyParser.registerTag(MaterialLightable.class, ElementTag.class, "lit", (attribute, material) -> {
            BukkitImplDeprecations.materialLit.warn(attribute.context);
            return new ElementTag(material.getLightable().isLit());
        });
    }

    public Lightable getLightable() {
        return (Lightable) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "lit";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("lit") && mechanism.requireBoolean()) {
            BukkitImplDeprecations.materialLit.warn(mechanism.context);
            getLightable().setLit(mechanism.getValue().asBoolean());
        }
    }
}
