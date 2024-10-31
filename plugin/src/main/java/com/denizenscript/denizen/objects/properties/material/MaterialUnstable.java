package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TNT;

public class MaterialUnstable extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name unstable
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this TNT block is unstable (explodes when punched).
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof TNT;
    }

    MaterialTag material;

    public static void register() {
        autoRegister("unstable", MaterialUnstable.class, ElementTag.class, true);
    }

    public TNT getTNT() {
        return (TNT) material.getModernData();
    }

    public boolean isUnstable() {
        return getTNT().isUnstable();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(isUnstable());
    }

    @Override
    public void setPropertyValue(ElementTag elementTag, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        getTNT().setUnstable(elementTag.asBoolean());
    }

    @Override
    public String getPropertyId() {
        return "unstable";
    }
}
